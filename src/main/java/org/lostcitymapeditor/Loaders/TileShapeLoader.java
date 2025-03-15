package org.lostcitymapeditor.Loaders;

import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.Map;

public class TileShapeLoader {
    public static void loadShapeImages(Map<Integer, Image> shapeImages) {
        for (int i = 0; i <= 11; i++) {
            String resourcePath = "Data/TileShapes/Shape-" + i + ".png";
            try (InputStream inputStream = TileShapeLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (inputStream != null) {
                    Image image = new Image(inputStream);
                    shapeImages.put(i, image);
                } else {
                    System.err.println("Could not load resource: " + resourcePath);
                }
            } catch (Exception e) {
                System.err.println("Could not load resource: " + resourcePath);
            }
        }
    }
}
