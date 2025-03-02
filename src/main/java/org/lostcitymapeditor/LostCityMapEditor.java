package org.lostcitymapeditor;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import org.lostcitymapeditor.DataObjects.MapData;
import org.lostcitymapeditor.DataObjects.OverlayData;
import org.lostcitymapeditor.DataObjects.TileData;
import org.lostcitymapeditor.DataObjects.UnderlayData;
import org.lostcitymapeditor.Loaders.MapDataLoader;
import org.lostcitymapeditor.Loaders.TextureLoader;
import org.lostcitymapeditor.Loaders.TileShapeLoader;
import org.lostcitymapeditor.Renderer.Renderer;
import org.lostcitymapeditor.Transformers.FloFileTransformer;
import org.lostcitymapeditor.Transformers.TileDataTransformer;
import org.lostcitymapeditor.Transformers.MapDataTransformer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.lostcitymapeditor.Transformers.PackFileTransformer.parseFloPack;

public class LostCityMapEditor extends Application {

    private static final double ROTATION_SPEED = 0.2;
    private static final double CAMERA_MOVEMENT_SPEED = 0.5;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Map<TileData, MeshView> tileMeshMap = new HashMap<>();
    private static final int REGION_SIZE = 64;
    private double mousePosX;
    private double mousePosY;
    private double mouseOldAngleX;
    private double mouseOldAngleY;
    private static Map<Integer, String> floMap;
    private static Map<String, Integer> underlayMap;
    private static Map<String, Object> overlayMap;
    private MapData currentMapData;
    private Group root3D;
    private SubScene subScene;
    private PerspectiveCamera camera;
    private CheckBox underlayCheckbox;
    private CheckBox overlayCheckbox;
    private CheckBox locCheckbox;
    private ComboBox<Integer> levelComboBox;
    private int currentLevel = 0;
    private ComboBox<String> underlayComboBox;
    private String selectedUnderlay;
    private ComboBox<String> overlayComboBox;
    private String selectedOverlay;
    private String currentMapFileName;
    private VBox tileInspector;
    private Label tileDetailsLabel;
    private TileData selectedTile;
    private TextField newHeightTextField;
    private Map<Integer, Image> shapeImages = new HashMap<>();
    private ComboBox<Integer> shapeComboBox;
    private Integer selectedShape = 0;
    private ComboBox<Integer> rotationComboBox;
    private Renderer renderer;

    @Override
    public void start(Stage stage) throws Exception {
        floMap = parseFloPack();
        underlayMap = FloFileTransformer.parseUnderlayFlo();
        overlayMap = FloFileTransformer.parseOverlayFlo();
        TileShapeLoader.loadShapeImages(shapeImages);

        root3D = new Group();
        subScene = new SubScene(root3D, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        camera = new PerspectiveCamera(true);
        camera.setTranslateX(52);
        camera.setTranslateY(-93);
        camera.setTranslateZ(1);
        rotateX.setAngle(640.0);
        rotateY.setAngle(350.0);
        camera.setNearClip(0.1);
        camera.setFarClip(500);
        camera.setFieldOfView(30.0);

        camera.getTransforms().addAll(rotateX, rotateY);
        subScene.setCamera(camera);

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
            selectedUnderlay = underlayComboBox.getValue();
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
                            Image texture = TextureLoader.loadTexture(textureName);
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
                            Image texture = TextureLoader.loadTexture(textureName);
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
            selectedOverlay = overlayComboBox.getValue();
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
        levelComboBox.setItems(FXCollections.observableArrayList(IntStream.range(0, 5).boxed().collect(Collectors.toList())));
        levelComboBox.setValue(0);

        Label currentLevelLabel = new Label("Current Level");

        Label displayLabel = new Label("Display:");
        underlayCheckbox = new CheckBox("Underlay");
        overlayCheckbox = new CheckBox("Overlay");
        locCheckbox = new CheckBox("{Render WIP} Locs");
        underlayCheckbox.setSelected(true);
        overlayCheckbox.setSelected(true);
        locCheckbox.setSelected(false);

        Button exportButton = new Button("Export Map");

        tileInspector = new VBox();
        tileInspector.setPrefWidth(200);
        tileInspector.setPadding(new Insets(10));
        tileInspector.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        Label inspectorTitle = new Label("--Tile Inspector--\nLeft Click to inspect\nCtrl + Click to update");
        tileDetailsLabel = new Label("No tile selected.");
        Label currentUnderlayLabel = new Label("New Underlay");
        Label currentOverlayLabel = new Label("New Overlay");
        Label newHeightLabel = new Label("New Height");
        Label newShapeLabel = new Label("{Render WIP} New Shape");
        Label newRotationLabel = new Label("{Render WIP} New Rotation");
        ObservableList<Integer> rotationValues = FXCollections.observableArrayList(0, 90, 180, 270);
        rotationComboBox = new ComboBox<>(rotationValues);
        rotationComboBox.setValue(0);
        newHeightTextField = new TextField();

        tileInspector.getChildren().addAll(inspectorTitle, tileDetailsLabel, currentUnderlayLabel, underlayComboBox, currentOverlayLabel, overlayComboBox, newHeightLabel, newHeightTextField, newRotationLabel, rotationComboBox, newShapeLabel, shapeComboBox);

        VBox controlsBox = new VBox(currentLevelLabel, levelComboBox, displayLabel, underlayCheckbox, overlayCheckbox, locCheckbox, exportButton);
        controlsBox.setPadding(new Insets(0));

        VBox sideBar = new VBox(new Label("Current Map"), mapListView, controlsBox);
        BorderPane.setMargin(controlsBox, new Insets(10, 0, 10, 0));

        HBox mainContent = new HBox(subScene, tileInspector);
        HBox.setHgrow(subScene, Priority.ALWAYS);

        BorderPane rootPane = new BorderPane();
        rootPane.setLeft(sideBar);
        rootPane.setCenter(mainContent);

        Scene scene = new Scene(rootPane, 1200, 600);

        renderer = new Renderer(root3D, tileMeshMap, overlayCheckbox, underlayCheckbox, REGION_SIZE);

        mapListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        loadAndRenderMap(MapDataLoader.MAPS_DIRECTORY + newValue);
                        currentMapFileName = newValue;
                    }
                });

        levelComboBox.setOnAction(e -> {
            currentLevel = levelComboBox.getValue();
            if (currentMapData != null) {
                renderer.renderMap(currentMapData, currentLevel);
            }
        });

        underlayCheckbox.setOnAction(e -> {
            if (currentMapData != null) {
                renderer.renderMap(currentMapData, currentLevel);
            }
        });

        overlayCheckbox.setOnAction(e -> {
            if (currentMapData != null) {
                renderer.renderMap(currentMapData, currentLevel);
            }
        });

        locCheckbox.setOnAction(e -> {
            if (currentMapData != null) {
                renderer.renderMap(currentMapData, currentLevel);
            }
        });

        exportButton.setOnAction(e -> MapDataLoader.exportMap(currentMapData, currentMapFileName));

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W:
                    camera.setTranslateZ(camera.getTranslateZ() + CAMERA_MOVEMENT_SPEED);
                    break;
                case S:
                    camera.setTranslateZ(camera.getTranslateZ() - CAMERA_MOVEMENT_SPEED);
                    break;
                case A:
                    camera.setTranslateX(camera.getTranslateX() - CAMERA_MOVEMENT_SPEED);
                    break;
                case D:
                    camera.setTranslateX(camera.getTranslateX() + CAMERA_MOVEMENT_SPEED);
                    break;
                case Q:
                    camera.setTranslateY(camera.getTranslateY() + CAMERA_MOVEMENT_SPEED);
                    break;
                case E:
                    camera.setTranslateY(camera.getTranslateY() - CAMERA_MOVEMENT_SPEED);
                    break;
            }
        });

        scene.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                mousePosX = event.getSceneX();
                mousePosY = event.getSceneY();
                mouseOldAngleX = rotateX.getAngle();
                mouseOldAngleY = rotateY.getAngle();
            }
        });

        scene.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                double mouseDeltaX = (event.getSceneX() - mousePosX) * ROTATION_SPEED;
                double mouseDeltaY = (event.getSceneY() - mousePosY) * ROTATION_SPEED;

                double angleX = mouseOldAngleX - mouseDeltaY;
                double angleY = mouseOldAngleY + mouseDeltaX;

                rotateX.setAngle(angleX);
                rotateY.setAngle(angleY);
                camera.getTransforms().clear();
                camera.getTransforms().addAll(rotateX, rotateY);
            }
        });

        subScene.setOnMouseClicked(this::handleImageClick);

        if (!mapFiles.isEmpty()) {
            mapListView.getSelectionModel().selectFirst();
        }

        stage.setTitle("LostCity Map Editor");
        stage.setScene(scene);
        scene.getRoot().requestFocus();
        stage.show();
    }

    private void loadAndRenderMap(String filePath) {
        try {
            currentMapData = MapDataTransformer.parseJM2File(filePath);
            TileDataTransformer.calculateCornerHeights(currentMapData);
            renderer.renderMap(currentMapData, currentLevel);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Loading Map");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load map file: " + filePath);
            alert.showAndWait();
        }
    }

    private void handleImageClick(MouseEvent event) {
        subScene.requestFocus();
        if (event.getButton() == MouseButton.PRIMARY) {
            PickResult pickResult = event.getPickResult();
            Node intersectedNode = pickResult.getIntersectedNode();

            if (intersectedNode instanceof MeshView) {
                MeshView meshView = (MeshView) intersectedNode;
                TileData tileData = (TileData) meshView.getUserData();

                if (tileData != null) {
                    selectedTile = tileData;
                    displayTileDetails(tileData);

                    if (event.isControlDown()) {
                        if (selectedUnderlay != null) {
                            if (selectedUnderlay.equals("Clear")) {
                                tileData.underlay = null;
                            } else if (!selectedUnderlay.equals("Original")) {
                                tileData.underlay = new UnderlayData(findOverlayOrUnderlayIdByName(selectedUnderlay));
                            }
                        }
                        if (selectedOverlay != null) {
                            if (selectedOverlay.equals("Clear")) {
                                tileData.overlay = null;
                            } else if (!selectedOverlay.equals("Original")) {
                                tileData.overlay = new OverlayData(findOverlayOrUnderlayIdByName(selectedOverlay));
                            }
                        }
                        if (newHeightTextField.getText() != null && !newHeightTextField.getText().isEmpty()) {
                            try {
                                double newHeight = Double.parseDouble(newHeightTextField.getText());
                                tileData.height = (int) Math.floor(newHeight);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid height value: " + newHeightTextField.getText());
                            }
                        }

                        Integer selectedRotation = rotationComboBox.getValue();
                        if (selectedRotation == null || selectedRotation == 0) {
                            tileData.rotation = null;
                        } else if (selectedRotation == 90) {
                            tileData.rotation = 1;
                        } else if (selectedRotation == 180) {
                            tileData.rotation = 2;
                        } else if (selectedRotation == 270) {
                            tileData.rotation = 3;
                        }
                        tileData.shape = selectedShape;
                        TileDataTransformer.calculateCornerHeights(currentMapData);
                        renderer.createOrUpdateTileMesh(tileData);
                        renderer.updateSurroundingTiles(tileData, currentMapData);
                    }
                }
            } else if (intersectedNode instanceof Box) {
                Box box = (Box) intersectedNode;

                Point3D worldIntersection = pickResult.getIntersectedPoint();

                Point3D localIntersection = box.sceneToLocal(worldIntersection);

                int tileX = (int) Math.floor(localIntersection.getX() + 64);
                int tileZ = (int) Math.floor(localIntersection.getZ() + 64);

                if (event.isControlDown()) {
                    createNewTile(tileX, tileZ);
                }

            } else {
                tileDetailsLabel.setText("No tile selected.");
            }
        }
    }

    private void displayTileDetails(TileData tile) {
        if (tile == null) {
            tileDetailsLabel.setText("No tile selected.");
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("X: ").append(tile.x).append("\n");
        details.append("Z: ").append(tile.z).append("\n");
        details.append("Height: ").append(tile.height).append("\n");
        details.append("Shape: ").append(tile.shape).append("\n");
        details.append("Rotation: ").append(tile.rotation).append("\n");

        if (tile.underlay != null) {
            String underlayName = floMap.get(tile.underlay.id);
            details.append("Underlay: ").append(underlayName != null ? underlayName : "Unknown").append("\n");
        } else {
            details.append("Underlay: None\n");
        }

        if (tile.overlay != null) {
            String overlayName = floMap.get(tile.overlay.id);
            details.append("Overlay: ").append(overlayName != null ? overlayName : "Unknown").append("\n");
        } else {
            details.append("Overlay: None\n");
        }

        tileDetailsLabel.setText(details.toString());
    }

    private void createNewTile(int x, int z) {
        TileData existingTile = TileDataTransformer.findTile(currentMapData.mapTiles, x, z, currentLevel);
        if (existingTile != null) {
            selectedTile = existingTile;
            displayTileDetails(selectedTile);
            return;
        }

        TileData newTile = new TileData(currentLevel, x, z);

        if (selectedOverlay != null && !selectedOverlay.equals("Original") && !selectedOverlay.equals("Clear")) {
            Integer overlayId = findOverlayOrUnderlayIdByName(selectedOverlay);
            newTile.overlay = new OverlayData(overlayId);
        } else if (selectedOverlay != null && selectedOverlay.equals("Clear")) {
            newTile.overlay = null;
        }

        if (selectedUnderlay != null && !selectedUnderlay.equals("Original") && !selectedUnderlay.equals("Clear")) {
            Integer underlayId = findOverlayOrUnderlayIdByName(selectedUnderlay);
            newTile.underlay = new UnderlayData(underlayId);
        } else if (selectedUnderlay != null && selectedUnderlay.equals("Clear")) {
            newTile.underlay = null;
        }

        if (newHeightTextField.getText() != null && !newHeightTextField.getText().isEmpty()) {
            try {
                double newHeight = Double.parseDouble(newHeightTextField.getText());
                newTile.height = (int) Math.floor(newHeight);
                TileDataTransformer.calculateCornerHeights(currentMapData);
            } catch (NumberFormatException e) {
                System.err.println("Invalid height value: " + newHeightTextField.getText());
            }
        }

        currentMapData.mapTiles.add(newTile);

        TileDataTransformer.calculateCornerHeights(currentMapData);
        renderer.createOrUpdateTileMesh(newTile);
        displayTileDetails(newTile);
        renderer.updateSurroundingTiles(newTile, currentMapData);
    }

    private Integer findOverlayOrUnderlayIdByName(String name) {
        for (Map.Entry<Integer, String> entry : floMap.entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey();
            }
        }
        System.err.println("No ID found for name: " + name);
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Map<Integer, String> getFloMap() {
        return floMap;
    }

    public static Map<String, Integer> getUnderlayMap() {
        return underlayMap;
    }

    public static Map<String, Object> getOverlayMap() {
        return overlayMap;
    }
}