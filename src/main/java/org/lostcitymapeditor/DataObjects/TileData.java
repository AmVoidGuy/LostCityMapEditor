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

    public TileData(TileData other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy a null TileData object.");
        }
        this.level = other.level;
        this.x = other.x;
        this.z = other.z;
        this.height = other.height;
        this.shape = other.shape;
        this.rotation = other.rotation;
        this.flag = other.flag;
        this.perlin = other.perlin;
        this.overlay = (other.overlay == null) ? null : new OverlayData(other.overlay);
        this.underlay = (other.underlay == null) ? null : new UnderlayData(other.underlay);
    }
}
