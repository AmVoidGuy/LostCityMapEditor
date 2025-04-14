package org.lostcitymapeditor.Transformers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PackFileTransformer {
    static String floPackPath = "/pack/flo.pack";
    static String texturePackPath = "/pack/texture.pack";
    static String locPackPath = "/pack/loc.pack";
    static String modelPackPath = "/pack/model.pack";

    public static Map<Integer, String> parseFloPack(String path) {
        return parsePackFile(path + floPackPath);
    }

    public static Map<Integer, String> parseTexturePack(String path) {
        return parsePackFile(path + texturePackPath);
    }

    public static Map<Integer, String> parseLocPack(String path) {
        return parsePackFile(path + locPackPath);
    }

    public static Map<String, Integer> parseModelPack(String path) {
        return parseModelPackFile(path + modelPackPath);
    }

    private static Map<String, Integer> parseModelPackFile(String packFilePath) {
        Map<String, Integer> packMap = new HashMap<>();

        try (FileInputStream fileInputStream = new FileInputStream(packFilePath);
             Scanner scanner = new Scanner(fileInputStream)) {

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

        try (FileInputStream fileInputStream = new FileInputStream(packFilePath);
             Scanner scanner = new Scanner(fileInputStream)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("=");
                if (parts.length == 2) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        if (packFilePath.contains(floPackPath)) {
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