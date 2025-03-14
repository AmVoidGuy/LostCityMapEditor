package org.lostcitymapeditor.OriginalCode;

import org.lostcitymapeditor.Loaders.FileLoader;
import java.util.Map;


public class Pix3D extends Pix2D {

    public static int centerW3D;

    public static int centerH3D;

    public static int[] divTable = new int[512];

    public static int[] divTable2 = new int[2048];

    public static int[] sinTable = new int[2048];

    public static int[] cosTable = new int[2048];

    public static int[] lineOffset;

    private static int textureCount;

    public static Pix8[] textures = new Pix8[50];

    private static boolean[] textureTranslucent = new boolean[50];

    private static int[] averageTextureRGB = new int[50];

    private static int poolSize;

    private static int[][] texelPool;

    private static int[][] activeTexels = new int[50][];

    public static int[] textureCycle = new int[50];

    public static int cycle;

    public static int[] colourTable = new int[65536];

    private static int[][] texturePalette = new int[50][];

    static {
        for (int i = 1; i < 512; i++) {
            divTable[i] = 32768 / i;
        }

        for (int i = 1; i < 2048; i++) {
            divTable2[i] = 65536 / i;
        }

        for (int i = 0; i < 2048; i++) {
            sinTable[i] = (int) (Math.sin((double) i * 0.0030679615D) * 65536.0D);
            cosTable[i] = (int) (Math.cos((double) i * 0.0030679615D) * 65536.0D);
        }
    }

    public static void init3D( int width,  int height) {
        lineOffset = new int[height];
        for ( int y = 0; y < height; y++) {
            lineOffset[y] = width * y;
        }
        centerW3D = width / 2;
        centerH3D = height / 2;
    }

    public static void initPool( int size) {
        if (texelPool != null) {
            return;
        }
        poolSize = size;
            texelPool = new int[poolSize][65536];
        for (int i = 0; i < 50; i++) {
            activeTexels[i] = null;
        }
    }

    public static void loadTextures() {
        textureCount = 0;
        Map<Integer, String> textureMap = FileLoader.getTextureMap();
        for ( int id = 0; id < textureMap.size(); id++) {
            try {
                String textureName = textureMap.get(id);
                textures[id] = new Pix8(String.valueOf(textureName));
                    textures[id].crop();
                textureCount++;
            } catch ( Exception ex) {
            }
        }
    }

    public static int getAverageTextureRGB( int id) {
        if (averageTextureRGB[id] != 0) {
            return averageTextureRGB[id];
        }

        int r = 0;
        int g = 0;
        int b = 0;
        int length = texturePalette[id].length;
        for ( int i = 0; i < length; i++) {
            r += (texturePalette[id][i] >> 16) & 0xFF;
            g += (texturePalette[id][i] >> 8) & 0xFF;
            b += texturePalette[id][i] & 0xFF;
        }

        int rgb = ((r / length) << 16) + ((g / length) << 8) + (b / length);
        rgb = setGamma(rgb, 1.4D);
        if (rgb == 0) {
            rgb = 1;
        }
        averageTextureRGB[id] = rgb;
        return rgb;
    }

    public static void pushTexture( int id) {
        if (activeTexels[id] != null) {
            texelPool[poolSize++] = activeTexels[id];
            activeTexels[id] = null;
        }
    }

    private static int[] getTexels( int id) {
        textureCycle[id] = cycle++;
        if (activeTexels[id] != null) {
            return activeTexels[id];
        }

        int[] texels;
        if (poolSize > 0) {
            texels = texelPool[--poolSize];
            texelPool[poolSize] = null;
        } else {
            int cycle = 0;
            int selected = -1;
            for (int t = 0; t < textureCount; t++) {
                if (activeTexels[t] != null && (textureCycle[t] < cycle || selected == -1)) {
                    cycle = textureCycle[t];
                    selected = t;
                }
            }
            texels = activeTexels[selected];
            activeTexels[selected] = null;
        }

        activeTexels[id] = texels;
        Pix8 texture = textures[id];
        int[] palette = texturePalette[id];

            if (texture.width == 64) {
                for (int y = 0; y < 128; y++) {
                    for (int x = 0; x < 128; x++) {
                        texels[x + (y << 7)] = palette[texture.pixels[(x >> 1) + (y >> 1 << 6)]];
                    }
                }
            } else {
                for (int i = 0; i < 16384; i++) {
                    texels[i] = palette[texture.pixels[i]];
                }
            }

            textureTranslucent[id] = false;
            for (int i = 0; i < 0x4000; i++) {
                texels[i] &= 0xF8F8FF;

                int rgb = texels[i];
                if (rgb == 0) {
                    textureTranslucent[id] = true;
                }

                texels[i + 0x4000] = (rgb - (rgb >>> 3)) & 0xF8F8FF;
                texels[i + 0x8000] = (rgb - (rgb >>> 2)) & 0xF8F8FF;
                texels[i + 0xc000] = (rgb - (rgb >>> 2) - (rgb >>> 3)) & 0xF8F8FF;
            }
        return texels;
    }

    public static void setBrightness( double brightness) {
        double randomized = brightness + Math.random() * 0.03D - 0.015D;

        int offset = 0;
        for ( int y = 0; y < 512; y++) {
            double hue = (double) (y / 8) / 64.0D + 0.0078125D;
            double saturation = (double) (y & 0x7) / 8.0D + 0.0625D;

            for ( int x = 0; x < 128; x++) {
                double lightness = (double) x / 128.0D;
                double r = lightness;
                double g = lightness;
                double b = lightness;

                if (saturation != 0.0D) {
                    double q;
                    if (lightness < 0.5D) {
                        q = lightness * (saturation + 1.0D);
                    } else {
                        q = lightness + saturation - lightness * saturation;
                    }

                    double p = lightness * 2.0D - q;

                    double t = hue + 0.3333333333333333D;
                    if (t > 1.0D) {
                        t--;
                    }

                    double d11 = hue - 0.3333333333333333D;
                    if (d11 < 0.0D) {
                        d11++;
                    }

                    if (t * 6.0D < 1.0D) {
                        r = p + (q - p) * 6.0D * t;
                    } else if (t * 2.0D < 1.0D) {
                        r = q;
                    } else if (t * 3.0D < 2.0D) {
                        r = p + (q - p) * (0.6666666666666666D - t) * 6.0D;
                    } else {
                        r = p;
                    }

                    if (hue * 6.0D < 1.0D) {
                        g = p + (q - p) * 6.0D * hue;
                    } else if (hue * 2.0D < 1.0D) {
                        g = q;
                    } else if (hue * 3.0D < 2.0D) {
                        g = p + (q - p) * (0.6666666666666666D - hue) * 6.0D;
                    } else {
                        g = p;
                    }

                    if (d11 * 6.0D < 1.0D) {
                        b = p + (q - p) * 6.0D * d11;
                    } else if (d11 * 2.0D < 1.0D) {
                        b = q;
                    } else if (d11 * 3.0D < 2.0D) {
                        b = p + (q - p) * (0.6666666666666666D - d11) * 6.0D;
                    } else {
                        b = p;
                    }
                }

                int intR = (int) (r * 256.0D);
                int intG = (int) (g * 256.0D);
                int intB = (int) (b * 256.0D);
                int rgb = (intR << 16) + (intG << 8) + intB;
                int rgbAdjusted = setGamma(rgb, randomized);
                colourTable[offset++] = rgbAdjusted;
            }
        }

        for ( int id = 0; id < 50; id++) {
            if (textures[id] != null) {
                int[] palette = textures[id].palette;
                texturePalette[id] = new int[palette.length];

                for ( int i = 0; i < palette.length; i++) {
                    texturePalette[id][i] = setGamma(palette[i], randomized);
                }
            }
        }

        for ( int id = 0; id < 50; id++) {
            pushTexture(id);
        }
    }

    private static int setGamma( int rgb,  double gamma) {
        double r = (double) ((rgb >> 16) & 0xFF) / 256.0D;
        double g = (double) ((rgb >> 8) & 0xFF) / 256.0D;
        double b = (double) (rgb & 0xFF) / 256.0D;

        double powR = Math.pow(r, gamma);
        double powG = Math.pow(g, gamma);
        double powB = Math.pow(b, gamma);

        int intR = (int) (powR * 256.0D);
        int intG = (int) (powG * 256.0D);
        int intB = (int) (powB * 256.0D);
        return (intR << 16) + (intG << 8) + intB;
    }
}
