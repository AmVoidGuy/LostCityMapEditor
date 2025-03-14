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
}