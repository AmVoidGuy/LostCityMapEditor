package org.lostcitymapeditor.Transformers;

import org.lostcitymapeditor.DataObjects.*;
import org.lostcitymapeditor.OriginalCode.World;
import org.lostcitymapeditor.Util.DataHelpers;

import java.io.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapDataTransformer {
    private static int baseX = 0;
    private static int baseY = 0;

    public static MapData parseJM2File(String filePath) {
        String currentSection = null;
        MapData currentMapData = new MapData();
        Pattern fileNamePattern = Pattern.compile("m(\\d+)_(\\d+)\\.jm2");
        Matcher fileNameMatcher = fileNamePattern.matcher(filePath);
        if (fileNameMatcher.find()) {
            baseX = Integer.parseInt(fileNameMatcher.group(1));
            baseY = Integer.parseInt(fileNameMatcher.group(2));
        }
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             Scanner scanner = new Scanner(fileInputStream)) {
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
                                                int height = Integer.parseInt(matcher.group(2));
                                                if (level == 0) {
                                                    tileData.height = height * -8;
                                                } else if (height != 1) {
                                                    tileData.height = height * -8;
                                                } else {
                                                    tileData.height = height;
                                                }
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
                                        if(tileData.height == 0 && tileData.level == 0) {
                                            int worldX = baseX * 64 + x + 932731;
                                            int worldZ = baseY * 64 + z + 556238;
                                            tileData.perlin = true;
                                            tileData.height = World.perlinNoise(worldX, worldZ) * -8;
                                        } else if(tileData.height == 0) {
                                            tileData.height = 0;
                                            tileData.perlin = true;
                                        } else if(tileData.height == 1) {
                                            tileData.height = 0;
                                        }
                                        tileData.shape = shapeTemp;
                                        currentMapData.mapTiles[level][x][z] = tileData;
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
                                        if (rotation != null)
                                            locationData.rotation = rotation;
                                        else
                                            locationData.rotation = 0;
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
                                        if (objParts.length > 2) {
                                            System.out.println("Skipping invalid OBJ line: " + line);
                                            continue;
                                        }
                                        Integer objId = DataHelpers.parseInteger(objParts[0]);
                                        if (objId == null) {
                                            System.out.println("Skipping invalid OBJ line (id missing): " + line);
                                            continue;
                                        }
                                        Integer objCount = DataHelpers.parseInteger(objParts[1]);
                                        ObjData objData = new ObjData(level, x, z, objId, objCount);
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

            for (int level = 0; level < mapData.mapTiles.length; level++) {
                if (mapData.mapTiles[level] != null) {
                    for (int x = 0; x < mapData.mapTiles[level].length; x++) {
                        if (mapData.mapTiles[level][x] != null) {
                            for (int z = 0; z < mapData.mapTiles[level][x].length; z++) {
                                TileData tile = mapData.mapTiles[level][x][z];
                                if (tile != null) {
                                    writer.write(String.format("%d %d %d:", tile.level, tile.x, tile.z));
                                    StringBuilder dataString = new StringBuilder();
                                    if (tile.height != null && tile.height != 0 && !tile.perlin) {
                                        dataString.append(" h").append(tile.height / -8);
                                    } else if(tile.height != null && tile.height == 0 && !tile.perlin) {
                                        dataString.append(" h").append(1);
                                    }
                                    if (tile.overlay != null) {
                                        dataString.append(" o").append(tile.overlay.id + 1);
                                    }

                                    if (tile.shape != null) {
                                        dataString.append(";").append(tile.shape);
                                    }
                                    if (tile.rotation != null) {
                                        dataString.append(";").append(tile.rotation);
                                    }

                                    if (tile.flag != null) {
                                        dataString.append(" f").append(tile.flag);
                                    }
                                    if (tile.underlay != null) {
                                        dataString.append(" u").append(tile.underlay.id);
                                    }

                                    writer.write(dataString.toString() + "\n");
                                }
                            }
                        }
                    }
                }
            }
            writer.write("\n==== LOC ====\n");
            if (!mapData.locations.isEmpty()) {
                for (LocData loc : mapData.locations) {
                    String rotationString = (loc.rotation != null && loc.rotation != 0) ? " " + loc.rotation : "";
                    writer.write(String.format("%d %d %d: %d %d%s\n", loc.level, loc.x, loc.z, loc.id, loc.shape, rotationString));
                }
            }

            if (mapData.npcs != null) {
                writer.write("\n==== NPC ====\n");
                for (NpcData npc : mapData.npcs) {
                    writer.write(String.format("%d %d %d: %d\n", npc.level, npc.x, npc.z, npc.id));
                }
            }

            if (mapData.objects != null) {
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
