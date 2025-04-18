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

    public NpcData(NpcData other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy a null NpcData object.");
        }
        this.level = other.level;
        this.x = other.x;
        this.z = other.z;
        this.id = other.id;
    }
}