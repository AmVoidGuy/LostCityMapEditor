package org.lostcitymapeditor.Transformers;

import org.lostcitymapeditor.Loaders.FileLoader;
import org.lostcitymapeditor.OriginalCode.Model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

public class Ob2FileTransformer {
    static String ModelBasePath = "Data/Models/";

    public static Map<Integer, Model> parseOb2Files() throws IOException {
        Map<String, Integer> modelMap = FileLoader.getModelMap();
        Map<Integer, Model> ob2Map = new HashMap<>();

        URL resourceUrl = Ob2FileTransformer.class.getClassLoader().getResource(ModelBasePath);
        if (resourceUrl == null) {
            throw new IOException("Resource directory not found");
        }

        Path resourcePath;
        try {
            resourcePath = Paths.get(resourceUrl.toURI());
        } catch (URISyntaxException e) {
            throw new IOException("Invalid resource URI", e);
        }

        try (Stream<Path> paths = Files.walk(resourcePath)) {
            paths.filter(path -> path.toString().endsWith(".ob2"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String fileNameNoExt = fileName.substring(0, fileName.lastIndexOf('.'));

                        if (modelMap.containsKey(fileNameNoExt)) {
                            try {
                                ob2Map.put(modelMap.get(fileNameNoExt), convert(fileNameNoExt));
                            } catch (Exception e) {
                                System.err.println("Failed to convert file: " + fileNameNoExt);
                                e.printStackTrace();
                            }
                        }
                    });
        }
        return ob2Map;
    }

    public static Model convert(String modelName) throws IOException {
        byte[] fileData = new byte[0];
        try (InputStream in = Ob2FileTransformer.class.getClassLoader().getResourceAsStream(ModelBasePath + modelName + ".ob2")) {
            if (in == null) {
                System.err.println("Could not find resource: " + ModelBasePath + modelName + ".ob2");
            } else {
                fileData = in.readAllBytes();
            }
        } catch (IOException e) {
            System.err.println("Error loading image from resource: " + ModelBasePath + modelName + ".ob2" + " - " + e.getMessage());
            e.printStackTrace();
        }
        OB2Packet packet = new OB2Packet(fileData);
        return parseOB2(packet);
    }

    private static Model parseOB2(OB2Packet data) {
        Model model = new Model();

        data.setPosition(data.getData().length - 18);

        int vertexCount = data.getUnsignedShort();
        int faceCount = data.getUnsignedShort();
        int texturedFaceCount = data.getUnsignedByte();
        boolean hasInfo = data.getUnsignedByte() == 1;
        int hasPriorities = data.getUnsignedByte();
        boolean hasAlpha = data.getUnsignedByte() == 1;
        boolean hasFaceLabels = data.getUnsignedByte() == 1;
        boolean hasVertexLabels = data.getUnsignedByte() == 1;
        int vertexXLength = data.getUnsignedShort();
        int vertexYLength = data.getUnsignedShort();
        int vertexZLength = data.getUnsignedShort();
        int faceVertexLength = data.getUnsignedShort();

        model.faceCount = faceCount;
        model.faceIndicesA = new int[faceCount];
        model.faceIndicesB = new int[faceCount];
        model.faceIndicesC = new int[faceCount];
        model.faceColors = new int[faceCount];

        model.vertexCount = vertexCount;
        model.verticesX = new int[vertexCount];
        model.verticesY = new int[vertexCount];
        model.verticesZ = new int[vertexCount];
        data.setPosition(0);

        model.texturedFaceCount = texturedFaceCount;
        if(texturedFaceCount != 0) {
            model.textureMCoordinate = new int[texturedFaceCount];
            model.texturePCoordinate = new int[texturedFaceCount];
            model.textureNCoordinate = new int[texturedFaceCount];
        }

        int[] vertexFlags = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            vertexFlags[i] = data.getUnsignedByte();
        }


        int[] faceIndices = new int[faceCount];
        for (int i = 0; i < faceCount; i++) {
            faceIndices[i] = data.getUnsignedByte();
        }


        int[] priorities = null;
        if (hasPriorities == 255) {
            priorities = new int[faceCount];
            for (int i = 0; i < faceCount; i++) {
                priorities[i] = data.getUnsignedByte();
            }
            model.facePriorities = priorities;
        }

        int[] faceLabels = null;
        if (hasFaceLabels) {
            faceLabels = new int[faceCount];
            for (int i = 0; i < faceCount; i++) {
                faceLabels[i] = data.getUnsignedByte();
            }
            model.faceLabels = faceLabels;
        }

        int[] faceInfo = null;
        int[] textureCoords = null;
        int[] faceTextures = null;
        if (hasInfo) {
            faceInfo = new int[faceCount];
            faceTextures = new int[faceCount];
            textureCoords = new int[faceCount];
            for (int i = 0; i < faceCount; i++) {
                faceInfo[i] = data.getUnsignedByte();
                if ((faceInfo[i] & 0x2) == 2) {
                    textureCoords[i] = faceInfo[i] >> 2;
                    faceTextures[i] = model.faceColors[i];
                } else {
                    textureCoords[i] = -1;
                    faceTextures[i] = -1;
                }
            }
            model.faceInfos = faceInfo;
            model.faceTextures = faceTextures;
            model.textureCoords = textureCoords;
        }

        int[] vertexLabels = null;
        if (hasVertexLabels) {
            vertexLabels = new int[vertexCount];
            for (int i = 0; i < vertexCount; i++) {
                vertexLabels[i] = data.getUnsignedByte();
            }
            model.vertexLabels = vertexLabels;
        }

        int[] alphaValues = null;
        if (hasAlpha) {
            alphaValues = new int[faceCount];
            for (int i = 0; i < faceCount; i++) {
                alphaValues[i] = data.getUnsignedByte();
            }
            model.faceAlphas = alphaValues;
        }

        int[] faceVertexData = new int[faceVertexLength];
        for (int i = 0; i < faceVertexLength; i++) {
            faceVertexData[i] = data.getUnsignedByte();
        }

        int[] faceTypeData = new int[faceCount * 2];
        for (int i = 0; i < faceCount * 2; i++) {
            faceTypeData[i] = data.getUnsignedByte();
        }

        int[] texturedFaceData = new int[texturedFaceCount * 6];
        for (int i = 0; i < texturedFaceCount * 6; i++) {
            texturedFaceData[i] = data.getUnsignedByte();
        }
        int[] vertexXData = new int[vertexXLength];
        for (int i = 0; i < vertexXLength; i++) {
            vertexXData[i] = data.getUnsignedByte();
        }

        int[] vertexYData = new int[vertexYLength];
        for (int i = 0; i < vertexYLength; i++) {
            vertexYData[i] = data.getUnsignedByte();
        }

        int[] vertexZData = new int[vertexZLength];
        for (int i = 0; i < vertexZLength; i++) {
            vertexZData[i] = data.getUnsignedByte();
        }

        processVertices(model, vertexXData, vertexYData, vertexZData, vertexFlags);

        processFaces(model, faceVertexData, faceIndices);

        processColors(model, faceTypeData);

        processTextures(model, texturedFaceData);
        return model;
    }

    private static void processColors(Model model, int[] faceTypeData) {
        OB2Packet colorData = new OB2Packet(faceTypeData);
        for (int f = 0;f < model.faceCount; f++) {
            int color = colorData.getUnsignedShort();
            model.faceColors[f] = color;
        }
    }

    private static void processVertices(Model model, int[] xData, int[] yData, int[] zData, int[] vertexFlags) {

        OB2Packet dataX = new OB2Packet(xData);
        OB2Packet dataY = new OB2Packet(yData);
        OB2Packet dataZ = new OB2Packet(zData);

        int dx = 0;
        int dy = 0;
        int dz = 0;
        for (int v = 0; v < model.vertexCount; v++) {
            int flags = vertexFlags[v];

            int a = 0;
            if ((flags & 1) != 0) {
                a = dataX.gSmart();
            }
            int b = 0;
            if ((flags & 2) != 0) {
                b = dataY.gSmart();
            }
            int c = 0;
            if ((flags & 4) != 0) {
                c = dataZ.gSmart();
            }

            int x = dx + a;
            int y = dy + b;
            int z = dz + c;

            dx = x;
            dy = y;
            dz = z;

            model.verticesX[v] = x;
            model.verticesY[v] = y;
            model.verticesZ[v] = z;
        }
    }

    private static void processFaces(Model model, int[] faceTypeData, int[] faceIndices) {
        OB2Packet vertexData = new OB2Packet(faceTypeData);
        OB2Packet orientationData = new OB2Packet(faceIndices);
        int a = 0;
        int b = 0;
        int c = 0;
        int last = 0;
        for (int f = 0;f < model.faceCount; f++) {
            int orientation = orientationData.getUnsignedByte();
            if (orientation == 1) {
                a = vertexData.gSmart() + last;
                last = a;
                b = vertexData.gSmart() + last;
                last = b;
                c = vertexData.gSmart() + last;
                last = c;
            } else if (orientation == 2) {
                b = c;
                c = vertexData.gSmart() + last;
                last = c;
            } else if (orientation == 3) {
                a = c;
                c = vertexData.gSmart() + last;
                last = c;
            } else if (orientation == 4) {
                int tmp = a;
                a = b;
                b = tmp;
                c = vertexData.gSmart() + last;
                last = c;
            }
            model.faceIndicesA[f] = a;
            model.faceIndicesB[f] = b;
            model.faceIndicesC[f] = c;
        }
    }

    private static void processTextures(Model model, int[] texturedFaceData) {
        OB2Packet textureData = new OB2Packet(texturedFaceData);

        for (int i = 0; i < model.texturedFaceCount; i++) {
            model.texturePCoordinate[i] = textureData.getUnsignedShort();
            model.textureMCoordinate[i] = textureData.getUnsignedShort();
            model.textureNCoordinate[i] = textureData.getUnsignedShort();
        }
    }

    private static class OB2Packet {
        private final byte[] data;
        private int position;

        public OB2Packet(byte[] data) {
            this.data = data;
            this.position = 0;
        }

        public OB2Packet(int[] data) {
            this.data = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                this.data[i] = (byte) data[i];
            }
            this.position = 0;
        }

        public byte[] getData() {
            return data;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }

        public int gSmart() {
            int peekByte = this.getData()[this.getPosition()] & 0xFF;

            if (peekByte < 128) {
                return this.getUnsignedByte() - 64;
            } else {
                return this.getUnsignedShort() - 49152;
            }
        }

        public int getUnsignedByte() {
            return data[position++] & 0xFF;
        }

        public int getUnsignedShort() {
            return ((data[position++] & 0xFF) << 8) | (data[position++] & 0xFF);
        }
    }
}