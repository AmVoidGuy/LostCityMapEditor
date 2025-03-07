package org.lostcitymapeditor.OriginalCode;

import org.lostcitymapeditor.DataObjects.MapData;
import org.lostcitymapeditor.DataObjects.TileData;

import java.util.ArrayList;
import java.util.List;

public class World {
    public static boolean lowMemory = true;
    public static int levelBuilt;
    public static boolean fullbright;
    private final int maxTileX;
    private final int maxTileZ;
    public int[][][] levelHeightmap = new int[4][65][65];
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
    private final int[][][] levelOccludemap;
    public static final int[] ROTATION_WALL_TYPE = new int[]{1, 2, 4, 8};
    public static final int[] ROTATION_WALL_CORNER_TYPE = new int[]{16, 32, 64, 128};
    public static final int[] WALL_DECORATION_ROTATION_FORWARD_X = new int[]{1, 0, -1, 0};
    public static final int[] WALL_DECORATION_ROTATION_FORWARD_Z = new int[]{0, -1, 0, 1};
    public static int randomHueOffset = (int) (Math.random() * 17.0D) - 8;
    public static int randomLightnessOffset = (int) (Math.random() * 33.0D) - 16;
    public static List<TileData> tileDataList = new ArrayList<>();

    public World(int maxTileX, int maxTileZ) {
        this.maxTileX = maxTileX;
        this.maxTileZ = maxTileZ;

        this.levelTileUnderlayIds = new byte[4][this.maxTileX][this.maxTileZ];
        this.levelTileOverlayIds = new byte[4][this.maxTileX][this.maxTileZ];
        this.levelTileOverlayShape = new byte[4][this.maxTileX][this.maxTileZ];
        this.levelTileOverlayRotation = new byte[4][this.maxTileX][this.maxTileZ];

        this.levelOccludemap = new int[4][this.maxTileX + 1][this.maxTileZ + 1];
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

    public static int perlinNoise(int x, int z) {
        int value = interpolatedNoise(x + 45365, z + 91923, 4) + (interpolatedNoise(x + 10294, z + 37821, 2) - 128 >> 1) + (interpolatedNoise(x, z, 1) - 128 >> 2) - 128;
        value = (int) ((double) value * 0.3D) + 35;
        if (value < 10) {
            value = 10;
        } else if (value > 60) {
            value = 60;
        }
        return value;
    }

    private static int interpolatedNoise(int x, int z, int scale) {
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
        return corners / 16 + sides / 8 + center / 4;
    }

    private static int noise(int x, int y) {
        int n = x + y * 57;
        int n1 = n << 13 ^ n;
        int n2 = n1 * (n1 * n1 * 15731 + 789221) + 1376312589 & Integer.MAX_VALUE;
        return n2 >> 19 & 0xFF;
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

                                if (level > 0) {
                                    boolean occludes = underlayId != 0 || this.levelTileOverlayShape[level][x0][z0] == 0;

                                    if (overlayId > 0 && !FloType.getInstances()[overlayId - 1].occlude) {
                                        occludes = false;
                                    }

                                    // occludes && flat
                                    if (occludes && heightSW == heightSE && heightSW == heightNE && heightSW == heightNW) {
                                        this.levelOccludemap[level][x0][z0] |= 0x924;
                                    }
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
//            for (int stz = 1; stz < this.maxTileZ - 1; stz++) {
//                for (int stx = 1; stx < this.maxTileX - 1; stx++) {
//                    scene.setDrawLevel(level, stx, stz, this.getDrawLevel(level, stx, stz));
//                }
//            }
//        }

//        if (!fullbright) {
//            scene.buildModels(64, 768, -50, -10, -50);
//        }
            for (int x = 0; x < this.maxTileX; x++) {
                for (int z = 0; z < this.maxTileZ; z++) {
                    if ((this.levelTileFlags[1][x][z] & 0x2) == 2) {
                        scene.setBridge(x, z);
                    }
                }
            }

            if (!fullbright) {
                int wall0 = 0x1; // this flag is set by walls with rotation 0 or 2
                int wall1 = 0x2; // this flag is set by walls with rotation 1 or 3
                int floor = 0x4; // this flag is set by floors which are flat

                for (int topLevel = 0; topLevel < 4; topLevel++) {
                    if (topLevel > 0) {
                        wall0 <<= 0x3;
                        wall1 <<= 0x3;
                        floor <<= 0x3;
                    }

                    for (int level2 = 0; level2 <= topLevel; level2++) {
                        for (int tileZ = 0; tileZ <= this.maxTileZ; tileZ++) {
                            for (int tileX = 0; tileX <= this.maxTileX; tileX++) {
                                if ((this.levelOccludemap[level2][tileX][tileZ] & wall0) != 0) {
                                    int minTileZ = tileZ;
                                    int maxTileZ = tileZ;
                                    int minLevel = level2;
                                    int maxLevel = level2;

                                    while (minTileZ > 0 && (this.levelOccludemap[level2][tileX][minTileZ - 1] & wall0) != 0) {
                                        minTileZ--;
                                    }

                                    while (maxTileZ < this.maxTileZ && (this.levelOccludemap[level2][tileX][maxTileZ + 1] & wall0) != 0) {
                                        maxTileZ++;
                                    }

                                    find_min_level:
                                    while (minLevel > 0) {
                                        for (int z = minTileZ; z <= maxTileZ; z++) {
                                            if ((this.levelOccludemap[minLevel - 1][tileX][z] & wall0) == 0) {
                                                break find_min_level;
                                            }
                                        }
                                        minLevel--;
                                    }

                                    find_max_level:
                                    while (maxLevel < topLevel) {
                                        for (int z = minTileZ; z <= maxTileZ; z++) {
                                            if ((this.levelOccludemap[maxLevel + 1][tileX][z] & wall0) == 0) {
                                                break find_max_level;
                                            }
                                        }
                                        maxLevel++;
                                    }

                                    int area = (maxLevel + 1 - minLevel) * (maxTileZ + 1 - minTileZ);
                                    if (area >= 8) {
                                        int minY = this.levelHeightmap[maxLevel][tileX][minTileZ] - 240;
                                        int maxX = this.levelHeightmap[minLevel][tileX][minTileZ];

                                        World3D.addOccluder(topLevel, 1, tileX * 128, minY, minTileZ * 128, tileX * 128, maxX, maxTileZ * 128 + 128);

                                        for (int l = minLevel; l <= maxLevel; l++) {
                                            for (int z = minTileZ; z <= maxTileZ; z++) {
                                                this.levelOccludemap[l][tileX][z] &= ~wall0;
                                            }
                                        }
                                    }
                                }

                                if ((this.levelOccludemap[level2][tileX][tileZ] & wall1) != 0) {
                                    int minTileX = tileX;
                                    int maxTileX = tileX;
                                    int minLevel = level2;
                                    int maxLevel = level2;

                                    while (minTileX > 0 && (this.levelOccludemap[level2][minTileX - 1][tileZ] & wall1) != 0) {
                                        minTileX--;
                                    }

                                    while (maxTileX < this.maxTileX && (this.levelOccludemap[level2][maxTileX + 1][tileZ] & wall1) != 0) {
                                        maxTileX++;
                                    }

                                    find_min_level2:
                                    while (minLevel > 0) {
                                        for (int x = minTileX; x <= maxTileX; x++) {
                                            if ((this.levelOccludemap[minLevel - 1][x][tileZ] & wall1) == 0) {
                                                break find_min_level2;
                                            }
                                        }
                                        minLevel--;
                                    }

                                    find_max_level2:
                                    while (maxLevel < topLevel) {
                                        for (int x = minTileX; x <= maxTileX; x++) {
                                            if ((this.levelOccludemap[maxLevel + 1][x][tileZ] & wall1) == 0) {
                                                break find_max_level2;
                                            }
                                        }
                                        maxLevel++;
                                    }

                                    int area = (maxLevel + 1 - minLevel) * (maxTileX + 1 - minTileX);

                                    if (area >= 8) {
                                        int minY = this.levelHeightmap[maxLevel][minTileX][tileZ] - 240;
                                        int maxY = this.levelHeightmap[minLevel][minTileX][tileZ];

                                        World3D.addOccluder(topLevel, 2, minTileX * 128, minY, tileZ * 128, maxTileX * 128 + 128, maxY, tileZ * 128);

                                        for (int l = minLevel; l <= maxLevel; l++) {
                                            for (int x = minTileX; x <= maxTileX; x++) {
                                                this.levelOccludemap[l][x][tileZ] &= ~wall1;
                                            }
                                        }
                                    }
                                }
                                if ((this.levelOccludemap[level2][tileX][tileZ] & floor) != 0) {
                                    int minTileX = tileX;
                                    int maxTileX = tileX;
                                    int minTileZ = tileZ;
                                    int maxTileZ = tileZ;

                                    while (minTileZ > 0 && (this.levelOccludemap[level2][tileX][minTileZ - 1] & floor) != 0) {
                                        minTileZ--;
                                    }

                                    while (maxTileZ < this.maxTileZ && (this.levelOccludemap[level2][tileX][maxTileZ + 1] & floor) != 0) {
                                        maxTileZ++;
                                    }

                                    find_min_tile_xz:
                                    while (minTileX > 0) {
                                        for (int z = minTileZ; z <= maxTileZ; z++) {
                                            if ((this.levelOccludemap[level2][minTileX - 1][z] & floor) == 0) {
                                                break find_min_tile_xz;
                                            }
                                        }
                                        minTileX--;
                                    }

                                    find_max_tile_xz:
                                    while (maxTileX < this.maxTileX) {
                                        for (int z = minTileZ; z <= maxTileZ; z++) {
                                            if ((this.levelOccludemap[level2][maxTileX + 1][z] & floor) == 0) {
                                                break find_max_tile_xz;
                                            }
                                        }
                                        maxTileX++;
                                    }

                                    if ((maxTileX + 1 - minTileX) * (maxTileZ + 1 - minTileZ) >= 4) {
                                        int y = this.levelHeightmap[level2][minTileX][minTileZ];

                                        World3D.addOccluder(topLevel, 4, minTileX * 128, y, minTileZ * 128, maxTileX * 128 + 128, y, maxTileZ * 128 + 128);

                                        for (int x = minTileX; x <= maxTileX; x++) {
                                            for (int z = minTileZ; z <= maxTileZ; z++) {
                                                this.levelOccludemap[level2][x][z] &= ~floor;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }
}
