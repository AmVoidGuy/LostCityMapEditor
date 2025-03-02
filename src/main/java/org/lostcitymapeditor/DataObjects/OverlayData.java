package org.lostcitymapeditor.DataObjects;


import org.lostcitymapeditor.Transformers.OverlayDataTransformer;

public class OverlayData {
    public int id = 0;
    public Integer color = 0;
    public String texture = null;
    public boolean occlude = true;

    public transient int hue = 0;
    public transient int saturation = 0;
    public transient int lightness = 0;
    public transient int hueMultiplier = 0;

    public OverlayData(int id) {
        this.id = id;
        OverlayDataTransformer.setOcclude(this);
        OverlayDataTransformer.findColorOrTexture(this);
        OverlayDataTransformer.calculateHsl(this);
    }
}
