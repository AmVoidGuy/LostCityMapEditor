package org.lostcitymapeditor.DataObjects;

public class ObjData {
    public int id;
    public int count;

    public ObjData(int id, int count) {
        this.id = id;
        this.count = count;
    }
    @Override
    public String toString() {
        return "ObjData{" +
                ", id=" + id +
                ", count=" + count +
                '}';
    }
}
