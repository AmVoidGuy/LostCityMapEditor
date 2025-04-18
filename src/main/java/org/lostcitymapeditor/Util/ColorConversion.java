package org.lostcitymapeditor.Util;

public class ColorConversion {
    public static final int[] RGB15_HSL16 = new int[32768];

    static {
        for (int rgb = 0; rgb < 32768; rgb++) {
            RGB15_HSL16[rgb] = rgb15toHsl16(rgb);
        }
    }

    public static int rgb15toHsl16(int rgb) {
        int r = (rgb >> 10) & 0x1f;
        int g = (rgb >> 5) & 0x1f;
        int b = rgb & 0x1f;

        double red = r / 31.0;
        double green = g / 31.0;
        double blue = b / 31.0;

        return rgbToHsl(red, green, blue);
    }

    /**
     * Converts RGB values (each in range 0.0-1.0) to a 16-bit HSL value.
     *
     * @param red The red component (0.0-1.0)
     * @param green The green component (0.0-1.0)
     * @param blue The blue component (0.0-1.0)
     * @return The resulting 16-bit HSL value
     */
    public static int rgbToHsl(double red, double green, double blue) {
        double min = red;
        if (green < min) {
            min = green;
        }
        if (blue < min) {
            min = blue;
        }

        double max = red;
        if (green > max) {
            max = green;
        }
        if (blue > max) {
            max = blue;
        }

        double hNorm = 0.0;
        double sNorm = 0.0;
        double lNorm = (min + max) / 2.0;

        if (min != max) {
            if (lNorm < 0.5) {
                sNorm = (max - min) / (max + min);
            } else if (lNorm >= 0.5) {
                sNorm = (max - min) / (2.0 - max - min);
            }

            if (red == max) {
                hNorm = (green - blue) / (max - min);
            } else if (green == max) {
                hNorm = (blue - red) / (max - min) + 2.0;
            } else if (blue == max) {
                hNorm = (red - green) / (max - min) + 4.0;
            }
        }

        hNorm /= 6.0;

        int hue = (int)(hNorm * 256.0);
        int saturation = (int)(sNorm * 256.0);
        int lightness = (int)(lNorm * 256.0);

        if (saturation < 0) {
            saturation = 0;
        } else if (saturation > 255) {
            saturation = 255;
        }

        if (lightness < 0) {
            lightness = 0;
        } else if (lightness > 255) {
            lightness = 255;
        }

        return hsl24to16(hue, saturation, lightness);
    }

    /**
     * Converts 24-bit HSL components to a 16-bit HSL value.
     *
     * @param hue The hue component (0-255)
     * @param saturation The saturation component (0-255)
     * @param lightness The lightness component (0-255)
     * @return The resulting 16-bit HSL value
     */
    public static int hsl24to16(int hue, int saturation, int lightness) {
        if (lightness > 243) {
            saturation >>= 4;
        } else if (lightness > 217) {
            saturation >>= 3;
        } else if (lightness > 192) {
            saturation >>= 2;
        } else if (lightness > 179) {
            saturation >>= 1;
        }

        return (((hue & 0xff) >> 2) << 10) + ((saturation >> 5) << 7) + (lightness >> 1);
    }

    /**
     * Finds all RGB15 values that map to the specified HSL16 value.
     *
     * @param hsl The HSL16 value to find matches for
     * @return An array of RGB15 values that map to the given HSL16 value
     */
    public static int[] reverseHsl(int hsl) {
        int count = 0;
        for (int rgb = 0; rgb < 32768; rgb++) {
            if (RGB15_HSL16[rgb] == hsl) {
                count++;
            }
        }

        int[] possible = new int[count];

        int index = 0;
        for (int rgb = 0; rgb < 32768; rgb++) {
            if (RGB15_HSL16[rgb] == hsl) {
                possible[index++] = rgb;
            }
        }

        return possible;
    }
}