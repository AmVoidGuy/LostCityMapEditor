package org.lostcitymapeditor.DataObjects;

public class LocData {
    public int level;
    public int x;
    public int z;
    public int id;
    public int shape;
    public Integer rotation;

    public LocData(int level, int x, int z, int id, int shape) {
        this.level = level;
        this.x = x;
        this.z = z;
        this.id = id;
        this.shape = shape;
    }
    @Override
    public String toString() {
        return "LocData{" +
                "level=" + level +
                ", x=" + x +
                ", z=" + z +
                ", id=" + id +
                ", shape=" + shape +
                ", rotation=" + rotation +
                '}';
    }
}