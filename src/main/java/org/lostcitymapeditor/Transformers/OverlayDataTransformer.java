package org.lostcitymapeditor.Transformers;

import org.lostcitymapeditor.DataObjects.OverlayData;
import org.lostcitymapeditor.Loaders.FileLoader;

import java.util.Map;

public class OverlayDataTransformer {

    public static void setOcclude(OverlayData data) {
        Map<Integer, String> floMap = FileLoader.getFloMap();
        Map<String, Object> overlayMap = FileLoader.getOverlayMap();
        String floName = floMap.get(data.id);
        Object overlayValue = overlayMap.get(floName);
            if (overlayValue instanceof Map) {
                Map<String, Object> overlayData = (Map<String, Object>) overlayValue;
                if (overlayData.containsKey("occlude")) {
                    Object occludeValue = overlayData.get("occlude");
                    if (occludeValue instanceof Boolean) {
                        data.occlude = (Boolean) occludeValue;
                    }
                }
            }
    }

    public static void findColorOrTexture(OverlayData data) {
        Map<Integer, String> floMap = FileLoader.getFloMap();
        Map<String, Integer> underlayMap = FileLoader.getUnderlayMap();
        Map<String, Object> overlayMap = FileLoader.getOverlayMap();
        String floName = floMap.get(data.id);
        data.color = underlayMap.get(floName);
        if (data.color == null) {
            data.color = 0;
            Object overlayValue = overlayMap.get(floName);
            if (overlayValue instanceof Map) {
                Map<String, Object> overlayData = (Map<String, Object>) overlayValue;
                if (overlayData.containsKey("rgb")) {
                    Object rgbValue = overlayData.get("rgb");
                    if (rgbValue instanceof Integer) {
                        data.color = (Integer) rgbValue;
                    }
                } else if (overlayData.containsKey("texture")) {
                    Object textureValue = overlayData.get("texture");
                    if (textureValue instanceof String) {
                        data.texture = (String) textureValue + ".png";
                    }
                }
            }
        }
    }

}
