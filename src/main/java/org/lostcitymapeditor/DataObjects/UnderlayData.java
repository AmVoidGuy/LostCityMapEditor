package org.lostcitymapeditor.DataObjects;

public class UnderlayData {
    public int id = 0;

    public UnderlayData(int id) {
        this.id = id;
    }

    public UnderlayData(UnderlayData other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy a null UnderlayData object.");
        }
        this.id = other.id;
    }
}