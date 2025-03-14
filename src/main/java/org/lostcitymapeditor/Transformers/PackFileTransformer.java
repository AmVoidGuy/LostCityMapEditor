package org.lostcitymapeditor.Transformers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PackFileTransformer {
    static String floPackPath = "Data/PackFiles/flo.pack";
    static String texturePackPath = "Data/PackFiles/texture.pack";
    static String locPackPath = "Data/PackFiles/loc.pack";
    static String modelPackPath = "Data/PackFiles/model.pack";

    public static Map<Integer, String> parseFloPack() {
        return parsePackFile(floPackPath);
    }

    public static Map<Integer, String> parseTexturePack() {
        return parsePackFile(texturePackPath);
    }

    public static Map<Integer, String> parseLocPack() {
        return parsePackFile(locPackPath);
    }

    public static Map<String, Integer> parseModelPack() {
        return parseModelPackFile(modelPackPath);
    }

    private static Map<String, Integer> parseModelPackFile(String packFilePath) {
        Map<String, Integer> packMap = new HashMap<>();

        try (InputStream inputStream = PackFileTransformer.class.getClassLoader().getResourceAsStream(packFilePath);
             Scanner scanner = new Scanner(inputStream)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("=");
                if (parts.length == 2) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        packMap.put(name, id);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid pack file line: " + line);
                    }
                } else {
                    System.err.println("Skipping invalid pack file line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading pack file: " + e.getMessage());
        }

        return packMap;
    }

    private static Map<Integer, String> parsePackFile(String packFilePath) {
        Map<Integer, String> packMap = new HashMap<>();

        try (InputStream inputStream = PackFileTransformer.class.getClassLoader().getResourceAsStream(packFilePath);
             Scanner scanner = new Scanner(inputStream)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("=");
                if (parts.length == 2) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        if (packFilePath.equals(floPackPath)) {
                            packMap.put(id, "[" + name + "]");
                        } else {
                            packMap.put(id, name);
                        }

                    } catch (NumberFormatException e) {
                        System.err.println("Invalid pack file line: " + line);
                    }
                } else {
                    System.err.println("Skipping invalid pack file line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading pack file: " + e.getMessage());
        }

        return packMap;
    }
}