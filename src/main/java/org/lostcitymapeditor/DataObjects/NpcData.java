package org.lostcitymapeditor.DataObjects;

public class NpcData {
    public int level;
    public int x;
    public int z;
    public int id;

    public NpcData(int level, int x, int z, int id) {
        this.level = level;
        this.x = x;
        this.z = z;
        this.id = id;
    }

    @Override
    public String toString() {
        return "NpcData{" +
                "level=" + level +
                ", x=" + x +
                ", z=" + z +
                ", id=" + id +
                '}';
    }
}