package org.lostcitymapeditor.OriginalCode;

public class Ground extends Linkable {
    public int level;
    public final int x;
    public final int z;
    public final int occludeLevel;
    public TileUnderlay underlay;
    public TileOverlay overlay;
    //public Wall wall;
    //public Decor decor;
    //public GroundDecor groundDecor;
    //public GroundObject groundObj;
    public int locCount;
    public final Location[] locs = new Location[5];
    public final int[] locSpan = new int[5];
    public int locSpans;
    public int drawLevel;
    public boolean visible;
    public boolean update;
    public boolean containsLocs;
    public int checkLocSpans;
    public int blockLocSpans;
    public int inverseBlockLocSpans;
    public int backWallTypes;
    public Ground bridge;

    public Ground( int level,  int x,  int z) {
        this.occludeLevel = this.level = level;
        this.x = x;
        this.z = z;
    }
}
