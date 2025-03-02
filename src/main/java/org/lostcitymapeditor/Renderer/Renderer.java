package org.lostcitymapeditor.Renderer;

import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.lostcitymapeditor.Loaders.TextureLoader;
import org.lostcitymapeditor.Transformers.TileDataTransformer;
import org.lostcitymapeditor.DataObjects.MapData;
import org.lostcitymapeditor.DataObjects.OverlayData;
import org.lostcitymapeditor.DataObjects.TileData;
import org.lostcitymapeditor.DataObjects.Triangle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Renderer {

    private final Group root3D;
    private final Map<TileData, MeshView> tileMeshMap;
    private final CheckBox overlayCheckbox;
    private final CheckBox underlayCheckbox;
    private final int regionSize;
    private final Map<String, Image> loadedTextures = new HashMap<>();

    public Renderer(Group root3D, Map<TileData, MeshView> tileMeshMap, CheckBox overlayCheckbox, CheckBox underlayCheckbox, int regionSize) {
        this.root3D = root3D;
        this.tileMeshMap = tileMeshMap;
        this.overlayCheckbox = overlayCheckbox;
        this.underlayCheckbox = underlayCheckbox;
        this.regionSize = regionSize;
    }

    public void renderMap(MapData mapData, int currentLevel) {
        if (mapData == null) return;

        root3D.getChildren().clear();
        tileMeshMap.clear();

        Color ambientColor = Color.color(1, 1, 1, 1.0);
        AmbientLight ambientLight = new AmbientLight(ambientColor);
        root3D.getChildren().add(ambientLight);

        List<TileData> tileList = mapData.mapTiles;

        for (TileData tile : tileList) {
            if (tile.level == currentLevel) {
                createOrUpdateTileMesh(tile);
            }
        }
        addGridOutline();
    }

    public void createOrUpdateTileMesh(TileData tile) {
        if (tileMeshMap.containsKey(tile)) {
            MeshView meshView = tileMeshMap.get(tile);
            root3D.getChildren().remove(meshView);
            MeshView newMeshView = createTileMesh(tile);
            tileMeshMap.put(tile, newMeshView);
            root3D.getChildren().add(newMeshView);
        } else {
            MeshView meshView = createTileMesh(tile);
            tileMeshMap.put(tile, meshView);
            root3D.getChildren().add(meshView);
        }
    }

    private MeshView createTileMesh(TileData tile) {
        int x = tile.x;
        int z = tile.z;
        int[] cornerheights = tile.cornerHeights;
        Triangle[] triangles = Triangle.generateTriangles(x, cornerheights, z);
        TriangleMesh mesh = new TriangleMesh();

        float[] points = new float[triangles.length * 9];
        float[] texCoords = new float[triangles.length * 6];
        int[] faces = new int[triangles.length * 6];

        for (int i = 0; i < triangles.length; i++) {
            Triangle triangle = triangles[i];

            points[i * 9 + 0] = (float) triangle.v1.getX();
            points[i * 9 + 1] = (float) triangle.v1.getY();
            points[i * 9 + 2] = (float) triangle.v1.getZ();
            texCoords[i * 6 + 0] = 0.0f;
            texCoords[i * 6 + 1] = 0.0f;

            points[i * 9 + 3] = (float) triangle.v2.getX();
            points[i * 9 + 4] = (float) triangle.v2.getY();
            points[i * 9 + 5] = (float) triangle.v2.getZ();
            texCoords[i * 6 + 2] = 1.0f;
            texCoords[i * 6 + 3] = 0.0f;

            points[i * 9 + 6] = (float) triangle.v3.getX();
            points[i * 9 + 7] = (float) triangle.v3.getY();
            points[i * 9 + 8] = (float) triangle.v3.getZ();
            texCoords[i * 6 + 4] = 0.0f;
            texCoords[i * 6 + 5] = 1.0f;

            faces[i * 6 + 0] = i * 3 + 0;
            faces[i * 6 + 1] = i * 3 + 0;
            faces[i * 6 + 2] = i * 3 + 1;
            faces[i * 6 + 3] = i * 3 + 1;
            faces[i * 6 + 4] = i * 3 + 2;
            faces[i * 6 + 5] = i * 3 + 2;
        }

        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(texCoords);
        mesh.getFaces().addAll(faces);

        MeshView meshView = new MeshView(mesh);
        meshView.setUserData(tile);

//        double centerX = tile.x + 0.5;
//        double centerZ = tile.z + 0.5;
//
////        if(tile.rotation != null) {
////            //Rotate rotate = new Rotate(tile.rotation * 90, centerX, 0, centerZ, Rotate.Y_AXIS);
////            //eshView.getTransforms().add(rotate);
////        }
        updateTileMesh(meshView, tile);

        return meshView;
    }

    public void updateTileMesh(MeshView meshView, TileData tile) {
        Integer rgbColor = null;
        String textureName = null;

        if (overlayCheckbox.isSelected() && tile.overlay != null) {
            OverlayData overlayData = tile.overlay;
            if (overlayData.texture != null) {
                textureName = overlayData.texture;
            } else if (overlayData.color != null) {
                rgbColor = overlayData.color;
            }
        } else if (underlayCheckbox.isSelected() && tile.underlay != null && tile.underlay.color != null) {
            rgbColor = tile.underlay.color;
            textureName = null;
        } else {
            rgbColor = null;
            textureName = null;
        }

        Color tileColor = (rgbColor != null) ? Color.rgb((rgbColor >> 16) & 0xFF, (rgbColor >> 8) & 0xFF, rgbColor & 0xFF) : Color.BLACK;
        PhongMaterial material = new PhongMaterial();
        if (textureName != null) {
            Image texture = loadedTextures.computeIfAbsent(textureName, this::loadTexture);
            if (texture != null) {
                material.setDiffuseMap(texture);
            } else {
                material.setDiffuseColor(tileColor);
            }
        } else {
            material.setDiffuseColor(tileColor);
        }

        meshView.setMaterial(material);
    }

    private Image loadTexture(String textureName) {
        return TextureLoader.loadTexture(textureName);
    }

    private void addGridOutline() {
        double gridSize = regionSize;
        double outlineThickness = 0.5;
        double outlineHeight = 0.5;
        double tileHeightOffset = 0.1;
        Color outlineColor = Color.WHITE;
        PhongMaterial outlineMaterial = new PhongMaterial(outlineColor);

        Box horizontalLine = new Box(gridSize, outlineHeight, outlineThickness);
        horizontalLine.setTranslateX(gridSize / 2);
        horizontalLine.setTranslateY(outlineHeight / 2 + tileHeightOffset);
        horizontalLine.setTranslateZ(0);
        horizontalLine.setMaterial(outlineMaterial);

        Box verticalLine = new Box(outlineThickness, outlineHeight, gridSize);
        verticalLine.setTranslateX(0);
        verticalLine.setTranslateY(outlineHeight / 2 + tileHeightOffset);
        verticalLine.setTranslateZ(gridSize / 2);
        verticalLine.setMaterial(outlineMaterial);

        Box horizontalLine2 = new Box(gridSize, outlineHeight, outlineThickness);
        horizontalLine2.setTranslateX(gridSize / 2);
        horizontalLine2.setTranslateY(outlineHeight / 2 + tileHeightOffset);
        horizontalLine2.setTranslateZ(gridSize);
        horizontalLine2.setMaterial(outlineMaterial);

        Box verticalLine2 = new Box(outlineThickness, outlineHeight, gridSize);
        verticalLine2.setTranslateX(gridSize);
        verticalLine2.setTranslateY(outlineHeight / 2 + tileHeightOffset);
        verticalLine2.setTranslateZ(gridSize / 2);
        verticalLine2.setMaterial(outlineMaterial);

        Box plane = new Box(gridSize, 0.01, gridSize);
        plane.setMaterial(new PhongMaterial(Color.TRANSPARENT));
        plane.setTranslateX(gridSize / 2);
        plane.setTranslateY(tileHeightOffset);
        plane.setTranslateZ(gridSize / 2);
        root3D.getChildren().addAll(horizontalLine, verticalLine, horizontalLine2, verticalLine2, plane);
    }

    public void updateSurroundingTiles(TileData tile, MapData currentMapData) {
        int x = tile.x;
        int z = tile.z;
        int level = tile.level;

        TileData northTile = TileDataTransformer.findTile(currentMapData.mapTiles, x, z - 1, level);
        TileData eastTile = TileDataTransformer.findTile(currentMapData.mapTiles, x + 1, z, level);
        TileData southTile = TileDataTransformer.findTile(currentMapData.mapTiles, x, z + 1, level);
        TileData westTile = TileDataTransformer.findTile(currentMapData.mapTiles, x - 1, z, level);
        TileData northWestTile = TileDataTransformer.findTile(currentMapData.mapTiles, x - 1, z - 1, level);
        TileData northEastTile = TileDataTransformer.findTile(currentMapData.mapTiles, x + 1, z - 1, level);
        TileData southEastTile = TileDataTransformer.findTile(currentMapData.mapTiles, x + 1, z + 1, level);
        TileData southWestTile = TileDataTransformer.findTile(currentMapData.mapTiles, x - 1, z + 1, level);

        if (northTile != null) createOrUpdateTileMesh(northTile);
        if (eastTile != null) createOrUpdateTileMesh(eastTile);
        if (southTile != null) createOrUpdateTileMesh(southTile);
        if (westTile != null) createOrUpdateTileMesh(westTile);
        if (northTile != null) createOrUpdateTileMesh(northEastTile);
        if (eastTile != null) createOrUpdateTileMesh(southEastTile);
        if (southTile != null) createOrUpdateTileMesh(southWestTile);
        if (westTile != null) createOrUpdateTileMesh(northWestTile);
    }
}