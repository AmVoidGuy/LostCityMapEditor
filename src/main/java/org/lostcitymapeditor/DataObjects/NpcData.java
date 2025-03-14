package org.lostcitymapeditor.DataObjects;

public class NpcData {
    public int id;
    public int level;
    public int x;
    public int z;

    public NpcData(int level, int x, int z, int id) {
        this.level = level;
        this.x = x;
        this.z = z;
        this.id = id;
    }
}