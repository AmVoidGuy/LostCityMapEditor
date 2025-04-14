package org.lostcitymapeditor.Transformers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FloFileTransformer {

    static String underlayFloPath = "/scripts/floors/underlay.flo";
    static String overlayFloPath = "/scripts/floors/overlay.flo";

    public static Map<String, Integer> parseUnderlayFlo(String path) {
        return parseUnderlayFloFile(path + underlayFloPath);
    }

    public static Map<String, Object> parseOverlayFlo(String path) {
        return parseOverlayFloFile(path + overlayFloPath);
    }

    private static Map<String, Integer> parseUnderlayFloFile(String floPath) {
        Map<String, Integer> underlayMap = new HashMap<>();
        String currentName = null;

        try (FileInputStream fileInputStream = new FileInputStream(floPath);
             Scanner scanner = new Scanner(fileInputStream)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                Pattern namePattern = Pattern.compile("\\[(.*?)\\]");
                Matcher nameMatcher = namePattern.matcher(line);

                if (nameMatcher.find()) {
                    currentName = nameMatcher.group(1);
                } else if (line.startsWith("rgb=")) {
                    try {
                        String hexColor = line.substring(4).trim().replace("0x", "");
                        int rgb = Integer.parseInt(hexColor, 16);
                        if (currentName != null) {
                            underlayMap.put("[" + currentName + "]", rgb);
                        } else {
                            System.err.println("RGB value found before name in " + floPath + ": " + line);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid RGB format in " + floPath + ": " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + floPath + " file: " + e.getMessage());
        }

        return underlayMap;
    }

    private static Map<String, Object> parseOverlayFloFile(String floPath) {
        Map<String, Object> overlayMap = new HashMap<>();
        String currentName = null;
        Integer rgb = null;
        String texture = null;
        boolean occlude = true;

        try (FileInputStream fileInputStream = new FileInputStream(floPath);
             Scanner scanner = new Scanner(fileInputStream)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                Pattern namePattern = Pattern.compile("\\[(.*?)\\]");
                Matcher nameMatcher = namePattern.matcher(line);

                if (nameMatcher.find()) {
                    if (currentName != null) {
                        Map<String, Object> data = new HashMap<>();
                        if (rgb != null) data.put("rgb", rgb);
                        if (texture != null) data.put("texture", texture);
                        data.put("occlude", occlude);
                        overlayMap.put(currentName, data);
                    }
                    currentName = "[" + nameMatcher.group(1) + "]";
                    rgb = null;
                    texture = null;
                    occlude = true;
                } else if (line.startsWith("rgb=")) {
                    try {
                        String hexColor = line.substring(4).trim().replace("0x", "");
                        rgb = Integer.parseInt(hexColor, 16);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid RGB format in " + floPath + ": " + line);
                    }
                } else if (line.startsWith("texture=")) {
                    texture = line.substring(8).trim();
                } else if (line.startsWith("occlude=")) {
                    String occludeValue = line.substring(8).trim().toLowerCase();
                    occlude = !occludeValue.equals("no");
                }
            }

            if (currentName != null) {
                Map<String, Object> data = new HashMap<>();
                if (rgb != null) data.put("rgb", rgb);
                if (texture != null) data.put("texture", texture);
                data.put("occlude", occlude);
                overlayMap.put(currentName, data);
            }

        } catch (IOException e) {
            System.err.println("Error reading " + floPath + " file: " + e.getMessage());
        }

        return overlayMap;
    }


}