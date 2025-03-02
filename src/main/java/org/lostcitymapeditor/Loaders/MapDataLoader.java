package org.lostcitymapeditor.Loaders;

import javafx.scene.control.Alert;
import org.lostcitymapeditor.DataObjects.MapData;
import org.lostcitymapeditor.Transformers.MapDataTransformer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MapDataLoader {

    public static final String MAPS_DIRECTORY = "Data/Maps/";
    public static final String NEW_MAPS_DIRECTORY = "Data/NewMaps/";

    public static void exportMap(MapData currentMapData, String currentMapFileName) {
        if (currentMapData == null || currentMapFileName == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText(null);
            alert.setContentText("No map loaded to export.");
            alert.showAndWait();
            return;
        }

        File newMapsDir = new File(NEW_MAPS_DIRECTORY);
        if (!newMapsDir.exists()) {
            if (!newMapsDir.mkdirs()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to create the directory: " + NEW_MAPS_DIRECTORY);
                alert.showAndWait();
                return;
            }
        }

        String exportFilePath = NEW_MAPS_DIRECTORY + currentMapFileName;
        MapDataTransformer.writeJM2File(currentMapData, exportFilePath);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Successful");
        alert.setHeaderText(null);
        alert.setContentText("Map exported to: " + exportFilePath);
        alert.showAndWait();
    }

    public static ObservableList<String> getJM2Files() {
        try {
            Path dirPath = Paths.get(MAPS_DIRECTORY);
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                System.err.println("Directory not found: " + MAPS_DIRECTORY);
                return FXCollections.observableArrayList();
            }

            List<String> files = Files.list(dirPath)
                    .filter(path -> path.toString().toLowerCase().endsWith(".jm2"))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
            return FXCollections.observableArrayList(files);
        } catch (IOException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }
}