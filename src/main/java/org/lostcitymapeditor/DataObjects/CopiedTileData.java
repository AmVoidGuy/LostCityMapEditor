package org.lostcitymapeditor.DataObjects;

import java.util.ArrayList;
import java.util.List;

public class CopiedTileData {
    public final int level;
    public final int x;
    public final int z;
    public final Integer underlayId; // Use Integer to allow null
    public final Integer overlayId;  // Use Integer to allow null
    public final int height;
    public final Integer flag;       // Use Integer to allow null
    public final Integer shape;      // Use Integer to allow null
    public final Integer rotation;   // Use Integer to allow null
    public final boolean perlin;

    public final List<LocData> locs;
    public final List<NpcData> npcs;
    public final List<ObjData> objs;

    public CopiedTileData(TileData tile, List<LocData> originalLocs, List<NpcData> originalNpcs, List<ObjData> originalObjs) {
        this.level = tile.level;
        this.x = tile.x;
        this.z = tile.z;
        this.underlayId = (tile.underlay != null) ? tile.underlay.id : null;
        this.overlayId = (tile.overlay != null) ? tile.overlay.id : null;
        this.height = tile.height;
        this.flag = tile.flag;
        this.shape = tile.shape;
        this.rotation = tile.rotation;
        this.perlin = tile.perlin;

        this.locs = new ArrayList<>();
        if (originalLocs != null) {
            for (LocData loc : originalLocs) {
                LocData newLoc = new LocData(loc.level, loc.x, loc.z, loc.id, loc.shape);
                newLoc.rotation = rotation;
                this.locs.add(newLoc);
            }
        }

        this.npcs = new ArrayList<>();
        if (originalNpcs != null) {
            for (NpcData npc : originalNpcs) {
                this.npcs.add(new NpcData(npc.level, npc.x, npc.z, npc.id));
            }
        }

        this.objs = new ArrayList<>();
        if (originalObjs != null) {
            for (ObjData obj : originalObjs) {
                this.objs.add(new ObjData(obj.level, obj.x, obj.z, obj.id, obj.count));
            }
        }
    }
}