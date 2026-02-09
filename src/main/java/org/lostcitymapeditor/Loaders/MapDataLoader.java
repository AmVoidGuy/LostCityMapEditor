package org.lostcitymapeditor.Loaders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.lostcitymapeditor.DataObjects.MapData;
import org.lostcitymapeditor.Transformers.MapDataTransformer;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapDataLoader {

    public static void exportMap(MapData currentMapData, String currentMapFileName, String path) {
        if (currentMapData == null || currentMapFileName == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText(null);
            alert.setContentText("No map loaded to export.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Map File");
        fileChooser.setInitialFileName(currentMapFileName);
        fileChooser.setInitialDirectory(new File(path + "/maps/"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JM2 Files", "*.jm2"));

        Stage stage = new Stage();
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            String exportFilePath = file.getAbsolutePath();
            MapDataTransformer.writeJM2File(currentMapData, exportFilePath);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("Map exported to: " + exportFilePath);
            alert.showAndWait();
        }
    }

    public static ObservableList<String> getJM2Files(String directoryPath) {
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            System.err.println("Directory not found: " + directoryPath);
            return FXCollections.observableArrayList();
        }

        if (!directory.isDirectory()) {
            System.err.println("Path is not a directory: " + directoryPath);
            return FXCollections.observableArrayList();
        }

        try (Stream<Path> paths = Files.list(directory.toPath())) {
            // Make it alphabetical order - Squid
            List<String> fileNames = paths
                .filter(path -> !Files.isDirectory(path))
                .filter(path -> path.toString().toLowerCase().endsWith(".jm2"))
                .map(path -> path.getFileName().toString())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
            return FXCollections.observableArrayList(fileNames);
        } catch (IOException e) {
            System.err.println("Error reading directory: " + directoryPath + " - " + e.getMessage());
            e.printStackTrace();
        }

        return FXCollections.observableArrayList();
    }
}
