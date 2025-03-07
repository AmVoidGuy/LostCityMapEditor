package org.lostcitymapeditor.OriginalCode;

import org.lostcitymapeditor.Loaders.FileLoader;

import java.util.Map;

public class FloType {
    private static FloType[] instances = null;
    public int rgb;
    public int texture = -1;
    private boolean overlay = false;
    public boolean occlude = true;
    public String name;
    public int hue;
    public int saturation;
    public int lightness;
    public int chroma;
    public int luminance;
    public int hsl;

    private FloType() {}


    public static FloType[] getInstances() {
        if (instances == null) {
            int size = FileLoader.getFloMap().size();
            instances = new FloType[size];
            for ( int id = 0; id < size; id++) {
                instances[id] = new FloType();
                instances[id].addData(id);
            }
        }
        return instances;
    }

    public void addData(int id) {
        Map<String, Integer> underlayMap = FileLoader.getUnderlayMap();
        Map<String, Object> overlayMap = FileLoader.getOverlayMap();
        Map<Integer, String> floMap = FileLoader.getFloMap();
        Map<Integer, String> textureMap = FileLoader.getTextureMap();
        String name = floMap.get(id);
        if (overlayMap.get(name) != null) {
            int rgb = 0;
            int texture = -1;
            boolean occlude = true;
            Map<String, Object> overlayData = (Map<String, Object>) overlayMap.get(name);
            if(overlayData.containsKey("rgb")) {
                rgb = (Integer) overlayData.get("rgb");
            }
            if(overlayData.containsKey("texture")) {
                String textureName = (String) overlayData.get("texture");
                for (Map.Entry<Integer, String> entry : textureMap.entrySet()) {
                    if (entry.getValue().equals(textureName)) {
                        texture = entry.getKey();
                        break;
                    }
                }
            }
            this.updateValues(rgb, texture, occlude, name);
        } else {
            this.updateValues(underlayMap.get(name), -1, true, name);
        }
    }

    public void updateValues(int rgb, int texture, boolean occlude, String name) {
        this.rgb = rgb;
        this.setColor(this.rgb);
        this.texture = texture;
        this.overlay = true;
        this.occlude = occlude;
        this.name = name;
    }

    private void setColor( int rgb) {
        double red = (double) (rgb >> 16 & 0xFF) / 256.0D;
        double green = (double) (rgb >> 8 & 0xFF) / 256.0D;
        double blue = (double) (rgb & 0xFF) / 256.0D;

        double min = red;
        if (green < red) {
            min = green;
        }
        if (blue < min) {
            min = blue;
        }

        double max = red;
        if (green > red) {
            max = green;
        }
        if (blue > max) {
            max = blue;
        }

        double h = 0.0D;
        double s = 0.0D;
        double l = (min + max) / 2.0D;

        if (min != max) {
            if (l < 0.5D) {
                s = (max - min) / (max + min);
            }
            if (l >= 0.5D) {
                s = (max - min) / (2.0D - max - min);
            }

            if (red == max) {
                h = (green - blue) / (max - min);
            } else if (green == max) {
                h = (blue - red) / (max - min) + 2.0D;
            } else if (blue == max) {
                h = (red - green) / (max - min) + 4.0D;
            }
        }

        h /= 6.0D;

        this.hue = (int) (h * 256.0D);
        this.saturation = (int) (s * 256.0D);
        this.lightness = (int) (l * 256.0D);

        if (this.saturation < 0) {
            this.saturation = 0;
        } else if (this.saturation > 255) {
            this.saturation = 255;
        }

        if (this.lightness < 0) {
            this.lightness = 0;
        } else if (this.lightness > 255) {
            this.lightness = 255;
        }

        if (l > 0.5D) {
            this.luminance = (int) ((1.0D - l) * s * 512.0D);
        } else {
            this.luminance = (int) (l * s * 512.0D);
        }

        if (this.luminance < 1) {
            this.luminance = 1;
        }

        this.chroma = (int) (h * (double) this.luminance);

        int hue = this.hue + (int) (Math.random() * 16.0D) - 8;
        if (hue < 0) {
            hue = 0;
        } else if (hue > 255) {
            hue = 255;
        }

        int saturation = this.saturation + (int) (Math.random() * 48.0D) - 24;
        if (saturation < 0) {
            saturation = 0;
        } else if (saturation > 255) {
            saturation = 255;
        }

        int lightness = this.lightness + (int) (Math.random() * 48.0D) - 24;
        if (lightness < 0) {
            lightness = 0;
        } else if (lightness > 255) {
            lightness = 255;
        }

        this.hsl = this.hsl24to16(hue, saturation, lightness);
    }

    private int hsl24to16( int hue,  int saturation,  int lightness) {
        if (lightness > 179) {
            saturation /= 2;
        }

        if (lightness > 192) {
            saturation /= 2;
        }

        if (lightness > 217) {
            saturation /= 2;
        }

        if (lightness > 243) {
            saturation /= 2;
        }

        return (hue / 4 << 10) + (saturation / 32 << 7) + lightness / 2;
    }
}