package org.lostcitymapeditor.DataObjects;

public class ObjData {
    public int level;
    public int x;
    public int z;
    public int id;
    public int count;

    public ObjData(int level, int x, int z, int id, int count) {
        this.level = level;
        this.x = x;
        this.z = z;
        this.id = id;
        this.count = count;
    }
    @Override
    public String toString() {
        return "ObjData{" +
                "level=" + level +
                ", x=" + x +
                ", z=" + z +
                ", id=" + id +
                ", count=" + count +
                '}';
    }
}
