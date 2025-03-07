package org.lostcitymapeditor.OriginalCode;

public class TileUnderlay {
    public final int southwestColor;
    public final int southeastColor;
    public final int northeastColor;
    public final int northwestColor;
    public final int textureId;
    public boolean flat = true;
    public final int rgb;

    public TileUnderlay( int southwestColor,  int southeastColor,  int northeastColor,  int northwestColor,  int textureId,  int rgb,  boolean flat) {
        this.southwestColor = southwestColor;
        this.southeastColor = southeastColor;
        this.northeastColor = northeastColor;
        this.northwestColor = northwestColor;
        this.textureId = textureId;
        this.rgb = rgb;
        this.flat = flat;
    }
}
