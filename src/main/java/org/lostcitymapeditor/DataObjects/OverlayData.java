package org.lostcitymapeditor.DataObjects;


import org.lostcitymapeditor.Transformers.OverlayDataTransformer;

public class OverlayData {
    public int id = 0;
    public Integer color = 0;
    public String texture = null;
    public boolean occlude = true;

    public OverlayData(int id) {
        this.id = id;
        OverlayDataTransformer.setOcclude(this);
        OverlayDataTransformer.findColorOrTexture(this);
    }

    public OverlayData(OverlayData other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy a null OverlayData object.");
        }
        this.id = other.id;
        this.color = other.color;
        this.texture = other.texture;
        this.occlude = other.occlude;
    }
}
