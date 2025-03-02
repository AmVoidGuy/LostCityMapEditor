package org.lostcitymapeditor.Transformers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FloFileTransformer {

    static String underlayFloPath = "Data/FloFiles/underlay.flo";
    static String overlayFloPath = "Data/FloFiles/overlay.flo";

    public static Map<String, Integer> parseUnderlayFlo() {
        Map<String, Integer> underlayMap = new HashMap<>();
        String currentName = null;

        try (Scanner scanner = new Scanner(new File(underlayFloPath))) {
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
                            System.err.println("RGB value found before name in underlay.flo: " + line);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid RGB format in underlay.flo: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading underlay.flo file: " + e.getMessage());
        }

        return underlayMap;
    }

    public static Map<String, Object> parseOverlayFlo() {
        Map<String, Object> overlayMap = new HashMap<>();
        String currentName = null;
        Integer rgb = null;
        String texture = null;
        boolean occlude = true;

        try (Scanner scanner = new Scanner(new File(overlayFloPath))) {
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
                        System.err.println("Invalid RGB format in overlay.flo: " + line);
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
            System.err.println("Error reading overlay.flo file: " + e.getMessage());
        }

        return overlayMap;
    }
}
