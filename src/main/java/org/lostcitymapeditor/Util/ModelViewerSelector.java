package org.lostcitymapeditor.Util;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.util.*;
import org.lostcitymapeditor.Loaders.FileLoader;
import org.lostcitymapeditor.OriginalCode.*;
import org.lostcitymapeditor.Renderer.ModelViewer;
import org.lostcitymapeditor.Renderer.OpenGLRenderer;

import static org.lostcitymapeditor.OriginalCode.World.findModelWithSuffix;
import static org.lostcitymapeditor.OriginalCode.World.SHAPE_SUFFIX_MAP;

public class ModelViewerSelector extends VBox {
    private final ModelViewer modelViewer;
    private final TextField searchField = new TextField();
    private final ListView<String> modelListView = new ListView<>();
    private final Map<String, Object> locMap;
    private final FilteredList<String> filteredModelList;
    private final Label statusLabel = new Label();
    private final int LOAD_CHUNK_SIZE = 3600;
    private final ProgressBar loadingProgress = new ProgressBar(0);
    private int loadedItemCount = 0;

    public ModelViewerSelector(ModelViewer modelViewer) {
        this.modelViewer = modelViewer;
        this.locMap = new HashMap<>(FileLoader.getAllLocMap());

        ObservableList<String> locNames = FXCollections.observableArrayList();
        this.filteredModelList = new FilteredList<>(locNames, p -> true);

        setupUI();
        loadInitialItems();
    }

    private void setupUI() {
        setPadding(new Insets(10));
        setSpacing(5);

        // Set up search field
        searchField.setPromptText("Search models...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchText = newValue.toLowerCase();
            filteredModelList.setPredicate(modelName -> {
                if (searchText == null || searchText.isEmpty()) {
                    return true;
                }
                return modelName.toLowerCase().contains(searchText);
            });
        });

        // Set up model list view
        modelListView.setItems(filteredModelList);
        modelListView.setPrefHeight(300);
        modelListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        loadSelectedModel(newValue, OpenGLRenderer.selectedShape);
                    }
                }
        );

        modelListView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!event.isControlDown() && !event.isAltDown() && !event.isShiftDown() &&
                    event.getCode() != KeyCode.UP && event.getCode() != KeyCode.DOWN &&
                    event.getCode() != KeyCode.PAGE_UP && event.getCode() != KeyCode.PAGE_DOWN) {
                searchField.requestFocus();
            }
        });

        loadingProgress.setMaxWidth(Double.MAX_VALUE);
        loadingProgress.setVisible(false);

        statusLabel.setText(String.format("Showing %d of %d models", 0, locMap.size()));

        Label titleLabel = new Label("Loc Viewer");
        titleLabel.setStyle("-fx-font-weight: bold;");

        getChildren().addAll(
                titleLabel,
                searchField,
                modelListView,
                statusLabel,
                loadingProgress
        );
    }

    private void loadInitialItems() {
        new Thread(() -> {
            List<String> sortedModels = new ArrayList<>(locMap.keySet());
            Collections.sort(sortedModels);

            List<String> initialChunk = sortedModels.subList(0,
                    Math.min(LOAD_CHUNK_SIZE, sortedModels.size()));

            Platform.runLater(() -> {
                ObservableList<String> source = (ObservableList<String>) filteredModelList.getSource();
                source.clear();
                source.addAll(initialChunk);
                loadedItemCount = initialChunk.size();
                statusLabel.setText(String.format("Showing %d of %d locs",
                        loadedItemCount, locMap.size()));

            });
        }).start();
    }
    public void updateModel(int shape){
        loadSelectedModel(modelListView.getSelectionModel().getSelectedItem(), shape);
    }

    private void loadSelectedModel(String locName, Integer shape) {
        int locId = -1;
        for (Integer id : FileLoader.getLocMap().keySet()) {
            if (FileLoader.getLocMap().get(id).equals(locName)) {
                locId = id;
                break;
            }
        }
        if (locId != -1) {
            LocType loc = LocType.get(locId);
            String target = loc.model;
            Integer modelId = FileLoader.getModelMap().get(target);
            if (modelId == null && shape != null) {
                String shapeSuffix = SHAPE_SUFFIX_MAP.get(shape);
                if (shapeSuffix != null) {
                    modelId = FileLoader.getModelMap().get(target + shapeSuffix);
                }
            }
            if (modelId == null) {
                modelId = findModelWithSuffix(target);
            }
            if (modelId != null) {
                LocType locType = new LocType();
                Model model = locType.getModel(modelId, 0, 0, 0, 0, 0, -1, 0, 0);
                modelViewer.loadModel(model);
            } else {
                System.out.println("No model id " + target);
            }
        }
    }

    public void setFilter(String filter) {
        searchField.setText(filter);
    }

    public String getSelectedModel() {
        return modelListView.getSelectionModel().getSelectedItem();
    }
}