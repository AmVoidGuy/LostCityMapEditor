package org.lostcitymapeditor.DataObjects;

import javafx.geometry.Point3D;

public class Triangle {
    public Point3D v1;
    public Point3D v2;
    public Point3D v3;

    public Triangle(Point3D v1, Point3D v2, Point3D v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public static Triangle[] generateTriangles(int x, int[] cornerHeights, int z) {
        // Create vertices with correct positions and heights
        Point3D nwVertex = new Point3D(x, -cornerHeights[0], z);
        Point3D neVertex = new Point3D(x + 1, -cornerHeights[1], z);
        Point3D seVertex = new Point3D(x + 1, -cornerHeights[2], z + 1);
        Point3D swVertex = new Point3D(x, -cornerHeights[3], z + 1);

        // Form triangles that properly represent the tile
        Triangle t1 = new Triangle(nwVertex, neVertex, swVertex);
        Triangle t2 = new Triangle(neVertex, seVertex, swVertex);

        return new Triangle[] {t1, t2};
    }
}