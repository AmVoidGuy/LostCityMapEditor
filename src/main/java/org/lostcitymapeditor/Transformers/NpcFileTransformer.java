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

public class NpcFileTransformer {

    public static Map<String, Object> parseAllNpcFiles(String directoryPath) {
        Map<String, Object> allNpcsCombined = new HashMap<>();

        try {
            Path dir = Paths.get(directoryPath, "scripts");

            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                System.err.println("Directory not found or is not a directory: " + directoryPath);
                return allNpcsCombined;
            }

            try (Stream<Path> paths = Files.walk(dir)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().toLowerCase().endsWith(".npc"))
                        .forEach(path -> {
                            Map<String, Object> npcData = null;
                            try {
                                npcData = parseNpcFile(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            npcData.forEach((key, value) -> {
                                if (!allNpcsCombined.containsKey(key)) {
                                    allNpcsCombined.put(key, value);
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

        return allNpcsCombined;
    }

    private static Map<String, Object> parseNpcFile(Path filePath) throws IOException {
        Map<String, Object> npcMap = new HashMap<>();
        String currentName = null;
        Map<Integer, String> modelMap = new HashMap<>();
        Map<Integer, int[]> recolMap = new HashMap<>();
        String name = null;
        String desc = null;
        int resizeh = 128;
        int resizev = 128;
        int size = 1;

        try (InputStream inputStream = Files.newInputStream(filePath);
             Scanner scanner = new Scanner(inputStream)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                Pattern namePattern = Pattern.compile("\\[(.*?)\\]");
                Matcher nameMatcher = namePattern.matcher(line);

                if (nameMatcher.find()) {
                    saveNpcData(npcMap, currentName, modelMap, recolMap, name, desc, resizeh, resizev, size);

                    currentName = nameMatcher.group(1);
                    modelMap.clear();
                    recolMap.clear();
                    name = null;
                    desc = null;
                    size = 1;
                    resizeh = 128;
                    resizev = 128;
                } else if (line.startsWith("name=")) {
                    name = line.substring(5).trim();
                } else if (line.startsWith("size=")) {
                    size = Integer.parseInt(line.substring(5).trim());
                } else if (line.startsWith("desc=")) {
                    desc = line.substring(5).trim();
                } else if (line.startsWith("resizeh=")) {
                    resizeh = Integer.parseInt(line.substring(8).trim());
                } else if (line.startsWith("resizev=")) {
                    resizev = Integer.parseInt(line.substring(8).trim());
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
                } else if (line.startsWith("model")) {
                    Pattern modelPattern = Pattern.compile("model(\\d+)=(.+)");
                    Matcher modelMatcher = modelPattern.matcher(line);
                    if (modelMatcher.find()) {
                        int modelIndex = Integer.parseInt(modelMatcher.group(1));
                        String modelValue = modelMatcher.group(2).trim();
                        modelMap.put(modelIndex, modelValue);
                    }
                }
            }
            saveNpcData(npcMap, currentName, modelMap, recolMap, name, desc, resizeh, resizev, size);
        }

        return npcMap;
    }

    private static void saveNpcData(Map<String, Object> npcMap, String currentName,
                                    Map<Integer, String> modelMap, Map<Integer, int[]> recolMap,
                                    String name, String desc, int resizeh, int resizev, int size) {
        if (currentName != null) {
            Map<String, Object> data = new HashMap<>();
            if (!modelMap.isEmpty()) {
                int maxModelIndex = Collections.max(modelMap.keySet());
                String[] models = new String[maxModelIndex];
                for (int i = 1; i <= maxModelIndex; i++) {
                    if (modelMap.containsKey(i)) {
                        models[i-1] = modelMap.get(i);
                    }
                }
                List<String> modelList = new ArrayList<>();
                for (String model : models) {
                    if (model != null) {
                        modelList.add(model);
                    }
                }
                data.put("models", modelList.toArray(new String[0]));
            }
            if (!recolMap.isEmpty()) {
                Map<Integer, int[]> recolsCopy = new HashMap<>();
                for (Map.Entry<Integer, int[]> entry : recolMap.entrySet()) {
                    recolsCopy.put(entry.getKey(), Arrays.copyOf(entry.getValue(), entry.getValue().length));
                }
                data.put("recols", recolsCopy);
            }
            if (name != null) data.put("name", name);
            if (desc != null) data.put("desc", desc);
            data.put("resizeh", resizeh);
            data.put("resizev", resizev);
            data.put("size", size);

            npcMap.put(currentName, data);
        }
    }
}
