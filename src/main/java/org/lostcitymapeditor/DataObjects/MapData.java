package org.lostcitymapeditor.DataObjects;

import java.util.ArrayList;
import java.util.List;

public class MapData {
    public List<TileData> mapTiles = new ArrayList<>();
    public List<LocData> locations = new ArrayList<>();
    public List<NpcData> npcs = new ArrayList<>();
    public List<ObjData> objects = new ArrayList<>();

    public MapData() {
    }

    @Override
    public String toString() {
        return "Jm2MapData{" +
                "mapTiles=" + mapTiles.size() +
                ", locations=" + locations.size() +
                ", npcs=" + npcs.size() +
                ", objects=" + objects.size() +
                '}';
    }
}