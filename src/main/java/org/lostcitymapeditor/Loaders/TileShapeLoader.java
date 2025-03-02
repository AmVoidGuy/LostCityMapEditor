package org.lostcitymapeditor.Loaders;

import javafx.scene.image.Image;

import java.io.File;
import java.util.Map;

public class TileShapeLoader {
    public static void loadShapeImages(Map<Integer, Image> shapeImages) {
        for (int i = 0; i <= 11; i++) {
            String imagePath = "Data/TileShapes/shape-" + i + ".png";
            try {
                Image image = new Image(new File(imagePath).toURI().toString());
                shapeImages.put(i, image);
            } catch (Exception e) {
                System.err.println("Could not load image: " + imagePath);
            }
        }
    }
}
