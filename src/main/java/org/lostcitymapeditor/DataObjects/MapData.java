package org.lostcitymapeditor.DataObjects;

import java.util.ArrayList;
import java.util.List;

public class MapData {
    public TileData[][][] mapTiles = new TileData[4][64][64];
    public List<LocData> locations = new ArrayList<>();
    public List<NpcData> npcs = new ArrayList<>();
    public ObjData[][][] objects = new ObjData[4][64][64];

    public MapData() {
    }

    public synchronized List<LocData> getLocData(int level, int x, int z) {
        List<LocData> results = new ArrayList<>();
        for (LocData loc : locations) {
            if (loc.level == level && loc.x == x && loc.z == z) {
                results.add(loc);
            }
        }
        return results;
    }

    public synchronized void removeLocData(int level, int x, int z) {
        locations.removeIf(loc -> loc.level == level && loc.x == x && loc.z == z);
    }

}