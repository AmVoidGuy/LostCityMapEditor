package org.lostcitymapeditor.OriginalCode;

import org.lostcitymapeditor.DataObjects.newTriangle;

public class World3D {
    public static final int LEVEL_COUNT = 4;
    public static Location[] locBuffer = new Location[100];
    private final Location[] temporaryLocs = new Location[5000];
    private final int maxLevel = LEVEL_COUNT;
    private int minLevel;
    private final int maxTileX = 64;
    private final int maxTileZ = 64;
    private final int[][][] levelHeightmaps;
    private int temporaryLocCount;
    public static Occlude[][] levelOccluders = new Occlude[LEVEL_COUNT][500];
    public static int[] levelOccluderCount = new int[LEVEL_COUNT];
    public static LinkList drawTileQueue = new LinkList();
    public static int tilesRemaining;
    public static int activeOccluderCount;
    public static final Occlude[] activeOccluders = new Occlude[500];
    public static int cycle;
    private final Ground[][][] levelTiles;
    private final int[][][] levelTileOcclusionCycles;
    public static boolean[][] visibilityMap;
    public static boolean[][][][] visibilityMatrix = new boolean[8][32][51][51];
    public static int topLevel;
    public static int minDrawTileX;
    public static int maxDrawTileX;
    public static int minDrawTileZ;
    public static int maxDrawTileZ;
    public static int eyeTileX;
    public static int eyeTileZ;
    public static int eyeX;
    public static int eyeY;
    public static int eyeZ;
    public static int sinEyePitch;
    public static int cosEyePitch;
    public static int sinEyeYaw;
    public static int cosEyeYaw;
    private static int viewportCenterX;
    private static int viewportCenterY;
    private static int viewportLeft;
    private static int viewportTop;
    private static int viewportRight;
    private static int viewportBottom;


    public World3D(int[][][] levelHeightmaps, int maxTileZ, int maxLevel, int maxTileX) {
        this.levelTiles = new Ground[maxLevel][maxTileX][maxTileZ];
        this.levelTileOcclusionCycles = new int[maxLevel][maxTileX + 1][maxTileZ + 1];
        this.levelHeightmaps = levelHeightmaps;
    }

    public static void init(int viewportWidth, int viewportHeight, int frustumStart, int frustumEnd, int[] pitchDistance) {
        viewportLeft = 0;
        viewportTop = 0;
        viewportRight = viewportWidth;
        viewportBottom = viewportHeight;
        viewportCenterX = viewportWidth / 2;
        viewportCenterY = viewportHeight / 2;
        levelOccluders = new Occlude[LEVEL_COUNT][500];

        boolean[][][][] matrix = new boolean[9][32][53][53];
        for (int pitch = 128; pitch <= 384; pitch += 32) {
            for (int yaw = 0; yaw < 2048; yaw += 64) {
                sinEyePitch = Model.sinTable[pitch];
                cosEyePitch = Model.cosTable[pitch];
                sinEyeYaw = Model.sinTable[yaw];
                cosEyeYaw = Model.cosTable[yaw];

                int pitchLevel = (pitch - 128) / 32;
                int yawLevel = yaw / 64;
                for (int dx = -26; dx <= 26; dx++) {
                    for (int dz = -26; dz <= 26; dz++) {
                        int x = dx * 128;
                        int z = dz * 128;

                        boolean visible = false;
                        for (int y = -frustumStart; y <= frustumEnd; y += 128) {
                            if (testPoint(x, z, pitchDistance[pitchLevel] + y)) {
                                visible = true;
                                break;
                            }
                        }

                        matrix[pitchLevel][yawLevel][dx + 25 + 1][dz + 25 + 1] = visible;
                    }
                }
            }
        }

        for (int pitchLevel = 0; pitchLevel < 8; pitchLevel++) {
            for (int yawLevel = 0; yawLevel < 32; yawLevel++) {
                for (int x = -25; x < 25; x++) {
                    for (int z = -25; z < 25; z++) {
                        boolean visible = false;
                        check_areas:
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                if (matrix[pitchLevel][yawLevel][x + dx + 25 + 1][z + dz + 25 + 1]) {
                                    visible = true;
                                    break check_areas;
                                }

                                if (matrix[pitchLevel][(yawLevel + 1) % 31][x + dx + 25 + 1][z + dz + 25 + 1]) {
                                    visible = true;
                                    break check_areas;
                                }

                                if (matrix[pitchLevel + 1][yawLevel][x + dx + 25 + 1][z + dz + 25 + 1]) {
                                    visible = true;
                                    break check_areas;
                                }

                                if (matrix[pitchLevel + 1][(yawLevel + 1) % 31][x + dx + 25 + 1][z + dz + 25 + 1]) {
                                    visible = true;
                                    break check_areas;
                                }
                            }
                        }

                        visibilityMatrix[pitchLevel][yawLevel][x + 25][z + 25] = visible;
                    }
                }
            }
        }
    }

    public void setTile( int level,  int x,  int z,  int shape,  int angle,  int textureId,  int southwestY,  int southeastY,  int northeastY,  int northwestY,  int southwestColor,  int southeastColor,  int northeastColor,  int northwestColor,  int southwestColor2,  int southeastColor2,  int northeastColor2,  int northwestColor2,  int backgroundRgb,  int foregroundRgb) {
        TileUnderlay underlay;
        int l;
        if (shape == 0) {
            underlay = new TileUnderlay(southwestColor, southeastColor, northeastColor, northwestColor, -1, backgroundRgb, false);
            for (l = level; l >= 0; l--) {
                if (this.levelTiles[l][x][z] == null) {
                    this.levelTiles[l][x][z] = new Ground(l, x, z);
                }
            }
            this.levelTiles[level][x][z].underlay = underlay;
        } else if (shape == 1) {
            underlay = new TileUnderlay(southwestColor2, southeastColor2, northeastColor2, northwestColor2, textureId, foregroundRgb, southwestY == southeastY && southwestY == northeastY && southwestY == northwestY);
            for (l = level; l >= 0; l--) {
                if (this.levelTiles[l][x][z] == null) {
                    this.levelTiles[l][x][z] = new Ground(l, x, z);
                }
            }
            this.levelTiles[level][x][z].underlay = underlay;
        } else {
            TileOverlay overlay = new TileOverlay(x, shape, southeastColor2, southeastY, northeastColor, angle, southwestColor, northwestY, foregroundRgb, southwestColor2, textureId, northwestColor2, backgroundRgb, northeastY, northeastColor2, northwestColor, southwestY, z, southeastColor);
            for (l = level; l >= 0; l--) {
                if (this.levelTiles[l][x][z] == null) {
                    this.levelTiles[l][x][z] = new Ground(l, x, z);
                }
            }
            this.levelTiles[level][x][z].overlay = overlay;
        }
    }

    public void setBridge(int stx, int stz) {
        Ground ground = this.levelTiles[0][stx][stz];
        for (int level = 0; level < 3; level++) {
            this.levelTiles[level][stx][stz] = this.levelTiles[level + 1][stx][stz];
            if (this.levelTiles[level][stx][stz] != null) {
                this.levelTiles[level][stx][stz].level--;
            }
        }

        if (this.levelTiles[0][stx][stz] == null) {
            this.levelTiles[0][stx][stz] = new Ground(0, stx, stz);
        }
        this.levelTiles[0][stx][stz].bridge = ground;
        this.levelTiles[3][stx][stz] = null;
    }

    public static void addOccluder(int level, int type, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        Occlude occluder = new Occlude();
        occluder.minTileX = minX / 128;
        occluder.maxTileX = maxX / 128;
        occluder.minTileZ = minZ / 128;
        occluder.maxTileZ = maxZ / 128;
        occluder.type = type;
        occluder.minX = minX;
        occluder.maxX = maxX;
        occluder.minZ = minZ;
        occluder.maxZ = maxZ;
        occluder.minY = minY;
        occluder.maxY = maxY;
        levelOccluders[level][levelOccluderCount[level]++] = occluder;
    }

    public void draw(int eyeX, int eyeY, int eyeZ, int topLevel, int eyeYaw, int eyePitch, int loopCycle,
                     int currentLevel) {
        cycle++;
        sinEyePitch = Model.sinTable[eyePitch];
        cosEyePitch = Model.cosTable[eyePitch];
        sinEyeYaw = Model.sinTable[eyeYaw];
        cosEyeYaw = Model.cosTable[eyeYaw];
        World3D.eyeX = eyeX;
        World3D.eyeY = eyeY;
        World3D.eyeZ = eyeZ;
        eyeTileX = eyeX / 128;
        eyeTileZ = eyeZ / 128;
        World3D.topLevel = topLevel;
        minDrawTileX = 0;
        minDrawTileZ = 0;
        maxDrawTileX = this.maxTileX;
        maxDrawTileZ = this.maxTileZ;

        Ground[][] tiles = this.levelTiles[currentLevel];
        for (int x = minDrawTileX; x < maxDrawTileX; x++) {
            for (int z = minDrawTileZ; z < maxDrawTileZ; z++) {
                Ground tile = tiles[x][z];
                if (tile == null) {
                    continue;
                }
                drawTile(tile, loopCycle);
            }
        }
    }

    private void drawTile(Ground tile, int loopCycle) {

        int tileX = tile.x;
        int tileZ = tile.z;
        int level = tile.level;
        int occludeLevel = tile.occludeLevel;

        if (tile.bridge != null) {
            Ground bridge = tile.bridge;
            if (bridge.underlay == null) {
                if (bridge.overlay != null) {
                    this.drawTileOverlay(tileX, tileZ, level, bridge.overlay, sinEyePitch, cosEyePitch, sinEyeYaw,
                            cosEyeYaw);
                }
            } else {
                this.drawTileUnderlay(bridge.underlay, 0, tileX, tileZ, sinEyePitch, cosEyePitch, sinEyeYaw, cosEyeYaw);
            }
        } else if (tile.underlay == null) {
            if (tile.overlay != null) {
                this.drawTileOverlay(tileX, tileZ, level, tile.overlay, sinEyePitch, cosEyePitch, sinEyeYaw,
                     cosEyeYaw);
            }
        } else {
           this.drawTileUnderlay(tile.underlay, occludeLevel, tileX, tileZ, sinEyePitch, cosEyePitch, sinEyeYaw,
                   cosEyeYaw);
        }
    }

    private void drawTileUnderlay( TileUnderlay underlay,  int level,  int tileX,  int tileZ,  int sinEyePitch,  int cosEyePitch,  int sinEyeYaw,  int cosEyeYaw) {

        // Corner positions within the tile
        float localX0 = 0; //(float) (tileX << 7);  // Local X coordinate (left edge of tile)
        float localZ0 = 0; //(float) (tileZ << 7);  // Local Z coordinate (top edge of tile)
        float localX1 = 1;  // Local X coordinate (right edge of tile)
        float localZ1 = 1;  // Local Z coordinate (bottom edge of tile)

        // Heights at the corners (from heightmap data)

        float sy0 = levelHeightmaps[level][tileX][tileZ];
        float sy1 = levelHeightmaps[level][tileX + 1][tileZ];
        float sy2 = levelHeightmaps[level][tileX + 1][tileZ + 1];
        float sy3 = levelHeightmaps[level][tileX][tileZ + 1];

        // Corrected world-space coordinates.  Tile offset is now included in the coordinates themselves.
        float sx0 = localX0 + (tileX << 7);
        float sz0 = localZ0 + (tileZ << 7);
        float sx1 = localX1 + ((tileX + 1) << 7);  //Note: tileX + 1
        float sz1 = localZ0 + (tileZ << 7);
        float sx2 = localX1 + ((tileX + 1) << 7);  //Note: tileX + 1
        float sz2 = localZ1 + ((tileZ + 1) << 7);  //Note: tileZ + 1
        float sx3 = localX0 + (tileX << 7);
        float sz3 = localZ1 + ((tileZ + 1) << 7);  //Note: tileZ + 1

        int colorNE = underlay.northeastColor;
        int colorNW = underlay.northwestColor;
        int colorSE = underlay.southeastColor;
        int colorSW = underlay.southwestColor;

        newTriangle.addTriangle(tileX, tileZ, level, 0, 0, sx2, sy2, sz2, sx3, sy3, sz3, sx1, sy1, sz1, colorNE, colorNW, colorSE, underlay.textureId, null);
        newTriangle.addTriangle(tileX, tileZ, level, 0, 0, sx0, sy0, sz0, sx1, sy1, sz1, sx3, sy3, sz3, colorSW, colorSE, colorNW, underlay.textureId, null);

        int x3;
        int x0 = x3 = (tileX << 7) - eyeX;
        int z1;
        int z0 = z1 = (tileZ << 7) - eyeZ;
        int x2;
        int x1 = x2 = x0 + 128;
        int z3;
        int z2 = z3 = z0 + 128;

        int y0 = this.levelHeightmaps[level][tileX][tileZ] - eyeY;
        int y1 = this.levelHeightmaps[level][tileX + 1][tileZ] - eyeY;
        int y2 = this.levelHeightmaps[level][tileX + 1][tileZ + 1] - eyeY;
        int y3 = this.levelHeightmaps[level][tileX][tileZ + 1] - eyeY;

        int tmp = z0 * sinEyeYaw + x0 * cosEyeYaw >> 16;
        z0 = z0 * cosEyeYaw - x0 * sinEyeYaw >> 16;
        x0 = tmp;

        tmp = y0 * cosEyePitch - z0 * sinEyePitch >> 16;
        z0 = y0 * sinEyePitch + z0 * cosEyePitch >> 16;
        y0 = tmp;

        if (z0 < 50) {
            return;
        }

        tmp = z1 * sinEyeYaw + x1 * cosEyeYaw >> 16;
        z1 = z1 * cosEyeYaw - x1 * sinEyeYaw >> 16;
        x1 = tmp;

        tmp = y1 * cosEyePitch - z1 * sinEyePitch >> 16;
        z1 = y1 * sinEyePitch + z1 * cosEyePitch >> 16;
        y1 = tmp;

        if (z1 < 50) {
            return;
        }

        tmp = z2 * sinEyeYaw + x2 * cosEyeYaw >> 16;
        z2 = z2 * cosEyeYaw - x2 * sinEyeYaw >> 16;
        x2 = tmp;

        tmp = y2 * cosEyePitch - z2 * sinEyePitch >> 16;
        z2 = y2 * sinEyePitch + z2 * cosEyePitch >> 16;
        y2 = tmp;

        if (z2 < 50) {
            return;
        }

        tmp = z3 * sinEyeYaw + x3 * cosEyeYaw >> 16;
        z3 = z3 * cosEyeYaw - x3 * sinEyeYaw >> 16;
        x3 = tmp;

        tmp = y3 * cosEyePitch - z3 * sinEyePitch >> 16;
        z3 = y3 * sinEyePitch + z3 * cosEyePitch >> 16;
        y3 = tmp;

        if (z3 < 50) {
            return;
        }
        int px0 = Pix3D.centerW3D + (x0 << 9) / z0;
        int py0 = Pix3D.centerH3D + (y0 << 9) / z0;
        int pz0 = Pix3D.centerW3D + (x1 << 9) / z1;
        int px1 = Pix3D.centerH3D + (y1 << 9) / z1;
        int py1 = Pix3D.centerW3D + (x2 << 9) / z2;
        int pz1 = Pix3D.centerH3D + (y2 << 9) / z2;
        int px3 = Pix3D.centerW3D + (x3 << 9) / z3;
        int py3 = Pix3D.centerH3D + (y3 << 9) / z3;

        Pix3D.trans = 0;

        if ((py1 - px3) * (px1 - py3) - (pz1 - py3) * (pz0 - px3) > 0) {
            Pix3D.hclip = py1 < 0 || px3 < 0 || pz0 < 0 || py1 > Pix2D.safeWidth || px3 > Pix2D.safeWidth || pz0 > Pix2D.safeWidth;
            if (underlay.textureId == -1) {
                if (underlay.northeastColor != 12345678) {
                    Pix3D.gouraudTriangle(py1, px3, pz0, pz1, py3, px1, underlay.northeastColor, underlay.northwestColor, underlay.southeastColor);
                }
            } else if (underlay.flat) {
                Pix3D.textureTriangle(py1, px3, pz0, pz1, py3, px1, underlay.northeastColor, underlay.northwestColor, underlay.southeastColor, x0, y0, z0, x1, x3, y1, y3, z1, z3, underlay.textureId);
            } else {
                Pix3D.textureTriangle(py1, px3, pz0, pz1, py3, px1, underlay.northeastColor, underlay.northwestColor, underlay.southeastColor, x2, y2, z2, x3, x1, y3, y1, z3, z1, underlay.textureId);
            }
        }
        if ((px0 - pz0) * (py3 - px1) - (py0 - px1) * (px3 - pz0) <= 0) {
            return;
        }
        Pix3D.hclip = px0 < 0 || pz0 < 0 || px3 < 0 || px0 > Pix2D.safeWidth || pz0 > Pix2D.safeWidth || px3 > Pix2D.safeWidth;
        if (underlay.textureId != -1) {
            Pix3D.textureTriangle(px0, pz0, px3, py0, px1, py3, underlay.southwestColor, underlay.southeastColor, underlay.northwestColor, x0, y0, z0, x1, x3, y1, y3, z1, z3, underlay.textureId);
        } else if (underlay.southwestColor != 12345678) {
            Pix3D.gouraudTriangle(px0, pz0, px3, py0, px1, py3, underlay.southwestColor, underlay.southeastColor, underlay.northwestColor);
        }

    }

    private void drawTileOverlay(int tileX, int tileZ, int level, TileOverlay overlay, int sinEyePitch, int cosEyePitch, int sinEyeYaw, int cosEyeYaw) {
        int vertexCount = overlay.vertexX.length;

        for (int v = 0; v < overlay.triangleVertexA.length; v++) {
            int a = overlay.triangleVertexA[v];
            int b = overlay.triangleVertexB[v];
            int c = overlay.triangleVertexC[v];

            // Get model-space coordinates
            float x0 = overlay.vertexX[a];
            float y0 = overlay.vertexY[a];
            float z0 = overlay.vertexZ[a];

            float x1 = overlay.vertexX[b];
            float y1 = overlay.vertexY[b];
            float z1 = overlay.vertexZ[b];

            float x2 = overlay.vertexX[c];
            float y2 = overlay.vertexY[c];
            float z2 = overlay.vertexZ[c];

            // Apply tile offset to get world coordinates
            float sx0 = x0;
            float sz0 = z0;
            float sx1 = x1;
            float sz1 = z1;
            float sx2 = x2;
            float sz2 = z2;

            int colorA = overlay.triangleColorA[v];
            int colorB = overlay.triangleColorB[v];
            int colorC = overlay.triangleColorC[v];

            int textureId = -1;
            if(overlay.triangleTextureIds != null) {
                textureId = overlay.triangleTextureIds[v];
            }
            newTriangle.addTriangle(tileX, tileZ, level, overlay.shape, overlay.rotation, sx0, y0, sz0, sx1, y1, sz1, sx2, y2, sz2, colorA, colorB, colorC, textureId, null);
        }

        for (int i = 0; i < vertexCount; i++) {
            int x = overlay.vertexX[i] - eyeX;
            int y = overlay.vertexY[i] - eyeY;
            int z = overlay.vertexZ[i] - eyeZ;
            int tmp = z * sinEyeYaw + x * cosEyeYaw >> 16;
            z = z * cosEyeYaw - x * sinEyeYaw >> 16;
            x = tmp;

            tmp = y * cosEyePitch - z * sinEyePitch >> 16;
            z = y * sinEyePitch + z * cosEyePitch >> 16;
            y = tmp;

            if (z < 50) {
                return;
            }

            if (overlay.triangleTextureIds != null) {
                TileOverlay.tmpViewspaceX[i] = x;
                TileOverlay.tmpViewspaceY[i] = y;
                TileOverlay.tmpViewspaceZ[i] = z;
            }
            TileOverlay.tmpScreenX[i] = Pix3D.centerW3D + (x << 9) / z;
            TileOverlay.tmpScreenY[i] = Pix3D.centerH3D + (y << 9) / z;
        }

        Pix3D.trans = 0;

        vertexCount = overlay.triangleVertexA.length;
        for (int v = 0; v < vertexCount; v++) {
            int a = overlay.triangleVertexA[v];
            int b = overlay.triangleVertexB[v];
            int c = overlay.triangleVertexC[v];

            int x0 = TileOverlay.tmpScreenX[a];
            int x1 = TileOverlay.tmpScreenX[b];
            int x2 = TileOverlay.tmpScreenX[c];
            int y0 = TileOverlay.tmpScreenY[a];
            int y1 = TileOverlay.tmpScreenY[b];
            int y2 = TileOverlay.tmpScreenY[c];

            if ((x0 - x1) * (y2 - y1) - (y0 - y1) * (x2 - x1) > 0) {
                Pix3D.hclip = x0 < 0 || x1 < 0 || x2 < 0 || x0 > Pix2D.safeWidth || x1 > Pix2D.safeWidth || x2 > Pix2D.safeWidth;
                if (overlay.triangleTextureIds == null || overlay.triangleTextureIds[v] == -1) {
                    if (overlay.triangleColorA[v] != 12345678) {
                        Pix3D.gouraudTriangle(x0, x1, x2, y0, y1, y2, overlay.triangleColorA[v], overlay.triangleColorB[v], overlay.triangleColorC[v]);
                    }
                } else if (overlay.flat) {
                    Pix3D.textureTriangle(x0, x1, x2, y0, y1, y2, overlay.triangleColorA[v], overlay.triangleColorB[v], overlay.triangleColorC[v], TileOverlay.tmpViewspaceX[0], TileOverlay.tmpViewspaceY[0], TileOverlay.tmpViewspaceZ[0], TileOverlay.tmpViewspaceX[1], TileOverlay.tmpViewspaceX[3], TileOverlay.tmpViewspaceY[1], TileOverlay.tmpViewspaceY[3], TileOverlay.tmpViewspaceZ[1], TileOverlay.tmpViewspaceZ[3], overlay.triangleTextureIds[v]);
                } else {
                    Pix3D.textureTriangle(x0, x1, x2, y0, y1, y2, overlay.triangleColorA[v], overlay.triangleColorB[v], overlay.triangleColorC[v], TileOverlay.tmpViewspaceX[a], TileOverlay.tmpViewspaceY[a], TileOverlay.tmpViewspaceZ[a], TileOverlay.tmpViewspaceX[b], TileOverlay.tmpViewspaceX[c], TileOverlay.tmpViewspaceY[b], TileOverlay.tmpViewspaceY[c], TileOverlay.tmpViewspaceZ[b], TileOverlay.tmpViewspaceZ[c], overlay.triangleTextureIds[v]);
                }
            }
        }
    }

    private int mulLightness(int hsl, int lightness) {
        int invLightness = 127 - lightness;
        lightness = invLightness * (hsl & 0x7F) / 160;
        if (lightness < 2) {
            lightness = 2;
        } else if (lightness > 126) {
            lightness = 126;
        }
        return (hsl & 0xFF80) + lightness;
    }

    private boolean tileVisible(int level, int x, int z) {
        return false;
    }

    private boolean occluded(int x, int y, int z) {
        for (int i = 0; i < activeOccluderCount; i++) {
            Occlude occluder = activeOccluders[i];

            if (occluder.mode == 1) {
                int dx = occluder.minX - x;
                if (dx > 0) {
                    int minZ = occluder.minZ + (occluder.minDeltaZ * dx >> 8);
                    int maxZ = occluder.maxZ + (occluder.maxDeltaZ * dx >> 8);
                    int minY = occluder.minY + (occluder.minDeltaY * dx >> 8);
                    int maxY = occluder.maxY + (occluder.maxDeltaY * dx >> 8);
                    if (z >= minZ && z <= maxZ && y >= minY && y <= maxY) {
                        return true;
                    }
                }
            } else if (occluder.mode == 2) {
                int dx = x - occluder.minX;
                if (dx > 0) {
                    int minZ = occluder.minZ + (occluder.minDeltaZ * dx >> 8);
                    int maxZ = occluder.maxZ + (occluder.maxDeltaZ * dx >> 8);
                    int minY = occluder.minY + (occluder.minDeltaY * dx >> 8);
                    int maxY = occluder.maxY + (occluder.maxDeltaY * dx >> 8);
                    if (z >= minZ && z <= maxZ && y >= minY && y <= maxY) {
                        return true;
                    }
                }
            } else if (occluder.mode == 3) {
                int dz = occluder.minZ - z;
                if (dz > 0) {
                    int minX = occluder.minX + (occluder.minDeltaX * dz >> 8);
                    int maxX = occluder.maxX + (occluder.maxDeltaX * dz >> 8);
                    int minY = occluder.minY + (occluder.minDeltaY * dz >> 8);
                    int maxY = occluder.maxY + (occluder.maxDeltaY * dz >> 8);
                    if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
                        return true;
                    }
                }
            } else if (occluder.mode == 4) {
                int dz = z - occluder.minZ;
                if (dz > 0) {
                    int minX = occluder.minX + (occluder.minDeltaX * dz >> 8);
                    int maxX = occluder.maxX + (occluder.maxDeltaX * dz >> 8);
                    int minY = occluder.minY + (occluder.minDeltaY * dz >> 8);
                    int maxY = occluder.maxY + (occluder.maxDeltaY * dz >> 8);
                    if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
                        return true;
                    }
                }
            } else if (occluder.mode == 5) {
                int dy = y - occluder.minY;
                if (dy > 0) {
                    int minX = occluder.minX + (occluder.minDeltaX * dy >> 8);
                    int maxX = occluder.maxX + (occluder.maxDeltaX * dy >> 8);
                    int minZ = occluder.minZ + (occluder.minDeltaZ * dy >> 8);
                    int maxZ = occluder.maxZ + (occluder.maxDeltaZ * dy >> 8);
                    if (x >= minX && x <= maxX && z >= minZ && z <= maxZ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void updateActiveOccluders() {
        int count = levelOccluderCount[topLevel];
        Occlude[] occluders = levelOccluders[topLevel];
        activeOccluderCount = 0;
        for (int i = 0; i < count; i++) {
            Occlude occluder = occluders[i];
            int deltaMaxY;
            int deltaMinTileZ;
            int deltaMaxTileZ;
            int deltaMaxTileX;
            if (occluder.type == 1) {
                deltaMaxY = occluder.minTileX + 25 - eyeTileX;
                if (deltaMaxY >= 0 && deltaMaxY <= 50) {
                    deltaMinTileZ = occluder.minTileZ + 25 - eyeTileZ;
                    if (deltaMinTileZ < 0) {
                        deltaMinTileZ = 0;
                    }
                    deltaMaxTileZ = occluder.maxTileZ + 25 - eyeTileZ;
                    if (deltaMaxTileZ > 50) {
                        deltaMaxTileZ = 50;
                    }
                    boolean ok = false;
                    while (deltaMinTileZ <= deltaMaxTileZ) {
                        if (visibilityMap[deltaMaxY][deltaMinTileZ++]) {
                            ok = true;
                            break;
                        }
                    }
                    if (ok) {
                        deltaMaxTileX = eyeX - occluder.minX;
                        if (deltaMaxTileX > 32) {
                            occluder.mode = 1;
                        } else {
                            if (deltaMaxTileX >= -32) {
                                continue;
                            }
                            occluder.mode = 2;
                            deltaMaxTileX = -deltaMaxTileX;
                        }
                        occluder.minDeltaZ = (occluder.minZ - eyeZ << 8) / deltaMaxTileX;
                        occluder.maxDeltaZ = (occluder.maxZ - eyeZ << 8) / deltaMaxTileX;
                        occluder.minDeltaY = (occluder.minY - eyeY << 8) / deltaMaxTileX;
                        occluder.maxDeltaY = (occluder.maxY - eyeY << 8) / deltaMaxTileX;
                        activeOccluders[activeOccluderCount++] = occluder;
                    }
                }
            } else if (occluder.type == 2) {
                deltaMaxY = occluder.minTileZ + 25 - eyeTileZ;
                if (deltaMaxY >= 0 && deltaMaxY <= 50) {
                    deltaMinTileZ = occluder.minTileX + 25 - eyeTileX;
                    if (deltaMinTileZ < 0) {
                        deltaMinTileZ = 0;
                    }
                    deltaMaxTileZ = occluder.maxTileX + 25 - eyeTileX;
                    if (deltaMaxTileZ > 50) {
                        deltaMaxTileZ = 50;
                    }
                    boolean ok = false;
                    while (deltaMinTileZ <= deltaMaxTileZ) {
                        if (visibilityMap[deltaMinTileZ++][deltaMaxY]) {
                            ok = true;
                            break;
                        }
                    }
                    if (ok) {
                        deltaMaxTileX = eyeZ - occluder.minZ;
                        if (deltaMaxTileX > 32) {
                            occluder.mode = 3;
                        } else {
                            if (deltaMaxTileX >= -32) {
                                continue;
                            }
                            occluder.mode = 4;
                            deltaMaxTileX = -deltaMaxTileX;
                        }
                        occluder.minDeltaX = (occluder.minX - eyeX << 8) / deltaMaxTileX;
                        occluder.maxDeltaX = (occluder.maxX - eyeX << 8) / deltaMaxTileX;
                        occluder.minDeltaY = (occluder.minY - eyeY << 8) / deltaMaxTileX;
                        occluder.maxDeltaY = (occluder.maxY - eyeY << 8) / deltaMaxTileX;
                        activeOccluders[activeOccluderCount++] = occluder;
                    }
                }
            } else if (occluder.type == 4) {
                deltaMaxY = occluder.minY - eyeY;
                if (deltaMaxY > 128) {
                    deltaMinTileZ = occluder.minTileZ + 25 - eyeTileZ;
                    if (deltaMinTileZ < 0) {
                        deltaMinTileZ = 0;
                    }
                    deltaMaxTileZ = occluder.maxTileZ + 25 - eyeTileZ;
                    if (deltaMaxTileZ > 50) {
                        deltaMaxTileZ = 50;
                    }
                    if (deltaMinTileZ <= deltaMaxTileZ) {
                        int deltaMinTileX = occluder.minTileX + 25 - eyeTileX;
                        if (deltaMinTileX < 0) {
                            deltaMinTileX = 0;
                        }
                        deltaMaxTileX = occluder.maxTileX + 25 - eyeTileX;
                        if (deltaMaxTileX > 50) {
                            deltaMaxTileX = 50;
                        }
                        boolean ok = false;
                        find_visible_tile:
                        for (int x = deltaMinTileX; x <= deltaMaxTileX; x++) {
                            for (int z = deltaMinTileZ; z <= deltaMaxTileZ; z++) {
                                if (visibilityMap[x][z]) {
                                    ok = true;
                                    break find_visible_tile;
                                }
                            }
                        }
                        if (ok) {
                            occluder.mode = 5;
                            occluder.minDeltaX = (occluder.minX - eyeX << 8) / deltaMaxY;
                            occluder.maxDeltaX = (occluder.maxX - eyeX << 8) / deltaMaxY;
                            occluder.minDeltaZ = (occluder.minZ - eyeZ << 8) / deltaMaxY;
                            occluder.maxDeltaZ = (occluder.maxZ - eyeZ << 8) / deltaMaxY;
                            activeOccluders[activeOccluderCount++] = occluder;
                        }
                    }
                }
            }
        }
    }

    private static boolean testPoint(int x, int z, int y) {
        int px = z * sinEyeYaw + x * cosEyeYaw >> 16;
        int tmp = z * cosEyeYaw - x * sinEyeYaw >> 16;
        int pz = y * sinEyePitch + tmp * cosEyePitch >> 16;
        int py = y * cosEyePitch - tmp * sinEyePitch >> 16;
        if (pz < 50 || pz > 3500) {
            return false;
        }

        int viewportX = viewportCenterX + (px << 9) / pz;
        int viewportY = viewportCenterY + (py << 9) / pz;
        return viewportX >= viewportLeft && viewportX <= viewportRight && viewportY >= viewportTop && viewportY <= viewportBottom;
    }
}
