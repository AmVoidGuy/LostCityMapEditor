package org.lostcitymapeditor.Transformers;

import java.io.*;
import java.util.*;

public class OptFileTransformer {

    public static Map<String, TextureOptions> loadTextureOptions(String directoryPath) {
        Map<String, TextureOptions> textureOptions = new HashMap<>();
        File directory = new File(directoryPath + "/textures/meta/");

        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Directory does not exist or is not a directory: " + directoryPath);
            return textureOptions;
        }

        try {
            File[] files = directory.listFiles();
            if (files == null || files.length == 0) {
                System.err.println("No files found in directory: " + directoryPath);
                return textureOptions;
            }

            Arrays.stream(files)
                    .filter(file -> file.isFile() && file.getName().toLowerCase().endsWith(".opt"))
                    .forEach(file -> {
                        String name = getFileNameWithoutExtension(file.getName());
                        try {
                            TextureOptions options = parseTextureOptions(file.getAbsolutePath());
                            if (options != null) {
                                textureOptions.put(name, options);
                            }
                        } catch (IOException e) {
                            System.err.println("Error processing texture options file: " + file.getAbsolutePath());
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            System.err.println("Error accessing texture options directory: " + directoryPath);
            e.printStackTrace();
        }

        return textureOptions;
    }

    private static TextureOptions parseTextureOptions(String filePath) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream))) {

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
                        System.err.println("Error parsing data in file: " + filePath + " - Invalid number format.");
                        throw new IOException("Invalid number format in file: " + filePath, e);
                    }
                } else {
                    System.err.println("Error in file: " + filePath + " - Incorrect number of values (expected 5, got " + parts.length + ")");
                    throw new IOException("Incorrect number of values in file: " + filePath);
                }
            } else {
                System.err.println("Warning: Empty file: " + filePath);
                return null;
            }
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