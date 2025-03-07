package org.lostcitymapeditor.Loaders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import org.lostcitymapeditor.DataObjects.MapData;
import org.lostcitymapeditor.Transformers.MapDataTransformer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        List<String> result = new ArrayList<>();
        URL dirURL = MapDataLoader.class.getClassLoader().getResource(MAPS_DIRECTORY);

        if (dirURL == null) {
            System.err.println("Directory not found in resources: " + MAPS_DIRECTORY);
            return FXCollections.observableArrayList();
        }

        try {
            if (dirURL.getProtocol().equals("file")) {
                try (Stream<Path> paths = Files.list(Paths.get(dirURL.toURI()))) {
                    List<String> fileNames = paths
                            .filter(path -> path.toString().toLowerCase().endsWith(".jm2"))
                            .map(path -> path.getFileName().toString())
                            .collect(Collectors.toList());
                    return FXCollections.observableArrayList(fileNames);
                }
            } else if (dirURL.getProtocol().equals("jar")) {
                String jarPath = dirURL.toString().substring(0, dirURL.toString().indexOf("!"));
                String resourcePath = MAPS_DIRECTORY;

                URI uri = URI.create(jarPath);
                FileSystem fs = null;
                try {
                    fs = FileSystems.getFileSystem(uri);
                } catch (FileSystemNotFoundException e) {
                    fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
                }

                try {
                    Path path = fs.getPath(resourcePath);
                    try (Stream<Path> paths = Files.list(path)) {
                        List<String> fileNames = paths
                                .filter(p -> !Files.isDirectory(p))
                                .filter(p -> p.toString().toLowerCase().endsWith(".jm2"))
                                .map(p -> p.getFileName().toString())
                                .collect(Collectors.toList());
                        return FXCollections.observableArrayList(fileNames);
                    }
                } finally {
                }


            } else {
                System.err.println("Unknown protocol: " + dirURL.getProtocol());

                try (InputStream in = MapDataLoader.class.getClassLoader().getResourceAsStream(MAPS_DIRECTORY)) {
                    if (in != null) {
                        System.out.println("Resource exists as a stream, but can't list contents directly");
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("Error reading resources: " + e.getMessage());
            e.printStackTrace();
        }

        return FXCollections.observableArrayList(result);
    }
}