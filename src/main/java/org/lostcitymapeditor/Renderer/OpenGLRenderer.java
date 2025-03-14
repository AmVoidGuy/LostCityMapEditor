package org.lostcitymapeditor.Renderer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.joml.*;
import org.lostcitymapeditor.DataObjects.MapData;
import org.lostcitymapeditor.DataObjects.OverlayData;
import org.lostcitymapeditor.DataObjects.TileData;
import org.lostcitymapeditor.DataObjects.UnderlayData;
import org.lostcitymapeditor.OriginalCode.Pix3D;
import org.lostcitymapeditor.OriginalCode.World;
import org.lostcitymapeditor.OriginalCode.World3D;
import org.lostcitymapeditor.DataObjects.newTriangle;
import org.lostcitymapeditor.Loaders.FileLoader;
import org.lostcitymapeditor.Loaders.MapDataLoader;
import org.lostcitymapeditor.Loaders.TextureLoader;
import org.lostcitymapeditor.Transformers.MapDataTransformer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;

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
    private int[] textureIDs;
    private boolean texturesLoaded = false;
    private List<newTriangle> triangleList;
    private boolean openglInitialized = false;
    private TileData selectedTile = null;
    private JFXPanel fxPanel;
    private ComboBox<String> underlayComboBox;
    private ComboBox<String> overlayComboBox;
    private ComboBox<Integer> shapeComboBox;
    private ComboBox<Integer> levelComboBox;
    private ComboBox<Integer> rotationComboBox;
    private CheckBox locCheckbox;
    private VBox tileInspector;
    private TextField newHeightTextField;
    public static String currentMapFileName = "m50_50.jm2";
    public static Map<String, Integer> underlayMap;
    public static Map<String, Object> overlayMap;
    public static Map<Integer, Image> shapeImages;
    public static int currentLevel = 0;
    private static MapData currentMapData;
    private Map<String, Object> underlayTileMeshMap;
    public static World world;
    public static World3D world3D;
    public static OpenGLRenderer renderer;
    private final BlockingQueue<Runnable> glQueue = new LinkedBlockingQueue<>();
    public static int[] distance;
    public static Integer selectedOverlayID = 0;
    public static Integer selectedUnderlayID = 0;
    public static Integer selectedShape;
    public static Label currentMapLabel;
    private Label tilePositionLabel;
    private Label tileOverlayLabel;
    private Label tileUnderlayLabel;
    private Label tileHeightLabel;
    private Label tileFlagLabel;
    private Label tileShapeLabel;
    private Label tileRotationLabel;
    private Label tileTextureLabel;
    private Set<Integer> hoveredTileTriangleIndices = new HashSet<>();

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
        frame.setSize(450, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Platform.runLater(() -> {
            ObservableList<String> mapFiles = MapDataLoader.getJM2Files();
            ListView<String> mapListView = new ListView<>(mapFiles);
            mapListView.setPrefWidth(200);

            ObservableList<String> underlayNames = FXCollections.observableArrayList(underlayMap.keySet());
            underlayNames.addFirst("Original");
            underlayNames.add(1, "Clear");
            underlayComboBox = new ComboBox<>(underlayNames);
            underlayComboBox.setPrefWidth(300);
            underlayComboBox.setCellFactory(param -> new ListCell<>() {
                private final Rectangle rect;

                {
                    rect = new Rectangle(20, 20);
                }

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
                            rect.setFill(javafx.scene.paint.Color.rgb((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF));
                            setGraphic(rect);
                        } else {
                            rect.setFill(javafx.scene.paint.Color.TRANSPARENT);
                            setGraphic(null);
                        }
                    }
                }
            });

            underlayComboBox.setButtonCell(new ListCell<>() {
                private final Rectangle rect;

                {
                    rect = new Rectangle(20, 20);
                }

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
                            rect.setFill(javafx.scene.paint.Color.rgb((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF));
                            setGraphic(rect);
                        } else {
                            rect.setFill(javafx.scene.paint.Color.TRANSPARENT);
                            setGraphic(null);
                        }
                    }
                }
            });

            underlayComboBox.setOnAction(e -> {
                String selectedUnderlay = underlayComboBox.getValue();
                if (selectedUnderlay == null || selectedUnderlay.equals("Original")) {
                    selectedUnderlayID = 0;
                } else if (selectedUnderlay.equals("Clear")) {
                    selectedUnderlayID = -1;
                } else {
                    selectedUnderlayID = findOverlayOrUnderlayIdByName(selectedUnderlay);
                }
            });

            ObservableList<String> overlayNames = FXCollections.observableArrayList(overlayMap.keySet());
            overlayNames.addFirst("Original");
            overlayNames.add(1, "Clear");
            overlayComboBox = new ComboBox<>(overlayNames);
            overlayComboBox.setPrefWidth(300);

            overlayComboBox.setCellFactory(param -> new ListCell<>() {
                private ImageView textureView = new ImageView();

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
                                    setText(item);
                                    Rectangle rect = new Rectangle(20, 20, javafx.scene.paint.Color.rgb((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF));
                                    setGraphic(rect);
                                } else {
                                    setText(item);
                                    setGraphic(null);
                                }
                            } else if (overlayData.containsKey("texture")) {
                                String textureName = (String) overlayData.get("texture");
                                Image texture = TextureLoader.loadTextureImage(textureName);
                                if (texture != null) {
                                    textureView.setImage(texture);
                                    textureView.setFitWidth(20);
                                    textureView.setFitHeight(20);
                                    setText(item);
                                    setGraphic(textureView);
                                } else {
                                    setText(item);
                                    setGraphic(null);
                                }
                            } else {
                                setText(item);
                                setGraphic(null);
                            }
                        } else {
                            setText(item);
                            setGraphic(null);
                        }
                    }
                }
            });

            overlayComboBox.setButtonCell(new ListCell<String>() {
                private ImageView textureView = new ImageView();

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Map<String, Object> overlayData = (Map<String, Object>) overlayMap.get(item);
                        if (overlayData != null) {
                            if (overlayData.containsKey("rgb")) {
                                Integer color = (Integer) overlayData.get("rgb");
                                if (color != null) {
                                    setText(item);
                                    Rectangle rect = new Rectangle(20, 20, javafx.scene.paint.Color.rgb((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF));
                                    setGraphic(rect);
                                } else {
                                    setText(item);
                                    setGraphic(null);
                                }
                            } else if (overlayData.containsKey("texture")) {
                                String textureName = (String) overlayData.get("texture");
                                Image texture = null;
                                if (texture != null) {
                                    textureView.setImage(texture);
                                    textureView.setFitWidth(20);
                                    textureView.setFitHeight(20);
                                    setText(item + "");
                                    setGraphic(textureView);
                                } else {
                                    setText(item + "");
                                    setGraphic(null);
                                }
                            } else {
                                setText(item);
                                setGraphic(null);
                            }
                        } else {
                            setText(item);
                            setGraphic(null);
                        }
                    }
                }
            });

            overlayComboBox.setOnAction(e -> {
                String selectedOverlay = overlayComboBox.getValue();
                if (selectedOverlay == null || selectedOverlay.equals("Original")) {
                    selectedOverlayID = 0;
                } else if (selectedOverlay.equals("Clear")) {
                    selectedOverlayID = -1;
                } else {
                    selectedOverlayID = findOverlayOrUnderlayIdByName(selectedOverlay);
                }
            });

            ObservableList<Integer> shapeValues = FXCollections.observableArrayList(IntStream.range(0, 12).boxed().collect(Collectors.toList()));
            shapeComboBox = new ComboBox<>(shapeValues);
            shapeComboBox.setPrefWidth(300);
            shapeComboBox.setCellFactory(param -> new ListCell<>() {
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
                        if (image != null) {
                            imageView.setImage(image);
                            setText("Shape " + item);
                            setGraphic(imageView);
                        } else {
                            setText("Shape " + item);
                            setGraphic(null);
                        }
                    }
                }
            });

            shapeComboBox.setButtonCell(new ListCell<>() {
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
                        if (image != null) {
                            imageView.setImage(image);
                            setText("Shape " + item);
                            setGraphic(imageView);
                        } else {
                            setText("Shape " + item);
                            setGraphic(null);
                        }
                    }
                }
            });

            shapeComboBox.setOnAction(e -> {
                selectedShape = shapeComboBox.getValue();
            });

            levelComboBox = new ComboBox<>();
            levelComboBox.setItems(FXCollections.observableArrayList(IntStream.range(0, LEVELS).boxed().collect(Collectors.toList())));
            levelComboBox.setValue(0);

            Label currentLevelLabel = new Label("Current Level");

            Label displayLabel = new Label("Display:");
            locCheckbox = new CheckBox("Locs");
            locCheckbox.setSelected(true);

            Button exportButton = new Button("Export Map");

            tileInspector = new VBox();
            tileInspector.setPrefWidth(200);
            tileInspector.setPadding(new Insets(10));
            tileInspector.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            Label inspectorTitle = new Label("--Tiles--\nLeft Click to inspect\nCtrl + Click to update");
            Label currentUnderlayLabel = new Label("New Underlay");
            Label currentOverlayLabel = new Label("New Overlay");
            Label newHeightLabel = new Label("New Height");
            Label newShapeLabel = new Label("New Shape");
            Label newRotationLabel = new Label("New Rotation");

            ObservableList<Integer> rotationValues = FXCollections.observableArrayList(0, 90, 180, 270);
            rotationComboBox = new ComboBox<>(rotationValues);
            rotationComboBox.setValue(0);
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
                    currentUnderlayLabel, underlayComboBox,
                    currentOverlayLabel, overlayComboBox,
                    newHeightLabel, newHeightTextField,
                    newRotationLabel, rotationComboBox,
                    newShapeLabel, shapeComboBox
            );

            VBox controlsBox = new VBox(
                    currentLevelLabel, levelComboBox,
                    displayLabel,
                    locCheckbox,
                    exportButton
            );
            controlsBox.setPadding(new Insets(0));
            currentMapLabel = new Label("Current Map: " + currentMapFileName);
            VBox sideBar = new VBox(currentMapLabel, mapListView, controlsBox);
            BorderPane.setMargin(controlsBox, new Insets(10, 0, 10, 0));
            sideBar.setPadding(new Insets(10));
            sideBar.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            HBox mainContent = new HBox(tileInspector);

            BorderPane rootPane = new BorderPane();
            rootPane.setLeft(sideBar);
            rootPane.setCenter(mainContent);

            Scene scene = new Scene(rootPane, 400, 600);
            fxPanel.setScene(scene);

            mapListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            currentMapFileName = newValue;
                            updateCurrentMapLabel(currentMapFileName);
                            currentMapData = MapDataTransformer.parseJM2File(MapDataLoader.MAPS_DIRECTORY + currentMapFileName);
                            drawNewMap(currentMapData);
                        }
                    }
            );

            levelComboBox.setOnAction(e -> {
                currentLevel = levelComboBox.getValue();
                if (currentMapData != null) {
                    drawMapLevel();
                }
            });

            locCheckbox.setOnAction(e -> {
                if (currentMapData != null) {
                    SwingUtilities.invokeLater(() -> {
                        drawNewMap(currentMapData);
                    });
                }
            });

            exportButton.setOnAction(e -> {
                MapDataLoader.exportMap(currentMapData, currentMapFileName);
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

                tileOverlayLabel.setText("Overlay ID: " + (selectedTile.overlay != null ?
                        selectedTile.overlay.id : "None"));

                tileUnderlayLabel.setText("Underlay ID: " + (selectedTile.underlay != null ?
                        selectedTile.underlay.id : "None"));
                if (selectedTile.perlin) {
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

    public void run() {
        setupJavaFXUI();
        init();
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
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
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
                updateTileInspector();

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
                                newTile.height = (int) Math.floor(newHeight);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid height value: " + newHeightTextField.getText());
                            }
                        } else {
                            newTile.height = selectedTile.height;
                        }
                        Integer selectedRotation = rotationComboBox.getValue();
                        if (newTile.overlay != null) {
                            if (selectedRotation == null || selectedRotation == 0) {
                                newTile.rotation = null;
                            } else if (selectedRotation == 90) {
                                newTile.rotation = 1;
                            } else if (selectedRotation == 180) {
                                newTile.rotation = 2;
                            } else if (selectedRotation == 270) {
                                newTile.rotation = 3;
                            }
                            newTile.shape = selectedShape;
                        }
                        currentMapData.mapTiles[selectedTile.level][selectedTile.x][selectedTile.z] = newTile;
                        drawNewMap(currentMapData);
                        selectedTile = newTile;
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
        openglInitialized = true;
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
        glDeleteTextures(textureIDs);
        glDeleteProgram(shaderProgram);
    }

    public static void startRender() throws IOException {
        FileLoader.loadFiles();
        underlayMap = FileLoader.getUnderlayMap();
        overlayMap = FileLoader.getOverlayMap();
        shapeImages = FileLoader.getShapeImages();

        currentMapData = MapDataTransformer.parseJM2File(MapDataLoader.MAPS_DIRECTORY + currentMapFileName);

        Pix3D.loadTextures();
        Pix3D.setBrightness(0.8);
        Pix3D.initPool(20);
        Pix3D.init3D(800, 600);
        world = new World(REGION_SIZE, REGION_SIZE);
        world.loadGround(currentMapData);
        world3D = new World3D(world.levelHeightmap, REGION_SIZE, LEVELS, REGION_SIZE);
        world.loadLocations(world3D, currentMapData);
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

    private void initializeTextures() {
        if (!openglInitialized) {
            System.err.println("ERROR: Attempting to load textures before OpenGL is initialized");
            return;
        }

        if (texturesLoaded) {
            System.out.println("Textures already loaded, skipping initialization");
            return;
        }


        int numTextures = FileLoader.getTextureMap().size();
        if (numTextures == 0) {
            System.out.println("No textures to load");
            texturesLoaded = true;
            return;
        }

        textureIDs = new int[numTextures];

        for (int i = 0; i < numTextures; i++) {
            textureIDs[i] = TextureLoader.loadTexture(i);
        }

        texturesLoaded = true;
    }

    private void setupShaders() {
        String vertexShaderSource =
                "#version 330 core\n" +
                        "layout (location = 0) in vec3 aPos;\n" +
                        "layout (location = 1) in vec3 aColor;\n" +
                        "layout (location = 2) in vec2 aTexCoord;\n" +
                        "layout (location = 3) in float aUseTexture;\n" +
                        "layout (location = 4) in float aTextureID;\n" +
                        "layout (location = 5) in float aIsHovered;\n" +
                        "\n" +
                        "out vec3 vertexColor;\n" +
                        "out vec2 TexCoord;\n" +
                        "out float useTexture;\n" +
                        "out float textureID;\n" +
                        "out float isHovered;\n" +
                        "\n" +
                        "uniform mat4 model;\n" +
                        "uniform mat4 view;\n" +
                        "uniform mat4 projection;\n" +
                        "\n" +
                        "void main() {\n" +
                        "    gl_Position = projection * view * model * vec4(aPos, 1.0);\n" +
                        "    vertexColor = aColor;\n" +
                        "    TexCoord = aTexCoord;\n" +
                        "    useTexture = aUseTexture;\n" +
                        "    textureID = aTextureID;\n" +
                        "    isHovered = aIsHovered;\n" +
                        "}";

        String fragmentShaderSource =
                "#version 330 core\n" +
                        "in vec3 vertexColor;\n" +
                        "in vec2 TexCoord;\n" +
                        "in float useTexture;\n" +
                        "in float textureID;\n" +
                        "in float isHovered;\n" +
                        "out vec4 FragColor;\n" +
                        "uniform sampler2D textures[50];\n" +
                        "\n" +
                        "void main() {\n" +
                        "    vec3 baseColor = vertexColor;\n" +
                        "\n" +
                        "    if (isHovered > 0.5) { \n" +
                        "        baseColor = min(baseColor + vec3(0.2), vec3(1.0));\n" +
                        "    }\n" +
                        "\n" +
                        "    if (useTexture > 0.5) {\n" +
                        "        int texID = int(round(textureID));\n" +
                        "        if (texID >= 0 && texID < 50) {\n" +
                        "            vec4 texColor = texture(textures[texID], TexCoord);\n" +
                        "            if (texColor.a < 0.1) discard;\n" +
                        "    if (isHovered > 0.5) { \n" +
                        "        FragColor = texColor * vec4(baseColor, 1.0);\n" +
                        "    } else {\n" +
                        "            FragColor = texColor; \n" +
                        "     }   } else {\n" +
                        "            FragColor = vec4(1.0, 0.0, 1.0, 1.0);\n" +
                        "        }\n" +
                        "    } else {\n" +
                        "        FragColor = vec4(baseColor, 1.0);\n" +
                        "    }\n" +
                        "}";

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);

        int success = glGetShaderi(vertexShader, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(vertexShader);
            System.err.println("ERROR::SHADER::VERTEX::COMPILATION_FAILED\n" + infoLog);
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);

        success = glGetShaderi(fragmentShader, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(fragmentShader);
            System.err.println("ERROR::SHADER::FRAGMENT::COMPILATION_FAILED\n" + infoLog);
        }

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        success = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            String infoLog = glGetProgramInfoLog(shaderProgram);
            System.err.println("ERROR::SHADER::PROGRAM::LINKING_FAILED\n" + infoLog);
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);


        initializeTextures();

        glUseProgram(shaderProgram);

        int texturesLocation = glGetUniformLocation(shaderProgram, "textures");
        if (texturesLocation == -1) {
            System.err.println("ERROR: Could not find uniform 'textures' in shader.");
        }

        int[] textureIndices = new int[50];
        for(int i = 0; i < 50; i++) {
            textureIndices[i] = i;
        }

        glUniform1iv(texturesLocation, textureIndices);

        glUseProgram(0);
    }

    private void setupVertexDataWithTriangles(List<newTriangle> triangles) {
        if (triangles == null || triangles.isEmpty()) {
            System.out.println("No triangles available");
            return;
        }

        int totalTriangles = triangles.size();
        int totalVertices = totalTriangles * 3;

        int stride = 11;
        float[] interleavedData = new float[totalVertices * stride];

        float[][] defaultTexCoords = {
                {0.0f, 0.0f},
                {1.0f, 0.0f},
                {0.0f, 1.0f}
        };

        int dataIndex = 0;

        for (int triangleIndex = 0; triangleIndex < totalTriangles; triangleIndex++) {
            newTriangle triangle = triangles.get(triangleIndex);
            boolean isHoveredTriangle = hoveredTileTriangleIndices.contains(triangleIndex);

            for (int vertexIndex = 0; vertexIndex < 3; vertexIndex++) {
                interleavedData[dataIndex++] = triangle.vertices[vertexIndex * 3];
                interleavedData[dataIndex++] = triangle.vertices[vertexIndex * 3 + 1];
                interleavedData[dataIndex++] = triangle.vertices[vertexIndex * 3 + 2];

                if (triangle.colors != null) {
                    int color = Pix3D.colourTable[triangle.colors[vertexIndex]];
                    interleavedData[dataIndex++] = ((color >> 16) & 0xFF) / 255.0f;
                    interleavedData[dataIndex++] = ((color >> 8) & 0xFF) / 255.0f;
                    interleavedData[dataIndex++] = (color & 0xFF) / 255.0f;
                } else {
                    interleavedData[dataIndex++] = 1.0f;
                    interleavedData[dataIndex++] = 0.5f;
                    interleavedData[dataIndex++] = 0.2f;
                }

                if (triangle.textureId > -1 && triangle.textureCoordinates != null) {
                    interleavedData[dataIndex++] = triangle.textureCoordinates[vertexIndex * 2];
                    interleavedData[dataIndex++] = triangle.textureCoordinates[vertexIndex * 2 + 1];
                } else if (triangle.textureId > -1) {
                    interleavedData[dataIndex++] = defaultTexCoords[vertexIndex][0];
                    interleavedData[dataIndex++] = defaultTexCoords[vertexIndex][1];
                } else {
                    interleavedData[dataIndex++] = 0.0f;
                    interleavedData[dataIndex++] = 0.0f;
                }

                interleavedData[dataIndex++] = (triangle.textureId > -1) ? 1.0f : 0.0f;

                interleavedData[dataIndex++] = (triangle.textureId > -1) ? triangle.textureId : -1.0f;

                interleavedData[dataIndex++] = isHoveredTriangle ? 1.0f : 0.0f;
            }
        }

        if (vao != 0) {
            glDeleteVertexArrays(vao);
        }
        if (vbo != 0) {
            glDeleteBuffers(vbo);
        }

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(interleavedData.length);
        buffer.put(interleavedData).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        int floatSize = Float.BYTES;
        int strideBytes = stride * floatSize;

        glVertexAttribPointer(0, 3, GL_FLOAT, false, strideBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, strideBytes, 3 * floatSize);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 2, GL_FLOAT, false, strideBytes, 6 * floatSize);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, 1, GL_FLOAT, false, strideBytes, 8 * floatSize);
        glEnableVertexAttribArray(3);

        glVertexAttribPointer(4, 1, GL_FLOAT, false, strideBytes, 9 * floatSize);
        glEnableVertexAttribArray(4);

        glVertexAttribPointer(5, 1, GL_FLOAT, false, strideBytes, 10 * floatSize);
        glEnableVertexAttribArray(5);

        glBindVertexArray(0);
    }

    private void processInput() {
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
            camera.processKeyboard(Camera.CameraMovement.FORWARD, deltaTime);
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
            camera.processKeyboard(Camera.CameraMovement.BACKWARD, deltaTime);
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
            camera.processKeyboard(Camera.CameraMovement.LEFT, deltaTime);
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
            camera.processKeyboard(Camera.CameraMovement.RIGHT, deltaTime);
        if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS)
            camera.processKeyboard(Camera.CameraMovement.ZOOM_IN, deltaTime);
        if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS)
            camera.processKeyboard(Camera.CameraMovement.ZOOM_OUT, deltaTime);
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
        // Convert mouse coordinates to normalized device coordinates
        float x = (2.0f * mouseX) / 800 - 1.0f;
        float y = 1.0f - (2.0f * mouseY) / 600;

        // Create the same matrices as in rendering
        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(camera.getZoom()),
                800.0f / 600.0f,
                0.1f,
                15000.0f
        );

        Matrix4f view = camera.getViewMatrix();
        Matrix4f model = new Matrix4f().identity();

        // Calculate the inverse of the combined MVP matrix
        Matrix4f inverse = new Matrix4f();
        projection.mul(view, inverse);
        inverse.mul(model);
        inverse.invert();

        // Create ray from camera through mouse position
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

        // Intersect with y=0 plane to find tile
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
}