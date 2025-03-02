package org.lostcitymapeditor.Transformers;

import org.lostcitymapeditor.DataObjects.UnderlayData;
import org.lostcitymapeditor.LostCityMapEditor;

import java.util.Map;

public class UnderlayDataTransformer {

    public static void findColor(UnderlayData data) {
        Map<Integer, String> floMap = LostCityMapEditor.getFloMap();
        Map<String, Integer> underlayMap = LostCityMapEditor.getUnderlayMap();
        Map<String, Object> overlayMap = LostCityMapEditor.getOverlayMap();
        String floName = floMap.get(data.id);
        data.color = underlayMap.get(floName);
        if (data.color == null) {
            Object overlayValue = overlayMap.get(floName);
            if (overlayValue instanceof Map) {
                Map<String, Object> overlayData = (Map<String, Object>) overlayValue;
                if (overlayData.containsKey("rgb")) {
                    Object rgbValue = overlayData.get("rgb");
                    if (rgbValue instanceof Integer) {
                        data.color = (Integer) rgbValue;
                    }
                }
            }
        }
    }

    public static void calculateHsl(UnderlayData data) {
        double var2 = (double) (data.color >> 16 & 255) / 256.0;
        double var4 = (double) (data.color >> 8 & 255) / 256.0;
        double var6 = (double) (data.color & 255) / 256.0;
        double var8 = var2;

        if (var4 < var2) {
            var8 = var4;
        }
        if (var6 < var8) {
            var8 = var6;
        }

        double var10 = var2;
        if (var4 > var2) {
            var10 = var4;
        }
        if (var6 > var10) {
            var10 = var6;
        }

        double var12 = 0.0;
        double var14 = 0.0;
        double var16 = (var10 + var8) / 2.0;

        if (var8 != var10) {
            if (var16 < 0.5) {
                var14 = (var10 - var8) / (var8 + var10);
            }
            if (var16 >= 0.5) {
                var14 = (var10 - var8) / (2.0 - var10 - var8);
            }

            if (var2 == var10) {
                var12 = (var4 - var6) / (var10 - var8);
            } else if (var10 == var4) {
                var12 = 2.0 + (var6 - var2) / (var10 - var8);
            } else if (var10 == var6) {
                var12 = 4.0 + (var2 - var4) / (var10 - var8);
            }
        }

        var12 /= 6.0;

        data.saturation = (int) (var14 * 256.0);
        data.lightness = (int) (var16 * 256.0);

        if (data.saturation < 0) {
            data.saturation = 0;
        } else if (data.saturation > 255) {
            data.saturation = 255;
        }

        if (data.lightness < 0) {
            data.lightness = 0;
        } else if (data.lightness > 255) {
            data.lightness = 255;
        }

        if (var16 > 0.5) {
            data.hueMultiplier = (int) (var14 * (1.0 - var16) * 512.0);
        } else {
            data.hueMultiplier = (int) (var14 * var16 * 512.0);
        }

        if (data.hueMultiplier < 1) {
            data.hueMultiplier = 1;
        }

        data.hue = (int) (data.hueMultiplier * var12);
    }
}
