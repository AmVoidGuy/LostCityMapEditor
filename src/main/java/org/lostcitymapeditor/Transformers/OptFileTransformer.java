package org.lostcitymapeditor.Transformers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OptFileTransformer {
    private static final String TEXTURES_OPTS_DIRECTORY = "Data/Textures/TextureOpts";

    public static Map<String, TextureOptions> loadTextureOptions() {
        Map<String, TextureOptions> textureOptions = new HashMap<>();

        try {
            List<String> resourcePaths = getResourcesInDirectory(TEXTURES_OPTS_DIRECTORY);
            if (resourcePaths == null) {
                System.err.println("No resources found in directory: " + TEXTURES_OPTS_DIRECTORY);
                return textureOptions;
            }

            resourcePaths.stream()
                    .filter(resourcePath -> resourcePath.toLowerCase().endsWith(".opt"))
                    .forEach(resourcePath -> {
                        String name = getFileNameWithoutExtension(resourcePath);
                        try {
                            TextureOptions options = parseTextureOptions(resourcePath);
                            if (options != null) {
                                textureOptions.put(name, options);
                            }
                        } catch (IOException e) {
                            System.err.println("Error processing texture options file: " + resourcePath);
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error accessing texture options directory: " + TEXTURES_OPTS_DIRECTORY);
            e.printStackTrace();
        }

        return textureOptions;
    }

    private static TextureOptions parseTextureOptions(String resourcePath) throws IOException {
        try (InputStream inputStream = OptFileTransformer.class.getClassLoader().getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream, "Input stream cannot be null")))) {

            String dataLine = reader.readLine();
            if (dataLine != null) {
                String[] parts = dataLine.split(",");
                if (parts.length == 5) {
                    try {
                        int cropX = Integer.parseInt(parts[0].trim());
                        int cropY = Integer.parseInt(parts[1].trim());
                        int width = Integer.parseInt(parts[2].trim());
                        int height = Integer.parseInt(parts[3].trim());
                        String pixelOrderStr = parts[4].trim().toLowerCase();

                        int pixelOrder = 0;
                        if (pixelOrderStr.equals("column")) {
                            pixelOrder = 1;
                        }

                        return new TextureOptions(cropX, cropY, width, height, pixelOrder);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing data in file: " + resourcePath + " - Invalid number format.");
                        throw new IOException("Invalid number format in file: " + resourcePath, e);
                    }
                } else {
                    System.err.println("Error in file: " + resourcePath + " - Incorrect number of values (expected 5, got " + parts.length + ")");
                    throw new IOException("Incorrect number of values in file: " + resourcePath);
                }
            } else {
                System.err.println("Warning: Empty file: " + resourcePath);
                return null;
            }
        }
    }

    private static List<String> getResourcesInDirectory(String directory) throws IOException {
        URL url = OptFileTransformer.class.getClassLoader().getResource(directory);
        if (url == null) {
            throw new IOException("Resource directory not found: " + directory);
        }

        URI uri = null;
        try {
            uri = url.toURI();
        } catch (Exception e) {
            throw new IOException("Failed to convert URL to URI: " + url, e);
        }

        Path myPath;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem = null;
            try {
                fileSystem = FileSystems.getFileSystem(uri);
            } catch (Exception e) {
                try {
                    fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                } catch (Exception ex) {
                    throw new IOException("Failed to create new filesystem for URI: " + uri, ex);
                }
            }
            myPath = fileSystem.getPath(directory);

        } else {
            myPath = Path.of(uri);
        }


        try (Stream<Path> stream = Files.list(myPath)) {
            return stream.map(path -> directory + "/" + path.getFileName().toString())
                    .collect(Collectors.toList());
        }
    }

    private static String getFileNameWithoutExtension(String path) {
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    public record TextureOptions(int cropX, int cropY, int width, int height, int pixelOrder) {
    }
}