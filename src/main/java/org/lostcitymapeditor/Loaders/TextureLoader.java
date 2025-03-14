package org.lostcitymapeditor.Loaders;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javafx.scene.image.Image;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class TextureLoader {
    public static Map<Integer, Integer> textureIdMap = new HashMap<>();
    private static final String TEXTURES_DIRECTORY = "Data/Textures/";

    public static int loadTexture(int id) {
        String textureName = "Not found";
        try {
            textureName = FileLoader.getTextureMap().get(id);
            String fileName = TEXTURES_DIRECTORY + textureName + (textureName.endsWith(".png") ? "" : ".png");

            BufferedImage image = loadImageFromResource(fileName);
            if (image == null) {
                System.err.println("Failed to load image: " + fileName);
                return -1;
            }

            int width = image.getWidth();
            int height = image.getHeight();

            // Convert magenta pixels to transparent during buffer creation
            ByteBuffer buffer = convertImageToByteBufferWithMagentaTransparency(image);

            int textureId = GL11.glGenTextures();

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height,
                    0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
            glBindVertexArray(0);
            glBindTexture(GL_TEXTURE_2D, 0);
            textureIdMap.put(id, textureId);
            return textureId;

        } catch (Exception e) {
            System.err.println("Error loading texture: " + textureName + " - " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    private static BufferedImage loadImageFromResource(String resourcePath) {
        try (InputStream in = TextureLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                System.err.println("Could not find resource: " + resourcePath);
                return null;
            }
            return ImageIO.read(in);
        } catch (IOException e) {
            System.err.println("Error loading image from resource: " + resourcePath + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static ByteBuffer convertImageToByteBufferWithMagentaTransparency(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                if (red == 255 && green == 0 && blue == 255) {
                    buffer.put((byte) 0);
                    buffer.put((byte) 0);
                    buffer.put((byte) 0);
                    buffer.put((byte) 0);
                } else {
                    buffer.put((byte) red);
                    buffer.put((byte) green);
                    buffer.put((byte) blue);
                    buffer.put((byte) ((pixel >> 24) & 0xFF));
                }
            }
        }
        buffer.flip();
        return buffer;
    }

    public static Image loadTextureImage(String textureName) {
        try (InputStream in = TextureLoader.class.getClassLoader().getResourceAsStream(TEXTURES_DIRECTORY + textureName + (textureName.endsWith(".png") ? "" : ".png"))) {
            if (in == null) {
                System.err.println("Could not find texture: " + textureName);
                return null;
            }
            return new Image(in);
        } catch (IOException e) {
            System.err.println("Error loading texture: " + textureName + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}