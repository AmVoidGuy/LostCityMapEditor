package org.lostcitymapeditor.Transformers;

import org.lostcitymapeditor.DataObjects.MapData;
import org.lostcitymapeditor.DataObjects.TileData;

import java.util.List;

public class TileDataTransformer {

    public static void calculateCornerHeights(MapData mapData) {
        List<TileData> tileList = mapData.mapTiles;

        for (TileData tile : tileList) {
            for (int i = 0; i < 4; i++) {
                tile.cornerHeights[i] = tile.height / 20;
            }
        }

        for (TileData tile : tileList) {
            int x = tile.x;
            int z = tile.z;
            int level = tile.level;

            TileData northTile = findTile(tileList, x, z - 1, level);
            TileData eastTile = findTile(tileList, x + 1, z, level);
            TileData southTile = findTile(tileList, x, z + 1, level);
            TileData westTile = findTile(tileList, x - 1, z, level);
            TileData northWestTile = findTile(tileList, x - 1, z - 1, level);
            TileData northEastTile = findTile(tileList, x + 1, z - 1, level);
            TileData southEastTile = findTile(tileList, x + 1, z + 1, level);
            TileData southWestTile = findTile(tileList, x - 1, z + 1, level);

            int nwHeight = tile.cornerHeights[0];
            if (northTile != null) nwHeight = Math.max(nwHeight, northTile.height / 20);
            if (westTile != null) nwHeight = Math.max(nwHeight, westTile.height / 20);
            if (northWestTile != null) nwHeight = Math.max(nwHeight, northWestTile.height / 20);
            tile.cornerHeights[0] = nwHeight;

            int neHeight = tile.cornerHeights[1];
            if (northTile != null) neHeight = Math.max(neHeight, northTile.height / 20);
            if (eastTile != null) neHeight = Math.max(neHeight, eastTile.height / 20);
            if (northEastTile != null) neHeight = Math.max(neHeight, northEastTile.height / 20);
            tile.cornerHeights[1] = neHeight;

            int seHeight =  tile.cornerHeights[2];
            if (southTile != null) seHeight = Math.max(seHeight, southTile.height / 20);
            if (eastTile != null) seHeight = Math.max(seHeight, eastTile.height / 20);
            if (southEastTile != null) seHeight = Math.max(seHeight, southEastTile.height / 20);
            tile.cornerHeights[2] = seHeight;

            int swHeight = tile.cornerHeights[3];
            if (southTile != null) swHeight = Math.max(swHeight, southTile.height / 20);
            if (westTile != null) swHeight = Math.max(swHeight, westTile.height / 20);
            if (southWestTile != null) swHeight = Math.max(swHeight, southWestTile.height / 20);
            tile.cornerHeights[3] = swHeight;
        }
    }

    public static TileData findTile(List<TileData> tileList, int x, int z, int level) {
        for (TileData tile : tileList) {
            if (tile.x == x && tile.z == z && tile.level == level) {
                return tile;
            }
        }
        return null;
    }
}