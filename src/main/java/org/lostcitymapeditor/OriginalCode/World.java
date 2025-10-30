package org.lostcitymapeditor.OriginalCode;

import org.lostcitymapeditor.DataObjects.*;
import org.lostcitymapeditor.Loaders.FileLoader;
import org.lostcitymapeditor.Util.ColorConversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lostcitymapeditor.Renderer.OpenGLRenderer.currentLevel;
import static org.lostcitymapeditor.Renderer.OpenGLRenderer.world;

public class World {
    private final int maxTileX;
    private final int maxTileZ;
    public  int[][][] levelHeightmap = new int[4][65][65];
    private byte[][][] levelTileFlags = new byte[4][65][65];
    private final byte[][][] levelTileUnderlayIds;
    private final byte[][][] levelTileOverlayIds;
    private final byte[][][] levelTileOverlayShape;
    private final byte[][][] levelTileOverlayRotation;
    private final byte[][][] levelShademap;
    private final int[][] levelLightmap;
    private final int[] blendChroma;
    private final int[] blendSaturation;
    private final int[] blendLightness;
    private final int[] blendLuminance;
    private final int[] blendMagnitude;
    public static final int[] ROTATION_WALL_TYPE = new int[]{1, 2, 4, 8};
    public static final int[] ROTATION_WALL_CORNER_TYPE = new int[]{16, 32, 64, 128};
    public static final int[] WALL_DECORATION_ROTATION_FORWARD_X = new int[]{1, 0, -1, 0};
    public static final int[] WALL_DECORATION_ROTATION_FORWARD_Z = new int[]{0, -1, 0, 1};
    public static int randomHueOffset = (int) (Math.random() * 17.0D) - 8;
    public static int randomLightnessOffset = (int) (Math.random() * 33.0D) - 16;
    public static List<TileData> tileDataList = new ArrayList<>();
    public static LinkList locs;
    private static final String[] SUFFIXES = {
            "_1", "_2", "_3", "_4", "_5", "_q", "_w", "_e", "_r", "_t",
            "_8", "_9", "_0", "_a", "_s", "_d", "_f", "_g", "_h", "_z",
            "_x", "_c", "_v"
    };

    public static final Map<Integer, String> SHAPE_SUFFIX_MAP = Map.ofEntries(
            Map.entry(0, "_1"),
            Map.entry(1, "_2"),
            Map.entry(2, "_3"),
            Map.entry(3, "_4"),
            Map.entry(4, "_q"),
            Map.entry(5, "_w"),
            Map.entry(6, "_r"),
            Map.entry(7, "_e"),
            Map.entry(8, "_t"),
            Map.entry(9, "_5"),
            Map.entry(10, "_8"),
            Map.entry(11, "_9"),
            Map.entry(12, "_a"),
            Map.entry(13, "_s"),
            Map.entry(14, "_d"),
            Map.entry(15, "_f"),
            Map.entry(16, "_g"),
            Map.entry(17, "_h"),
            Map.entry(18, "_z"),
            Map.entry(19, "_x"),
            Map.entry(20, "_c"),
            Map.entry(21, "_v"),
            Map.entry(22, "_0")
    );

    public World(int maxTileX, int maxTileZ) {
        this.maxTileX = maxTileX;
        this.maxTileZ = maxTileZ;

        this.levelTileUnderlayIds = new byte[4][this.maxTileX][this.maxTileZ];
        this.levelTileOverlayIds = new byte[4][this.maxTileX][this.maxTileZ];
        this.levelTileOverlayShape = new byte[4][this.maxTileX][this.maxTileZ];
        this.levelTileOverlayRotation = new byte[4][this.maxTileX][this.maxTileZ];

        this.levelShademap = new byte[4][this.maxTileX + 1][this.maxTileZ + 1];
        this.levelLightmap = new int[this.maxTileX + 1][this.maxTileZ + 1];

        this.blendChroma = new int[this.maxTileZ];
        this.blendSaturation = new int[this.maxTileZ];
        this.blendLightness = new int[this.maxTileZ];
        this.blendLuminance = new int[this.maxTileZ];
        this.blendMagnitude = new int[this.maxTileZ];
        tileDataList.clear();
    }

    public void loadGround(MapData currentMapData) {
        for (int level = 0; level < 4; level++) {
            for (int x = 0; x < 64; x++) {
                for (int z = 0; z < 64; z++) {
                    TileData tile = currentMapData.mapTiles[level][x][z];
                    tileDataList.add(tile);
                    if (tile != null) {
                        this.levelTileFlags[level][x][z] = (tile.flag != null) ? (byte) (int) tile.flag : (byte) 0;
                        this.levelHeightmap[level][x][z] = (tile.height != null) ? tile.height : 0;
                        this.levelTileOverlayRotation[level][x][z] = (tile.rotation != null) ? (byte) (int) tile.rotation : (byte) 0;
                        this.levelTileOverlayShape[level][x][z] = (tile.shape != null) ? (byte) (int) tile.shape : (byte) 0;
                        this.levelTileUnderlayIds[level][x][z] = (tile.underlay != null) ? (byte) (int) tile.underlay.id : (byte) 0;
                        this.levelTileOverlayIds[level][x][z] = (tile.overlay != null) ? (byte) (int) tile.overlay.id : (byte) 0;
                    } else {
                        this.levelTileFlags[level][x][z] = (byte) 0;
                        this.levelHeightmap[level][x][z] = 0;
                        this.levelTileOverlayRotation[level][x][z] = (byte) 0;
                        this.levelTileOverlayShape[level][x][z] = (byte) 0;
                        this.levelTileUnderlayIds[level][x][z] = (byte) 0;
                        this.levelTileOverlayIds[level][x][z] = (byte) 0;
                    }
                }
            }
        }
    }

    public void loadLocations(World3D scene, MapData mapData) {
        if (mapData.locations != null) {
            for(LocData loc: mapData.locations) {
                this.addLoc(loc.level, loc.x, loc.z, scene, locs, loc.id, loc.shape, loc.rotation);
            }
        }
    }

    public void loadNpcs(World3D scene, MapData mapData) {
        if (mapData.npcs != null) {
            for(NpcData npc: mapData.npcs) {
                this.addNpc(npc.level, npc.x, npc.z, scene, npc.id);
            }
        }
    }

    public void loadObjs(World3D scene, MapData mapData) {
        if (mapData.objects != null) {
            for(ObjData obj: mapData.objects) {
                this.addObj(obj.level, obj.x, obj.z, scene, obj.id);
            }
        }
    }

    private void addObj(int level, int x, int z, World3D scene, int objId) {
        int height = this.levelHeightmap[level][x][z];
        String objName = FileLoader.getObjMap().get(objId);
        String modelName = "";
        if(objName.startsWith("cert_")) {
            modelName = "model_2429_obj";
        }
        Map<String, Object> objData = (Map<String, Object>) FileLoader.getAllObjMap().get(objName);
        if(objData != null) {
            modelName = (String) objData.getOrDefault("model", null);
        }
        Integer modelId = FileLoader.getModelMap().get(modelName);
        Model model = new Model(modelId).createCopy();
        if(objData != null) {
            Map<Integer, int[]> recols = (Map<Integer, int[]>) objData.get("recols");
            int[] recol_s = null;
            int[] recol_d = null;
            if (recols != null) {
                int maxIndex = 0;
                for (Integer index : recols.keySet()) {
                    maxIndex = Math.max(maxIndex, index);
                }
                recol_s = new int[maxIndex + 1];
                recol_d = new int[maxIndex + 1];
                for (Map.Entry<Integer, int[]> entry : recols.entrySet()) {
                    int index = entry.getKey();
                    int[] values = entry.getValue();
                    recol_s[index] = values[0];
                    recol_d[index] = values[1];
                }
            }
            if (recol_s != null) {
                for (int j = 1; j < recol_s.length; j++) {
                    int src = ColorConversion.rgb15toHsl16(recol_s[j]);
                    int des = ColorConversion.rgb15toHsl16(recol_d[j]);
                    model.recolor(src, des);
                }
            }
        }
        model.calculateNormals(64, 768, -50, -10, -50, true);
        model.baseX = x;
        model.baseZ = z;
        scene.addObj(level, x, z, height, model);
    }

    private void addNpc(int level, int x, int z, World3D scene, int npcId) {
        int height = this.levelHeightmap[level][x][z];
        String npcName = FileLoader.getNpcMap().get(npcId);
        Map<String, Object> npcData = (Map<String, Object>) FileLoader.getAllNpcMap().get(npcName);
        String[] models = (String[]) npcData.getOrDefault("models", null);
        if (models == null) {
            return;
        }
        Model[] models1 = new Model[models.length];
        int resizeh = (int) npcData.getOrDefault("resizeh", 128);
        int resizev = (int) npcData.getOrDefault("resizev", 128);
        int size = (int) npcData.getOrDefault("size", 1);
        for (int i = 0; i < models.length; i++) {
            Integer modelId = FileLoader.getModelMap().get(models[i]);
            Model currentModel = new Model(modelId).createCopy();
            models1[i] = currentModel;
        }
        Model model;
        if (models1.length > 1) {
            model = new Model(models1, models1.length);
        } else {
            model = models1[0];
        }
        Map<Integer, int[]> recols = (Map<Integer, int[]>) npcData.get("recols");
        int[] recol_s = null;
        int[] recol_d = null;
        if (recols != null) {
            int maxIndex = 0;
            for (Integer index : recols.keySet()) {
                maxIndex = Math.max(maxIndex, index);
            }
            recol_s = new int[maxIndex + 1];
            recol_d = new int[maxIndex + 1];
            for (Map.Entry<Integer, int[]> entry : recols.entrySet()) {
                int index = entry.getKey();
                int[] values = entry.getValue();
                recol_s[index] = values[0];
                recol_d[index] = values[1];
            }
        }
        if (recol_s != null) {
            for (int j = 1; j < recol_s.length; j++) {
                int src = ColorConversion.rgb15toHsl16(recol_s[j]);
                int des = ColorConversion.rgb15toHsl16(recol_d[j]);
                model.recolor(src, des);
            }
        }
        model.calculateNormals(64, 850, -30, -50, -30, true);
        if (resizeh != 128 || resizev != 128) {
            model.scale(resizeh, resizev, resizeh);
        }
        model.baseX = x;
        model.baseZ = z;
        scene.addNpc(level, x, z, height, model, size);
    }

    private void addLoc(int level, int x, int z, World3D scene, LinkList locs, int locId, int shape, int rotation) {
        int heightSW = this.levelHeightmap[level][x][z];
        int heightSE = this.levelHeightmap[level][x + 1][z];
        int heightNW = this.levelHeightmap[level][x + 1][z + 1];
        int heightNE = this.levelHeightmap[level][x][z + 1];
        int y = (heightSW + heightSE + heightNW + heightNE >> 2);
        LocType loc = LocType.get(locId);
        String target = loc.model;
        Integer modelId = FileLoader.getModelMap().get(target);


        if (modelId == null) {
            String shapeSuffix = SHAPE_SUFFIX_MAP.get(shape);

            if (shapeSuffix != null) {
                modelId = FileLoader.getModelMap().get(target + shapeSuffix);
            } else {
                System.err.println("Unexpected shape value: " + shape);
            }
        }


        if (modelId == null) {
            modelId = findModelWithSuffix(target);
        }


        if (modelId == null) {
            System.out.println("No model id " + target);
            return;
        }

        int bitset = x + (z << 7) + (locId << 14) + 0x40000000;
        if (!loc.active) {
            bitset += Integer.MIN_VALUE;
        }

        byte info = (byte) ((rotation << 6) + shape);
        Model model;

        int width;
        int offset;
        Model model1;
        if (shape == LocType.GROUNDDECOR) {
            model = loc.getModel(modelId, rotation, heightSW, heightSE, heightNW, heightNE, -1, x, z);
            scene.addGroundDecoration(model, level, x, z, y, bitset, info);
        } else if (shape == LocType.CENTREPIECE_STRAIGHT || shape == LocType.CENTREPIECE_DIAGONAL) {
            model = loc.getModel(modelId, rotation, heightSW, heightSE, heightNW, heightNE, -1, x, z);
            if (model != null) {
                int yaw = 0;
                if (shape == LocType.CENTREPIECE_DIAGONAL) {
                    yaw += 256;
                }
                int height;
                if (rotation == 1 || rotation == 3) {
                    width = loc.length;
                    height = loc.width;
                } else {
                    width = loc.width;
                    height = loc.length;
                }
                if (scene.addLoc(level, x, z, y, model, null, bitset, info, width, height, yaw) && loc.shadow) {
                    for (int dx = 0; dx <= width; dx++) {
                        for (int dz = 0; dz <= height; dz++) {
                            int shade = model.radius / 4;
                            if (shade > 30) {
                                shade = 30;
                            }

                            if (shade > this.levelShademap[level][x + dx][z + dz]) {
                                this.levelShademap[level][x + dx][z + dz] = (byte) shade;
                            }
                        }
                    }
                }
            }
        } else if (shape >= LocType.ROOF_STRAIGHT) {
            model = loc.getModel(modelId, rotation, heightSW, heightSE, heightNW, heightNE, -1, x, z);
            scene.addLoc(level, x, z, y, model, null, bitset, info, 1, 1, 0);
        } else if (shape == LocType.WALL_STRAIGHT) {
            model = loc.getModel(modelId, rotation, heightSW, heightSE, heightNW, heightNE, -1, x, z);
            scene.addWall(level, x, z, y, ROTATION_WALL_TYPE[rotation], 0, model, null, bitset, info);

            if (rotation == 0) {
                if (loc.shadow) {
                    this.levelShademap[level][x][z] = 50;
                    this.levelShademap[level][x][z + 1] = 50;
                }
            } else if (rotation == 1) {
                if (loc.shadow) {
                    this.levelShademap[level][x][z + 1] = 50;
                    this.levelShademap[level][x + 1][z + 1] = 50;
                }
            } else if (rotation == 2) {
                if (loc.shadow) {
                    this.levelShademap[level][x + 1][z] = 50;
                    this.levelShademap[level][x + 1][z + 1] = 50;
                }
            } else if (rotation == 3) {
                if (loc.shadow) {
                    this.levelShademap[level][x][z] = 50;
                    this.levelShademap[level][x + 1][z] = 50;
                }
            }
            if (loc.wallwidth != 16) {
                scene.setWallDecorationOffset(level, x, z, loc.wallwidth);
            }
        } else if (shape == LocType.WALL_DIAGONALCORNER) {
            model = loc.getModel(modelId, rotation, heightSW, heightSE, heightNW, heightNE, -1, x, z);
            scene.addWall(level, x, z, y, ROTATION_WALL_CORNER_TYPE[rotation], 0, model, null, bitset, info);
            if (loc.shadow) {
                if (rotation == 0) {
                    this.levelShademap[level][x][z + 1] = 50;
                } else if (rotation == 1) {
                    this.levelShademap[level][x + 1][z + 1] = 50;
                } else if (rotation == 2) {
                    this.levelShademap[level][x + 1][z] = 50;
                } else if (rotation == 3) {
                    this.levelShademap[level][x][z] = 50;
                }
            }
        } else if (shape == LocType.WALL_L) {
            int nextRotation = rotation + 1 & 0x3;
            Model model3 = loc.getModel(modelId, rotation + 4, heightSW, heightSE, heightNW, heightNE, -1, x, z);
            model1 = loc.getModel(modelId, nextRotation, heightSW, heightSE, heightNW, heightNE, -1, x, z);
            scene.addWall(level, x, z, y, ROTATION_WALL_TYPE[rotation], ROTATION_WALL_TYPE[nextRotation], model3, model1, bitset, info);
            if (loc.wallwidth != 16) {
                scene.setWallDecorationOffset(level, x, z, loc.wallwidth);
            }
        } else if (shape == LocType.WALL_SQUARECORNER) {
            model = loc.getModel(modelId, rotation, heightSW, heightSE, heightNW, heightNE, -1, x, z);
            scene.addWall(level, x, z, y, ROTATION_WALL_CORNER_TYPE[rotation], 0, model, null, bitset, info);

            if (loc.shadow) {
                if (rotation == 0) {
                    this.levelShademap[level][x][z + 1] = 50;
                } else if (rotation == 1) {
                    this.levelShademap[level][x + 1][z + 1] = 50;
                } else if (rotation == 2) {
                    this.levelShademap[level][x + 1][z] = 50;
                } else if (rotation == 3) {
                    this.levelShademap[level][x][z] = 50;
                }
            }
        } else if (shape == LocType.WALL_DIAGONAL) {
            model = loc.getModel(modelId, rotation, heightSW, heightSE, heightNW, heightNE, -1, x, z);
            scene.addLoc(level, x, z, y, model, null, bitset, info, 1, 1, 0);
        } else if (shape == LocType.WALLDECOR_STRAIGHT_NOOFFSET) {
            model = loc.getModel(modelId, 0, heightSW, heightSE, heightNW, heightNE, -1, x, z);
            scene.setWallDecoration(level, x, z, y, 0, 0, bitset, model, info, rotation * 512, ROTATION_WALL_TYPE[rotation]);
        } else if (shape == LocType.WALLDECOR_STRAIGHT_OFFSET) {
            offset = 16;
            width = scene.getWallBitset(level, x, z);
            if (width > 0) {
                offset = LocType.get(width >> 14 & 0x7FFF).wallwidth;
            }
            model1 = loc.getModel(modelId, 0, heightSW, heightSE, heightNW, heightNE, -1, x, z);
            scene.setWallDecoration(level, x, z, y, WALL_DECORATION_ROTATION_FORWARD_X[rotation] * offset, WALL_DECORATION_ROTATION_FORWARD_Z[rotation] * offset, bitset, model1, info, rotation * 512, ROTATION_WALL_TYPE[rotation]);
        } else if (shape == LocType.WALLDECOR_DIAGONAL_OFFSET) {
            model = loc.getModel(modelId, 0, heightSW, heightSE, heightNW, heightNE, -1, x, z);
            scene.setWallDecoration(level, x, z, y, 0, 0, bitset, model, info, rotation, 256);
        } else if (shape == LocType.WALLDECOR_DIAGONAL_NOOFFSET) {
            model = loc.getModel(modelId, 0, heightSW, heightSE, heightNW, heightNE, -1, x, z);
            scene.setWallDecoration(level, x, z, y, 0, 0, bitset, model, info, rotation, 512);
        } else if (shape == LocType.WALLDECOR_DIAGONAL_BOTH) {
            model = loc.getModel(modelId, 0, heightSW, heightSE, heightNW, heightNE, -1, x, z);
            scene.setWallDecoration(level, x, z, y, 0, 0, bitset, model, info, rotation, 768);
        }
    }

    public static int perlinNoise(int x, int z) {
        int value = perlinScale(x + 45365, z + 91923, 4) + (perlinScale(x + 10294, z + 37821, 2) - 128 >> 1) + (perlinScale(x, z, 1) - 128 >> 2) - 128;
        value = (int) ((double) value * 0.3D) + 35;
        if (value < 10) {
            value = 10;
        } else if (value > 60) {
            value = 60;
        }
        return value;
    }

    private static int perlinScale(int x, int z, int scale) {
        int intX = x / scale;
        int fracX = x & scale - 1;
        int intZ = z / scale;
        int fracZ = z & scale - 1;
        int v1 = smoothNoise(intX, intZ);
        int v2 = smoothNoise(intX + 1, intZ);
        int v3 = smoothNoise(intX, intZ + 1);
        int v4 = smoothNoise(intX + 1, intZ + 1);
        int i1 = interpolate(v1, v2, fracX, scale);
        int i2 = interpolate(v3, v4, fracX, scale);
        return interpolate(i1, i2, fracZ, scale);
    }

    private static int interpolate(int a, int b, int x, int scale) {
        int f = 65536 - Pix3D.cosTable[x * 1024 / scale] >> 1;
        return (a * (65536 - f) >> 16) + (b * f >> 16);
    }

    private static int smoothNoise(int x, int y) {
        int corners = noise(x - 1, y - 1) + noise(x + 1, y - 1) + noise(x - 1, y + 1) + noise(x + 1, y + 1);
        int sides = noise(x - 1, y) + noise(x + 1, y) + noise(x, y - 1) + noise(x, y + 1);
        int center = noise(x, y);
        return (corners / 16) + (sides / 8) + (center / 4);
    }

    private static int noise(int x, int y) {
        int n = x + y * 57;
        int n1 = (n << 13) ^ n;
        int result = n1 * (n1 * n1 * 15731 + 789221) + 1376312589 & Integer.MAX_VALUE;
        return result >> 19 & 0xFF;
    }

    public static int mulHSL(int hsl, int lightness) {
        if (hsl == -1) {
            return 12345678;
        }

        lightness = lightness * (hsl & 0x7F) / 128;
        if (lightness < 2) {
            lightness = 2;
        } else if (lightness > 126) {
            lightness = 126;
        }

        return (hsl & 0xFF80) + lightness;
    }

    private int adjustLightness(int hsl, int scalar) {
        if (hsl == -2) {
            return 12345678;
        }

        if (hsl == -1) {
            if (scalar < 0) {
                scalar = 0;
            } else if (scalar > 127) {
                scalar = 127;
            }
            return 127 - scalar;
        } else {
            scalar = scalar * (hsl & 0x7F) / 128;
            if (scalar < 2) {
                scalar = 2;
            } else if (scalar > 126) {
                scalar = 126;
            }
            return (hsl & 0xFF80) + scalar;
        }
    }

    private int hsl24to16(int hue, int saturation, int lightness) {
        if (lightness > 179) {
            saturation /= 2;
        }

        if (lightness > 192) {
            saturation /= 2;
        }

        if (lightness > 217) {
            saturation /= 2;
        }

        if (lightness > 243) {
            saturation /= 2;
        }

        return (hue / 4 << 10) + (saturation / 32 << 7) + lightness / 2;
    }

    public void build(World3D scene) {
        randomHueOffset += (int) (Math.random() * 5.0D) - 2;
        if (randomHueOffset < -8) {
            randomHueOffset = -8;
        } else if (randomHueOffset > 8) {
            randomHueOffset = 8;
        }

        randomLightnessOffset += (int) (Math.random() * 5.0D) - 2;
        if (randomLightnessOffset < -16) {
            randomLightnessOffset = -16;
        } else if (randomLightnessOffset > 16) {
            randomLightnessOffset = 16;
        }

        for (int level = 0; level < 4; level++) {
            byte[][] shademap = this.levelShademap[level];
            byte lightAmbient = 96;
            short lightAttenuation = 768;
            byte lightX = -50;
            byte lightY = -10;
            byte lightZ = -50;
            int lightMag = (int) Math.sqrt(lightX * lightX + lightY * lightY + lightZ * lightZ);
            int lightMagnitude = lightAttenuation * lightMag >> 8;

            for (int z = 1; z < this.maxTileZ; z++) {
                for (int x = 1; x < this.maxTileX; x++) {
                    int dx = this.levelHeightmap[level][x + 1][z] - this.levelHeightmap[level][x - 1][z];
                    int dz = this.levelHeightmap[level][x][z + 1] - this.levelHeightmap[level][x][z - 1];
                    int len = (int) Math.sqrt(dx * dx + dz * dz + 65536);
                    int normalX = (dx << 8) / len;
                    int normalY = 65536 / len;
                    int normalZ = (dz << 8) / len;
                    int light = lightAmbient + (lightX * normalX + lightY * normalY + lightZ * normalZ) / lightMagnitude;
                    int shade = (shademap[x - 1][z] >> 2) + (shademap[x + 1][z] >> 3) + (shademap[x][z - 1] >> 2) + (shademap[x][z + 1] >> 3) + (shademap[x][z] >> 1);
                    this.levelLightmap[x][z] = light - shade;
                }
            }

            for (int z = 0; z < this.maxTileZ; z++) {
                this.blendChroma[z] = 0;
                this.blendSaturation[z] = 0;
                this.blendLightness[z] = 0;
                this.blendLuminance[z] = 0;
                this.blendMagnitude[z] = 0;
            }

            for (int x0 = -5; x0 < this.maxTileX + 5; x0++) {
                for (int z0 = 0; z0 < this.maxTileZ; z0++) {
                    int x1 = x0 + 5;
                    int debugMag;

                    if (x1 >= 0 && x1 < this.maxTileX) {
                        int underlayId = this.levelTileUnderlayIds[level][x1][z0] & 0xFF;

                        if (underlayId > 0) {
                            FloType flu = FloType.getInstances()[underlayId - 1];
                            this.blendChroma[z0] += flu.chroma;
                            this.blendSaturation[z0] += flu.saturation;
                            this.blendLightness[z0] += flu.lightness;
                            this.blendLuminance[z0] += flu.luminance;
                            debugMag = this.blendMagnitude[z0]++;
                        }
                    }

                    int x2 = x0 - 5;
                    if (x2 >= 0 && x2 < this.maxTileX) {
                        int underlayId = this.levelTileUnderlayIds[level][x2][z0] & 0xFF;

                        if (underlayId > 0) {
                            FloType flu = FloType.getInstances()[underlayId - 1];
                            this.blendChroma[z0] -= flu.chroma;
                            this.blendSaturation[z0] -= flu.saturation;
                            this.blendLightness[z0] -= flu.lightness;
                            this.blendLuminance[z0] -= flu.luminance;
                            debugMag = this.blendMagnitude[z0]--;
                        }
                    }
                }

                if (x0 >= 0 && x0 < this.maxTileX) {
                    int hueAccumulator = 0;
                    int saturationAccumulator = 0;
                    int lightnessAccumulator = 0;
                    int luminanceAccumulator = 0;
                    int magnitudeAccumulator = 0;

                    for (int z0 = -5; z0 < this.maxTileZ + 5; z0++) {
                        int dz1 = z0 + 5;
                        if (dz1 >= 0 && dz1 < this.maxTileZ) {
                            hueAccumulator += this.blendChroma[dz1];
                            saturationAccumulator += this.blendSaturation[dz1];
                            lightnessAccumulator += this.blendLightness[dz1];
                            luminanceAccumulator += this.blendLuminance[dz1];
                            magnitudeAccumulator += this.blendMagnitude[dz1];
                        }

                        int dz2 = z0 - 5;
                        if (dz2 >= 0 && dz2 < this.maxTileZ) {
                            hueAccumulator -= this.blendChroma[dz2];
                            saturationAccumulator -= this.blendSaturation[dz2];
                            lightnessAccumulator -= this.blendLightness[dz2];
                            luminanceAccumulator -= this.blendLuminance[dz2];
                            magnitudeAccumulator -= this.blendMagnitude[dz2];
                        }

                        if (z0 >= 0 && z0 < this.maxTileZ && (this.levelTileFlags[level][x0][z0] & 0x10) == 0) {
                            int underlayId = this.levelTileUnderlayIds[level][x0][z0] & 0xFF;
                            int overlayId = this.levelTileOverlayIds[level][x0][z0] & 0xFF;

                            if (underlayId > 0 || overlayId > 0) {
                                int heightSW = this.levelHeightmap[level][x0][z0];
                                int heightSE = this.levelHeightmap[level][x0 + 1][z0];
                                int heightNE = this.levelHeightmap[level][x0 + 1][z0 + 1];
                                int heightNW = this.levelHeightmap[level][x0][z0 + 1];

                                int lightSW = this.levelLightmap[x0][z0];
                                int lightSE = this.levelLightmap[x0 + 1][z0];
                                int lightNE = this.levelLightmap[x0 + 1][z0 + 1];
                                int lightNW = this.levelLightmap[x0][z0 + 1];

                                int baseColor = -1;
                                int tintColor = -1;

                                if (underlayId > 0) {
                                    int hue = hueAccumulator * 256 / luminanceAccumulator;
                                    int saturation = saturationAccumulator / magnitudeAccumulator;
                                    int lightness = lightnessAccumulator / magnitudeAccumulator;
                                    baseColor = this.hsl24to16(hue, saturation, lightness);
                                    int randomHue = hue + randomHueOffset & 0xFF;
                                    lightness += randomLightnessOffset;
                                    if (lightness < 0) {
                                        lightness = 0;
                                    } else if (lightness > 255) {
                                        lightness = 255;
                                    }
                                    tintColor = this.hsl24to16(randomHue, saturation, lightness);
                                }

                                int shadeColor = 0;
                                if (baseColor != -1) {
                                    shadeColor = Pix3D.colourTable[mulHSL(tintColor, 96)];
                                }
                                if (overlayId == 0) {
                                    scene.setTile(level, x0, z0, 0, 0, -1, heightSW, heightSE, heightNE, heightNW, mulHSL(baseColor, lightSW), mulHSL(baseColor, lightSE), mulHSL(baseColor, lightNE), mulHSL(baseColor, lightNW), 0, 0, 0, 0, shadeColor, 0);
                                } else {
                                    int shape = this.levelTileOverlayShape[level][x0][z0] + 1;
                                    byte rotation = this.levelTileOverlayRotation[level][x0][z0];
                                    FloType flo = FloType.getInstances()[overlayId];
                                    int textureId = flo.texture;
                                    int hsl;
                                    int rgb;
                                    if (textureId >= 0) {
                                        rgb = Pix3D.getAverageTextureRGB(textureId);
                                        hsl = -1;
                                    } else if (flo.rgb == 16711935) {
                                        rgb = 0;
                                        hsl = -2;
                                        textureId = -1;
                                    } else {
                                        hsl = this.hsl24to16(flo.hue, flo.saturation, flo.lightness);
                                        rgb = Pix3D.colourTable[this.adjustLightness(flo.hsl, 96)];
                                    }
                                    scene.setTile(level, x0, z0, shape, rotation, textureId, heightSW, heightSE, heightNE, heightNW, mulHSL(baseColor, lightSW), mulHSL(baseColor, lightSE), mulHSL(baseColor, lightNE), mulHSL(baseColor, lightNW), this.adjustLightness(hsl, lightSW), this.adjustLightness(hsl, lightSE), this.adjustLightness(hsl, lightNE), this.adjustLightness(hsl, lightNW), shadeColor, rgb);
                                }
                            }
                        }
                    }
                }
            }
        }
        scene.buildModels(64, 768, -50, -10, -50);
        for (int x = 0; x < this.maxTileX; x++) {
            for (int z = 0; z < this.maxTileZ; z++) {
                if ((this.levelTileFlags[1][x][z] & 0x2) == 2) {
                    scene.setBridge(x, z);
                }
            }
        }
    }

    public int getHeightmapY(int level, int tileX, int tileZ) {
        int realLevel = level;
        if (level < 3 && (this.levelTileFlags[1][tileX][tileZ] & 0x2) == 2) {
            //Bridge logic
            //realLevel = level + 1;
        }
        int y00 = this.levelHeightmap[realLevel][tileX][tileZ] * (128 - tileX) + this.levelHeightmap[realLevel][tileX + 1][tileZ] * tileX >> 7;
        int y11 = this.levelHeightmap[realLevel][tileX][tileZ + 1] * (128 - tileX) + this.levelHeightmap[realLevel][tileX + 1][tileZ + 1] * tileX >> 7;
        return y00 * (128 - tileZ) + y11 * tileZ >> 7;
    }

    public static Integer findModelWithSuffix(String target) {
        for (String suffix : SUFFIXES) {
            Integer modelId = FileLoader.getModelMap().get(target + suffix);
            if (modelId != null) {
                return modelId;
            }
        }
        return null;
    }

}
