package org.lostcitymapeditor.Transformers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FloFileTransformer {

    public static class FloData {
        private final Map<String, Integer> underlays;
        private final Map<String, Object> overlays;

        public FloData(Map<String, Integer> underlays, Map<String, Object> overlays) {
            this.underlays = underlays;
            this.overlays = overlays;
        }

        public Map<String, Integer> getUnderlays() {
            return underlays;
        }

        public Map<String, Object> getOverlays() {
            return overlays;
        }
    }

    public static FloData parseFloData(String basePath) {
        Map<String, Integer> underlays = new HashMap<>();
        Map<String, Object> overlays = new HashMap<>();
        File baseDir = new File(basePath);

        if (baseDir.exists() && baseDir.isDirectory()) {
            findAndParseFloFiles(baseDir, underlays, overlays);
        } else {
            System.err.println("Error: Provided base path is not a valid directory: " + basePath);
        }

        return new FloData(underlays, overlays);
    }

    private static void findAndParseFloFiles(File directory, Map<String, Integer> underlayMap, Map<String, Object> overlayMap) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                findAndParseFloFiles(file, underlayMap, overlayMap);
            } else if (file.getName().toLowerCase().endsWith(".flo")) {
                parseFloFile(file.getPath(), underlayMap, overlayMap);
            }
        }
    }

    private static void parseFloFile(String floPath, Map<String, Integer> underlayMap, Map<String, Object> overlayMap) {
        try (FileInputStream fileInputStream = new FileInputStream(floPath);
             Scanner scanner = new Scanner(fileInputStream)) {

            String currentName = null;
            Integer rgb = null;
            String texture = null;
            Boolean occlude = null;
            Boolean isOverlay = null;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                Pattern namePattern = Pattern.compile("\\[(.*?)\\]");
                Matcher nameMatcher = namePattern.matcher(line);

                if (nameMatcher.find()) {
                    if (currentName != null) {
                        processFloEntry(currentName, rgb, texture, occlude, isOverlay, underlayMap, overlayMap);
                    }

                    currentName = "[" + nameMatcher.group(1) + "]";
                    rgb = null;
                    texture = null;
                    occlude = null;
                    isOverlay = null;
                } else if (line.startsWith("rgb=") || line.startsWith("colour=")) {
                    try {
                        String valuePart = line.substring(line.indexOf('=') + 1).trim();
                        String hexColor = valuePart.replace("0x", "");
                        rgb = Integer.parseInt(hexColor, 16);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid RGB/Colour format in " + floPath + ": " + line);
                    }
                } else if (line.startsWith("texture=")) {
                    texture = line.substring(8).trim();
                } else if (line.startsWith("occlude=")) {
                    String occludeValue = line.substring(8).trim().toLowerCase();
                    occlude = !occludeValue.equals("no");
                } else if (line.startsWith("overlay=")) {
                    String overlayValue = line.substring(8).trim().toLowerCase();
                    isOverlay = overlayValue.equals("yes");
                }
            }
            if (currentName != null) {
                processFloEntry(currentName, rgb, texture, occlude, isOverlay, underlayMap, overlayMap);
            }

        } catch (IOException e) {
            System.err.println("Error reading " + floPath + " file: " + e.getMessage());
        }
    }

    private static void processFloEntry(String name, Integer rgb, String texture, Boolean occlude, Boolean isOverlay,
                                        Map<String, Integer> underlayMap, Map<String, Object> overlayMap) {
        if (Boolean.TRUE.equals(isOverlay)) {
            Map<String, Object> data = new HashMap<>();
            if (rgb != null) data.put("rgb", rgb);
            if (texture != null) data.put("texture", texture);
            data.put("occlude", occlude == null || occlude);
            overlayMap.put(name, data);
        }
        else if (rgb != null) {
            underlayMap.put(name, rgb);
        }
    }
}