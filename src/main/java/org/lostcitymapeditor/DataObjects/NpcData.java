package org.lostcitymapeditor.DataObjects;

public class NpcData {
    public int id;

    public NpcData(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "NpcData{" +
                ", id=" + id +
                '}';
    }
}