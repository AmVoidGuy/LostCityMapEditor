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
}
