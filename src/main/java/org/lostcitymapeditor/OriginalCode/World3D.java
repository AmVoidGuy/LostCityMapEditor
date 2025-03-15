package org.lostcitymapeditor.OriginalCode;

import org.lostcitymapeditor.DataObjects.newTriangle;

public class World3D {
    private final int maxTileX = 64;
    private final int maxTileZ = 64;
    private final int[][][] levelHeightmaps;
    public final Ground[][][] levelTiles;
    private final int[] mergeIndexA = new int[10000];
    private final int[] mergeIndexB = new int[10000];
    private int tmpMergeIndex;

    public World3D(int[][][] levelHeightmaps, int maxTileZ, int maxLevel, int maxTileX) {
        this.levelTiles = new Ground[maxLevel][maxTileX][maxTileZ];
        this.levelHeightmaps = levelHeightmaps;
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
        //TODO: Disable for now
//        Ground ground = this.levelTiles[0][stx][stz];
//        for (int level = 0; level < 3; level++) {
//            this.levelTiles[level][stx][stz] = this.levelTiles[level + 1][stx][stz];
//            if (this.levelTiles[level][stx][stz] != null) {
//                this.levelTiles[level][stx][stz].level--;
//
//            }
//        }
//
//        if (this.levelTiles[0][stx][stz] == null) {
//            this.levelTiles[0][stx][stz] = new Ground(0, stx, stz);
//        }
//        this.levelTiles[0][stx][stz].bridge = ground;
//        this.levelTiles[3][stx][stz] = null;
    }

    public void draw(int currentLevel) {
        for (int x = 0; x < maxTileX; x++) {
            for (int z = 0; z < maxTileZ; z++) {
                Ground tile = this.levelTiles[currentLevel][x][z];
                if (tile == null) {
                    continue;
                }
                drawTile(tile);
            }
        }
    }

    private void drawTile(Ground tile) {
        int tileX = tile.x;
        int tileZ = tile.z;
        int level = tile.level;

//        if (tile.bridge != null) {
//            Ground bridge = tile.bridge;
//            if (bridge.underlay == null) {
//                if (bridge.overlay != null) {
//                    this.drawTileOverlay(tileX, tileZ, level, bridge.overlay);
//                }
//            } else {
//                this.drawTileUnderlay(bridge.underlay, level, tileX, tileZ);
//            }
//        }
        if (tile.underlay == null) {
            if (tile.overlay != null) {
                this.drawTileOverlay(tileX, tileZ, level, tile.overlay);
            }
        } else {
           this.drawTileUnderlay(tile.underlay, level, tileX, tileZ);
        }

        if (tile.wall != null) {
            if(tile.wall.modelA != null) {
                tile.wall.modelA.draw(0, tile.wall.x, tile.wall.y, tile.wall.z, tile.wall.bitset);
            }
            if(tile.wall.modelB != null) {
                tile.wall.modelB.draw(0, tile.wall.x, tile.wall.y, tile.wall.z, tile.wall.bitset);
            }
        }
        if (tile.decor != null  && tile.decor.model != null) {
            tile.decor.model.draw(tile.decor.angle, tile.decor.x, tile.decor.y, tile.decor.z, tile.decor.bitset);
        }

        if (tile.groundDecor != null && tile.groundDecor.model != null) {
            tile.groundDecor.model.draw(0, tile.groundDecor.x, tile.groundDecor.y, tile.groundDecor.z, tile.groundDecor.bitset);
        }

        for (int i = 0; i < tile.locCount; i++) {
            Location loc = tile.locs[i];
            if (loc != null && loc.model != null) {
                loc.model.draw(loc.yaw, loc.x, loc.y, loc.z, loc.bitset);
            }
        }
    }

    private void drawTileUnderlay( TileUnderlay underlay,  int level,  int tileX,  int tileZ) {

        float sy0 = levelHeightmaps[level][tileX][tileZ];
        float sy1 = levelHeightmaps[level][tileX + 1][tileZ];
        float sy2 = levelHeightmaps[level][tileX + 1][tileZ + 1];
        float sy3 = levelHeightmaps[level][tileX][tileZ + 1];

        float sx0 = tileX * 128;
        float sz0 = tileZ * 128;
        float sx1 = (tileX + 1) * 128;
        float sz1 = tileZ * 128;
        float sx2 = (tileX + 1) * 128;
        float sz2 = (tileZ + 1) * 128;
        float sx3 = tileX * 128;
        float sz3 = (tileZ + 1) * 128;

        int colorNE = underlay.northeastColor;
        int colorNW = underlay.northwestColor;
        int colorSE = underlay.southeastColor;
        int colorSW = underlay.southwestColor;

        newTriangle.addTriangle(false, tileX, tileZ, level, 0, 0, sx2, sy2, sz2, sx3, sy3, sz3, sx1, sy1, sz1, colorNE, colorNW, colorSE, underlay.textureId, null);
        newTriangle.addTriangle(false, tileX, tileZ, level, 0, 0, sx0, sy0, sz0, sx1, sy1, sz1, sx3, sy3, sz3, colorSW, colorSE, colorNW, underlay.textureId, null);
    }

    private void drawTileOverlay(int tileX, int tileZ, int level, TileOverlay overlay) {
        for (int v = 0; v < overlay.triangleVertexA.length; v++) {
            int a = overlay.triangleVertexA[v];
            int b = overlay.triangleVertexB[v];
            int c = overlay.triangleVertexC[v];

            float x0 = overlay.vertexX[a];
            float y0 = overlay.vertexY[a];
            float z0 = overlay.vertexZ[a];

            float x1 = overlay.vertexX[b];
            float y1 = overlay.vertexY[b];
            float z1 = overlay.vertexZ[b];

            float x2 = overlay.vertexX[c];
            float y2 = overlay.vertexY[c];
            float z2 = overlay.vertexZ[c];

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
            newTriangle.addTriangle(false, tileX, tileZ, level, overlay.shape, overlay.rotation, sx0, y0, sz0, sx1, y1, sz1, sx2, y2, sz2, colorA, colorB, colorC, textureId, null);
        }
    }

    public void addGroundDecoration(Model model, int tileLevel, int tileX, int tileZ, int y, int bitset, byte info) {
        GroundDecor decor = new GroundDecor();
        decor.model = model;
        decor.x = tileX * 128 + 64;
        decor.z = tileZ * 128 + 64;
        decor.y = y;
        decor.bitset = bitset;
        decor.info = info;
        if (this.levelTiles[tileLevel][tileX][tileZ] == null) {
            this.levelTiles[tileLevel][tileX][tileZ] = new Ground(tileLevel, tileX, tileZ);
        }
        this.levelTiles[tileLevel][tileX][tileZ].groundDecor = decor;
    }

    public void addWall(int level, int tileX, int tileZ, int y, int typeA, int typeB, Model modelA, Model modelB, int bitset, byte info) {
        if (modelA == null && modelB == null) {
            return;
        }
        Wall wall = new Wall();
        wall.bitset = bitset;
        wall.info = info;
        wall.x = tileX * 128 + 64;
        wall.z = tileZ * 128 + 64;
        wall.y = y;
        wall.modelA = modelA;
        wall.modelB = modelB;
        wall.typeA = typeA;
        wall.typeB = typeB;
        for (int l = level; l >= 0; l--) {
            if (this.levelTiles[l][tileX][tileZ] == null) {
                this.levelTiles[l][tileX][tileZ] = new Ground(l, tileX, tileZ);
            }
        }
        this.levelTiles[level][tileX][tileZ].wall = wall;
    }

    public void setWallDecorationOffset(int level, int x, int z, int offset) {
        Ground tile = this.levelTiles[level][x][z];
        if (tile == null) {
            return;
        }

        Decor decor = tile.decor;
        if (decor == null) {
            return;
        }

        int sx = x * 128 + 64;
        int sz = z * 128 + 64;
        decor.x = sx + (decor.x - sx) * offset / 16;
        decor.z = sz + (decor.z - sz) * offset / 16;
    }

    public void setWallDecoration(int level, int tileX, int tileZ, int y, int offsetX, int offsetZ, int bitset, Model model, byte info, int angle, int type) {
        if (model == null) {
            return;
        }
        Decor decor = new Decor();
        decor.bitset = bitset;
        decor.info = info;
        decor.x = tileX * 128 + offsetX + 64;
        decor.z = tileZ * 128 + offsetZ + 64;
        decor.y = y;
        decor.model = model;
        decor.type = type;
        decor.angle = angle;
        for (int l = level; l >= 0; l--) {
            if (this.levelTiles[l][tileX][tileZ] == null) {
                this.levelTiles[l][tileX][tileZ] = new Ground(l, tileX, tileZ);
            }
        }
        this.levelTiles[level][tileX][tileZ].decor = decor;
    }

    public boolean addLoc(int level, int tileX, int tileZ, int y, Model model, Entity entity, int bitset, byte info, int width,int length, int yaw) {
        if (model == null && entity == null) {
            return true;
        } else {
            int sceneX = tileX * 128 + width * 64;
            int sceneZ = tileZ * 128 + length * 64;
            return this.addLoc(sceneX, sceneZ, y, level, tileX, tileZ, width, length, model, entity, bitset, info, yaw, false);
        }
    }

    public int getWallBitset(int level, int x, int z) {
        Ground tile = this.levelTiles[level][x][z];
        return tile == null || tile.wall == null ? 0 : tile.wall.bitset;
    }

    private boolean addLoc(int x, int z, int y, int level, int tileX, int tileZ, int tileSizeX, int tileSizeZ, Model model, Entity entity, int bitset, byte info, int yaw, boolean temporary) {
        if (model == null && entity == null) {
            return false;
        }
        for (int tx = tileX; tx < tileX + tileSizeX; tx++) {
            for (int tz = tileZ; tz < tileZ + tileSizeZ; tz++) {
                if (tx < 0 || tz < 0 || tx >= this.maxTileX || tz >= this.maxTileZ) {
                    return false;
                }
                Ground tile = this.levelTiles[level][tx][tz];
                if (tile != null && tile.locCount >= 5) {
                    return false;
                }
            }
        }
        Location loc = new Location();
        loc.bitset = bitset;
        loc.info = info;
        loc.level = level;
        loc.x = x;
        loc.z = z;
        loc.y = y;
        loc.model = model;
        loc.entity = entity;
        loc.yaw = yaw;
        loc.minSceneTileX = tileX;
        loc.minSceneTileZ = tileZ;
        loc.maxSceneTileX = tileX + tileSizeX - 1;
        loc.maxSceneTileZ = tileZ + tileSizeZ - 1;
        for (int tx = tileX; tx < tileX + 1; tx++) {
            for (int tz = tileZ; tz < tileZ + 1; tz++) {
                int spans = 0;
                if (tx > tileX) {
                    spans |= 0x1;
                }
                if (tx < tileX + tileSizeX - 1) {
                    spans += 0x4;
                }
                if (tz > tileZ) {
                    spans += 0x8;
                }
                if (tz < tileZ + tileSizeZ - 1) {
                    spans += 0x2;
                }
                for (int l = level; l >= 0; l--) {
                    if (this.levelTiles[l][tx][tz] == null) {
                        this.levelTiles[l][tx][tz] = new Ground(l, tx, tz);
                    }
                }
                Ground tile = this.levelTiles[level][tx][tz];
                tile.locs[tile.locCount] = loc;
                tile.locSpan[tile.locCount] = spans;
                tile.locSpans |= spans;
                tile.locCount++;
            }
        }
        return true;
    }

    public void buildModels(int lightAmbient, int lightAttenuation, int lightSrcX, int lightSrcY, int lightSrcZ) {
        int lightMagnitude = (int) Math.sqrt(lightSrcX * lightSrcX + lightSrcY * lightSrcY + lightSrcZ * lightSrcZ);
        int attenuation = lightAttenuation * lightMagnitude >> 8;

        for (int level = 0; level < 4; level++) {
            for (int tileX = 0; tileX < this.maxTileX; tileX++) {
                for (int tileZ = 0; tileZ < this.maxTileZ; tileZ++) {
                    Ground tile = this.levelTiles[level][tileX][tileZ];
                    if (tile == null) {
                        continue;
                    }

                    Wall wall = tile.wall;
                    if (wall != null && wall.modelA != null && wall.modelA.vertexNormal != null) {
                        this.mergeLocNormals(level, tileX, tileZ, 1, 1, wall.modelA);
                        if (wall.modelB != null && wall.modelB.vertexNormal != null) {
                            this.mergeLocNormals(level, tileX, tileZ, 1, 1, wall.modelB);
                            this.mergeNormals(wall.modelA, wall.modelB, 0, 0, 0, false);
                            wall.modelB.applyLighting(lightAmbient, attenuation, lightSrcX, lightSrcY, lightSrcZ);
                        }
                        wall.modelA.applyLighting(lightAmbient, attenuation, lightSrcX, lightSrcY, lightSrcZ);
                    }

                    for (int i = 0; i < tile.locCount; i++) {
                        Location loc = tile.locs[i];
                        if (loc != null && loc.model != null && loc.model.vertexNormal != null) {
                            this.mergeLocNormals(level, tileX, tileZ, loc.maxSceneTileX + 1 - loc.minSceneTileX, loc.maxSceneTileZ - loc.minSceneTileZ + 1, loc.model);
                            loc.model.applyLighting(lightAmbient, attenuation, lightSrcX, lightSrcY, lightSrcZ);
                        }
                    }

                    GroundDecor decor = tile.groundDecor;
                    if (decor != null && decor.model != null && decor.model.vertexNormal != null) {
                        this.mergeGroundDecorationNormals(level, tileX, tileZ, decor.model);
                        decor.model.applyLighting(lightAmbient, attenuation, lightSrcX, lightSrcY, lightSrcZ);
                    }
                }
            }
        }
    }

    private void mergeGroundDecorationNormals(int level, int tileX, int tileZ, Model model) {
        Ground tile;
        if (tileX < this.maxTileX - 1) {
            tile = this.levelTiles[level][tileX + 1][tileZ];
            if (tile != null && tile.groundDecor != null  && tile.groundDecor.model != null && tile.groundDecor.model.vertexNormal != null) {
                this.mergeNormals(model, tile.groundDecor.model, 128, 0, 0, true);
            }
        }

        if (tileZ < this.maxTileX - 1) {
            tile = this.levelTiles[level][tileX][tileZ + 1];
            if (tile != null && tile.groundDecor != null && tile.groundDecor.model != null && tile.groundDecor.model.vertexNormal != null) {
                this.mergeNormals(model, tile.groundDecor.model, 0, 0, 128, true);
            }
        }

        if (tileX < this.maxTileX - 1 && tileZ < this.maxTileZ - 1) {
            tile = this.levelTiles[level][tileX + 1][tileZ + 1];
            if (tile != null && tile.groundDecor != null && tile.groundDecor.model != null && tile.groundDecor.model.vertexNormal != null) {
                this.mergeNormals(model, tile.groundDecor.model, 128, 0, 128, true);
            }
        }

        if (tileX < this.maxTileX - 1 && tileZ > 0) {
            tile = this.levelTiles[level][tileX + 1][tileZ - 1];
            if (tile != null && tile.groundDecor != null  && tile.groundDecor.model != null && tile.groundDecor.model.vertexNormal != null) {
                this.mergeNormals(model, tile.groundDecor.model, 128, 0, -128, true);
            }
        }
    }

    private void mergeLocNormals(int level, int tileX, int tileZ, int tileSizeX, int tileSizeZ, Model model) {
        boolean allowFaceRemoval = true;

        int minTileX = tileX;
        int maxTileX = tileX + tileSizeX;
        int minTileZ = tileZ - 1;
        int maxTileZ = tileZ + tileSizeZ;

        for (int l = level; l <= level + 1; l++) {
            if (l == 4) {
                continue;
            }

            for (int x = minTileX; x <= maxTileX; x++) {
                if (x < 0 || x >= this.maxTileX) {
                    continue;
                }

                for (int z = minTileZ; z <= maxTileZ; z++) {
                    if (z < 0 || z >= this.maxTileZ || (allowFaceRemoval && x < maxTileX && z < maxTileZ && (z >= tileZ || x == tileX))) {
                        continue;
                    }

                    Ground tile = this.levelTiles[l][x][z];
                    if (tile == null) {
                        continue;
                    }

                    int offsetY = (this.levelHeightmaps[l][x][z] + this.levelHeightmaps[l][x + 1][z] + this.levelHeightmaps[l][x][z + 1] + this.levelHeightmaps[l][x + 1][z + 1]) / 4 - (this.levelHeightmaps[level][tileX][tileZ] + this.levelHeightmaps[level][tileX + 1][tileZ] + this.levelHeightmaps[level][tileX][tileZ + 1] + this.levelHeightmaps[level][tileX + 1][tileZ + 1]) / 4;

                    Wall wall = tile.wall;
                    if (wall != null && wall.modelA != null && wall.modelA.vertexNormal != null) {
                        this.mergeNormals(model, wall.modelA, (x - tileX) * 128 + (1 - tileSizeX) * 64, offsetY, (z - tileZ) * 128 + (1 - tileSizeZ) * 64, allowFaceRemoval);
                    }

                    if (wall != null && wall.modelB != null && wall.modelB.vertexNormal != null) {
                        this.mergeNormals(model, wall.modelB, (x - tileX) * 128 + (1 - tileSizeX) * 64, offsetY, (z - tileZ) * 128 + (1 - tileSizeZ) * 64, allowFaceRemoval);
                    }

                    for (int i = 0; i < tile.locCount; i++) {
                        Location loc = tile.locs[i];
                        if (loc == null || loc.model == null || loc.model.vertexNormal == null) {
                            continue;
                        }

                        int locTileSizeX = loc.maxSceneTileX + 1 - loc.minSceneTileX;
                        int locTileSizeZ = loc.maxSceneTileZ + 1 - loc.minSceneTileZ;
                        this.mergeNormals(model, loc.model, (loc.minSceneTileX - tileX) * 128 + (locTileSizeX - tileSizeX) * 64, offsetY, (loc.minSceneTileZ - tileZ) * 128 + (locTileSizeZ - tileSizeZ) * 64, allowFaceRemoval);
                    }
                }
            }

            minTileX--;
            allowFaceRemoval = false;
        }
    }

    private void mergeNormals(Model modelA, Model modelB, int offsetX, int offsetY, int offsetZ, boolean allowFaceRemoval) {
        this.tmpMergeIndex++;

        int merged = 0;
        int[] vertexX = modelB.verticesX;
        int vertexCountB = modelB.vertexCount;

        for (int vertexA = 0; vertexA < modelA.vertexCount; vertexA++) {
            Model.VertexNormal normalA = modelA.vertexNormal[vertexA];
            Model.VertexNormal originalNormalA = modelA.vertexNormalOriginal[vertexA];
            if (originalNormalA.w != 0) {
                int y = modelA.verticesY[vertexA] - offsetY;
                if (y > modelB.minY) {
                    continue;
                }

                int x = modelA.verticesX[vertexA] - offsetX;
                if (x < modelB.minX || x > modelB.maxX) {
                    continue;
                }

                int z = modelA.verticesZ[vertexA] - offsetZ;
                if (z < modelB.minZ || z > modelB.maxZ) {
                    continue;
                }

                for (int vertexB = 0; vertexB < vertexCountB; vertexB++) {
                    Model.VertexNormal normalB = modelB.vertexNormal[vertexB];
                    Model.VertexNormal originalNormalB = modelB.vertexNormalOriginal[vertexB];
                    if (x != vertexX[vertexB] || z != modelB.verticesZ[vertexB] || y != modelB.verticesY[vertexB] || originalNormalB.w == 0) {
                        continue;
                    }

                    normalA.x += originalNormalB.x;
                    normalA.y += originalNormalB.y;
                    normalA.z += originalNormalB.z;
                    normalA.w += originalNormalB.w;
                    normalB.x += originalNormalA.x;
                    normalB.y += originalNormalA.y;
                    normalB.z += originalNormalA.z;
                    normalB.w += originalNormalA.w;
                    merged++;
                    this.mergeIndexA[vertexA] = this.tmpMergeIndex;
                    this.mergeIndexB[vertexB] = this.tmpMergeIndex;
                }
            }
        }

        if (merged < 3 || !allowFaceRemoval) {
            return;
        }

        for (int i = 0; i < modelA.faceCount; i++) {
            if (this.mergeIndexA[modelA.faceIndicesA[i]] == this.tmpMergeIndex && this.mergeIndexA[modelA.faceIndicesB[i]] == this.tmpMergeIndex && this.mergeIndexA[modelA.faceIndicesC[i]] == this.tmpMergeIndex) {
                modelA.faceInfos[i] = -1;
            }
        }

        for (int i = 0; i < modelB.faceCount; i++) {
            if (this.mergeIndexB[modelB.faceIndicesA[i]] == this.tmpMergeIndex && this.mergeIndexB[modelB.faceIndicesB[i]] == this.tmpMergeIndex && this.mergeIndexB[modelB.faceIndicesC[i]] == this.tmpMergeIndex) {
                modelB.faceInfos[i] = -1;
            }
        }
    }

}
