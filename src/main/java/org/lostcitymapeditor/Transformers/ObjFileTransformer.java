package org.lostcitymapeditor.Transformers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ObjFileTransformer {

    public static Map<String, Object> parseAllObjFiles(String directoryPath) {
        Map<String, Object> allObjsCombined = new HashMap<>();

        try {
            Path dir = Paths.get(directoryPath, "scripts");

            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                System.err.println("Directory not found or is not a directory: " + directoryPath);
                return allObjsCombined;
            }

            try (Stream<Path> paths = Files.walk(dir)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().toLowerCase().endsWith(".obj"))
                        .forEach(path -> {
                            Map<String, Object> objData = null;
                            try {
                                objData = parseObjFile(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            objData.forEach((key, value) -> {
                                if (!allObjsCombined.containsKey(key)) {
                                    allObjsCombined.put(key, value);
                                }
                            });
                        });
            } catch (NullPointerException e) {
                System.err.println("NullPointerException occurred: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Error processing directory: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Error accessing directory: " + directoryPath);
            e.printStackTrace();
        }

        return allObjsCombined;
    }

    private static Map<String, Object> parseObjFile(Path filePath) throws IOException {
        Map<String, Object> objMap = new HashMap<>();
        String currentName = null;
        String model = null;
        Map<Integer, int[]> recolMap = new HashMap<>();
        String name = null;
        String desc = null;

        try (InputStream inputStream = Files.newInputStream(filePath);
             Scanner scanner = new Scanner(inputStream)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                Pattern namePattern = Pattern.compile("\\[(.*?)\\]");
                Matcher nameMatcher = namePattern.matcher(line);

                if (nameMatcher.find()) {
                    saveObjData(objMap, currentName, model, recolMap, name, desc);

                    currentName = nameMatcher.group(1);
                    model = null;
                    recolMap.clear();
                    name = null;
                    desc = null;
                } else if (line.startsWith("name=")) {
                    name = line.substring(5).trim();
                } else if (line.startsWith("desc=")) {
                    desc = line.substring(5).trim();
                } else if (line.startsWith("model=")) {
                    model = line.substring(6).trim();
                } else if (line.startsWith("recol")) {
                    Pattern recolPattern = Pattern.compile("recol(\\d+)([sd])=(\\d+)");
                    Matcher recolMatcher = recolPattern.matcher(line);
                    if (recolMatcher.find()) {
                        int recolIndex = Integer.parseInt(recolMatcher.group(1));
                        String recolType = recolMatcher.group(2);
                        int recolValue = Integer.parseInt(recolMatcher.group(3));
                        int[] recolValues = recolMap.getOrDefault(recolIndex, new int[2]);
                        if (recolType.equals("s")) {
                            recolValues[0] = recolValue;
                        } else if (recolType.equals("d")) {
                            recolValues[1] = recolValue;
                        }
                        recolMap.put(recolIndex, recolValues);
                    }
                }
            }
            saveObjData(objMap, currentName, model, recolMap, name, desc);
        }

        return objMap;
    }

    private static void saveObjData(Map<String, Object> objMap, String currentName,
                                    String model, Map<Integer, int[]> recolMap,
                                    String name, String desc) {
        if (currentName != null) {
            Map<String, Object> data = new HashMap<>();
            if (!recolMap.isEmpty()) {
                Map<Integer, int[]> recolsCopy = new HashMap<>();
                for (Map.Entry<Integer, int[]> entry : recolMap.entrySet()) {
                    recolsCopy.put(entry.getKey(), Arrays.copyOf(entry.getValue(), entry.getValue().length));
                }
                data.put("recols", recolsCopy);
            }
            if (name != null) data.put("name", name);
            if (desc != null) data.put("desc", desc);
            data.put("model", model);

            objMap.put(currentName, data);
        }
    }
}
