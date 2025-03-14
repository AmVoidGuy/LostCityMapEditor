package org.lostcitymapeditor.OriginalCode;

public class TileOverlay {
    public final int[] vertexX;
    public final int[] vertexY;
    public final int[] vertexZ;
    public final int[] triangleColorA;
    public final int[] triangleColorB;
    public final int[] triangleColorC;
    public final int[] triangleVertexA;
    public final int[] triangleVertexB;
    public final int[] triangleVertexC;
    public int[] triangleTextureIds;
    public boolean flat = true;
    public final int shape;
    public final int rotation;
    public final int backgroundRgb;
    public final int foregroundRgb;
    public static final int[] tmpScreenX = new int[6];
    public static final int[] tmpScreenY = new int[6];
    public static final int[] tmpViewspaceX = new int[6];
    public static final int[] tmpViewspaceY = new int[6];
    public static final int[] tmpViewspaceZ = new int[6];
    public static final int[][] SHAPE_POINTS = new int[][] {
            { 1, 3, 5, 7 },
            { 1, 3, 5, 7 }, // PLAIN_SHAPE
            { 1, 3, 5, 7 }, // DIAGONAL_SHAPE
            { 1, 3, 5, 7, 6 }, // LEFT_SEMI_DIAGONAL_SMALL_SHAPE
            { 1, 3, 5, 7, 6 }, // RIGHT_SEMI_DIAGONAL_SMALL_SHAPE
            { 1, 3, 5, 7, 6 }, // LEFT_SEMI_DIAGONAL_BIG_SHAPE
            { 1, 3, 5, 7, 6 }, // RIGHT_SEMI_DIAGONAL_BIG_SHAPE
            { 1, 3, 5, 7, 2, 6 }, // HALF_SQUARE_SHAPE
            { 1, 3, 5, 7, 2, 8 }, // CORNER_SMALL_SHAPE
            { 1, 3, 5, 7, 2, 8 }, // CORNER_BIG_SHAPE
            { 1, 3, 5, 7, 11, 12 }, // FAN_SMALL_SHAPE
            { 1, 3, 5, 7, 11, 12 }, // FAN_BIG_SHAPE
            { 1, 3, 5, 7, 13, 14 } // TRAPEZIUM_SHAPE
    };

    public static final int[][] SHAPE_PATHS = new int[][] {
            { 0, 1, 2, 3, 0, 0, 1, 3 },
            { 1, 1, 2, 3, 1, 0, 1, 3 }, // PLAIN_SHAPE
            { 0, 1, 2, 3, 1, 0, 1, 3 }, // DIAGONAL_SHAPE
            { 0, 0, 1, 2, 0, 0, 2, 4, 1, 0, 4, 3 }, // LEFT_SEMI_DIAGONAL_SMALL_SHAPE
            { 0, 0, 1, 4, 0, 0, 4, 3, 1, 1, 2, 4 }, // RIGHT_SEMI_DIAGONAL_SMALL_SHAPE
            { 0, 0, 4, 3, 1, 0, 1, 2, 1, 0, 2, 4 }, // LEFT_SEMI_DIAGONAL_BIG_SHAPE
            { 0, 1, 2, 4, 1, 0, 1, 4, 1, 0, 4, 3 }, // RIGHT_SEMI_DIAGONAL_BIG_SHAPE
            { 0, 4, 1, 2, 0, 4, 2, 5, 1, 0, 4, 5, 1, 0, 5, 3 }, // HALF_SQUARE_SHAPE
            { 0, 4, 1, 2, 0, 4, 2, 3, 0, 4, 3, 5, 1, 0, 4, 5 }, // CORNER_SMALL_SHAPE
            { 0, 0, 4, 5, 1, 4, 1, 2, 1, 4, 2, 3, 1, 4, 3, 5 }, // CORNER_BIG_SHAPE
            { 1, 0, 1, 5, 1, 1, 4, 5, 1, 1, 2, 4, 1, 0, 5, 3, 1, 5, 4, 3, 1, 4, 2, 3 },
            { 1, 0, 1, 5, 1, 1, 4, 5, 1, 1, 2, 4, 0, 0, 5, 3, 0, 5, 4, 3, 0, 4, 2, 3 }, // FAN_BIG_SHAPE
            { 1, 0, 5, 4, 1, 0, 1, 5, 0, 0, 4, 3, 0, 4, 5, 3, 0, 5, 2, 3, 0, 1, 2, 5 } // TRAPEZIUM_SHAPE
    };

    public TileOverlay( int tileX,  int shape,  int southeastColor2,  int southeastY,  int northeastColor1,  int rotation,  int southwestColor1,  int northwestY,  int foregroundRgb,  int southwestColor2,  int textureId,  int northwestColor2,  int backgroundRgb,  int northeastY,  int northeastColor2,  int northwestColor1,  int southwestY,  int tileZ,  int southeastColor1) {
        if (southwestY != southeastY || southwestY != northeastY || southwestY != northwestY) {
            this.flat = false;
        }

        this.shape = shape;
        this.rotation = rotation;
        this.backgroundRgb = backgroundRgb;
        this.foregroundRgb = foregroundRgb;

        short ONE = 128;
        int HALF = ONE / 2;
        int QUARTER = ONE / 4;
        int THREE_QUARTER = ONE * 3 / 4;

        int[] points = SHAPE_POINTS[shape];
        int vertexCount = points.length;
        this.vertexX = new int[vertexCount];
        this.vertexY = new int[vertexCount];
        this.vertexZ = new int[vertexCount];
        int[] primaryColors = new int[vertexCount];
        int[] secondaryColors = new int[vertexCount];

        int sceneX = tileX * ONE;
        int sceneZ = tileZ * ONE;

        for ( int v = 0; v < vertexCount; v++) {
            int type = points[v];

            if ((type & 0x1) == 0 && type <= 8) {
                type = (type - rotation - rotation - 1 & 0x7) + 1;
            }

            if (type > 8 && type <= 12) {
                type = (type - rotation - 9 & 0x3) + 9;
            }

            if (type > 12 && type <= 16) {
                type = (type - rotation - 13 & 0x3) + 13;
            }

            int x;
            int z;
            int y;
            int color1;
            int color2;

            if (type == 1) {
                x = sceneX;
                z = sceneZ;
                y = southwestY;
                color1 = southwestColor1;
                color2 = southwestColor2;
            } else if (type == 2) {
                x = sceneX + HALF;
                z = sceneZ;
                y = southwestY + southeastY >> 1;
                color1 = southwestColor1 + southeastColor1 >> 1;
                color2 = southwestColor2 + southeastColor2 >> 1;
            } else if (type == 3) {
                x = sceneX + ONE;
                z = sceneZ;
                y = southeastY;
                color1 = southeastColor1;
                color2 = southeastColor2;
            } else if (type == 4) {
                x = sceneX + ONE;
                z = sceneZ + HALF;
                y = southeastY + northeastY >> 1;
                color1 = southeastColor1 + northeastColor1 >> 1;
                color2 = southeastColor2 + northeastColor2 >> 1;
            } else if (type == 5) {
                x = sceneX + ONE;
                z = sceneZ + ONE;
                y = northeastY;
                color1 = northeastColor1;
                color2 = northeastColor2;
            } else if (type == 6) {
                x = sceneX + HALF;
                z = sceneZ + ONE;
                y = northeastY + northwestY >> 1;
                color1 = northeastColor1 + northwestColor1 >> 1;
                color2 = northeastColor2 + northwestColor2 >> 1;
            } else if (type == 7) {
                x = sceneX;
                z = sceneZ + ONE;
                y = northwestY;
                color1 = northwestColor1;
                color2 = northwestColor2;
            } else if (type == 8) {
                x = sceneX;
                z = sceneZ + HALF;
                y = northwestY + southwestY >> 1;
                color1 = northwestColor1 + southwestColor1 >> 1;
                color2 = northwestColor2 + southwestColor2 >> 1;
            } else if (type == 9) {
                x = sceneX + HALF;
                z = sceneZ + QUARTER;
                y = southwestY + southeastY >> 1;
                color1 = southwestColor1 + southeastColor1 >> 1;
                color2 = southwestColor2 + southeastColor2 >> 1;
            } else if (type == 10) {
                x = sceneX + THREE_QUARTER;
                z = sceneZ + HALF;
                y = southeastY + northeastY >> 1;
                color1 = southeastColor1 + northeastColor1 >> 1;
                color2 = southeastColor2 + northeastColor2 >> 1;
            } else if (type == 11) {
                x = sceneX + HALF;
                z = sceneZ + THREE_QUARTER;
                y = northeastY + northwestY >> 1;
                color1 = northeastColor1 + northwestColor1 >> 1;
                color2 = northeastColor2 + northwestColor2 >> 1;
            } else if (type == 12) {
                x = sceneX + QUARTER;
                z = sceneZ + HALF;
                y = northwestY + southwestY >> 1;
                color1 = northwestColor1 + southwestColor1 >> 1;
                color2 = northwestColor2 + southwestColor2 >> 1;
            } else if (type == 13) {
                x = sceneX + QUARTER;
                z = sceneZ + QUARTER;
                y = southwestY;
                color1 = southwestColor1;
                color2 = southwestColor2;
            } else if (type == 14) {
                x = sceneX + THREE_QUARTER;
                z = sceneZ + QUARTER;
                y = southeastY;
                color1 = southeastColor1;
                color2 = southeastColor2;
            } else if (type == 15) {
                x = sceneX + THREE_QUARTER;
                z = sceneZ + THREE_QUARTER;
                y = northeastY;
                color1 = northeastColor1;
                color2 = northeastColor2;
            } else {
                x = sceneX + QUARTER;
                z = sceneZ + THREE_QUARTER;
                y = northwestY;
                color1 = northwestColor1;
                color2 = northwestColor2;
            }

            this.vertexX[v] = x;
            this.vertexY[v] = y;
            this.vertexZ[v] = z;
            primaryColors[v] = color1;
            secondaryColors[v] = color2;
        }

        int[] paths = SHAPE_PATHS[shape];
        int triangleCount = paths.length / 4;
        this.triangleVertexA = new int[triangleCount];
        this.triangleVertexB = new int[triangleCount];
        this.triangleVertexC = new int[triangleCount];
        this.triangleColorA = new int[triangleCount];
        this.triangleColorB = new int[triangleCount];
        this.triangleColorC = new int[triangleCount];

        if (textureId != -1) {
            this.triangleTextureIds = new int[triangleCount];
        }

        int index = 0;
        for (int t = 0; t < triangleCount; t++) {
            int color = paths[index];
            int a = paths[index + 1];
            int b = paths[index + 2];
            int c = paths[index + 3];
            index += 4;

            if (a < 4) {
                a = a - rotation & 0x3;
            }

            if (b < 4) {
                b = b - rotation & 0x3;
            }

            if (c < 4) {
                c = c - rotation & 0x3;
            }

            this.triangleVertexA[t] = a;
            this.triangleVertexB[t] = b;
            this.triangleVertexC[t] = c;

            if (color == 0) {
                this.triangleColorA[t] = primaryColors[a];
                this.triangleColorB[t] = primaryColors[b];
                this.triangleColorC[t] = primaryColors[c];

                if (this.triangleTextureIds != null) {
                    this.triangleTextureIds[t] = -1;
                }
            } else {
                this.triangleColorA[t] = secondaryColors[a];
                this.triangleColorB[t] = secondaryColors[b];
                this.triangleColorC[t] = secondaryColors[c];

                if (this.triangleTextureIds != null) {
                    this.triangleTextureIds[t] = textureId;
                }
            }
        }
    }
}
