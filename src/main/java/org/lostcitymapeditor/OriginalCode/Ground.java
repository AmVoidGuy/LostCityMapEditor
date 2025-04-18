package org.lostcitymapeditor.OriginalCode;

import org.lostcitymapeditor.DataObjects.GroundObject;
import org.lostcitymapeditor.DataObjects.Npc;

public class Ground extends Linkable {
    public int level;
    public final int x;
    public final int z;
    public final int occludeLevel;
    public TileUnderlay underlay;
    public TileOverlay overlay;
    public Wall wall;
    public Decor decor;
    public GroundDecor groundDecor;
    public Npc npc;
    public GroundObject groundObj;
    public int locCount;
    public final Location[] locs = new Location[5];
    public final int[] locSpan = new int[5];
    public int locSpans;
    public Ground bridge;

    public Ground( int level,  int x,  int z) {
        this.occludeLevel = this.level = level;
        this.x = x;
        this.z = z;
    }
}
