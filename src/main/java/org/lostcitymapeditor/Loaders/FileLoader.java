package org.lostcitymapeditor.Loaders;

import javafx.scene.image.Image;
import org.lostcitymapeditor.Transformers.FloFileTransformer;
import org.lostcitymapeditor.Transformers.OptFileTransformer;

import java.util.HashMap;
import java.util.Map;

import static org.lostcitymapeditor.Loaders.TileShapeLoader.loadShapeImages;
import static org.lostcitymapeditor.Transformers.PackFileTransformer.parseFloPack;
import static org.lostcitymapeditor.Transformers.PackFileTransformer.parseTexturePack;

public class FileLoader {

    private static Map<Integer, Image> shapeImages = new HashMap<>();
    private static Map<Integer, String> floMap;
    private static Map<Integer, String> textureMap;
    private static Map<String, Integer> underlayMap;
    private static Map<String, Object> overlayMap;
    private static Map<String, OptFileTransformer.TextureOptions> textureOptsMap;

    public static void loadFiles() {
        floMap = parseFloPack();
        textureMap = parseTexturePack();
        underlayMap = FloFileTransformer.parseUnderlayFlo();
        overlayMap = FloFileTransformer.parseOverlayFlo();
        textureOptsMap = OptFileTransformer.loadTextureOptions();
        loadShapeImages(shapeImages);
    }

    public static Map<Integer, Image> getShapeImages() {
        return shapeImages;
    }

    public static Map<Integer, String> getFloMap() {
        return floMap;
    }

    public static Map<Integer, String> getTextureMap() {
        return textureMap;
    }

    public static Map<String, Integer> getUnderlayMap() {
        return underlayMap;
    }

    public static Map<String, Object> getOverlayMap() {
        return overlayMap;
    }

    public static Map<String, OptFileTransformer.TextureOptions> getTextureOptsMap() {
        return textureOptsMap;
    }
}
