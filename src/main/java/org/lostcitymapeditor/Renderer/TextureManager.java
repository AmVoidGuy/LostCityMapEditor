package org.lostcitymapeditor.Renderer;

import org.lostcitymapeditor.Loaders.FileLoader;
import org.lostcitymapeditor.Loaders.TextureLoader;

public class TextureManager {

    private int[] textureIDs;
    private boolean texturesLoaded = false;

    public void initializeTextures(String path) {
        if (texturesLoaded) {
            System.out.println("Textures already loaded, skipping initialization");
            return;
        }

        int numTextures = FileLoader.getTextureMap().size();
        if (numTextures == 0) {
            System.out.println("No textures to load");
            texturesLoaded = true;
            return;
        }

        textureIDs = new int[numTextures];

        for (int i = 0; i < numTextures; i++) {
            textureIDs[i] = TextureLoader.loadTexture(path, i);
        }

        texturesLoaded = true;
    }

    public int[] getTextureIDs() {
        return textureIDs;
    }
}