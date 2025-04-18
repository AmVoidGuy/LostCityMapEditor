package org.lostcitymapeditor.DataObjects;

import org.lostcitymapeditor.OriginalCode.Model;

public class Npc {
    public int height;
    public int size;
    public Model model;

    public Npc(int height, Model model, int size) {
        this.height = height;
        this.model = model;
        this.size = size;
    }
}