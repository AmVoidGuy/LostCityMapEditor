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

import java.lang.Math;
import java.util.*;

import static org.lostcitymapeditor.DataObjects.newTriangle.getTriangles;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.nanovg.*;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
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
    private newTriangle selectedTriangle = null;
    private TileData selectedTile = null;
    private long nvgContext;
    private int fontRegular;
    private boolean uiInitialized = false;
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
            world3D.draw(4050, -6350, 4050, 4, 0, 511, 0, currentLevel);
            List<newTriangle> triangleList = getTriangles();
            setTriangles(triangleList);
            setupVertexData();
        });
    }

    public void drawNewMap(MapData currentMapData) {
        enqueueGLTask(() -> {
            world = new World(REGION_SIZE, REGION_SIZE);
            world.loadGround(currentMapData);
            world3D = new World3D(world.levelHeightmap, REGION_SIZE, LEVELS, REGION_SIZE);
            world.build(world3D);
            World3D.init(800, 600, 500, 800, distance);
            newTriangle.clearCollectedTriangles();
            world3D.draw(4050, -6350, 4050, 4, 0, 511, 0, currentLevel);
            List<newTriangle> triangleList = getTriangles();
            setTriangles(triangleList);
            setupVertexData();
        });
    }

    public void enqueueGLTask(Runnable task) {
        glQueue.add(task);
    }

    public void setupJavaFXUI() {
        JFrame frame = new JFrame("LostCity Map Editor Config");
        fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setSize(400, 600);
        frame.setVisible(true);

        Platform.runLater(() -> {
            ObservableList<String> mapFiles = MapDataLoader.getJM2Files();
            ListView<String> mapListView = new ListView<>(mapFiles);
            mapListView.setPrefWidth(200);

            ObservableList<String> underlayNames = FXCollections.observableArrayList(underlayMap.keySet());
            underlayNames.addFirst("Original");
            underlayNames.add(1, "Clear");
            underlayComboBox = new ComboBox<>(underlayNames);
            underlayComboBox.setPrefWidth(200);
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
            overlayComboBox.setPrefWidth(200);

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
            shapeComboBox.setPrefWidth(200);
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
            locCheckbox = new CheckBox("{Render WIP} Locs");
            locCheckbox.setSelected(false);

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

            tileInspector.getChildren().addAll(
                    inspectorTitle,
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
                        //TODO:
                    });
                }
            });

            exportButton.setOnAction(e -> {
                    MapDataLoader.exportMap(currentMapData, currentMapFileName);
            });
        });
    }

    public void run() {
        setupJavaFXUI();
        init();
        enableTextureRendering();
        setupVertexData();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        if (uiInitialized) {
            nvgDelete(nvgContext);
        }
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
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
                    firstMouse = true; // Reset firstMouse on right click press, otherwise the first camera movement is a large jump.
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
                selectedTriangle = pickTriangle(mouseX, mouseY);
                if (selectedTriangle != null) {
                    selectedTile = currentMapData.mapTiles[currentLevel][selectedTriangle.tileData.tileX][selectedTriangle.tileData.tileZ];
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
                    }
                }
                if (ctrlPressed) {
                    if (selectedTriangle != null && selectedTile != null) {
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
                    } else {
                        Vector2i tileCoords = pickTile(mouseX, mouseY);
                        TileData currentTile = currentMapData.mapTiles[selectedTile.level][selectedTile.x][selectedTile.z];
                        if (tileCoords != null) {
                            int tileX = tileCoords.x;
                            int tileZ = tileCoords.y;
                            TileData newTile = new TileData(currentLevel, tileX, tileZ);
                            if (selectedOverlayID == null || selectedOverlayID == -1) {
                                newTile.overlay = null;
                            } else if (selectedOverlayID == 0) {
                                if(currentTile.overlay != null) {
                                    newTile.overlay = currentTile.overlay;
                                } else {
                                    newTile.overlay = null;
                                }
                            } else {
                                newTile.overlay = new OverlayData(selectedOverlayID);
                            }
                            if (selectedUnderlayID == null || selectedUnderlayID == -1) {
                                newTile.underlay = null;
                            } else if (selectedUnderlayID == 0) {
                                if(currentTile.underlay != null) {
                                    newTile.underlay = currentTile.underlay;
                                } else {
                                    newTile.underlay = null;
                                }
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
                                if(currentTile.height != null) {
                                    newTile.height = currentTile.height;
                                } else {
                                    newTile.height = 0;
                                }
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
                            if(newTile.overlay != null || newTile.underlay != null) {
                                currentMapData.mapTiles[currentLevel][tileX][tileZ] = newTile;
                                drawNewMap(currentMapData);
                                selectedTile = newTile;
                            }
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
        initUI();
        openglInitialized = true;
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        camera = new Camera(new Vector3f(4400.0f, -8000.0f, 4000.0f));
        setupShaders();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            // Poll events first
            glfwPollEvents();

            // Calculate delta time
            float currentFrame = (float) glfwGetTime();
            deltaTime = currentFrame - lastFrame;
            lastFrame = currentFrame;

            processInput();

            // Process OpenGL tasks from the queue
            Runnable task;
            while ((task = glQueue.poll()) != null) {
                task.run();  // Execute the OpenGL task on the OpenGL thread
            }

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glUseProgram(shaderProgram);

            // Create transformation matrices
            Matrix4f model = new Matrix4f();
            Matrix4f view = camera.getViewMatrix();
            Matrix4f projection = new Matrix4f().perspective(
                    (float) Math.toRadians(camera.getZoom()),
                    800.0f / 600.0f,
                    0.1f,
                    15000.0f
            );

            // Get uniform locations
            int modelLoc = glGetUniformLocation(shaderProgram, "model");
            int viewLoc = glGetUniformLocation(shaderProgram, "view");
            int projectionLoc = glGetUniformLocation(shaderProgram, "projection");

            // Pass the matrices to the shader
            FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
            model.get(modelBuffer);
            glUniformMatrix4fv(modelLoc, false, modelBuffer);

            FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
            view.get(viewBuffer);
            glUniformMatrix4fv(viewLoc, false, viewBuffer);

            FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);
            projection.get(projectionBuffer);
            glUniformMatrix4fv(projectionLoc, false, projectionBuffer);

            // Render 3D scene
            Map<Integer, List<Integer>> textureGroups = groupTrianglesByTexture();
            // Bind the VAO
            glBindVertexArray(vao);

            for (Map.Entry<Integer, List<Integer>> entry : textureGroups.entrySet()) {
                int triangleTexId = entry.getKey();
                List<Integer> triangleIndices = entry.getValue();

                // Skip non-textured triangles
                if (triangleTexId < 0) {
                    for (int triangleIndex : triangleIndices) {
                        // Each triangle has 3 vertices
                        glDrawArrays(GL_TRIANGLES, triangleIndex * 3, 3);
                    }
                    continue;
                }

                // Get the OpenGL texture ID from the map
                Integer openglTexId = TextureLoader.textureIdMap.get(triangleTexId);
                if (openglTexId == null) {
                    System.err.println("No OpenGL texture ID found for triangle texture ID: " + triangleTexId);
                    continue;
                }

                // Activate the appropriate texture unit and bind the texture
                glActiveTexture(GL_TEXTURE0 + triangleTexId);
                glBindTexture(GL_TEXTURE_2D, openglTexId);

                // Set the uniform to use the correct texture unit
                int textureUniformLoc = glGetUniformLocation(shaderProgram, "textures[" + triangleTexId + "]");
                if (textureUniformLoc != -1) {
                    glUniform1i(textureUniformLoc, triangleTexId); // Tell shader which texture unit to use
                } else {
                    System.err.println("Warning: Could not find uniform 'textures[" + triangleTexId + "]'");
                }

                // Draw just the triangles for this texture
                for (int triangleIndex : triangleIndices) {
                    // Each triangle has 3 vertices
                    glDrawArrays(GL_TRIANGLES, triangleIndex * 3, 3);
                }
            }

            // Unbind everything
            glBindVertexArray(0);
            glBindTexture(GL_TEXTURE_2D, 0);

            // Your custom UI rendering if needed
            int[] width = new int[1];
            int[] height = new int[1];
            glfwGetWindowSize(window, width, height);
            renderUI(width[0], height[0]);

            // Swap buffers last
            glfwSwapBuffers(window);
        }

        // Cleanup
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteTextures(textureIDs);
        glDeleteProgram(shaderProgram);
    }

    public static void startRender() {
        FileLoader.loadFiles();
        underlayMap = FileLoader.getUnderlayMap();
        overlayMap = FileLoader.getOverlayMap();
        shapeImages = FileLoader.getShapeImages();

        // Set the map data for later use in the application
        currentMapData = MapDataTransformer.parseJM2File(MapDataLoader.MAPS_DIRECTORY + currentMapFileName);

        // Initialize necessary components before launching
        Pix3D.loadTextures();
        Pix3D.setBrightness(0.8);
        Pix3D.initPool(20);
        Pix3D.init3D(800, 600);

        world = new World(REGION_SIZE, REGION_SIZE);
        world.loadGround(currentMapData);
        world3D = new World3D(world.levelHeightmap, REGION_SIZE, LEVELS, REGION_SIZE);
        world.build(world3D);

        distance = new int[9];
        for (int x = 0; x < 9; x++) {
            int angle = x * 32 + 128 + 15;
            int offset = angle * 3 + 600;
            int sin = Pix3D.sinTable[angle];
            distance[x] = offset * sin >> 16;
        }
        World3D.init(800, 600, 500, 800, distance);
        newTriangle.clearCollectedTriangles();
        world3D.draw(4050, -6350, 4050, 4, 0, 511, 0, currentLevel);
        List<newTriangle> triangleList = getTriangles();

        renderer = new OpenGLRenderer();
        renderer.setTriangles(triangleList);
        renderer.run();
    }

    private newTriangle pickTriangle(int mouseX, int mouseY) {
        // Convert mouse coordinates to normalized device coordinates (NDC).
        float x = (2.0f * mouseX) / 800 - 1.0f;
        float y = 1.0f - (2.0f * mouseY) / 600;

        // Create projection and view matrices.
        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(camera.getZoom()),
                800.0f / 600.0f,
                0.1f,
                15000.0f
        );
        Matrix4f view = camera.getViewMatrix();

        // Create an inverse transformation matrix.
        Matrix4f inverse = new Matrix4f(projection).mul(view).invert();

        // Transform the near and far plane coordinates into world space.
        Vector4f nearPointNDC = new Vector4f(x, y, -1.0f, 1.0f);
        Vector4f farPointNDC = new Vector4f(x, y, 1.0f, 1.0f);

        Vector4f nearPointWorld = inverse.transform(nearPointNDC);
        Vector4f farPointWorld = inverse.transform(farPointNDC);

        nearPointWorld.div(nearPointWorld.w);
        farPointWorld.div(farPointWorld.w);

        Vector3f rayOrigin = new Vector3f(nearPointWorld.x, nearPointWorld.y, nearPointWorld.z);
        Vector3f rayDirection = new Vector3f(farPointWorld.x - nearPointWorld.x, farPointWorld.y - nearPointWorld.y, farPointWorld.z - nearPointWorld.z).normalize();

        // Perform ray-triangle intersection test for each triangle.
        newTriangle closestTriangle = null;
        float closestDistance = Float.MAX_VALUE;
        for (newTriangle triangle : triangleList) {
            // Extract the vertices of the triangle.
            Vector3f v0 = new Vector3f(triangle.vertices[0], triangle.vertices[1], triangle.vertices[2]);
            Vector3f v1 = new Vector3f(triangle.vertices[3], triangle.vertices[4], triangle.vertices[5]);
            Vector3f v2 = new Vector3f(triangle.vertices[6], triangle.vertices[7], triangle.vertices[8]);

            // Compute the intersection using the Möller–Trumbore intersection algorithm
            Vector3f edge1 = new Vector3f(v1).sub(v0);
            Vector3f edge2 = new Vector3f(v2).sub(v0);

            Vector3f h = new Vector3f(rayDirection).cross(edge2);
            float a = edge1.dot(h);
            if (a > -0.00001 && a < 0.00001) {
                continue; // This ray is parallel to this triangle.
            }

            float f = 1.0f / a;
            Vector3f s = new Vector3f(rayOrigin).sub(v0);
            float u = f * s.dot(h);
            if (u < 0.0 || u > 1.0) {
                continue;
            }

            Vector3f q = new Vector3f(s).cross(edge1);
            float v = f * rayDirection.dot(q);
            if (v < 0.0 || u + v > 1.0) {
                continue;
            }

            // At this stage we can compute t to find out where the intersection point is on the line.
            float t = f * edge2.dot(q);
            if (t > 0.00001) { // ray intersection
                if (t < closestDistance) {
                    closestDistance = t;
                    closestTriangle = triangle;
                }
            } else {
                continue; // This means that there is a line intersection but not a ray intersection.
            }
        }
        return closestTriangle;
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
            // Load texture and store the returned ID directly
            textureIDs[i] = TextureLoader.loadTexture(i);
        }

        texturesLoaded = true;
        System.out.println("Finished loading " + numTextures + " textures");
    }

    private void setupShaders() {
        // Vertex shader source code with MVP matrices and texture support
        String vertexShaderSource =
                "#version 330 core\n" +
                        "layout (location = 0) in vec3 aPos;\n" +
                        "layout (location = 1) in vec3 aColor;\n" +
                        "layout (location = 2) in vec2 aTexCoord;\n" +  // Add texture coordinate attribute
                        "layout (location = 3) in float aUseTexture;\n" + // Flag to indicate if using texture
                        "layout (location = 4) in float aTextureID;\n" + // Texture ID to use
                        "out vec3 vertexColor;\n" +
                        "out vec2 TexCoord;\n" +  // Output texture coordinates to fragment shader
                        "out float useTexture;\n" + // Pass texture flag to fragment shader
                        "out float textureID;\n" + // Pass texture ID to fragment shader
                        "uniform mat4 model;\n" +
                        "uniform mat4 view;\n" +
                        "uniform mat4 projection;\n" +
                        "void main()\n" +
                        "{\n" +
                        "   gl_Position = projection * view * model * vec4(aPos, 1.0);\n" +
                        "   vertexColor = aColor;\n" +
                        "   TexCoord = aTexCoord;\n" +
                        "   useTexture = aUseTexture;\n" +
                        "   textureID = aTextureID;\n" +
                        "}\0";

        String fragmentShaderSource =
                "#version 330 core\n" +
                        "in vec3 vertexColor;\n" +
                        "in vec2 TexCoord;\n" +
                        "in float useTexture;\n" +
                        "in float textureID;\n" +
                        "out vec4 FragColor;\n" +
                        "uniform sampler2D textures[50];\n" +
                        "void main()\n" +
                        "{\n" +
                        "    if (useTexture > 0.5) {\n" +
                        "        // Round to nearest integer to avoid precision issues\n" +
                        "        int texID = int(round(textureID));\n" +
                        "        if (texID >= 0 && texID < 50) {\n" +
                        "            vec4 texColor = texture(textures[texID], TexCoord);\n" +
                        "            if (texColor.a < 0.1) discard; // Handle transparency better\n" +
                        "            FragColor = texColor;\n" +
                        "        } else {\n" +
                        "            FragColor = vec4(1.0, 0.0, 1.0, 1.0);\n" +
                        "        }\n" +
                        "    } else {\n" +
                        "        FragColor = vec4(vertexColor, 1.0);\n" +
                        "    }\n" +
                        "}\0";

        // Compile shaders as before
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);

        // Check for vertex shader compile errors
        int success = glGetShaderi(vertexShader, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(vertexShader);
            System.err.println("ERROR::SHADER::VERTEX::COMPILATION_FAILED\n" + infoLog);
        }

        // Compile shaders as before (Fragment Shader)
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);

        // Check for fragment shader compile errors
        success = glGetShaderi(fragmentShader, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(fragmentShader);
            System.err.println("ERROR::SHADER::FRAGMENT::COMPILATION_FAILED\n" + infoLog);
        }

        // Create shader program
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        // Check for shader program link errors
        success = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            String infoLog = glGetProgramInfoLog(shaderProgram);
            System.err.println("ERROR::SHADER::PROGRAM::LINKING_FAILED\n" + infoLog);
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);


        //Initialize textures
        initializeTextures();

        // **Crucially, assign the texture units to the samplers:**
        glUseProgram(shaderProgram); // Activate the shader program

        int texturesLocation = glGetUniformLocation(shaderProgram, "textures");
        if (texturesLocation == -1) {
            System.err.println("ERROR: Could not find uniform 'textures' in shader.");
        }
        //Create an array of texture indices.
        int[] textureIndices = new int[50];
        for(int i = 0; i < 50; i++) {
            textureIndices[i] = i;
        }

        //Set the value of the samplers in the shader.
        glUniform1iv(texturesLocation, textureIndices);

        glUseProgram(0); // Unbind the shader program
    }

    private void setupVertexData() {
        if (triangleList == null || triangleList.isEmpty()) {
            System.out.println("No triangles available");
            return;
        }

        setupVertexDataWithTriangles(triangleList);
    }

    private void setupVertexDataWithTriangles(List<newTriangle> triangles) {

        // Count total vertices
        int totalVertices = triangles.size() * 3; // 3 vertices per triangle

        // Create arrays to hold all vertex data
        float[] vertices = new float[totalVertices * 3]; // xyz for each vertex
        float[] colors = new float[totalVertices * 3]; // rgb for each vertex
        float[] texCoords = new float[totalVertices * 2]; // uv for each vertex
        float[] useTexture = new float[totalVertices]; // flag for each vertex
        float[] textureID = new float[totalVertices]; // textureId for each vertex

        // Default texture coordinates for triangles
        float[][] defaultTexCoords = {
                {0.0f, 0.0f}, // Bottom-left
                {1.0f, 0.0f}, // Bottom-right
                {0.0f, 1.0f}  // Top-center
        };

        int vertexIndex = 0;
        int colorIndex = 0;
        int texCoordIndex = 0;
        int flagIndex = 0;

        for (newTriangle triangle : triangles) {
            // Each triangle has 3 vertices with xyz coordinates
            for (int i = 0; i < 9; i++) {
                vertices[vertexIndex++] = triangle.vertices[i];
            }

            // Set texture coordinates and flags for each vertex
            for (int i = 0; i < 3; i++) {
                if (triangle.textureId > -1) {
                    texCoords[texCoordIndex++] = defaultTexCoords[i][0];
                    texCoords[texCoordIndex++] = defaultTexCoords[i][1];
                    useTexture[flagIndex] = 1.0f;
                    textureID[flagIndex] = triangle.textureId;
                    flagIndex++;
                } else {
                    texCoords[texCoordIndex++] = 0.0f;
                    texCoords[texCoordIndex++] = 0.0f;
                    useTexture[flagIndex] = 0.0f;
                    textureID[flagIndex] = -1.0f;
                    flagIndex++;
                }
            }

            if (triangle.colors != null) {
                for (int i = 0; i < 3; i++) {
                    int color = Pix3D.colourTable[triangle.colors[i]];
                    float r = ((color >> 16) & 0xFF) / 255.0f;
                    float g = ((color >> 8) & 0xFF) / 255.0f;
                    float b = (color & 0xFF) / 255.0f;

                    colors[colorIndex++] = r;
                    colors[colorIndex++] = g;
                    colors[colorIndex++] = b;
                }
            } else {
                // Default color if none specified
                for (int i = 0; i < 3; i++) {
                    colors[colorIndex++] = 1.0f; // r
                    colors[colorIndex++] = 0.5f; // g
                    colors[colorIndex++] = 0.2f; // b
                }
            }
        }

        // Create and bind VAO
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Create and bind VBO for vertex positions
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // Copy vertex data to VBO
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
        verticesBuffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        // Set up vertex attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Create and bind VBO for colors
        int colorVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, colorVBO);

        // Copy color data to VBO
        FloatBuffer colorsBuffer = BufferUtils.createFloatBuffer(colors.length);
        colorsBuffer.put(colors).flip();
        glBufferData(GL_ARRAY_BUFFER, colorsBuffer, GL_STATIC_DRAW);

        // Set up color attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(1);

        // Create and bind VBO for texture coordinates
        int texCoordVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, texCoordVBO);

        // Copy texture coordinate data to VBO
        FloatBuffer texCoordsBuffer = BufferUtils.createFloatBuffer(texCoords.length);
        texCoordsBuffer.put(texCoords).flip();
        glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);

        // Set up texture coordinate attribute
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(2);

        // Create and bind VBO for texture usage flag
        int useTextureVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, useTextureVBO);

        // Copy texture usage flag data to VBO
        FloatBuffer useTextureBuffer = BufferUtils.createFloatBuffer(useTexture.length);
        useTextureBuffer.put(useTexture).flip();
        glBufferData(GL_ARRAY_BUFFER, useTextureBuffer, GL_STATIC_DRAW);

        // Set up texture usage flag attribute
        glVertexAttribPointer(3, 1, GL_FLOAT, false, Float.BYTES, 0);
        glEnableVertexAttribArray(3);

        // Create and bind VBO for texture ID
        int textureIDVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, textureIDVBO);

        // Copy texture ID data to VBO
        FloatBuffer textureIDBuffer = BufferUtils.createFloatBuffer(textureID.length);
        textureIDBuffer.put(textureID).flip();
        glBufferData(GL_ARRAY_BUFFER, textureIDBuffer, GL_STATIC_DRAW);

        // Set up texture ID attribute
        glVertexAttribPointer(4, 1, GL_FLOAT, false, Float.BYTES, 0);
        glEnableVertexAttribArray(4);

        // Unbind
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        System.out.println("Successfully set up " + totalVertices + " vertices");
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

    private void initUI() {
        // Create NanoVG context
        nvgContext = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        if (nvgContext == 0) {
            throw new RuntimeException("Could not initialize NanoVG");
        }

        // Load font
        fontRegular = nvgCreateFont(nvgContext, "sans", "C:/Windows/Fonts/arial.ttf");
        if (fontRegular == -1) {
            System.err.println("Could not load font, UI text may not appear correctly");
        }

        uiInitialized = true;
    }

    private void renderUI(int windowWidth, int windowHeight) {
        if (!uiInitialized || selectedTile == null) {
            return;
        }

        // Begin NanoVG frame
        nvgBeginFrame(nvgContext, windowWidth, windowHeight, 1);
        // Draw panel background
        nvgBeginPath(nvgContext);
        nvgRect(nvgContext, 10, 10, 250, 170);
        nvgFillColor(nvgContext, NVGColor.calloc().r(0.2f).g(0.2f).b(0.2f).a(0.8f));
        nvgFill(nvgContext);

        // Draw panel border
        nvgBeginPath(nvgContext);
        nvgRect(nvgContext, 10, 10, 250, 170);
        nvgStrokeColor(nvgContext, NVGColor.calloc().r(0.5f).g(0.5f).b(0.5f).a(1.0f));
        nvgStrokeWidth(nvgContext, 1.0f);
        nvgStroke(nvgContext);

        // Draw header
        nvgFontSize(nvgContext, 18.0f);
        nvgFontFace(nvgContext, "sans");
        nvgTextAlign(nvgContext, NVG_ALIGN_LEFT | NVG_ALIGN_MIDDLE);
        nvgFillColor(nvgContext, NVGColor.calloc().r(1.0f).g(1.0f).b(1.0f).a(1.0f));
        nvgText(nvgContext, 20, 30, "Tile Information");

        // Draw triangle information
        nvgFontSize(nvgContext, 14.0f);

        // Position
        String posText = String.format("Position: X=%d, Z=%d Level=%d",
                selectedTile.x,
                selectedTile.z,
                selectedTile.level);
        nvgText(nvgContext, 20, 60, posText);

        // overlay underlay
        String tileInfoText = "Overlay ID: " + (selectedTile.overlay != null ?
                selectedTile.overlay.id : "None") + ", Underlay ID: " + (selectedTile.underlay!= null ?
                selectedTile.underlay.id : "None") ;
        nvgText(nvgContext, 20, 80, tileInfoText);

        // Height
        String heightText = String.format("Height: %d",
                (selectedTile.height));
        nvgText(nvgContext, 20, 100, heightText);


        // shape info
        String shapeText = "Shape ID: " + (selectedTile.shape != null ? selectedTile.shape : 0);
        nvgText(nvgContext, 20, 120, shapeText);

        // rotation info
        String rotationText = "Shape Rotation: " + (selectedTile.rotation != null ? selectedTile.rotation : 0);
        nvgText(nvgContext, 20, 140, rotationText);

        // Texture info
        String textureText = "Texture Name: " + (selectedTile.overlay != null && selectedTile.overlay.texture != null ?
                selectedTile.overlay.texture : "None");
        nvgText(nvgContext, 20, 160, textureText);

        // End NanoVG frame
        nvgEndFrame(nvgContext);
    }

    private void enableTextureRendering() {
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
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

    private Vector2i pickTile(int mouseX, int mouseY) { // return Vector2i for x,z coords

        // Convert mouse coordinates to normalized device coordinates (NDC).
        float x = (2.0f * mouseX) / 800 - 1.0f;
        float y = 1.0f - (2.0f * mouseY) / 600;

        // Create projection and view matrices.
        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(camera.getZoom()),
                800.0f / 600.0f,
                0.1f,
                15000.0f
        );
        Matrix4f view = camera.getViewMatrix();

        // Create an inverse transformation matrix.
        Matrix4f inverse = new Matrix4f(projection).mul(view).invert();

        // Transform the near and far plane coordinates into world space.
        Vector4f nearPointNDC = new Vector4f(x, y, -1.0f, 1.0f);
        Vector4f farPointNDC = new Vector4f(x, y, 1.0f, 1.0f);

        Vector4f nearPointWorld = inverse.transform(nearPointNDC);
        Vector4f farPointWorld = inverse.transform(farPointNDC);

        nearPointWorld.div(nearPointWorld.w);
        farPointWorld.div(farPointWorld.w);

        Vector3f rayOrigin = new Vector3f(nearPointWorld.x, nearPointWorld.y, nearPointWorld.z);
        Vector3f rayDirection = new Vector3f(farPointWorld.x - nearPointWorld.x, farPointWorld.y - nearPointWorld.y, farPointWorld.z - nearPointWorld.z).normalize();

        // Ground plane intersection (y = 0)
        float t = -rayOrigin.y / rayDirection.y; // t = distance along ray

        if (t >= 0) { // Ray points downwards towards the ground
            float intersectionX = rayOrigin.x + t * rayDirection.x;
            float intersectionZ = rayOrigin.z + t * rayDirection.z;

            // Convert world coordinates to tile coordinates
            int tileX = (int) Math.floor(intersectionX / (REGION_SIZE * 2));
            int tileZ = (int) Math.floor(intersectionZ / (REGION_SIZE * 2));
            if(tileX > REGION_SIZE - 1 || tileZ > REGION_SIZE - 1 || tileZ < 0 || tileX < 0)
                return null;
            return new Vector2i(tileX, tileZ);
        } else {
            System.out.println("Ray does not intersect ground plane.");
            return null;
        }
    }
}