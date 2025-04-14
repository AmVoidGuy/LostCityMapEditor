package org.lostcitymapeditor.OriginalCode;

import org.lostcitymapeditor.Loaders.FileLoader;
import org.lostcitymapeditor.Transformers.OptFileTransformer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Pix8 extends Pix2D {

    public byte[] pixels;
    public int[] palette = null;
    public int width;
    public int height;
    public int cropX;
    public int cropY;
    public int cropW;
    private int cropH;

    public Pix8(String name, String filePath) {
        Map<String, OptFileTransformer.TextureOptions> textureOptionsMap = FileLoader.getTextureOptsMap();
        OptFileTransformer.TextureOptions textureOptions = textureOptionsMap.get(name);
        BufferedImage image = null;
        try {
            File imageFile = new File(filePath + "/textures/" + name + ".png");

            if (!imageFile.exists()) {
                System.err.println("Error loading image: " + filePath + " (file not found)");
                return;
            }
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
            System.err.println("Error loading image: " + filePath);
            e.printStackTrace();
            return;
        }

        this.cropW = image.getWidth();
        this.cropH = image.getHeight();

        this.palette = generatePalette(image);

        this.cropX = textureOptions.cropX();
        this.cropY = textureOptions.cropY();
        this.width = textureOptions.width();
        this.height = textureOptions.width();

        int pixelOrder = textureOptions.pixelOrder();
        int len = this.width * this.height;
        this.pixels = new byte[len];
        loadPixelData(image, pixelOrder);
    }

    private int[] generatePalette(BufferedImage image) {
        List<Integer> colorList = new ArrayList<>();
        colorList.add(0xFF00FF);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y) & 0xFFFFFF;
                if (rgb == 0xFF00FF) {
                    continue;
                }
                if (!colorList.contains(rgb)) {
                    colorList.add(rgb);
                }
            }
        }

        int[] palette = new int[colorList.size()];
        for (int i = 0; i < colorList.size(); i++) {
            palette[i] = colorList.get(i);
        }
        return palette;
    }

    private void loadPixelData(BufferedImage image, int pixelOrder) {
        int len = this.width * this.height;

        if (pixelOrder == 0) {
            for (int i = 0; i < len; i++) {
                int x = i % this.width;
                int y = i / this.width;

                int imageX = cropX + x;
                int imageY = cropY + y;
                if (imageX < 0 || imageX >= image.getWidth() || imageY < 0 || imageY >= image.getHeight()) {
                    pixels[i] = 0;
                    continue;
                }

                int rgb = image.getRGB(imageX, imageY) & 0xFFFFFF;
                byte index = findPaletteIndex(rgb);
                pixels[i] = index;
            }
        } else if (pixelOrder == 1) {
            for (int x = 0; x < this.width; x++) {
                for (int y = 0; y < this.height; y++) {
                    int imageX = cropX + x;
                    int imageY = cropY + y;
                    if (imageX < 0 || imageX >= image.getWidth() || imageY < 0 || imageY >= image.getHeight()) {
                        pixels[x + y * this.width] = 0;
                        continue;
                    }

                    int rgb = image.getRGB(imageX, imageY) & 0xFFFFFF;
                    byte index = findPaletteIndex(rgb);
                    pixels[x + y * this.width] = index;
                }
            }
        }
    }

    private byte findPaletteIndex(int rgb) {
        for (byte i = 0; i < palette.length; i++) {
            if (palette[i] == rgb) {
                return i;
            }
        }
        return 0;
    }

    public void shrink() {
        this.cropW /= 2;
        this.cropH /= 2;

        byte[] pixels = new byte[this.cropW * this.cropH];
        int off = 0;
        for ( int y = 0; y < this.height; y++) {
            for ( int x = 0; x < this.width; x++) {
                pixels[(x + this.cropX >> 1) + (y + this.cropY >> 1) * this.cropW] = this.pixels[off++];
            }
        }

        this.pixels = pixels;
        this.width = this.cropW;
        this.height = this.cropH;
        this.cropX = 0;
        this.cropY = 0;
    }

    public void crop() {
        if (this.width == this.cropW && this.height == this.cropH) {
            return;
        }

        byte[] pixels = new byte[this.cropW * this.cropH];
        int off = 0;
        for ( int y = 0; y < this.height; y++) {
            for ( int x = 0; x < this.width; x++) {
                pixels[x + this.cropX + (y + this.cropY) * this.cropW] = this.pixels[off++];
            }
        }

        this.pixels = pixels;
        this.width = this.cropW;
        this.height = this.cropH;
        this.cropX = 0;
        this.cropY = 0;
    }

    public void flipHorizontally() {
        byte[] pixels = new byte[this.width * this.height];
        int off = 0;
        for ( int y = 0; y < this.height; y++) {
            for ( int x = this.width - 1; x >= 0; x--) {
                pixels[off++] = this.pixels[x + y * this.width];
            }
        }

        this.pixels = pixels;
        this.cropX = this.cropW - this.width - this.cropX;
    }

    public void flipVertically() {
        byte[] pixels = new byte[this.width * this.height];
        int off = 0;
        for ( int y = this.height - 1; y >= 0; y--) {
            for ( int x = 0; x < this.width; x++) {
                pixels[off++] = this.pixels[x + y * this.width];
            }
        }

        this.pixels = pixels;
        this.cropY = this.cropH - this.height - this.cropY;
    }

    public void translate( int r,  int g,  int b) {
        for ( int i = 0; i < this.palette.length; i++) {
            int red = this.palette[i] >> 16 & 0xFF;
            red += r;
            if (red < 0) {
                red = 0;
            } else if (red > 255) {
                red = 255;
            }

            int green = this.palette[i] >> 8 & 0xFF;
            green += g;
            if (green < 0) {
                green = 0;
            } else if (green > 255) {
                green = 255;
            }

            int blue = this.palette[i] & 0xFF;
            blue += b;
            if (blue < 0) {
                blue = 0;
            } else if (blue > 255) {
                blue = 255;
            }

            this.palette[i] = (red << 16) + (green << 8) + blue;
        }
    }

    public void draw( int x,  int y) {
        x += this.cropX;
        y += this.cropY;

        int dstOff = x + y * Pix2D.width2d;
        int srcOff = 0;
        int h = this.height;
        int w = this.width;
        int dstStep = Pix2D.width2d - w;
        int srcStep = 0;

        if (y < Pix2D.boundTop) {
            int cutoff = Pix2D.boundTop - y;
            h -= cutoff;
            y = Pix2D.boundTop;
            srcOff += cutoff * w;
            dstOff += cutoff * Pix2D.width2d;
        }

        if (y + h > Pix2D.boundBottom) {
            h -= y + h - Pix2D.boundBottom;
        }

        if (x < Pix2D.boundLeft) {
            int cutoff = Pix2D.boundLeft - x;
            w -= cutoff;
            x = Pix2D.boundLeft;
            srcOff += cutoff;
            dstOff += cutoff;
            srcStep += cutoff;
            dstStep += cutoff;
        }

        if (x + w > Pix2D.boundRight) {
            int cutoff = x + w - Pix2D.boundRight;
            w -= cutoff;
            srcStep += cutoff;
            dstStep += cutoff;
        }

        if (w > 0 && h > 0) {
            this.copyPixels(w, h, this.pixels, srcOff, srcStep, Pix2D.data, dstOff, dstStep, this.palette);
        }
    }

    private void copyPixels( int w,  int h,  byte[] src,  int srcOff,  int srcStep,  int[] dst,  int dstOff,  int dstStep,  int[] palette) {
        int qw = -(w >> 2);
        w = -(w & 0x3);

        for ( int y = -h; y < 0; y++) {
            for ( int x = qw; x < 0; x++) {
                byte palIndex = src[srcOff++];
                if (palIndex == 0) {
                    dstOff++;
                } else {
                    dst[dstOff++] = palette[palIndex & 0xFF];
                }

                palIndex = src[srcOff++];
                if (palIndex == 0) {
                    dstOff++;
                } else {
                    dst[dstOff++] = palette[palIndex & 0xFF];
                }

                palIndex = src[srcOff++];
                if (palIndex == 0) {
                    dstOff++;
                } else {
                    dst[dstOff++] = palette[palIndex & 0xFF];
                }

                palIndex = src[srcOff++];
                if (palIndex == 0) {
                    dstOff++;
                } else {
                    dst[dstOff++] = palette[palIndex & 0xFF];
                }
            }

            for ( int x = w; x < 0; x++) {
                byte palIndex = src[srcOff++];
                if (palIndex == 0) {
                    dstOff++;
                } else {
                    dst[dstOff++] = palette[palIndex & 0xFF];
                }
            }

            dstOff += dstStep;
            srcOff += srcStep;
        }
    }

    public void clip( int arg0,  int arg1,  int arg2,  int arg3) {
        try {
            int local2 = this.width;
            int local5 = this.height;
            int local7 = 0;
            int local9 = 0;
            int local15 = (local2 << 16) / arg2;
            int local21 = (local5 << 16) / arg3;
            int local24 = this.cropW;
            int local27 = this.cropH;
            int local33 = (local24 << 16) / arg2;
            int local39 = (local27 << 16) / arg3;
            arg0 += (this.cropX * arg2 + local24 - 1) / local24;
            arg1 += (this.cropY * arg3 + local27 - 1) / local27;
            if (this.cropX * arg2 % local24 != 0) {
                local7 = (local24 - this.cropX * arg2 % local24 << 16) / arg2;
            }
            if (this.cropY * arg3 % local27 != 0) {
                local9 = (local27 - this.cropY * arg3 % local27 << 16) / arg3;
            }
            arg2 = arg2 * (this.width - (local7 >> 16)) / local24;
            arg3 = arg3 * (this.height - (local9 >> 16)) / local27;
            int local133 = arg0 + arg1 * Pix2D.width2d;
            int local137 = Pix2D.width2d - arg2;
            int local144;
            if (arg1 < Pix2D.boundTop) {
                local144 = Pix2D.boundTop - arg1;
                arg3 -= local144;
                arg1 = 0;
                local133 += local144 * Pix2D.width2d;
                local9 += local39 * local144;
            }
            if (arg1 + arg3 > Pix2D.boundBottom) {
                arg3 -= arg1 + arg3 - Pix2D.boundBottom;
            }
            if (arg0 < Pix2D.boundLeft) {
                local144 = Pix2D.boundLeft - arg0;
                arg2 -= local144;
                arg0 = 0;
                local133 += local144;
                local7 += local33 * local144;
                local137 += local144;
            }
            if (arg0 + arg2 > Pix2D.boundRight) {
                local144 = arg0 + arg2 - Pix2D.boundRight;
                arg2 -= local144;
                local137 += local144;
            }
            this.plot_scale(Pix2D.data, this.pixels, this.palette, local7, local9, local133, local137, arg2, arg3, local33, local39, local2);
        } catch ( Exception local239) {
            System.out.println("error in sprite clipping routine");
        }
    }

    private void plot_scale( int[] arg0,  byte[] arg1,  int[] arg2,  int arg3,  int arg4,  int arg5,  int arg6,  int arg7,  int arg8,  int arg9,  int arg10,  int arg11) {
        try {
            int local3 = arg3;
            for ( int local6 = -arg8; local6 < 0; local6++) {
                int local14 = (arg4 >> 16) * arg11;
                for ( int local17 = -arg7; local17 < 0; local17++) {
                    byte local27 = arg1[(arg3 >> 16) + local14];
                    if (local27 == 0) {
                        arg5++;
                    } else {
                        arg0[arg5++] = arg2[local27 & 0xFF];
                    }
                    arg3 += arg9;
                }
                arg4 += arg10;
                arg3 = local3;
                arg5 += arg6;
            }
        } catch ( Exception local63) {
            System.out.println("error in plot_scale");
        }
    }
}
