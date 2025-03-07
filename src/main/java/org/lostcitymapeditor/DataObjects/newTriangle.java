package org.lostcitymapeditor.DataObjects;

import java.util.ArrayList;
import java.util.List;

public class newTriangle {
    public float[] vertices;
    public int[] colors;
    int tileX;
    int tileZ;
    int level;
    int shape;
    int rotation;
    public int textureId;
    float[] textureCoordinates;
    static List<newTriangle> trianglesToRender = new ArrayList<>();
    public newTriangle.TileDatas tileData;

    public static void addTriangle(int x, int z, int level, int shape, int rotation, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int color1, int color2, int color3, int textureId, float[] textureCoordinates) {
            if(color1 == 12345678)
                return;
            newTriangle triangle = new newTriangle();
            triangle.tileX = x;
            triangle.tileZ = z;
            triangle.level = level;
            triangle.shape = shape;
            triangle.rotation = rotation;
            triangle.vertices = new float[]{x1, -y1, z1, x2, -y2, z2, x3, -y3, z3};
            triangle.colors = new int[]{color1, color2, color3};
            triangle.textureId = textureId;
            triangle.textureCoordinates = textureCoordinates;
            trianglesToRender.add(triangle);
            triangle.tileData = new TileDatas(x, z, level, shape, rotation);
    }

    public static List<newTriangle> getTriangles() {
        return trianglesToRender;
    }

    public static synchronized void clearCollectedTriangles() {
        trianglesToRender.clear();
    }

    public static class TileDatas {
        public int tileX;
        public int tileZ;
        public int level;
        public int shape;
        public int rotation;

        public TileDatas(int tileX, int tileZ, int level, int shape, int rotation) {
            this.tileX = tileX;
            this.tileZ = tileZ;
            this.level = level;
            this.shape = shape;
            this.rotation = rotation;
        }
    }
}