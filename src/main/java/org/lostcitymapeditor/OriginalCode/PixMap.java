package org.lostcitymapeditor.OriginalCode;

import java.awt.*;
import java.awt.image.*;

public class PixMap implements ImageProducer, ImageObserver {
    public final int[] pixels;
    private final int width;
    private final int height;
    private final ColorModel colorModel;
    private ImageConsumer imageConsumer;
    private final Image image;

    public PixMap(Component c, int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
        this.colorModel = new DirectColorModel(32, 0xff0000, 0xff00, 0xff);
        this.image = c.createImage(this);

        this.setPixels();
        c.prepareImage(this.image, this);

        this.setPixels();
        c.prepareImage(this.image, this);

        this.setPixels();
        c.prepareImage(this.image, this);

        this.bind();
    }

    public void bind() {
        Pix2D.bind(this.width, this.height, this.pixels);
    }

    public void draw(Graphics g, int width, int height) {
        this.setPixels();
        g.drawImage(this.image, width, height, this);
    }

    @Override
    public synchronized void addConsumer(ImageConsumer consumer) {
        this.imageConsumer = consumer;
        consumer.setDimensions(this.width, this.height);
        consumer.setProperties(null);
        consumer.setColorModel(this.colorModel);
        consumer.setHints(14);
    }

    @Override
    public synchronized boolean isConsumer(ImageConsumer consumer) {
        return this.imageConsumer == consumer;
    }

    @Override
    public synchronized void removeConsumer(ImageConsumer consumer) {
        if (this.imageConsumer == consumer) {
            this.imageConsumer = null;
        }
    }

    @Override
    public void startProduction(ImageConsumer consumer) {
        this.addConsumer(consumer);
    }

    @Override
    public void requestTopDownLeftRightResend(ImageConsumer consumer) {
        System.out.println("TDLR");
    }

    private synchronized void setPixels() {
        if (this.imageConsumer != null) {
            this.imageConsumer.setPixels(0, 0, this.width, this.height, this.colorModel, this.pixels, 0, this.width);
            this.imageConsumer.imageComplete(2);
        }
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        return true;
    }


    public BufferedImage getBufferedImage() {
        BufferedImage bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bimage.setRGB(0, 0, width, height, pixels, 0, width);
        return bimage;
    }
}