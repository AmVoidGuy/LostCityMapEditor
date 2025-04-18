package org.lostcitymapeditor.DataObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // Import for stream operations

public class MapData {
    private static final int LEVELS = 4;
    private static final int SIZE_X = 64;
    private static final int SIZE_Z = 64;

    public TileData[][][] mapTiles = new TileData[LEVELS][SIZE_X][SIZE_Z];
    public List<LocData> locations = new ArrayList<>();
    public List<NpcData> npcs = new ArrayList<>();
    public List<ObjData> objects = new ArrayList<>();

    public MapData() {
    }

    public MapData deepCopy() {
        MapData copy = new MapData();

        copy.mapTiles = new TileData[LEVELS][SIZE_X][SIZE_Z];
        for (int level = 0; level < LEVELS; level++) {
            for (int x = 0; x < SIZE_X; x++) {
                for (int z = 0; z < SIZE_Z; z++) {
                    if (this.mapTiles[level][x][z] != null) {
                        copy.mapTiles[level][x][z] = new TileData(this.mapTiles[level][x][z]);
                    } else {
                        copy.mapTiles[level][x][z] = null;
                    }
                }
            }
        }

        copy.locations = this.locations.stream()
                .map(LocData::new)
                .collect(Collectors.toCollection(ArrayList::new));

        copy.npcs = this.npcs.stream()
                .map(NpcData::new)
                .collect(Collectors.toCollection(ArrayList::new));

        copy.objects = this.objects.stream()
                .map(ObjData::new)
                .collect(Collectors.toCollection(ArrayList::new));

        return copy;
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

    public synchronized List<NpcData> getNpcData(int level, int x, int z) {
        List<NpcData> results = new ArrayList<>();
        for (NpcData npc : npcs) {
            if (npc.level == level && npc.x == x && npc.z == z) {
                results.add(npc);
            }
        }
        return results;
    }

    public synchronized void removeNpcData(int level, int x, int z) {
        npcs.removeIf(npc -> npc.level == level && npc.x == x && npc.z == z);
    }

    public synchronized List<ObjData> getObjData(int level, int x, int z) {
        List<ObjData> results = new ArrayList<>();
        for (ObjData obj : objects) {
            if (obj.level == level && obj.x == x && obj.z == z) {
                results.add(obj);
            }
        }
        return results;
    }

    public synchronized void removeObjData(int level, int x, int z) {
        objects.removeIf(obj -> obj.level == level && obj.x == x && obj.z == z);
    }
}