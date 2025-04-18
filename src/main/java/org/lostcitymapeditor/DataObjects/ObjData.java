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

    public ObjData(ObjData other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy a null ObjData object.");
        }
        this.level = other.level;
        this.x = other.x;
        this.z = other.z;
        this.id = other.id;
        this.count = other.count;
    }

    @Override
    public String toString() {
        return "ObjData{" +
                ", id=" + id +
                ", count=" + count +
                '}';
    }
}
