package org.lostcitymapeditor.Transformers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class LocFileTransformer {
    static String locDirectoryPath = "Data/Locs";

    public static Map<String, Object> parseAllLocFiles() {
        Map<String, Object> allLocsCombined = new HashMap<>();

        try {
            Path dir = Paths.get(LocFileTransformer.class.getClassLoader().getResource(locDirectoryPath).toURI());

            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                System.err.println("Directory not found or is not a directory: " + locDirectoryPath);
                return allLocsCombined;
            }

            try (Stream<Path> paths = Files.walk(dir)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().toLowerCase().endsWith(".loc"))
                        .forEach(path -> {
                            Map<String, Object> locData = parseLocFile(path);
                            locData.forEach((key, value) -> {
                                if (!allLocsCombined.containsKey(key)) {
                                    allLocsCombined.put(key, value);
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
            System.err.println("Error getting resource URI: " + e.getMessage());
        }

        return allLocsCombined;
    }


    private static Map<String, Object> parseLocFile(Path filePath) {
        Map<String, Object> locMap = new HashMap<>();
        String currentName = null;

        int[] models = null;
        int[] recol_s = null;
        int[] recol_d = null;
        int width = 1;
        int length = 1;
        boolean hillskew = false;
        boolean sharelight = false;
        boolean occlude = true;
        int anim = 0;
        int wallwidth = 16;
        byte ambient = 0;
        byte contrast = 0;
        boolean animHasAlpha = false;
        int mapfunction = 0;
        boolean mirror = false;
        boolean shadow = false;
        int resizex = 128;
        int resizey = 128;
        int resizez = 128;
        int offsetx = 0;
        int offsety = 0;
        int offsetz = 0;
        boolean forcedecor = false;
        String name = null;
        String desc = null;
        String model = null;
        boolean active = false;
        int mapscene = 0;

        Map<Integer, int[]> recolMap = new HashMap<>();
        Map<Integer, String[]> retexMap = new HashMap<>();
        String op1 = null;
        String op2 = null;
        String category = null;
        Boolean blockrange = null;
        String forceapproach = null;



        try (InputStream inputStream = Files.newInputStream(filePath);
             Scanner scanner = new Scanner(inputStream)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                Pattern namePattern = Pattern.compile("\\[(.*?)\\]");
                Matcher nameMatcher = namePattern.matcher(line);

                if (nameMatcher.find()) {
                    saveLocData(locMap, currentName, models, recol_s, recol_d, width, length, hillskew, sharelight,
                            occlude, anim, wallwidth, ambient, contrast, animHasAlpha, mapfunction, mirror, shadow,
                            resizex, resizey, resizez, offsetx, offsety, offsetz, forcedecor, name, desc, model,
                            active, mapscene, recolMap, retexMap, op1, op2, category, blockrange, forceapproach);

                    currentName = nameMatcher.group(1);
                    models = null;
                    recol_s = null;
                    recol_d = null;
                    width = 1;
                    length = 1;
                    hillskew = false;
                    sharelight = false;
                    occlude = true;
                    anim = 0;
                    wallwidth = 16;
                    ambient = 0;
                    contrast = 0;
                    animHasAlpha = false;
                    mapfunction = 0;
                    mirror = false;
                    shadow = false;
                    resizex = 128;
                    resizey = 128;
                    resizez = 128;
                    offsetx = 0;
                    offsety = 0;
                    offsetz = 0;
                    forcedecor = false;
                    name = null;
                    desc = null;
                    model = null;
                    active = false;
                    mapscene = 0;
                    recolMap.clear();
                    retexMap.clear();
                    op1 = null;
                    op2 = null;
                    category = null;
                    blockrange = null;
                    forceapproach = null;

                } else if (line.startsWith("occlude=")) {
                    occlude = !line.substring(8).trim().equalsIgnoreCase("no");
                } else if (line.startsWith("name=")) {
                    name = line.substring(5).trim();
                } else if (line.startsWith("desc=")) {
                    desc = line.substring(5).trim();
                } else if (line.startsWith("model=")) {
                    model = line.substring(6).trim();
                } else if (line.startsWith("active=")) {
                    active = line.substring(7).trim().equalsIgnoreCase("yes");
                } else if (line.startsWith("mapscene=")) {
                    try {
                        mapscene = Integer.parseInt(line.substring(9).trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid mapscene format in " + filePath + ": " + line);
                    }
                } else if (line.startsWith("width=")) {
                    try {
                        width = Integer.parseInt(line.substring(6).trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid width format in " + filePath + ": " + line);
                    }
                } else if (line.startsWith("hillskew=")) {
                    hillskew = line.substring(9).trim().equalsIgnoreCase("yes");
                } else if (line.startsWith("length=")) {
                    try {
                        length = Integer.parseInt(line.substring(7).trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid length format in " + filePath + ": " + line);
                    }
                } else if (line.startsWith("wallwidth=")) {
                    try {
                        wallwidth = Integer.parseInt(line.substring(10).trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid wallwidth format in " + filePath + ": " + line);
                    }
                } else if (line.startsWith("mirror=")) {
                    mirror = line.substring(7).trim().equalsIgnoreCase("yes");
                }else if (line.startsWith("sharelight=")) {
                    sharelight = line.substring(11).trim().equalsIgnoreCase("yes");
                } else if (line.startsWith("resizex=")) {
                    try {
                        resizex = Integer.parseInt(line.substring(8).trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid resizex format in " + filePath + ": " + line);
                    }
                } else if (line.startsWith("resizey=")) {
                    try {
                        resizey = Integer.parseInt(line.substring(8).trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid resizey format in " + filePath + ": " + line);
                    }
                } else if (line.startsWith("ambient=")) {
                    try {
                        ambient = (byte) Integer.parseInt(line.substring(8).trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid ambient format in " + filePath + ": " + line);
                    }
                } else if (line.startsWith("contrast=")) {
                    try {
                        contrast = (byte) Integer.parseInt(line.substring(9).trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid contrast format in " + filePath + ": " + line);
                    }
                } else if (line.startsWith("resizez=")) {
                    try {
                        resizez = Integer.parseInt(line.substring(8).trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid resizez format in " + filePath + ": " + line);
                    }
                } else if (line.startsWith("offsetx=")) {
                    try {
                        offsetx = Integer.parseInt(line.substring(8).trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid offsetx format in " + filePath + ": " + line);
                    }
                } else if (line.startsWith("offsety=")) {
                    try {
                        offsety = Integer.parseInt(line.substring(8).trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid offsety format in " + filePath + ": " + line);
                    }
                } else if (line.startsWith("offsetz=")) {
                    try {
                        offsetz = Integer.parseInt(line.substring(8).trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid offsetz format in " + filePath + ": " + line);
                    }
                } else if (line.startsWith("mapfunction=")) {
                    try {
                        mapfunction = Integer.parseInt(line.substring(12).trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid mapfunction format in " + filePath + ": " + line);
                    }
                } else if (line.startsWith("forcedecor=")) {
                    forcedecor = line.substring(11).trim().equalsIgnoreCase("yes");
                } else if (line.startsWith("op1=")) {
                    op1 = line.substring(4).trim();
                } else if (line.startsWith("op2=")) {
                    op2 = line.substring(4).trim();
                } else if (line.startsWith("category=")) {
                    category = line.substring(9).trim();
                } else if (line.startsWith("blockrange=")) {
                    blockrange = !line.substring(11).trim().equalsIgnoreCase("yes");
                } else if (line.startsWith("forceapproach=")) {
                    forceapproach = line.substring(14).trim();
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
                } else if (line.startsWith("retex")) {
                    Pattern retexPattern = Pattern.compile("retex(\\d+)([sd])=(.*)");
                    Matcher retexMatcher = retexPattern.matcher(line);
                    if (retexMatcher.find()) {
                        int retexIndex = Integer.parseInt(retexMatcher.group(1));
                        String retexType = retexMatcher.group(2);
                        String retexValue = retexMatcher.group(3).trim();
                        String[] retexValues = retexMap.getOrDefault(retexIndex, new String[2]);
                        if (retexType.equals("s")) {
                            retexValues[0] = retexValue;
                        } else if (retexType.equals("d")) {
                            retexValues[1] = retexValue;
                        }
                        retexMap.put(retexIndex, retexValues);
                    }
                }
            }
            saveLocData(locMap, currentName, models, recol_s, recol_d, width, length, hillskew, sharelight,
                    occlude, anim, wallwidth, ambient, contrast, animHasAlpha, mapfunction, mirror, shadow,
                    resizex, resizey, resizez, offsetx, offsety, offsetz, forcedecor, name, desc, model,
                    active, mapscene, recolMap, retexMap, op1, op2, category, blockrange, forceapproach);


        } catch (IOException e) {
            System.err.println("Error reading " + filePath + " file: " + e.getMessage());
        }

        return locMap;
    }

    private static void saveLocData(Map<String, Object> locMap, String currentName, int[] models, int[] recol_s,
                                    int[] recol_d, int width, int length, boolean hillskew, boolean sharelight,
                                    boolean occlude, int anim, int wallwidth, byte ambient, byte contrast,
                                    boolean animHasAlpha, int mapfunction, boolean mirror, boolean shadow,
                                    int resizex, int resizey, int resizez, int offsetx,
                                    int offsety, int offsetz, boolean forcedecor, String name, String desc,
                                    String model, boolean active, int mapscene, Map<Integer, int[]> recolMap,
                                    Map<Integer, String[]> retexMap, String op1, String op2, String category, Boolean blockrange, String forceapproach) {

        if (currentName != null) {
            Map<String, Object> data = new HashMap<>();
            if (models != null) data.put("models", models);
            if (recol_s != null) data.put("recol_s", recol_s);
            if (recol_d != null) data.put("recol_d", recol_d);
            data.put("width", width);
            data.put("length", length);
            data.put("hillskew", hillskew);
            data.put("sharelight", sharelight);
            data.put("occlude", occlude);
            data.put("anim", anim);
            data.put("wallwidth", wallwidth);
            data.put("ambient", ambient);
            data.put("contrast", contrast);
            data.put("animHasAlpha", animHasAlpha);
            data.put("mapfunction", mapfunction);
            data.put("mirror", mirror);
            data.put("shadow", shadow);
            data.put("resizex", resizex);
            data.put("resizey", resizey);
            data.put("resizez", resizez);
            data.put("offsetx", offsetx);
            data.put("offsety", offsety);
            data.put("offsetz", offsetz);
            data.put("forcedecor", forcedecor);
            if (name != null) data.put("name", name);
            if (desc != null) data.put("desc", desc);
            if (model != null) data.put("model", model);
            data.put("active", active);
            data.put("mapscene", mapscene);
            if (!recolMap.isEmpty()) data.put("recols", recolMap);
            if (!retexMap.isEmpty()) data.put("retexs", retexMap);
            if (op1 != null) data.put("op1", op1);
            if (op2 != null) data.put("op2", op2);
            if (category != null) data.put("category", category);
            if (blockrange != null) data.put("blockrange", blockrange);
            if (forceapproach != null) data.put("forceapproach", forceapproach);
            locMap.put(currentName, data);
        }
    }
}
