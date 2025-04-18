package org.lostcitymapeditor.Renderer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.joml.*;
import org.lostcitymapeditor.DataObjects.*;
import org.lostcitymapeditor.OriginalCode.*;
import org.lostcitymapeditor.Loaders.FileLoader;
import org.lostcitymapeditor.Loaders.MapDataLoader;
import org.lostcitymapeditor.Loaders.TextureLoader;
import org.lostcitymapeditor.Transformers.MapDataTransformer;
import org.lostcitymapeditor.Util.ModelViewerSelector;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;

import java.awt.*;
import java.io.IOException;
import java.lang.Math;
import java.util.*;

import static org.lostcitymapeditor.DataObjects.newTriangle.getTriangles;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.system.MemoryStack;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;

import javax.swing.*;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class OpenGLRenderer {
    private static final int LEVELS = 4;
    private static final int REGION_SIZE = 64;
    private long window;
    private int shaderProgram;
    private int vao;
    private int vbo;
    private Camera camera;
    private float deltaTime = 0.0f;
    private float lastFrame = 0.0f;
    private float lastX = 400, lastY = 300;
    private boolean firstMouse = true;
    private List<newTriangle> triangleList;
    private TileData selectedTile = null;
    private JFXPanel fxPanel;
    private ListView<Integer> shapeListView;
    private CheckBox locCheckbox;
    private CheckBox npcCheckbox;
    private CheckBox objCheckbox;
    private VBox tileInspector;
    private VBox locInspector;
    private VBox npcInspector;
    private VBox objInspector;
    private Label inspectorTitle;
    private Label locPositionLabel;
    private Label npcPositionLabel;
    private Label objPositionLabel;
    private TextField newHeightTextField;
    private TextField objAmountTextField;
    public static String currentMapFileName = "m50_50.jm2";
    public static Map<String, Integer> underlayMap;
    public static Map<String, Object> overlayMap;
    public static Map<String, Object> npcMap;
    public static Map<String, Object> objMap;
    public static Map<Integer, Image> shapeImages;
    public static int currentLevel = 0;
    private static MapData currentMapData;
    public static World world;
    public static World3D world3D;
    public static OpenGLRenderer renderer;
    private final BlockingQueue<Runnable> glQueue = new LinkedBlockingQueue<>();
    public static int[] distance;
    public static Integer selectedOverlayID = 0;
    public static Integer selectedUnderlayID = 0;
    public static Integer selectedShape = -1;
    public static Integer selectedFlag = -1;
    public static Label currentMapLabel;
    private Label tilePositionLabel;
    private Label tileOverlayLabel;
    private Label tileUnderlayLabel;
    private Label tileHeightLabel;
    private Label tileFlagLabel;
    private Label tileShapeLabel;
    private Label tileRotationLabel;
    private Label tileTextureLabel;
    private Label locDetailsLabel;
    private Label objDetailsLabel;
    private Label npcDetailsLabel;
    private Set<Integer> hoveredTileTriangleIndices = new HashSet<>();
    private final TextureManager textureManager = new TextureManager();
    private VertexDataHandler vertexDataHandler = new VertexDataHandler();
    private final ModelViewer modelViewer = new ModelViewer(300, 350);
    private ModelViewerSelector modelViewerSelector;
    private int selectedRotation = -1;
    private int selectedLocRotation;
    private int selectedLocShape = 10;
    private int selectedNpcId = -1;
    private int selectedObjId = -1;
    private static String serverDirectoryPath;
    private CopiedTileData copiedTileData = null;
    private final Deque<MapData> historyStack = new ArrayDeque<>();
    private static final int MAX_HISTORY_SIZE = 30;

    private void updateHoveredTriangle(double mouseX, double mouseY) {
        hoveredTileTriangleIndices.clear();

        newTriangle foundTriangle = pickTriangle((int)mouseX, (int)mouseY);
        if (foundTriangle != null) {
            int tileX = foundTriangle.tileData.tileX;
            int tileZ = foundTriangle.tileData.tileZ;
            for (int i = 0; i < triangleList.size(); i++) {
                newTriangle triangle = triangleList.get(i);
                if (triangle.tileData.tileX == tileX && triangle.tileData.tileZ == tileZ) {
                    hoveredTileTriangleIndices.add(i);
                }
            }
        }

        updateHoveredVBO();
    }
    private int isHoveredVBO = -1;

    private void updateHoveredVBO() {
        if (vao == 0) return;

        float[] isHovered = new float[triangleList.size() * 3];
        for (int triangleIndex = 0; triangleIndex < triangleList.size(); triangleIndex++) {
            boolean isHoveredTriangle = hoveredTileTriangleIndices.contains(triangleIndex);
            for (int i = 0; i < 3; i++) {
                isHovered[triangleIndex * 3 + i] = isHoveredTriangle ? 1.0f : 0.0f;
            }
        }

        glBindVertexArray(vao);

        if (isHoveredVBO == -1) {
            isHoveredVBO = glGenBuffers();
        }

        glBindBuffer(GL_ARRAY_BUFFER, isHoveredVBO);

        FloatBuffer isHoveredBuffer = BufferUtils.createFloatBuffer(isHovered.length);
        isHoveredBuffer.put(isHovered).flip();
        glBufferData(GL_ARRAY_BUFFER, isHoveredBuffer, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(5, 1, GL_FLOAT, false, Float.BYTES, 0);
        glEnableVertexAttribArray(5);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void updateCurrentMapLabel(String mapFileName) {
        Platform.runLater(() -> {
            if (currentMapLabel != null) {
                currentMapLabel.setText("Current Map: " + mapFileName);
            }
        });
    }

    public void drawMapLevel() {
        enqueueGLTask(() -> {
            newTriangle.clearCollectedTriangles();
            world = new World(REGION_SIZE, REGION_SIZE);
            world.loadGround(currentMapData);
            world3D = new World3D(world.levelHeightmap, REGION_SIZE, LEVELS, REGION_SIZE);
            world.loadLocations(world3D, currentMapData);
            world.loadNpcs(world3D, currentMapData);
            world.loadObjs(world3D, currentMapData);
            world.build(world3D);
            world3D.draw(currentLevel);
            List<newTriangle> triangleList = getTriangles();
            setTriangles(triangleList);
            setupVertexDataWithTriangles(triangleList);
        });
    }

    private newTriangle pickTriangle(int mouseX, int mouseY) {
        float x = (2.0f * mouseX) / 800 - 1.0f;
        float y = 1.0f - (2.0f * mouseY) / 600;

        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(camera.getZoom()),
                800.0f / 600.0f,
                0.1f,
                15000.0f
        );

        Matrix4f view = camera.getViewMatrix();
        Matrix4f model = new Matrix4f().identity();

        Matrix4f inverse = new Matrix4f();
        projection.mul(view, inverse);
        inverse.mul(model);
        inverse.invert();

        Vector4f nearPointNDC = new Vector4f(x, y, -1.0f, 1.0f);
        Vector4f farPointNDC = new Vector4f(x, y, 1.0f, 1.0f);

        Vector4f nearPointWorld = inverse.transform(nearPointNDC);
        Vector4f farPointWorld = inverse.transform(farPointNDC);

        nearPointWorld.div(nearPointWorld.w);
        farPointWorld.div(farPointWorld.w);

        Vector3f rayOrigin = new Vector3f(nearPointWorld.x, nearPointWorld.y, nearPointWorld.z);
        Vector3f rayDirection = new Vector3f(
                farPointWorld.x - nearPointWorld.x,
                farPointWorld.y - nearPointWorld.y,
                farPointWorld.z - nearPointWorld.z
        ).normalize();

        for (int i = 0; i < triangleList.size(); i++) {
            newTriangle triangle = triangleList.get(i);

            Vector3f vertex0 = new Vector3f(triangle.vertices[0], triangle.vertices[1], triangle.vertices[2]);
            Vector3f vertex1 = new Vector3f(triangle.vertices[3], triangle.vertices[4], triangle.vertices[5]);
            Vector3f vertex2 = new Vector3f(triangle.vertices[6], triangle.vertices[7], triangle.vertices[8]);

            Vector3f edge1 = new Vector3f(vertex1).sub(vertex0);
            Vector3f edge2 = new Vector3f(vertex2).sub(vertex0);

            Vector3f h = new Vector3f(rayDirection).cross(edge2);
            float a = edge1.dot(h);

            if (a > -0.00001 && a < 0.00001)
                continue;

            float f = 1.0f / a;
            Vector3f s = new Vector3f(rayOrigin).sub(vertex0);
            float u = f * s.dot(h);

            if (u < 0.0 || u > 1.0)
                continue;

            Vector3f q = new Vector3f(s).cross(edge1);
            float v = f * rayDirection.dot(q);

            if (v < 0.0 || u + v > 1.0)
                continue;

            float t = f * edge2.dot(q);

            if (t > 0.00001)
            {
                return triangle;
            }
        }

        return null;
    }

    public void drawNewMap(MapData currentMapData) {
        enqueueGLTask(() -> {
            world = new World(REGION_SIZE, REGION_SIZE);
            world.loadGround(currentMapData);
            world3D = new World3D(world.levelHeightmap, REGION_SIZE, LEVELS, REGION_SIZE);
            if(locCheckbox.isSelected()) {
                world.loadLocations(world3D, currentMapData);
            }
            if(npcCheckbox.isSelected()) {
                world.loadNpcs(world3D, currentMapData);
            }
            if(objCheckbox.isSelected()) {
                world.loadObjs(world3D, currentMapData);
            }
            world.build(world3D);
            newTriangle.clearCollectedTriangles();
            world3D.draw(currentLevel);
            List<newTriangle> triangleList = getTriangles();
            setTriangles(triangleList);
            setupVertexDataWithTriangles(triangleList);
        });
    }

    public void enqueueGLTask(Runnable task) {
        glQueue.add(task);
    }

    public void setupJavaFXUI() {
        JFrame frame = new JFrame("LostCity Map Editor Config");
        fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setSize(1100, 900);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Platform.runLater(() -> {
            ObservableList<String> mapFiles = MapDataLoader.getJM2Files(serverDirectoryPath + "/maps/");
            ListView<String> mapListView = new ListView<>(mapFiles);
            mapListView.setPrefWidth(150);

            ObservableList<String> underlayNames = FXCollections.observableArrayList(underlayMap.keySet());
            underlayNames.addFirst("Original");
            underlayNames.add(1, "Clear");

            ObservableList<String> overlayNames = FXCollections.observableArrayList(overlayMap.keySet());
            overlayNames.addFirst("Original");
            overlayNames.add(1, "Clear");

            ListView<String>overlayListView = new ListView<>(overlayNames);
            overlayListView.setPrefHeight(50);
            overlayListView.setCellFactory(param -> new ListCell<>() {
                private final Rectangle rect = new Rectangle(20, 20);
                private final ImageView textureView = new ImageView();

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        Map<String, Object> overlayData = (Map<String, Object>) overlayMap.get(item);
                        if (overlayData != null) {
                            if (overlayData.containsKey("rgb")) {
                                Integer color = (Integer) overlayData.get("rgb");
                                if (color != null) {
                                    rect.setFill(Color.rgb((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF));
                                    setGraphic(rect);
                                } else {
                                    rect.setFill(Color.TRANSPARENT);
                                    setGraphic(null);
                                }
                            } else if (overlayData.containsKey("texture")) {
                                String textureName = (String) overlayData.get("texture");
                                Image texture = TextureLoader.loadTextureImage(serverDirectoryPath, textureName);
                                if (texture != null) {
                                    textureView.setImage(texture);
                                    textureView.setFitWidth(20);
                                    textureView.setFitHeight(20);
                                    setGraphic(textureView);
                                } else {
                                    setGraphic(null);
                                }
                            } else {
                                setGraphic(null);
                            }
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            });

            overlayListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue == null || newValue.equals("Original")) {
                            selectedOverlayID = 0;
                        } else if (newValue.equals("Clear")) {
                            selectedOverlayID = -1;
                        } else {
                            selectedOverlayID = findOverlayOrUnderlayIdByName(newValue);
                        }
                    }
            );


            ObservableList<Integer> shapeValues = FXCollections.observableArrayList(IntStream.range(0, 12).boxed().collect(Collectors.toList()));
            shapeListView = new ListView<>(shapeValues);
            shapeListView.setPrefWidth(250);
            shapeListView.setPrefHeight(150);
            String[] shapeNames = TileOverlay.SHAPE_NAMES;

            shapeListView.setCellFactory(param -> new ListCell<>() {
                private final ImageView imageView = new ImageView();

                {
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                }

                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Image image = shapeImages.get(item);
                        String shapeName = (item >= 0 && item < shapeNames.length) ? shapeNames[item] : "Unknown";
                        if (image != null) {
                            imageView.setImage(image);
                            setText(item + " - " + shapeName);
                            setGraphic(imageView);
                        } else {
                            setText(item + " - " + shapeName);
                            setGraphic(null);
                        }
                    }
                }
            });

            shapeListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            selectedShape = newValue;
                        }
                    });

            ObservableList<Integer> flagValues = FXCollections.observableArrayList(0, 1, 2, 4, 8, 16);
            ListView<Integer>flagListView = new ListView<>(flagValues);
            flagListView.setPrefWidth(250);
            flagListView.setPrefHeight(150);

            flagListView.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String description = switch (item) {
                            case 0 -> "Default";
                            case 1 -> "Unwalkable";
                            case 2 -> "Bridge";
                            case 4 -> "Remove roof";
                            case 8 -> "Render on level below";
                            case 16 -> "Don't draw on map";
                            default -> "";
                        };
                        setText(item + " - " + description);
                    }
                }
            });

            flagListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            selectedFlag = newValue;
                        }
                    });

            Label currentLevelLabel = new Label("Current Level:");
            ToggleGroup levelToggleGroup = new ToggleGroup();
            VBox levelRadioButtons = new VBox(5);
            for (int i = 0; i < LEVELS; i++) {
                RadioButton levelButton = new RadioButton("Level " + i);
                levelButton.setToggleGroup(levelToggleGroup);
                levelButton.setUserData(i);
                levelRadioButtons.getChildren().add(levelButton);

                int level = i;
                levelButton.setOnAction(e -> {
                    currentLevel = level;
                    if (currentMapData != null) {
                        historyStack.clear();
                        drawMapLevel();
                    }
                });
            }
            ((RadioButton) levelRadioButtons.getChildren().get(0)).setSelected(true);

            Label displayLabel = new Label("Display:");
            locCheckbox = new CheckBox("Locs");
            locCheckbox.setSelected(true);
            npcCheckbox = new CheckBox("Npcs");
            npcCheckbox.setSelected(true);
            objCheckbox = new CheckBox("Objs");
            objCheckbox.setSelected(true);

            Button exportButton = new Button("Export Map");

            locInspector = new VBox();
            locInspector.setPadding(new Insets(10));
            locInspector.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            locInspector.setPrefWidth(250);

            Label newLocRotationLabel = new Label("New Loc Rotation");
            VBox locRotationBox = new VBox(5);
            ToggleGroup locRotationGroup = new ToggleGroup();

            Integer[] rotations = {0, 90, 180, 270};
            for (Integer rotation : rotations) {
                RadioButton rb = new RadioButton(rotation.toString());
                rb.setToggleGroup(locRotationGroup);
                rb.setUserData(rotation);
                locRotationBox.getChildren().add(rb);
            }

            locRotationGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectedLocRotation = (Integer) newValue.getUserData();
                }
            });

            Label newLocShapeLabel = new Label("New Loc Shape");
            ObservableList<Integer> locShapeValues = FXCollections.observableArrayList();
            for (int i = 0; i <= 22; i++) {
                locShapeValues.add(i);
            }

            ListView<Integer>locShapeListView = new ListView<>(locShapeValues);
            locShapeListView.setPrefWidth(100);
            locShapeListView.setPrefHeight(100);

            String[] locShapeNames = new String[]{
                    "Wall Straight", "Wall Diagonal Corner", "Wall L", "Wall Square Corner",
                    "Wall Decor Straight No Offset", "Wall Decor Straight Offset", "Wall Decor Diagonal Offset",
                    "Wall Decor Diagonal No Offset", "Wall Decor Diagonal Both", "Wall Diagonal",
                    "(DEFAULT) Centrepiece Straight", "Centrepiece Diagonal", "Roof Straight",
                    "Roof Diagonal With Roof Edge", "Roof Diagonal", "Roof L Concave", "Roof L Convex",
                    "Roof Flat", "Roof Edge Straight", "Roof Edge Diagonal Corner", "Roof Edge L",
                    "Roof Edge Square Corner", "Ground Decor"
            };

            locShapeListView.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String shapeName = (item >= 0 && item < locShapeNames.length) ? locShapeNames[item] : "Unknown";
                        setText(item + " - " + shapeName);
                    }
                }
            });


            locShapeListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            selectedLocShape = newValue;
                            modelViewerSelector.updateModel(selectedLocShape);
                        }
                    });

            inspectorTitle = new Label("--Locs--\nLeft Click to inspect\nL + click to update");
            locPositionLabel = new Label("Position: ");
            locDetailsLabel = new Label();
            Button clearLocsButton = new Button("Clear Tile Locs");
            clearLocsButton.setOnAction(e -> {
                if (selectedTile != null) {
                    saveStateBeforeChange();
                    Platform.runLater(() -> {
                        currentMapData.removeLocData(selectedTile.level, selectedTile.x, selectedTile.z);
                        drawNewMap(currentMapData);
                        updateLocInspector();
                    });
                } else {
                    System.out.println("No tile selected to clear Locs from.");
                }
            });

            locInspector.getChildren().addAll(inspectorTitle, locPositionLabel, locDetailsLabel, clearLocsButton, newLocRotationLabel, locRotationBox, newLocShapeLabel, locShapeListView);

            npcInspector = new VBox();
            npcInspector.setPadding(new Insets(10));
            npcInspector.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            npcInspector.setPrefWidth(250);

            inspectorTitle = new Label("--Npcs--\nLeft Click to inspect\nN + Click to update");
            npcPositionLabel = new Label("Position: ");
            npcDetailsLabel = new Label();
            Button clearNpcsButton = new Button("Clear Tile Npcs");
            clearNpcsButton.setOnAction(e -> {
                if (selectedTile != null) {
                    saveStateBeforeChange();
                    Platform.runLater(() -> {
                        currentMapData.removeNpcData(selectedTile.level, selectedTile.x, selectedTile.z);
                        drawNewMap(currentMapData);
                        updateNpcInspector();
                    });
                } else {
                    System.out.println("No tile selected to clear Npcs from.");
                }
            });

            Set<String> npcNameSet = npcMap.keySet();

            List<String> sortedNpcNames = new ArrayList<>(npcNameSet);
            Collections.sort(sortedNpcNames);
            ObservableList<String> npcNamesObservable = FXCollections.observableArrayList(sortedNpcNames);

            npcNamesObservable.addFirst("None");

            ListView<String> npcListView = new ListView<>(npcNamesObservable);
            npcListView.setPrefHeight(150);

            npcListView.setCellFactory(param -> new ListCell<String>() {
                @Override
                protected void updateItem(String npcName, boolean empty) {
                    super.updateItem(npcName, empty);
                    if (empty || npcName == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(npcName);
                        setGraphic(null);
                    }
                }
            });

            npcListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue == null || newValue.equals("None")) {
                            selectedNpcId = -1;
                        } else {
                            int npcId = -1;
                            for (Integer id : FileLoader.getNpcMap().keySet()) {
                                if (FileLoader.getNpcMap().get(id).equals(newValue)) {
                                    npcId = id;
                                    break;
                                }
                            }
                            if (npcId == -1) {
                                System.err.println("Error: Could not find NPC data or ID key for selected item '" + newValue + "'.");
                            }
                            selectedNpcId = npcId;
                        }
                    }
            );

            npcListView.getSelectionModel().select("None");

            npcInspector.getChildren().addAll(inspectorTitle, npcPositionLabel, npcDetailsLabel, clearNpcsButton, npcListView);

            objInspector = new VBox();
            objInspector.setPadding(new Insets(10));
            objInspector.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            objInspector.setPrefWidth(250);

            inspectorTitle = new Label("--Objs--\nLeft Click to inspect\nO + Click to update");
            objPositionLabel = new Label("Position: ");
            objDetailsLabel = new Label();
            Button clearObjsButton = new Button("Clear Tile Objs");
            clearObjsButton.setOnAction(e -> {
                if (selectedTile != null) {
                    saveStateBeforeChange();
                    Platform.runLater(() -> {
                        currentMapData.removeObjData(selectedTile.level, selectedTile.x, selectedTile.z); // Modification
                        drawNewMap(currentMapData);
                        updateObjInspector();
                    });
                } else {
                    System.out.println("No tile selected to clear Objs from.");
                }
            });

            Set<String> objNameSet = objMap.keySet();

            List<String> sortedObjNames = new ArrayList<>(objNameSet);
            Collections.sort(sortedObjNames);
            ObservableList<String> objNamesObservable = FXCollections.observableArrayList(sortedObjNames);

            objNamesObservable.addFirst("None");

            ListView<String> objListView = new ListView<>(objNamesObservable);
            objListView.setPrefHeight(150);

            objListView.setCellFactory(param -> new ListCell<String>() {
                @Override
                protected void updateItem(String objName, boolean empty) {
                    super.updateItem(objName, empty);
                    if (empty || objName == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(objName);
                        setGraphic(null);
                    }
                }
            });

            objListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue == null || newValue.equals("None")) {
                            selectedObjId = -1;
                        } else {
                            int objId = -1;
                            for (Integer id : FileLoader.getObjMap().keySet()) {
                                if (FileLoader.getObjMap().get(id).equals(newValue)) {
                                    objId = id;
                                    break;
                                }
                            }
                            if (objId == -1) {
                                System.err.println("Error: Could not find OBJ data or ID key for selected item '" + newValue + "'.");
                            }
                            selectedObjId = objId;
                        }
                    }
            );

            objListView.getSelectionModel().select("None");

            Label objAmountLabel = new Label("Amount:");
            objAmountTextField = new TextField();

            objInspector.getChildren().addAll(inspectorTitle, objPositionLabel, objDetailsLabel, clearObjsButton, objListView, objAmountLabel, objAmountTextField);

            tileInspector = new VBox();
            tileInspector.setPadding(new Insets(10));
            tileInspector.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            tileInspector.setPrefWidth(250);

            Label inspectorTitle = new Label("--Tiles--\nLeft Click to inspect\nL + Click to update");
            Label currentUnderlayLabel = new Label("New Underlay");
            ListView<String> underlayListView = new ListView<>(underlayNames);
            underlayListView.setPrefHeight(50);
            underlayListView.setCellFactory(param -> new ListCell<>() {
                private final Rectangle rect = new Rectangle(20, 20);

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        Integer color = underlayMap.get(item);
                        if (color != null) {
                            rect.setFill(Color.rgb((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF));
                            setGraphic(rect);
                        } else {
                            rect.setFill(Color.TRANSPARENT);
                            setGraphic(null);
                        }
                    }
                }
            });

            underlayListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue == null || newValue.equals("Original")) {
                            selectedUnderlayID = 0;
                        } else if (newValue.equals("Clear")) {
                            selectedUnderlayID = -1;
                        } else {
                            selectedUnderlayID = findOverlayOrUnderlayIdByName(newValue);
                        }
                    }
            );

            Label currentOverlayLabel = new Label("New Overlay");
            Label newHeightLabel = new Label("New Height");
            Label newFlagLabel = new Label("New Flag");
            Label newShapeLabel = new Label("New Shape");
            Label newRotationLabel = new Label("New Rotation");
            VBox rotationBox = new VBox(5);
            ToggleGroup rotationGroup = new ToggleGroup();

            for (Integer rotation : rotations) {
                RadioButton rb = new RadioButton(rotation.toString());
                rb.setToggleGroup(rotationGroup);
                rb.setUserData(rotation);
                rotationBox.getChildren().add(rb);
            }

            rotationGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectedRotation = (Integer) newValue.getUserData() / 90;
                }
            });

            newHeightTextField = new TextField();

            tilePositionLabel = new Label("Position: ");
            tileOverlayLabel = new Label("Overlay ID: ");
            tileUnderlayLabel = new Label("Underlay ID: ");
            tileFlagLabel = new Label("Flag ID: ");
            tileHeightLabel = new Label("Height: ");
            tileShapeLabel = new Label("Shape ID: ");
            tileRotationLabel = new Label("Shape Rotation: ");
            tileTextureLabel = new Label("Texture Name: ");

            tileInspector.getChildren().addAll(
                    inspectorTitle,
                    tilePositionLabel,
                    tileOverlayLabel,
                    tileUnderlayLabel,
                    tileFlagLabel,
                    tileHeightLabel,
                    tileShapeLabel,
                    tileRotationLabel,
                    tileTextureLabel,
                    currentUnderlayLabel, underlayListView,
                    currentOverlayLabel, overlayListView,
                    newHeightLabel, newHeightTextField,
                    newFlagLabel, flagListView,
                    newRotationLabel, rotationBox,
                    newShapeLabel, shapeListView
            );

            VBox controlsBox = new VBox(
                    currentLevelLabel, levelRadioButtons,
                    displayLabel,
                    locCheckbox,
                    npcCheckbox,
                    objCheckbox,
                    exportButton
            );
            controlsBox.setPadding(new Insets(0));
            currentMapLabel = new Label("Current Map: " + currentMapFileName);
            VBox sideBar = new VBox(currentMapLabel, mapListView, controlsBox);
            BorderPane.setMargin(controlsBox, new Insets(10, 0, 10, 0));
            sideBar.setPadding(new Insets(10));
            sideBar.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            modelViewerSelector = new ModelViewerSelector(modelViewer);

            SwingNode modelViewerSwingNode = new SwingNode();
            Platform.runLater(() -> modelViewerSwingNode.setContent(modelViewer));
            VBox.setVgrow(modelViewerSwingNode, Priority.ALWAYS);
            VBox modelViewerBox = new VBox();
            modelViewerBox.setPadding(new Insets(10));
            modelViewerBox.setAlignment(Pos.CENTER);
            modelViewerBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            modelViewerBox.setPrefWidth(250);
            modelViewerBox.getChildren().addAll(modelViewerSelector, modelViewerSwingNode);
            HBox mainContent = new HBox(tileInspector, locInspector, modelViewerBox, npcInspector, objInspector);

            BorderPane rootPane = new BorderPane();
            rootPane.setLeft(sideBar);
            rootPane.setCenter(mainContent);

            Scene scene = new Scene(rootPane, 800, 600);
            fxPanel.setScene(scene);

            mapListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            currentMapFileName = newValue;
                            updateCurrentMapLabel(currentMapFileName);
                            currentMapData = MapDataTransformer.parseJM2File(serverDirectoryPath + "/maps/" + currentMapFileName);

                            historyStack.clear();

                            drawNewMap(currentMapData);
                            selectedTile = null;
                            updateTileInspector();
                            updateLocInspector();
                            updateNpcInspector();
                            updateObjInspector();
                        }
                    }
            );

            locCheckbox.setOnAction(e -> {
                if (currentMapData != null) {
                    SwingUtilities.invokeLater(() -> {
                        drawNewMap(currentMapData);
                    });
                }
            });

            npcCheckbox.setOnAction(e -> {
                if (currentMapData != null) {
                    SwingUtilities.invokeLater(() -> {
                        drawNewMap(currentMapData);
                    });
                }
            });

            objCheckbox.setOnAction(e -> {
                if (currentMapData != null) {
                    SwingUtilities.invokeLater(() -> {
                        drawNewMap(currentMapData);
                    });
                }
            });

            exportButton.setOnAction(e -> {
                MapDataLoader.exportMap(currentMapData, currentMapFileName, serverDirectoryPath);
            });
        });
    }

    private void updateTileInspector() {
        Platform.runLater(() -> {
            if (selectedTile != null) {
                tilePositionLabel.setText(String.format("Position: X=%d, Z=%d Level=%d",
                        selectedTile.x,
                        selectedTile.z,
                        selectedTile.level));

                String overlayText = "Overlay ID: ";
                if (selectedTile.overlay == null) {
                    overlayText += "None";
                } else {
                    String overlayName = FileLoader.getFloMap().get(selectedTile.overlay.id);
                    overlayText += selectedTile.overlay.id;
                    if (overlayName != null) {
                        overlayText += " - " + overlayName;
                    }
                }
                tileOverlayLabel.setText(overlayText);

                String underlayText = "Underlay ID: ";
                if (selectedTile.underlay == null) {
                    underlayText += "None";
                } else {
                    String underlayName = FileLoader.getFloMap().get(selectedTile.underlay.id);
                    underlayText += selectedTile.underlay.id;
                    if (underlayName != null) {
                        underlayText += " - " + underlayName;
                    }
                }
                tileUnderlayLabel.setText(underlayText);

                if (selectedTile.perlin && selectedTile.level == 0) {
                    tileHeightLabel.setText("Height: Perlin Generated");
                } else {
                    tileHeightLabel.setText(String.format("Height: %d",
                            (selectedTile.height / -8)));
                }
                tileFlagLabel.setText("Flag ID: " + (selectedTile.flag != null ?
                        selectedTile.flag : "None"));

                tileShapeLabel.setText("Shape ID: " + (selectedTile.shape != null ? selectedTile.shape : 0));

                tileRotationLabel.setText("Shape Rotation: " + (selectedTile.rotation != null ? selectedTile.rotation : 0));

                tileTextureLabel.setText("Texture Name: " + (selectedTile.overlay != null && selectedTile.overlay.texture != null ?
                        selectedTile.overlay.texture : "None"));
            } else {
                tilePositionLabel.setText("Position: ");
                tileOverlayLabel.setText("Overlay ID: ");
                tileUnderlayLabel.setText("Underlay ID: ");
                tileHeightLabel.setText("Height: ");
                tileShapeLabel.setText("Shape ID: ");
                tileRotationLabel.setText("Shape Rotation: ");
                tileTextureLabel.setText("Texture Name: ");
            }
        });
    }

    public void updateLocInspector() {
        Platform.runLater(() -> {
            if (selectedTile != null) {
                locPositionLabel.setText(String.format("Position: X=%d, Z=%d Level=%d",
                        selectedTile.x,
                        selectedTile.z,
                        selectedTile.level));
                List<LocData> currentLocs = currentMapData.getLocData(selectedTile.level, selectedTile.x,selectedTile.z);

                StringBuilder details = new StringBuilder();
                details.append(String.format("--------------------%n"));
                if (!currentLocs.isEmpty()) {
                    for (int i = 0; i < currentLocs.size(); i++) {
                        LocData loc = currentLocs.get(i);
                        details.append(String.format("Name: %s%nID: %d%nShape: %d%nRotation: %s%n", FileLoader.getLocMap().get(loc.id), loc.id, loc.shape, loc.rotation * 90));
                        if (i < currentLocs.size() - 1) {
                            details.append(String.format("--------------------%n"));
                        }
                    }
                } else {
                    details.append("No LocData found for this tile.");
                }
                locDetailsLabel.setText(details.toString());
            }
        });
    }

    public void updateNpcInspector() {
        Platform.runLater(() -> {
            if (selectedTile != null) {
                npcPositionLabel.setText(String.format("Position: X=%d, Z=%d Level=%d",
                        selectedTile.x,
                        selectedTile.z,
                        selectedTile.level));
                List<NpcData> currentNpcs = currentMapData.getNpcData(selectedTile.level, selectedTile.x,selectedTile.z);

                StringBuilder details = new StringBuilder();
                details.append(String.format("--------------------%n"));
                if (!currentNpcs.isEmpty()) {
                    for (int i = 0; i < currentNpcs.size(); i++) {
                        NpcData npc = currentNpcs.get(i);
                        details.append(String.format("Name: %s%nID: %d%n", FileLoader.getNpcMap().get(npc.id), npc.id));
                        if (i < currentNpcs.size() - 1) {
                            details.append(String.format("--------------------%n"));
                        }
                    }
                } else {
                    details.append("No NpcData found for this tile.");
                }
                npcDetailsLabel.setText(details.toString());
            }
        });
    }

    public void updateObjInspector() {
        Platform.runLater(() -> {
            if (selectedTile != null) {
                objPositionLabel.setText(String.format("Position: X=%d, Z=%d Level=%d",
                        selectedTile.x,
                        selectedTile.z,
                        selectedTile.level));
                List<ObjData> currentObjs = currentMapData.getObjData(selectedTile.level, selectedTile.x,selectedTile.z);

                StringBuilder details = new StringBuilder();
                details.append(String.format("--------------------%n"));
                if (!currentObjs.isEmpty()) {
                    for (int i = 0; i < currentObjs.size(); i++) {
                        ObjData obj = currentObjs.get(i);
                        details.append(String.format("Name: %s%nCount: %d%nID: %d%n", FileLoader.getObjMap().get(obj.id), obj.count, obj.id));
                        if (i < currentObjs.size() - 1) {
                            details.append(String.format("--------------------%n"));
                        }
                    }
                } else {
                    details.append("No ObjData found for this tile.");
                }
                objDetailsLabel.setText(details.toString());
            }
        });
    }

    public void run() {
        setupJavaFXUI();
        init();
        Platform.runLater(() -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(fxPanel);
            if (frame != null) {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int screenHeight = screenSize.height;
                int frameX = 0;
                int frameY = (screenHeight - frame.getHeight()) / 2;
                frame.setLocation(frameX, frameY);
                int windowHeight = 600;
                int windowX = frameX + frame.getWidth();
                int windowY = (screenHeight - windowHeight) / 2;
                glfwSetWindowPos(window, windowX, windowY + 10);
            }
        });
        enableTextureRendering();
        setupVertexDataWithTriangles(triangleList);
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_DEPTH_BITS, 24);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(800, 600, "LostCity Map Renderer", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }

            if ((mods & GLFW_MOD_CONTROL) != 0 && key == GLFW_KEY_Z && action == GLFW_RELEASE) {
                performUndo();
                return;
            }

            if ((mods & GLFW_MOD_CONTROL) != 0) {
                if (key == GLFW_KEY_C && action == GLFW_RELEASE) {
                    copyTileDataToMemory();
                } else if (key == GLFW_KEY_V && action == GLFW_RELEASE) {
                    saveStateBeforeChange();
                    pasteTileDataFromMemory();
                }
            }
        });


        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {

            if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) {
                if (firstMouse) {
                    lastX = (float) xpos;
                    lastY = (float) ypos;
                    firstMouse = false;
                }

                float xOffset = (float) xpos - lastX;
                float yOffset = lastY - (float) ypos;

                lastX = (float) xpos;
                lastY = (float) ypos;

                camera.processMouseMovement(xOffset, yOffset, true);
            } else {
                firstMouse = true;
            }
        });

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                if (action == GLFW_PRESS) {
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                    firstMouse = true;
                } else if (action == GLFW_RELEASE) {
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                }
            }
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                double[] xpos = new double[1];
                double[] ypos = new double[1];
                glfwGetCursorPos(window, xpos, ypos);

                int mouseX = (int) xpos[0];
                int mouseY = (int) ypos[0];

                boolean ctrlPressed = (mods & GLFW_MOD_CONTROL) != 0;
                boolean OPressed = glfwGetKey(window, GLFW_KEY_O) == GLFW_PRESS;
                boolean LPressed = glfwGetKey(window, GLFW_KEY_L) == GLFW_PRESS;
                boolean NPressed = glfwGetKey(window, GLFW_KEY_N) == GLFW_PRESS;
                newTriangle clickedTriangle = pickTriangle(mouseX, mouseY);

                if (clickedTriangle != null) {
                    int tileX = clickedTriangle.tileData.tileX;
                    int tileZ = clickedTriangle.tileData.tileZ;

                    TileData tile;
                    tile = currentMapData.mapTiles[currentLevel][tileX][tileZ];
                    if(tile == null)
                        tile = new TileData(currentLevel, tileX, tileZ);
                    selectedTile = tile;
                } else {
                    Vector2i tileCoords = pickTile(mouseX, mouseY);

                    if (tileCoords != null) {
                        int tileX = tileCoords.x;
                        int tileZ = tileCoords.y;
                        TileData tile;
                        tile = currentMapData.mapTiles[currentLevel][tileX][tileZ];
                        if(tile == null)
                            tile = new TileData(currentLevel, tileX, tileZ);
                        selectedTile = tile;
                    } else {
                        selectedTile = null;
                    }
                }
                updateTileInspector();
                updateLocInspector();
                updateNpcInspector();
                updateObjInspector();

                boolean modificationAttempted = false;

                if (ctrlPressed) {
                    if (selectedTile != null) {
                        modificationAttempted = true;
                    }
                }

                if (LPressed) {
                    if (selectedTile != null && modelViewerSelector.getSelectedModel() != null) {
                        modificationAttempted = true;
                    }
                }

                if (NPressed) {
                    if (selectedTile != null && selectedNpcId != -1) {
                        modificationAttempted = true;
                    }
                }

                if (OPressed) {
                    if (selectedTile != null && selectedObjId != -1) {
                        modificationAttempted = true;
                    }
                }

                if (modificationAttempted) {
                    saveStateBeforeChange();
                }

                if (ctrlPressed) {
                    if (selectedTile != null) {
                        TileData newTile = new TileData(selectedTile.level, selectedTile.x, selectedTile.z);

                        if (selectedOverlayID == null || selectedOverlayID == -1) {
                            newTile.overlay = null;
                        } else if (selectedOverlayID == 0) {
                            newTile.overlay = selectedTile.overlay;
                        } else {
                            newTile.overlay = new OverlayData(selectedOverlayID);
                        }
                        if (selectedUnderlayID == null || selectedUnderlayID == -1) {
                            newTile.underlay = null;
                        } else if (selectedUnderlayID == 0) {
                            newTile.underlay = selectedTile.underlay;
                        } else {
                            newTile.underlay = new UnderlayData(selectedUnderlayID);
                        }
                        if (newHeightTextField.getText() != null && !newHeightTextField.getText().isEmpty()) {
                            try {
                                double newHeight = Double.parseDouble(newHeightTextField.getText());
                                if(newHeight == 0 && currentLevel == 0) {
                                    int baseX = 0;
                                    int baseY = 0;
                                    Pattern fileNamePattern = Pattern.compile("m(\\d+)_(\\d+)\\.jm2");
                                    Matcher fileNameMatcher = fileNamePattern.matcher(currentMapFileName);
                                    if (fileNameMatcher.find()) {
                                        baseX = Integer.parseInt(fileNameMatcher.group(1));
                                        baseY = Integer.parseInt(fileNameMatcher.group(2));
                                    }
                                    int worldX = newTile.x + baseX + 932731;
                                    int worldZ = newTile.z + baseY + 556238;
                                    newTile.perlin = true;
                                    newTile.height = World.perlinNoise(worldX, worldZ) * -8;
                                } else {
                                    newTile.height = (int) Math.floor(newHeight) * -8;
                                }
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid height value: " + newHeightTextField.getText());
                            }
                        } else {
                            if (selectedTile.perlin)
                                newTile.perlin = true;
                            newTile.height = selectedTile.height;
                        }
                        if (newTile.overlay != null) {
                            if (selectedRotation == -1) {
                                newTile.rotation = selectedTile.rotation;
                            } else if (selectedRotation == 0) {
                                newTile.rotation = null;
                            } else if (selectedRotation == 90) {
                                newTile.rotation = 1;
                            } else if (selectedRotation == 180) {
                                newTile.rotation = 2;
                            } else if (selectedRotation == 270) {
                                newTile.rotation = 3;
                            }
                            if (selectedShape == -1) {
                                newTile.shape = selectedTile.shape;
                            } else {
                                newTile.shape = selectedShape;
                            }
                            if (selectedFlag == -1) {
                                newTile.flag = selectedTile.flag;
                            } else {
                                newTile.flag = selectedFlag;
                            }
                        }
                        currentMapData.mapTiles[selectedTile.level][selectedTile.x][selectedTile.z] = newTile;
                        drawNewMap(currentMapData);
                        selectedTile = newTile;
                    }
                }

                if (LPressed) {
                    if (selectedTile != null) {
                        String selectedLocName = modelViewerSelector.getSelectedModel();
                        if (selectedLocName != null) {
                            int locId = -1;
                            for (Integer id : FileLoader.getLocMap().keySet()) {
                                if (FileLoader.getLocMap().get(id).equals(selectedLocName)) {
                                    locId = id;
                                    break;
                                }
                            }
                            if(locId == -1) {
                                System.out.println("Couldn't find Loc ID for Loc: " + selectedLocName);
                                return;
                            }
                            LocData newLoc = new LocData(selectedTile.level, selectedTile.x, selectedTile.z, locId, selectedLocShape);
                            newLoc.rotation = selectedLocRotation / 90;
                            currentMapData.locations.add(newLoc);
                            drawNewMap(currentMapData);
                        } else {
                            System.err.println("No model selected in Model Viewer.");
                        }
                    }
                }

                if (NPressed) {
                    if (selectedTile != null) {
                        if (selectedNpcId != -1) {
                            NpcData newNpc = new NpcData(selectedTile.level, selectedTile.x, selectedTile.z, selectedNpcId);
                            currentMapData.npcs.add(newNpc);
                            drawNewMap(currentMapData);
                        } else {
                            System.err.println("No NPC selected.");
                        }
                    }
                }

                if (OPressed) {
                    if (selectedTile != null) {
                        if (selectedObjId != -1) {
                            int objAmount = 1;
                            if (objAmountTextField.getText() != null && !objAmountTextField.getText().isEmpty()) {
                                try {
                                    objAmount = Integer.parseInt(objAmountTextField.getText());
                                } catch (NumberFormatException e) {
                                    System.err.println("Invalid object amount: " + objAmountTextField.getText());
                                }
                            }
                            if (objAmount < 1) {
                                objAmount = 1;
                            }
                            ObjData newObj = new ObjData(selectedTile.level, selectedTile.x, selectedTile.z, selectedObjId, objAmount);
                            currentMapData.objects.add(newObj);
                            drawNewMap(currentMapData);
                        } else {
                            System.err.println("No OBJ selected.");
                        }
                    }
                }
            }
        });
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        camera = new Camera(new Vector3f(4400.0f, -7000.0f, 4000.0f));
        setupShaders();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();

            float currentFrame = (float) glfwGetTime();
            deltaTime = currentFrame - lastFrame;
            lastFrame = currentFrame;

            processInput();

            double[] xpos = new double[1];
            double[] ypos = new double[1];
            glfwGetCursorPos(window, xpos, ypos);
            if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) != GLFW_PRESS) {
                updateHoveredTriangle(xpos[0], ypos[0]);
            }

            Runnable task;
            while ((task = glQueue.poll()) != null) {
                task.run();
            }

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glUseProgram(shaderProgram);

            Matrix4f model = new Matrix4f().identity();
            Matrix4f view = camera.getViewMatrix();
            Matrix4f projection = new Matrix4f().perspective(
                    (float) Math.toRadians(camera.getZoom()),
                    800.0f / 600.0f,
                    10f,
                    15000.0f
            );

            int modelLoc = glGetUniformLocation(shaderProgram, "model");
            int viewLoc = glGetUniformLocation(shaderProgram, "view");
            int projectionLoc = glGetUniformLocation(shaderProgram, "projection");

            try (MemoryStack stack = stackPush()) {
                FloatBuffer modelBuffer = stack.mallocFloat(16);
                model.get(modelBuffer);
                glUniformMatrix4fv(modelLoc, false, modelBuffer);

                FloatBuffer viewBuffer = stack.mallocFloat(16);
                view.get(viewBuffer);
                glUniformMatrix4fv(viewLoc, false, viewBuffer);

                FloatBuffer projectionBuffer = stack.mallocFloat(16);
                projection.get(projectionBuffer);
                glUniformMatrix4fv(projectionLoc, false, projectionBuffer);
            }

            Map<Integer, List<Integer>> textureGroups = groupTrianglesByTexture();
            glBindVertexArray(vao);

            for (Map.Entry<Integer, List<Integer>> entry : textureGroups.entrySet()) {
                int triangleTexId = entry.getKey();
                List<Integer> triangleIndices = entry.getValue();

                if (triangleTexId < 0) {
                    for (int triangleIndex : triangleIndices) {
                        glDrawArrays(GL_TRIANGLES, triangleIndex * 3, 3);
                    }
                    continue;
                }

                Integer openglTexId = TextureLoader.textureIdMap.get(triangleTexId);
                if (openglTexId == null) {
                    System.err.println("No OpenGL texture ID found for triangle texture ID: " + triangleTexId);
                    continue;
                }

                glActiveTexture(GL_TEXTURE0 + triangleTexId);
                glBindTexture(GL_TEXTURE_2D, openglTexId);

                int textureUniformLoc = glGetUniformLocation(shaderProgram, "textures[" + triangleTexId + "]");
                if (textureUniformLoc != -1) {
                    glUniform1i(textureUniformLoc, triangleTexId);
                } else {
                    System.err.println("Warning: Could not find uniform 'textures[" + triangleTexId + "]'");
                }

                for (int triangleIndex : triangleIndices) {
                    glDrawArrays(GL_TRIANGLES, triangleIndex * 3, 3);
                }
            }

            glBindVertexArray(0);
            glBindTexture(GL_TEXTURE_2D, 0);

            int[] width = new int[1];
            int[] height = new int[1];
            glfwGetWindowSize(window, width, height);
            glfwSwapBuffers(window);
        }

        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteProgram(shaderProgram);
    }

    public static void startRender() throws IOException {
        serverDirectoryPath = chooseServerDirectory();
        if (serverDirectoryPath == null) {
            System.err.println("No server directory selected. Exiting.");
            return;
        }
        FileLoader.loadFiles(serverDirectoryPath);
        underlayMap = FileLoader.getUnderlayMap();
        overlayMap = FileLoader.getOverlayMap();
        shapeImages = FileLoader.getShapeImages();
        npcMap = FileLoader.getAllNpcMap();
        objMap = FileLoader.getAllObjMap();

        currentMapData = MapDataTransformer.parseJM2File(serverDirectoryPath + "/maps/" + currentMapFileName);

        Pix3D.loadTextures(serverDirectoryPath);
        Pix3D.setBrightness(0.8);
        Pix3D.initPool(20);
        Pix3D.init3D(800, 600);
        world = new World(REGION_SIZE, REGION_SIZE);
        world.loadGround(currentMapData);
        world3D = new World3D(world.levelHeightmap, REGION_SIZE, LEVELS, REGION_SIZE);
        world.loadLocations(world3D, currentMapData);
        world.loadNpcs(world3D, currentMapData);
        world.loadObjs(world3D, currentMapData);
        world.build(world3D);

        distance = new int[9];
        for (int x = 0; x < 9; x++) {
            int angle = x * 32 + 128 + 15;
            int offset = angle * 3 + 600;
            int sin = Pix3D.sinTable[angle];
            distance[x] = offset * sin >> 16;
        }
        newTriangle.clearCollectedTriangles();
        world3D.draw(currentLevel);
        List<newTriangle> triangleList = getTriangles();

        renderer = new OpenGLRenderer();
        renderer.setTriangles(triangleList);
        renderer.run();
    }

    private void setupShaders() {
        ShaderManager shaderManager = new ShaderManager();
        shaderProgram = shaderManager.createProgram();

        textureManager.initializeTextures(serverDirectoryPath);

        glUseProgram(shaderProgram);

        int texturesLocation = glGetUniformLocation(shaderProgram, "textures");
        if (texturesLocation == -1) {
            System.err.println("ERROR: Could not find uniform 'textures' in shader.");
        }

        int[] textureIndices = new int[50];
        for (int i = 0; i < 50; i++) {
            textureIndices[i] = i;
        }

        glUniform1iv(texturesLocation, textureIndices);

        glUseProgram(0);
    }

    private void setupVertexDataWithTriangles(List<newTriangle> triangles) {
        int[] vaoAndVbo = {vao, vbo};
        vertexDataHandler.setupVertexDataWithTriangles(triangles, vaoAndVbo);
        vao = vaoAndVbo[0];
        vbo = vaoAndVbo[1];
    }

    private void processInput() {
        camera.processKeyboardInput(window, deltaTime);
    }

    public void setTriangles(List<newTriangle> triangles) {
        this.triangleList = triangles;
    }

    private Map<Integer, List<Integer>> groupTrianglesByTexture() {
        Map<Integer, List<Integer>> groups = new HashMap<>();

        for (int i = 0; i < triangleList.size(); i++) {
            int texId = triangleList.get(i).textureId;

            if (!groups.containsKey(texId)) {
                groups.put(texId, new ArrayList<>());
            }

            groups.get(texId).add(i);
        }

        return groups;
    }

    private void enableTextureRendering() {
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private Integer findOverlayOrUnderlayIdByName(String name) {
        for (Map.Entry<Integer, String> entry : FileLoader.getFloMap().entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey();
            }
        }
        System.err.println("No ID found for name: " + name);
        return null;
    }

    private Vector2i pickTile(int mouseX, int mouseY) {
        float x = (2.0f * mouseX) / 800 - 1.0f;
        float y = 1.0f - (2.0f * mouseY) / 600;

        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(camera.getZoom()),
                800.0f / 600.0f,
                0.1f,
                15000.0f
        );

        Matrix4f view = camera.getViewMatrix();
        Matrix4f model = new Matrix4f().identity();

        Matrix4f inverse = new Matrix4f();
        projection.mul(view, inverse);
        inverse.mul(model);
        inverse.invert();

        Vector4f nearPointNDC = new Vector4f(x, y, -1.0f, 1.0f);
        Vector4f farPointNDC = new Vector4f(x, y, 1.0f, 1.0f);

        Vector4f nearPointWorld = inverse.transform(nearPointNDC);
        Vector4f farPointWorld = inverse.transform(farPointNDC);

        nearPointWorld.div(nearPointWorld.w);
        farPointWorld.div(farPointWorld.w);

        Vector3f rayOrigin = new Vector3f(nearPointWorld.x, nearPointWorld.y, nearPointWorld.z);
        Vector3f rayDirection = new Vector3f(
                farPointWorld.x - nearPointWorld.x,
                farPointWorld.y - nearPointWorld.y,
                farPointWorld.z - nearPointWorld.z
        ).normalize();

        float t = -rayOrigin.y / rayDirection.y;

        if (t >= 0) {
            float intersectionX = rayOrigin.x + t * rayDirection.x;
            float intersectionZ = rayOrigin.z + t * rayDirection.z;

            int tileX = (int) Math.floor(intersectionX / (REGION_SIZE * 2));
            int tileZ = (int) Math.floor(intersectionZ / (REGION_SIZE * 2));

            if (tileX > REGION_SIZE - 1 || tileZ > REGION_SIZE - 1 || tileZ < 0 || tileX < 0)
                return null;

            return new Vector2i(tileX, tileZ);
        } else {
            return null;
        }
    }

    public static String chooseServerDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Server Data Source Directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JOptionPane.showMessageDialog(null,
                "Please select the root directory containing your server's 'models', 'sprites', 'fonts', etc. folders (e.g., '../Server/data/src/').",
                "Directory Selection",
                JOptionPane.INFORMATION_MESSAGE);

        int result = fileChooser.showDialog(null, "Select Directory");

        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        } else {
            return null;
        }
    }

    private void copyTileDataToMemory() {
        if (selectedTile == null || currentMapData == null) {
            copiedTileData = null;
            return;
        }
        List<LocData> locsOnTile = currentMapData.getLocData(selectedTile.level, selectedTile.x, selectedTile.z);
        List<NpcData> npcsOnTile = currentMapData.getNpcData(selectedTile.level, selectedTile.x, selectedTile.z);
        List<ObjData> objsOnTile = currentMapData.getObjData(selectedTile.level, selectedTile.x, selectedTile.z);

        copiedTileData = new CopiedTileData(selectedTile, locsOnTile, npcsOnTile, objsOnTile);

        System.out.println("Tile data copied");
    }

    private void pasteTileDataFromMemory() {
        if (copiedTileData == null) {
            System.out.println("Paste Error: No tile data copied to memory (use Ctrl+C).");
            return;
        }
        if (selectedTile == null) {
            System.out.println("Paste Error: No target tile selected.");
            return;
        }

        int targetLevel = selectedTile.level;
        int targetX = selectedTile.x;
        int targetZ = selectedTile.z;

        TileData newTargetTile = new TileData(targetLevel, targetX, targetZ);
        newTargetTile.height = copiedTileData.height;
        newTargetTile.perlin = copiedTileData.perlin;
        newTargetTile.overlay = copiedTileData.overlayId != null ? new OverlayData(copiedTileData.overlayId) : null;
        newTargetTile.underlay = copiedTileData.underlayId != null ? new UnderlayData(copiedTileData.underlayId) : null;
        newTargetTile.shape = copiedTileData.shape;
        newTargetTile.rotation = copiedTileData.rotation;
        newTargetTile.flag = copiedTileData.flag;

        currentMapData.mapTiles[targetLevel][targetX][targetZ] = newTargetTile;

        currentMapData.locations.removeIf(loc -> loc.level == targetLevel && loc.x == targetX && loc.z == targetZ);
        for (LocData copiedLoc : copiedTileData.locs) {
            LocData newLoc = new LocData(targetLevel, targetX, targetZ, copiedLoc.id, copiedLoc.shape);
            if (copiedLoc.rotation != null) {
                newLoc.rotation = copiedLoc.rotation;
            } else {
                newLoc.rotation = 0;
            }
            currentMapData.locations.add(newLoc);
        }

        currentMapData.npcs.removeIf(npc -> npc.level == targetLevel && npc.x == targetX && npc.z == targetZ);
        for (NpcData copiedNpc : copiedTileData.npcs) {
            NpcData newNpc = new NpcData(targetLevel, targetX, targetZ, copiedNpc.id);
            currentMapData.npcs.add(newNpc);
        }

        currentMapData.objects.removeIf(obj -> obj.level == targetLevel && obj.x == targetX && obj.z == targetZ);
        for (ObjData copiedObj : copiedTileData.objs) {
            ObjData newObj = new ObjData(targetLevel, targetX, targetZ, copiedObj.id, copiedObj.count);
            currentMapData.objects.add(newObj);
        }

        drawNewMap(currentMapData);

        selectedTile = newTargetTile;

        updateTileInspector();
        updateLocInspector();
        updateNpcInspector();
        updateObjInspector();
    }

    private void saveStateBeforeChange() {
        if (currentMapData == null) return;

        MapData stateToSave = currentMapData.deepCopy();
        historyStack.push(stateToSave);

        if (historyStack.size() > MAX_HISTORY_SIZE) {
            historyStack.removeLast();
        }
    }

    private void performUndo() {
        if (historyStack.isEmpty()) {
            System.out.println("Nothing to undo.");
            return;
        }
        currentMapData = historyStack.pop();
        drawNewMap(currentMapData);
        selectedTile = null;
        updateTileInspector();
        updateLocInspector();
        updateNpcInspector();
        updateObjInspector();
    }

}