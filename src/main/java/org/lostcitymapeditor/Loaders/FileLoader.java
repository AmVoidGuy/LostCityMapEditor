package org.lostcitymapeditor.Loaders;

import javafx.scene.image.Image;
import org.lostcitymapeditor.OriginalCode.Model;
import org.lostcitymapeditor.Transformers.FloFileTransformer;
import org.lostcitymapeditor.Transformers.LocFileTransformer;
import org.lostcitymapeditor.Transformers.Ob2FileTransformer;
import org.lostcitymapeditor.Transformers.OptFileTransformer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.lostcitymapeditor.Loaders.TileShapeLoader.loadShapeImages;
import static org.lostcitymapeditor.Transformers.PackFileTransformer.*;

public class FileLoader {

    private static Map<Integer, Image> shapeImages = new HashMap<>();
    private static Map<Integer, String> floMap;
    private static Map<Integer, String> textureMap;
    private static Map<Integer, String> locMap;
    private static Map<String, Integer> modelMap;
    private static Map<String, Integer> underlayMap;
    private static Map<String, Object> overlayMap;
    private static Map<String, Object> allLocMap;
    private static Map<String, OptFileTransformer.TextureOptions> textureOptsMap;
    private static Map<Integer, Model> modelOb2Map;

    public static void loadFiles(String path) throws IOException {
        floMap = parseFloPack(path);
        textureMap = parseTexturePack(path);
        locMap = parseLocPack(path);
        modelMap = parseModelPack(path);
        underlayMap = FloFileTransformer.parseUnderlayFlo(path);
        overlayMap = FloFileTransformer.parseOverlayFlo(path);
        textureOptsMap = OptFileTransformer.loadTextureOptions(path);
        allLocMap = LocFileTransformer.parseAllLocFiles(path);
        modelOb2Map = Ob2FileTransformer.parseOb2Files(path);
        loadShapeImages(shapeImages);
    }

    public static Map<Integer, Image> getShapeImages() {
        return shapeImages;
    }

    public static Map<Integer, String> getFloMap() {
        return floMap;
    }

    public static Map<Integer, String> getLocMap() {
        return locMap;
    }

    public static Map<Integer, String> getTextureMap() {
        return textureMap;
    }

    public static Map<String, Integer> getModelMap() {
        return modelMap;
    }

    public static Map<String, Integer> getUnderlayMap() {
        return underlayMap;
    }

    public static Map<String, Object> getOverlayMap() {
        return overlayMap;
    }

    public static Map<String, Object> getAllLocMap() {
        return allLocMap;
    }

    public static Map<Integer, Model> getModelOb2Map() { return modelOb2Map; }

    public static Map<String, OptFileTransformer.TextureOptions> getTextureOptsMap() {
        return textureOptsMap;
    }
}
