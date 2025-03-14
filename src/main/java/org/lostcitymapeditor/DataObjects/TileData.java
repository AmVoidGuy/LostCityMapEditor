package org.lostcitymapeditor.DataObjects;

public class TileData {
    public int level;
    public int x;
    public int z;
    public Integer height = 0;
    public OverlayData overlay;
    public Integer shape;
    public Integer rotation;
    public Integer flag;
    public UnderlayData underlay;
    public boolean perlin = false;

    public TileData(int level, int x, int z) {
        this.level = level;
        this.x = x;
        this.z = z;
    }
}
