package org.lostcitymapeditor.DataObjects;

import org.lostcitymapeditor.OriginalCode.Model;

public class GroundObject {
    public int height;
    public Model model;

    public GroundObject(int height, Model model) {
        this.height = height;
        this.model = model;
    }
}