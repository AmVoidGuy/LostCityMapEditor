package org.lostcitymapeditor.Transformers;

import org.lostcitymapeditor.DataObjects.*;
import org.lostcitymapeditor.Util.DataHelpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapDataTransformer {
    public static MapData parseJM2File(String filePath) {
        String currentSection = null;
        MapData currentMapData = new MapData();
        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                switch (line) {
                    case "==== MAP ====":
                        currentSection = "MAP";
                        break;
                    case "==== LOC ====":
                        currentSection = "LOC";
                        break;
                    case "==== NPC ====":
                        currentSection = "NPC";
                        break;
                    case "==== OBJ ====":
                        currentSection = "OBJ";
                        break;
                    default:
                        if (line.startsWith("===")) {
                            currentSection = null;
                        } else if (!line.isEmpty() && !line.startsWith("//") && currentSection != null) {
                            try {
                                String[] parts = line.split(":");
                                if (parts.length != 2) {
                                    System.out.println("Skipping invalid line format: " + line);
                                    continue;
                                }

                                String[] coordinates = parts[0].trim().split(" ");
                                if (coordinates.length != 3) {
                                    System.out.println("Skipping invalid coordinate format: " + line);
                                    continue;
                                }

                                int level = Integer.parseInt(coordinates[0]);
                                int x = Integer.parseInt(coordinates[1]);
                                int z = Integer.parseInt(coordinates[2]);

                                String dataString = parts[1].trim();

                                switch (currentSection) {
                                    case "MAP":
                                        TileData tileData = new TileData(level, x, z);
                                        Integer shapeTemp = null;
                                        Pattern pattern = Pattern.compile("(h(\\d+))|(o(\\d+))|(r(\\d+))|(f(\\d+))|(u(\\d+))|(\\d+)");
                                        Matcher matcher = pattern.matcher(dataString);

                                        while (matcher.find()) {
                                            if (matcher.group(2) != null) {
                                                tileData.height = Integer.parseInt(matcher.group(2));
                                                Arrays.fill(tileData.cornerHeights, tileData.height);
                                            } else if (matcher.group(4) != null) {
                                                Integer overlayID = Integer.parseInt(matcher.group(4));
                                                tileData.overlay = new OverlayData(overlayID - 1);
                                            } else if (matcher.group(6) != null) {
                                                tileData.rotation = Integer.parseInt(matcher.group(6));
                                            } else if (matcher.group(8) != null) {
                                                tileData.flag = Integer.parseInt(matcher.group(8));
                                            } else if (matcher.group(10) != null) {
                                                Integer underLayID = Integer.parseInt(matcher.group(10));
                                                tileData.underlay = new UnderlayData(underLayID);
                                            } else if (matcher.group(11) != null) {
                                                if (shapeTemp == null) {
                                                    shapeTemp = Integer.parseInt(matcher.group(11));
                                                } else {
                                                    tileData.rotation = Integer.parseInt(matcher.group(11));
                                                }
                                            }
                                        }
                                        tileData.shape = shapeTemp;
                                        currentMapData.mapTiles.add(tileData);
                                        break;
                                    case "LOC":
                                        String[] locParts = dataString.split(" ");
                                        if (locParts.length < 2) {
                                            System.out.println("Skipping invalid LOC line: " + line);
                                            continue;
                                        }
                                        Integer id = DataHelpers.parseInteger(locParts[0]);
                                        Integer shape = DataHelpers.parseInteger(locParts[1]);
                                        Integer rotation = (locParts.length > 2) ? DataHelpers.parseInteger(locParts[2]) : null;

                                        if (id == null || shape == null) {
                                            System.out.println("Skipping invalid LOC line (id or shape missing): " + line);
                                            continue;
                                        }

                                        LocData locationData = new LocData(level, x, z, id, shape);
                                        locationData.rotation = rotation;
                                        currentMapData.locations.add(locationData);
                                        break;
                                    case "NPC":
                                        String[] npcParts = dataString.split(" ");
                                        if (npcParts.length != 1) {
                                            System.out.println("Skipping invalid NPC line: " + line);
                                            continue;
                                        }
                                        Integer npcId = DataHelpers.parseInteger(npcParts[0]);
                                        if (npcId == null) {
                                            System.out.println("Skipping invalid NPC line (id missing): " + line);
                                            continue;
                                        }
                                        NpcData npcData = new NpcData(level, x, z, npcId);
                                        currentMapData.npcs.add(npcData);
                                        break;
                                    case "OBJ":
                                        String[] objParts = dataString.split(" ");
                                        if (objParts.length != 2) {
                                            System.out.println("Skipping invalid OBJ line: " + line);
                                            continue;
                                        }
                                        Integer objId = DataHelpers.parseInteger(objParts[0]);
                                        Integer count = DataHelpers.parseInteger(objParts[1]);
                                        if (objId == null || count == null) {
                                            System.out.println("Skipping invalid OBJ line (id or count missing): " + line);
                                            continue;
                                        }
                                        ObjData objData = new ObjData(level, x, z, objId, count);
                                        currentMapData.objects.add(objData);
                                        break;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Error parsing number in line: " + line + " - " + e.getMessage());
                            } catch (Exception e) {
                                System.out.println("Error parsing line: " + line + " - " + e.getMessage());
                            }
                        }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return currentMapData;
    }

    public static void writeJM2File(MapData mapData, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("==== MAP ====\n");
            for (TileData tile : mapData.mapTiles) {
                writer.write(String.format("%d %d %d:", tile.level, tile.x, tile.z));

                String dataString = "";
                if (tile.height != null && tile.height != 0) {
                    dataString += " h" + tile.height;
                }
                if (tile.overlay != null) {
                    dataString += " o" + (tile.overlay.id + 1);
                }
                if (tile.height != null && tile.height != 0) {
                    dataString += " h" + tile.height;
                }
                if (tile.overlay != null) {
                    dataString += " o" + (tile.overlay.id + 1);
                }
                if (tile.shape != null || tile.rotation != null) {
                    dataString += ";";

                    if (tile.shape != null) {
                        dataString += tile.shape;
                    } else {
                        dataString += "0";
                    }
                    if (tile.rotation != null) {
                        dataString += ";";
                        dataString += tile.rotation;
                    }
                }
                if (tile.flag != null) {
                    dataString += " f" + tile.flag;
                }
                if (tile.underlay != null) {
                    dataString += " u" + tile.underlay.id;
                }

                writer.write(dataString + "\n");
            }

            writer.write("\n==== LOC ====\n");
            if (!mapData.locations.isEmpty()) {
                for (LocData loc : mapData.locations) {
                    String rotationString = (loc.rotation != null) ? " " + loc.rotation : "";
                    writer.write(String.format("%d %d %d: %d %d%s\n", loc.level, loc.x, loc.z, loc.id, loc.shape, rotationString));
                }
            }

            if (!mapData.npcs.isEmpty()) {
                writer.write("\n==== NPC ====\n");
                for (NpcData npc : mapData.npcs) {
                    writer.write(String.format("%d %d %d: %d\n", npc.level, npc.x, npc.z, npc.id));
                }
            }

            if (!mapData.objects.isEmpty()) {
                writer.write("\n==== OBJ ====\n");
                for (ObjData obj : mapData.objects) {
                    writer.write(String.format("%d %d %d: %d %d\n", obj.level, obj.x, obj.z, obj.id, obj.count));
                }
            }

        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
