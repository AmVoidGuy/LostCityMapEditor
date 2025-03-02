package org.lostcitymapeditor.Loaders;

import javafx.scene.image.Image;
import java.io.File;

public class TextureLoader {
    private static final String TEXTURES_DIRECTORY = "Data/Textures/";

    public static Image loadTexture(String textureName) {
        try {
            File textureFile = new File(TEXTURES_DIRECTORY + textureName + (textureName.endsWith(".png") ? "" : ".png"));
            if (textureFile.exists()) {
                return new Image(textureFile.toURI().toString());
            } else {
                System.err.println("Texture file not found: " + textureFile.getAbsolutePath());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error loading texture: " + textureName + " - " + e.getMessage());
            return null;
        }
    }
}
