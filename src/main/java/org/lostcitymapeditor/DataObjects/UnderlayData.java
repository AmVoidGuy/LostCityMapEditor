package org.lostcitymapeditor.DataObjects;

import org.lostcitymapeditor.Transformers.UnderlayDataTransformer;

public class UnderlayData {
    public int id = 0;
    public Integer color = 0;

    public transient int hue = 0;
    public transient int saturation = 0;
    public transient int lightness = 0;
    public transient int hueMultiplier = 0;

    public UnderlayData(int id) {
        this.id = id;
        UnderlayDataTransformer.findColor(this);
        UnderlayDataTransformer.calculateHsl(this);
    }
}