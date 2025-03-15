package org.lostcitymapeditor.OriginalCode;

import org.lostcitymapeditor.DataObjects.newTriangle;
import org.lostcitymapeditor.Loaders.FileLoader;

import java.util.Map;

import static org.lostcitymapeditor.Renderer.OpenGLRenderer.currentLevel;
import static org.lostcitymapeditor.Renderer.OpenGLRenderer.world;

public class Model extends DoublyLinkable {
    public String name;
    public int vertexCount;
    public int[] verticesX;
    public int[] verticesY;
    public int[] verticesZ;
    public int faceCount;
    public int[] faceIndicesA;
    public int[] faceIndicesB;
    public int[] faceIndicesC;
    public int[] faceColorA;
    private int[] faceColorB;
    private int[] faceColorC;
    public int[] faceInfos;
    public int[] facePriorities;
    public int[] faceAlphas;
    public int[] faceColors;
    private int modelPriority;
    public int texturedFaceCount;
    public int[] texturePCoordinate;
    public int[] textureMCoordinate;
    public int[] textureNCoordinate;
    public int minX;
    public int maxX;
    public int maxZ;
    public int minZ;
    public int radius;
    public int maxY;
    public int minY;
    private int maxDepth;
    private int minDepth;
    public int objRaise;
    public int[] vertexLabels;
    public int[] faceLabels;
    public int[][] labelVertices;
    public int[][] labelFaces;
    public VertexNormal[] vertexNormal;
    public VertexNormal[] vertexNormalOriginal;
    public int baseX = 0;
    public int baseY = 0;
    public int baseZ = 0;
    public static final int[] pickedBitsets = new int[1000];
    public static int[] sinTable = Pix3D.sinTable;
    public static int[] cosTable = Pix3D.cosTable;
    public int[] faceTextures;
    public int[] textureCoords;

    public Model(){

    }

    public Model(int id) {
        Map<Integer, Model> modelMap = FileLoader.getModelOb2Map();
        Model loadedModel = modelMap.get(id);
        if(loadedModel == null) {
            return;
        }
            this.vertexCount = loadedModel.vertexCount;
            this.faceCount = loadedModel.faceCount;
            this.texturedFaceCount = loadedModel.texturedFaceCount;

            this.verticesX = loadedModel.verticesX;
            this.verticesY = loadedModel.verticesY;
            this.verticesZ = loadedModel.verticesZ;

            this.faceIndicesA = loadedModel.faceIndicesA;
            this.faceIndicesB = loadedModel.faceIndicesB;
            this.faceIndicesC = loadedModel.faceIndicesC;

            this.texturePCoordinate = loadedModel.texturePCoordinate;
            this.textureMCoordinate = loadedModel.textureMCoordinate;
            this.textureNCoordinate = loadedModel.textureNCoordinate;

            this.vertexLabels = loadedModel.vertexLabels;
            this.faceInfos = loadedModel.faceInfos;
            this.facePriorities = loadedModel.facePriorities;
            this.modelPriority = loadedModel.modelPriority;
            this.faceAlphas = loadedModel.faceAlphas;
            this.faceLabels = loadedModel.faceLabels;
            this.faceColors = loadedModel.faceColors;
            this.textureCoords = loadedModel.textureCoords;
            this.faceTextures = loadedModel.faceTextures;
    }

    public Model( Model[] models,  int count) {
        boolean copyInfo = false;
        boolean copyPriorities = false;
        boolean copyAlpha = false;
        boolean copyLabels = false;

        this.vertexCount = 0;
        this.faceCount = 0;
        this.texturedFaceCount = 0;
        this.modelPriority = -1;

        for ( int i = 0; i < count; i++) {
            Model model = models[i];
            if (model != null) {
                this.vertexCount += model.vertexCount;
                this.faceCount += model.faceCount;
                this.texturedFaceCount += model.texturedFaceCount;
                copyInfo |= model.faceInfos != null;

                if (model.facePriorities == null) {
                    if (this.modelPriority == -1) {
                        this.modelPriority = model.modelPriority;
                    }

                    if (this.modelPriority != model.modelPriority) {
                        copyPriorities = true;
                    }
                } else {
                    copyPriorities = true;
                }

                copyAlpha |= model.faceAlphas != null;
                copyLabels |= model.faceLabels != null;
            }
        }

        this.verticesX = new int[this.vertexCount];
        this.verticesY = new int[this.vertexCount];
        this.verticesZ = new int[this.vertexCount];
        this.vertexLabels = new int[this.vertexCount];
        this.faceIndicesA = new int[this.faceCount];
        this.faceIndicesB = new int[this.faceCount];
        this.faceIndicesC = new int[this.faceCount];
        this.texturePCoordinate = new int[this.texturedFaceCount];
        this.textureMCoordinate = new int[this.texturedFaceCount];
        this.textureNCoordinate = new int[this.texturedFaceCount];

        if (copyInfo) {
            this.faceInfos = new int[this.faceCount];
        }

        if (copyPriorities) {
            this.facePriorities = new int[this.faceCount];
        }

        if (copyAlpha) {
            this.faceAlphas = new int[this.faceCount];
        }

        if (copyLabels) {
            this.faceLabels = new int[this.faceCount];
        }

        this.faceColors = new int[this.faceCount];
        this.vertexCount = 0;
        this.faceCount = 0;
        this.texturedFaceCount = 0;

        for ( int i = 0; i < count; i++) {
            Model model = models[i];

            if (model != null) {
                for ( int face = 0; face < model.faceCount; face++) {
                    if (copyInfo) {
                        if (model.faceInfos == null) {
                            this.faceInfos[this.faceCount] = 0;
                        } else {
                            this.faceInfos[this.faceCount] = model.faceInfos[face];
                        }
                    }

                    if (copyPriorities) {
                        if (model.facePriorities == null) {
                            this.facePriorities[this.faceCount] = model.modelPriority;
                        } else {
                            this.facePriorities[this.faceCount] = model.facePriorities[face];
                        }
                    }

                    if (copyAlpha) {
                        if (model.faceAlphas == null) {
                            this.faceAlphas[this.faceCount] = 0;
                        } else {
                            this.faceAlphas[this.faceCount] = model.faceAlphas[face];
                        }
                    }

                    if (copyLabels && model.faceLabels != null) {
                        this.faceLabels[this.faceCount] = model.faceLabels[face];
                    }

                    this.faceColors[this.faceCount] = model.faceColors[face];
                    this.faceIndicesA[this.faceCount] = this.addVertex(model, model.faceIndicesA[face]);
                    this.faceIndicesB[this.faceCount] = this.addVertex(model, model.faceIndicesB[face]);
                    this.faceIndicesC[this.faceCount] = this.addVertex(model, model.faceIndicesC[face]);
                    this.faceCount++;
                }

                for ( int f = 0; f < model.texturedFaceCount; f++) {
                    this.texturePCoordinate[this.texturedFaceCount] = this.addVertex(model, model.texturePCoordinate[f]);
                    this.textureMCoordinate[this.texturedFaceCount] = this.addVertex(model, model.textureMCoordinate[f]);
                    this.textureNCoordinate[this.texturedFaceCount] = this.addVertex(model, model.textureNCoordinate[f]);
                    this.texturedFaceCount++;
                }
            }
        }
    }

    public Model( Model[] models,  int count,  boolean dummy) {
        boolean copyInfo = false;
        boolean copyPriority = false;
        boolean copyAlpha = false;
        boolean copyColor = false;

        this.vertexCount = 0;
        this.faceCount = 0;
        this.texturedFaceCount = 0;
        this.modelPriority = -1;

        for ( int i = 0; i < count; i++) {
            Model model = models[i];
            if (model != null) {
                this.vertexCount += model.vertexCount;
                this.faceCount += model.faceCount;
                this.texturedFaceCount += model.texturedFaceCount;

                copyInfo |= model.faceInfos != null;

                if (model.facePriorities == null) {
                    if (this.modelPriority == -1) {
                        this.modelPriority = model.modelPriority;
                    }
                    if (this.modelPriority != model.modelPriority) {
                        copyPriority = true;
                    }
                } else {
                    copyPriority = true;
                }

                copyAlpha |= model.faceAlphas != null;
                copyColor |= model.faceColors != null;
            }
        }

        this.verticesX = new int[this.vertexCount];
        this.verticesY = new int[this.vertexCount];
        this.verticesZ = new int[this.vertexCount];
        this.faceIndicesA = new int[this.faceCount];
        this.faceIndicesB = new int[this.faceCount];
        this.faceIndicesC = new int[this.faceCount];
        this.faceColorA = new int[this.faceCount];
        this.faceColorB = new int[this.faceCount];
        this.faceColorC = new int[this.faceCount];
        this.texturePCoordinate = new int[this.texturedFaceCount];
        this.textureMCoordinate = new int[this.texturedFaceCount];
        this.textureNCoordinate = new int[this.texturedFaceCount];

        if (copyInfo) {
            this.faceInfos = new int[this.faceCount];
        }

        if (copyPriority) {
            this.facePriorities = new int[this.faceCount];
        }

        if (copyAlpha) {
            this.faceAlphas = new int[this.faceCount];
        }

        if (copyColor) {
            this.faceColors = new int[this.faceCount];
        }

        this.vertexCount = 0;
        this.faceCount = 0;
        this.texturedFaceCount = 0;

        int i;
        for (i = 0; i < count; i++) {
            Model model = models[i];
            if (model != null) {
                int vertexCount = this.vertexCount;

                for ( int v = 0; v < model.vertexCount; v++) {
                    this.verticesX[this.vertexCount] = model.verticesX[v];
                    this.verticesY[this.vertexCount] = model.verticesY[v];
                    this.verticesZ[this.vertexCount] = model.verticesZ[v];
                    this.vertexCount++;
                }

                for ( int f = 0; f < model.faceCount; f++) {
                    this.faceIndicesA[this.faceCount] = model.faceIndicesA[f] + vertexCount;
                    this.faceIndicesB[this.faceCount] = model.faceIndicesB[f] + vertexCount;
                    this.faceIndicesC[this.faceCount] = model.faceIndicesC[f] + vertexCount;
                    this.faceColorA[this.faceCount] = model.faceColorA[f];
                    this.faceColorB[this.faceCount] = model.faceColorB[f];
                    this.faceColorC[this.faceCount] = model.faceColorC[f];

                    if (copyInfo) {
                        if (model.faceInfos == null) {
                            this.faceInfos[this.faceCount] = 0;
                        } else {
                            this.faceInfos[this.faceCount] = model.faceInfos[f];
                        }
                    }

                    if (copyPriority) {
                        if (model.facePriorities == null) {
                            this.facePriorities[this.faceCount] = model.modelPriority;
                        } else {
                            this.facePriorities[this.faceCount] = model.facePriorities[f];
                        }
                    }

                    if (copyAlpha) {
                        if (model.faceAlphas == null) {
                            this.faceAlphas[this.faceCount] = 0;
                        } else {
                            this.faceAlphas[this.faceCount] = model.faceAlphas[f];
                        }
                    }

                    if (copyColor && model.faceColors != null) {
                        this.faceColors[this.faceCount] = model.faceColors[f];
                    }

                    this.faceCount++;
                }

                for ( int f = 0; f < model.texturedFaceCount; f++) {
                    this.texturePCoordinate[this.texturedFaceCount] = model.texturePCoordinate[f] + vertexCount;
                    this.textureMCoordinate[this.texturedFaceCount] = model.textureMCoordinate[f] + vertexCount;
                    this.textureNCoordinate[this.texturedFaceCount] = model.textureNCoordinate[f] + vertexCount;
                    this.texturedFaceCount++;
                }
            }
        }

        this.calculateBoundsCylinder();
    }

    public Model( Model src,  boolean shareColors,  boolean shareAlpha,  boolean shareVertices) {
        this.vertexCount = src.vertexCount;
        this.faceCount = src.faceCount;
        this.texturedFaceCount = src.texturedFaceCount;
        this.name = src.name;

        this.baseX = src.baseX;
        this.baseZ = src.baseZ;

        if (shareVertices) {
            this.verticesX = src.verticesX;
            this.verticesY = src.verticesY;
            this.verticesZ = src.verticesZ;
        } else {
            this.verticesX = new int[this.vertexCount];
            this.verticesY = new int[this.vertexCount];
            this.verticesZ = new int[this.vertexCount];

            for (int v = 0; v < this.vertexCount; v++) {
                this.verticesX[v] = src.verticesX[v];
                this.verticesY[v] = src.verticesY[v];
                this.verticesZ[v] = src.verticesZ[v];
            }
        }

        if (shareColors) {
            this.faceColors = src.faceColors;
        } else {
            this.faceColors = new int[this.faceCount];
            System.arraycopy(src.faceColors, 0, this.faceColors, 0, this.faceCount);
        }

        if (shareAlpha) {
            this.faceAlphas = src.faceAlphas;
        } else {
            this.faceAlphas = new int[this.faceCount];
            if (src.faceAlphas == null) {
                for (int f = 0; f < this.faceCount; f++) {
                    this.faceAlphas[f] = 0;
                }
            } else {
                System.arraycopy(src.faceAlphas, 0, this.faceAlphas, 0, this.faceCount);
            }
        }

        this.vertexLabels = src.vertexLabels;
        this.faceLabels = src.faceLabels;
        this.faceInfos = src.faceInfos;
        this.faceIndicesA = src.faceIndicesA;
        this.faceIndicesB = src.faceIndicesB;
        this.faceIndicesC = src.faceIndicesC;
        this.facePriorities = src.facePriorities;
        this.modelPriority = src.modelPriority;
        this.texturePCoordinate = src.texturePCoordinate;
        this.textureMCoordinate = src.textureMCoordinate;
        this.textureNCoordinate = src.textureNCoordinate;
        this.faceTextures = src.faceTextures;
        this.textureCoords = src.textureCoords;
    }

    public Model( Model src,  boolean copyVertexY,  boolean copyFaces) {
        this.vertexCount = src.vertexCount;
        this.faceCount = src.faceCount;
        this.texturedFaceCount = src.texturedFaceCount;
        this.baseX = src.baseX;
        this.baseZ = src.baseZ;
        if (copyVertexY) {
            this.verticesY = new int[this.vertexCount];
            System.arraycopy(src.verticesY, 0, this.verticesY, 0, this.vertexCount);
        } else {
            this.verticesY = src.verticesY;
        }

        if (copyFaces) {
            this.faceColorA = new int[this.faceCount];
            this.faceColorB = new int[this.faceCount];
            this.faceColorC = new int[this.faceCount];
            for (int f = 0; f < this.faceCount; f++) {
                this.faceColorA[f] = src.faceColorA[f];
                this.faceColorB[f] = src.faceColorB[f];
                this.faceColorC[f] = src.faceColorC[f];
            }

            this.faceInfos = new int[this.faceCount];
            if (src.faceInfos == null) {
                for (int f = 0; f < this.faceCount; f++) {
                    this.faceInfos[f] = 0;
                }
            } else {
                System.arraycopy(src.faceInfos, 0, this.faceInfos, 0, this.faceCount);
            }

            this.vertexNormal = new VertexNormal[this.vertexCount];
            for (int v = 0; v < this.vertexCount; v++) {
                VertexNormal copy = this.vertexNormal[v] = new VertexNormal();
                VertexNormal original = src.vertexNormal[v];
                copy.x = original.x;
                copy.y = original.y;
                copy.z = original.z;
                copy.w = original.w;
            }

            this.vertexNormalOriginal = src.vertexNormalOriginal;
        } else {
            this.faceColorA = src.faceColorA;
            this.faceColorB = src.faceColorB;
            this.faceColorC = src.faceColorC;
            this.faceInfos = src.faceInfos;
        }

        this.verticesX = src.verticesX;
        this.verticesZ = src.verticesZ;
        this.faceColors = src.faceColors;
        this.faceAlphas = src.faceAlphas;
        this.facePriorities = src.facePriorities;
        this.modelPriority = src.modelPriority;
        this.faceIndicesA = src.faceIndicesA;
        this.faceIndicesB = src.faceIndicesB;
        this.faceIndicesC = src.faceIndicesC;
        this.texturePCoordinate = src.texturePCoordinate;
        this.textureMCoordinate = src.textureMCoordinate;
        this.textureNCoordinate = src.textureNCoordinate;
        this.textureCoords = src.textureCoords;
        this.faceTextures = src.faceTextures;
        this.maxY = src.maxY;
        this.minY = src.minY;
        this.radius = src.radius;
        this.minDepth = src.minDepth;
        this.maxDepth = src.maxDepth;
        this.minX = src.minX;
        this.maxZ = src.maxZ;
        this.minZ = src.minZ;
        this.maxX = src.maxX;
    }

    public Model( Model src,  boolean shareAlpha) {
        this.vertexCount = src.vertexCount;
        this.faceCount = src.faceCount;
        this.texturedFaceCount = src.texturedFaceCount;

        this.verticesX = new int[this.vertexCount];
        this.verticesY = new int[this.vertexCount];
        this.verticesZ = new int[this.vertexCount];

        for ( int v = 0; v < this.vertexCount; v++) {
            this.verticesX[v] = src.verticesX[v];
            this.verticesY[v] = src.verticesY[v];
            this.verticesZ[v] = src.verticesZ[v];
        }

        if (shareAlpha) {
            this.faceAlphas = src.faceAlphas;
        } else {
            this.faceAlphas = new int[this.faceCount];
            if (src.faceAlphas == null) {
                for (int f = 0; f < this.faceCount; f++) {
                    this.faceAlphas[f] = 0;
                }
            } else {
                System.arraycopy(src.faceAlphas, 0, this.faceAlphas, 0, this.faceCount);
            }
        }

        this.faceInfos = src.faceInfos;
        this.faceColors = src.faceColors;
        this.facePriorities = src.facePriorities;
        this.modelPriority = src.modelPriority;
        this.labelFaces = src.labelFaces;
        this.labelVertices = src.labelVertices;
        this.faceIndicesA = src.faceIndicesA;
        this.faceIndicesB = src.faceIndicesB;
        this.faceIndicesC = src.faceIndicesC;
        this.faceColorA = src.faceColorA;
        this.faceColorB = src.faceColorB;
        this.faceColorC = src.faceColorC;
        this.texturePCoordinate = src.texturePCoordinate;
        this.textureMCoordinate = src.textureMCoordinate;
        this.textureNCoordinate = src.textureNCoordinate;
        this.textureCoords = src.textureCoords;
        this.faceTextures = src.faceTextures;
    }

    public static int mulColorLightness( int hsl,  int scalar,  int faceInfo) {
        if ((faceInfo & 0x2) == 2) {
            if (scalar < 0) {
                scalar = 0;
            } else if (scalar > 127) {
                scalar = 127;
            }
            return 127 - scalar;
        }
        scalar = scalar * (hsl & 0x7F) >> 7;
        if (scalar < 2) {
            scalar = 2;
        } else if (scalar > 126) {
            scalar = 126;
        }
        return (hsl & 0xFF80) + scalar;
    }

    private int addVertex( Model src,  int vertexId) {
        int identical = -1;
        int x = src.verticesX[vertexId];
        int y = src.verticesY[vertexId];
        int z = src.verticesZ[vertexId];
        for ( int v = 0; v < this.vertexCount; v++) {
            if (x == this.verticesX[v] && y == this.verticesY[v] && z == this.verticesZ[v]) {
                identical = v;
                break;
            }
        }
        if (identical == -1) {
            this.verticesX[this.vertexCount] = x;
            this.verticesY[this.vertexCount] = y;
            this.verticesZ[this.vertexCount] = z;
            if (src.vertexLabels != null) {
                this.vertexLabels[this.vertexCount] = src.vertexLabels[vertexId];
            }
            identical = this.vertexCount++;
        }
        return identical;
    }

    public void calculateBoundsCylinder() {
        this.maxY = 0;
        this.radius = 0;
        this.minY = 0;

        for ( int i = 0; i < this.vertexCount; i++) {
            int x = this.verticesX[i];
            int y = this.verticesY[i];
            int z = this.verticesZ[i];

            if (-y > this.maxY) {
                this.maxY = -y;
            }
            if (y > this.minY) {
                this.minY = y;
            }

            int radiusSqr = x * x + z * z;
            if (radiusSqr > this.radius) {
                this.radius = radiusSqr;
            }
        }

        this.radius = (int) (Math.sqrt(this.radius) + 0.99D);
        this.minDepth = (int) (Math.sqrt(this.radius * this.radius + this.maxY * this.maxY) + 0.99D);
        this.maxDepth = this.minDepth + (int) (Math.sqrt(this.radius * this.radius + this.minY * this.minY) + 0.99D);
    }

    public void calculateBoundsY() {
        this.maxY = 0;
        this.minY = 0;

        for ( int v = 0; v < this.vertexCount; v++) {
            int y = this.verticesY[v];
            if (-y > this.maxY) {
                this.maxY = -y;
            }
            if (y > this.minY) {
                this.minY = y;
            }
        }

        this.minDepth = (int) (Math.sqrt(this.radius * this.radius + this.maxY * this.maxY) + 0.99D);
        this.maxDepth = this.minDepth + (int) (Math.sqrt(this.radius * this.radius + this.minY * this.minY) + 0.99D);
    }

    private void calculateBoundsAABB() {
        this.maxY = 0;
        this.radius = 0;
        this.minY = 0;
        this.minX = 999999;
        this.maxX = -999999;
        this.maxZ = -99999;
        this.minZ = 99999;

        for ( int v = 0; v < this.vertexCount; v++) {
            int x = this.verticesX[v];
            int y = this.verticesY[v];
            int z = this.verticesZ[v];

            if (x < this.minX) {
                this.minX = x;
            }
            if (x > this.maxX) {
                this.maxX = x;
            }

            if (z < this.minZ) {
                this.minZ = z;
            }
            if (z > this.maxZ) {
                this.maxZ = z;
            }

            if (-y > this.maxY) {
                this.maxY = -y;
            }
            if (y > this.minY) {
                this.minY = y;
            }

            int radiusSqr = x * x + z * z;
            if (radiusSqr > this.radius) {
                this.radius = radiusSqr;
            }
        }

        this.radius = (int) Math.sqrt(this.radius);
        this.minDepth = (int) Math.sqrt(this.radius * this.radius + this.maxY * this.maxY);
        this.maxDepth = this.minDepth + (int) Math.sqrt(this.radius * this.radius + this.minY * this.minY);
    }

    public void rotateY90() {
        for ( int v = 0; v < this.vertexCount; v++) {
            int tmp = this.verticesX[v];
            this.verticesX[v] = this.verticesZ[v];
            this.verticesZ[v] = -tmp;
        }
    }

    public void rotateX( int angle) {
        int sin = Model.sinTable[angle];
        int cos = Model.cosTable[angle];
        for ( int v = 0; v < this.vertexCount; v++) {
            int tmp = this.verticesY[v] * cos - this.verticesZ[v] * sin >> 16;
            this.verticesZ[v] = this.verticesY[v] * sin + this.verticesZ[v] * cos >> 16;
            this.verticesY[v] = tmp;
        }
    }

    public void translate( int y,  int x,  int z) {
        for ( int v = 0; v < this.vertexCount; v++) {
            this.verticesX[v] += x;
            this.verticesY[v] += y;
            this.verticesZ[v] += z;
        }
    }

    public void recolor( int src,  int dst) {
        for ( int f = 0; f < this.faceCount; f++) {
            if (this.faceColors[f] == src) {
                this.faceColors[f] = dst;
            }
        }
    }

    public void rotateY180() {
        for ( int v = 0; v < this.vertexCount; v++) {
            this.verticesZ[v] = -this.verticesZ[v];
        }

        for ( int f = 0; f < this.faceCount; f++) {
            int temp = this.faceIndicesA[f];
            this.faceIndicesA[f] = this.faceIndicesC[f];
            this.faceIndicesC[f] = temp;
        }
    }

    public void scale( int x,  int y,  int z) {
        for ( int v = 0; v < this.vertexCount; v++) {
            this.verticesX[v] = this.verticesX[v] * x / 128;
            this.verticesY[v] = this.verticesY[v] * y / 128;
            this.verticesZ[v] = this.verticesZ[v] * z / 128;
        }
    }

    public void calculateNormals( int lightAmbient,  int lightAttenuation,  int lightSrcX,  int lightSrcY,  int lightSrcZ,  boolean applyLighting) {
        int lightMagnitude = (int) Math.sqrt(lightSrcX * lightSrcX + lightSrcY * lightSrcY + lightSrcZ * lightSrcZ);
        int attenuation = lightAttenuation * lightMagnitude >> 8;

        if (this.faceColorA == null) {
            this.faceColorA = new int[this.faceCount];
            this.faceColorB = new int[this.faceCount];
            this.faceColorC = new int[this.faceCount];
        }

        if (this.vertexNormal == null) {
            this.vertexNormal = new VertexNormal[this.vertexCount];
            for (int v = 0; v < this.vertexCount; v++) {
                this.vertexNormal[v] = new VertexNormal();
            }
        }

        for (int f = 0; f < this.faceCount; f++) {
            int a = this.faceIndicesA[f];
            int b = this.faceIndicesB[f];
            int c = this.faceIndicesC[f];

            int dxAB = this.verticesX[b] - this.verticesX[a];
            int dyAB = this.verticesY[b] - this.verticesY[a];
            int dzAB = this.verticesZ[b] - this.verticesZ[a];

            int dxAC = this.verticesX[c] - this.verticesX[a];
            int dyAC = this.verticesY[c] - this.verticesY[a];
            int dzAC = this.verticesZ[c] - this.verticesZ[a];

            int nx = dyAB * dzAC - dyAC * dzAB;
            int ny = dzAB * dxAC - dzAC * dxAB;
            int nz;
            for (nz = dxAB * dyAC - dxAC * dyAB; nx > 8192 || ny > 8192 || nz > 8192 || nx < -8192 || ny < -8192 || nz < -8192; nz >>= 0x1) {
                nx >>= 0x1;
                ny >>= 0x1;
            }

            int length = (int) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (length <= 0) {
                length = 1;
            }

            nx = nx * 256 / length;
            ny = ny * 256 / length;
            nz = nz * 256 / length;

            if (this.faceInfos == null || (this.faceInfos[f] & 0x1) == 0) {
                VertexNormal n = this.vertexNormal[a];
                n.x += nx;
                n.y += ny;
                n.z += nz;
                n.w++;

                n = this.vertexNormal[b];
                n.x += nx;
                n.y += ny;
                n.z += nz;
                n.w++;

                n = this.vertexNormal[c];
                n.x += nx;
                n.y += ny;
                n.z += nz;
                n.w++;
            } else {
                int lightness = lightAmbient + (lightSrcX * nx + lightSrcY * ny + lightSrcZ * nz) / (attenuation + attenuation / 2);
                this.faceColorA[f] = mulColorLightness(this.faceColors[f], lightness, this.faceInfos[f]);
            }
        }

        if (applyLighting) {
            this.applyLighting(lightAmbient, attenuation, lightSrcX, lightSrcY, lightSrcZ);
        } else {
            this.vertexNormalOriginal = new VertexNormal[this.vertexCount];
            for (int v = 0; v < this.vertexCount; v++) {
                VertexNormal normal = this.vertexNormal[v];
                VertexNormal copy = this.vertexNormalOriginal[v] = new VertexNormal();
                copy.x = normal.x;
                copy.y = normal.y;
                copy.z = normal.z;
                copy.w = normal.w;
            }
        }

        if (applyLighting) {
            this.calculateBoundsCylinder();
        } else {
            this.calculateBoundsAABB();
        }
    }

    public void applyLighting( int lightAmbient,  int lightAttenuation,  int lightSrcX,  int lightSrcY,  int lightSrcZ) {
        for ( int f = 0; f < this.faceCount; f++) {
            int a = this.faceIndicesA[f];
            int b = this.faceIndicesB[f];
            int c = this.faceIndicesC[f];

            if (this.faceInfos == null) {
                int color = this.faceColors[f];

                VertexNormal n = this.vertexNormal[a];
                int lightness = lightAmbient + (lightSrcX * n.x + lightSrcY * n.y + lightSrcZ * n.z) / (lightAttenuation * n.w);
                this.faceColorA[f] = mulColorLightness(color, lightness, 0);

                n = this.vertexNormal[b];
                lightness = lightAmbient + (lightSrcX * n.x + lightSrcY * n.y + lightSrcZ * n.z) / (lightAttenuation * n.w);
                this.faceColorB[f] = mulColorLightness(color, lightness, 0);

                n = this.vertexNormal[c];
                lightness = lightAmbient + (lightSrcX * n.x + lightSrcY * n.y + lightSrcZ * n.z) / (lightAttenuation * n.w);
                this.faceColorC[f] = mulColorLightness(color, lightness, 0);
            } else if ((this.faceInfos[f] & 0x1) == 0) {
                int color = this.faceColors[f];
                int info = this.faceInfos[f];

                VertexNormal n = this.vertexNormal[a];
                int lightness = lightAmbient + (lightSrcX * n.x + lightSrcY * n.y + lightSrcZ * n.z) / (lightAttenuation * n.w);
                this.faceColorA[f] = mulColorLightness(color, lightness, info);

                n = this.vertexNormal[b];
                lightness = lightAmbient + (lightSrcX * n.x + lightSrcY * n.y + lightSrcZ * n.z) / (lightAttenuation * n.w);
                this.faceColorB[f] = mulColorLightness(color, lightness, info);

                n = this.vertexNormal[c];
                lightness = lightAmbient + (lightSrcX * n.x + lightSrcY * n.y + lightSrcZ * n.z) / (lightAttenuation * n.w);
                this.faceColorC[f] = mulColorLightness(color, lightness, info);
            }
        }

        this.vertexNormal = null;
        this.vertexNormalOriginal = null;
        this.vertexLabels = null;
        this.faceLabels = null;

        if (this.faceInfos != null) {
            for (int f = 0; f < this.faceCount; f++) {
                if ((this.faceInfos[f] & 0x2) == 2) {
                    return;
                }
            }
        }

        this.faceColors = null;
    }

    public void draw(int yaw, int relativeX,  int relativeY,  int relativeZ,  int bitset) {
        try {
            int sinYaw = 0;
            int cosYaw = 0;
            if (yaw != 0) {
                sinYaw = sinTable[yaw];
                cosYaw = cosTable[yaw];
            }
            relativeX = relativeX - (128 * this.baseX);
            relativeZ = relativeZ - (128 * this.baseZ);
            int[] transformedX = new int[this.vertexCount];
            int[] transformedZ = new int[this.vertexCount];

            for (int v = 0; v < this.vertexCount; v++) {
                int x = this.verticesX[v];
                int z = this.verticesZ[v];

                if (yaw != 0) {
                    int rotatedX = (x * cosYaw + z * sinYaw) >> 16;
                    int rotatedZ = (z * cosYaw - x * sinYaw) >> 16;

                    transformedX[v] = (rotatedX + relativeX);
                    transformedZ[v] = (rotatedZ + relativeZ);
                } else {
                    transformedX[v] = x + relativeX;
                    transformedZ[v] = z + relativeZ;
                }
            }

            this.verticesX = transformedX;
            this.verticesZ = transformedZ;
            this.draw();
        } catch ( Exception ex) {
        }
    }

    private void draw() {
        for (int f = 0; f < this.faceCount; f++) {
            if (this.faceInfos == null || this.faceInfos[f] != -1) {
                this.drawFace(f);
            }
        }
    }
    private void drawFace(int face) {

        int a = this.faceIndicesA[face];
        int b = this.faceIndicesB[face];
        int c = this.faceIndicesC[face];

        int tileX = this.baseX;
        int tileZ = this.baseZ;

        float x0 = this.verticesX[a] + (tileX << 7);
        float y0 = this.verticesY[a] + world.getHeightmapY(currentLevel, tileX, tileZ);
        float z0 = this.verticesZ[a] + (tileZ << 7);

        float x1 = this.verticesX[b] + (tileX << 7);
        float y1 = this.verticesY[b] + world.getHeightmapY(currentLevel, tileX, tileZ);
        float z1 = this.verticesZ[b] + (tileZ << 7);

        float x2 = this.verticesX[c] + (tileX << 7);
        float y2 = this.verticesY[c] + world.getHeightmapY(currentLevel, tileX, tileZ);
        float z2 = this.verticesZ[c] + (tileZ << 7);

        int colorA = this.faceColorA[face];
        int colorB = this.faceColorB[face];
        int colorC = this.faceColorC[face];

        int type = 0;
        if (this.faceInfos != null) {
            type = this.faceInfos[face] & 0x3;
        }

        int textureId = -1;
        float[] texCoords = new float[6];
        if (type == 2 || type == 3) {
            texCoords = calculateTextureCoordinates(face);
            textureId = this.faceColors[face];
        }

        newTriangle.addTriangle(true,
                tileX, tileZ, currentLevel,
                type, 0,
                x0, y0, z0,
                x1, y1, z1,
                x2, y2, z2,
                colorA, colorB, colorC,
                textureId, texCoords
        );
    }

    private float[] calculateTextureCoordinates(int face) {
        int index0 = this.faceIndicesA[face];
        int index1 = this.faceIndicesB[face];
        int index2 = this.faceIndicesC[face];

        int p, m, n;
        if (this.faceTextures[face] != -1) {
            int textureCoordinate = this.textureCoords[face];
            p = this.texturePCoordinate[textureCoordinate];
            m = this.textureMCoordinate[textureCoordinate];
            n = this.textureNCoordinate[textureCoordinate];
        } else {
            p = index0;
            m = index1;
            n = index2;
        }

        float vx = (float) this.verticesX[p];
        float vy = (float) this.verticesY[p];
        float vz = (float) this.verticesZ[p];

        float f882 = this.verticesX[m] - vx;
        float f883 = this.verticesY[m] - vy;
        float f884 = this.verticesZ[m] - vz;

        float f885 = this.verticesX[n] - vx;
        float f886 = this.verticesY[n] - vy;
        float f887 = this.verticesZ[n] - vz;

        float f888 = this.verticesX[index0] - vx;
        float f889 = this.verticesY[index0] - vy;
        float f890 = this.verticesZ[index0] - vz;

        float f891 = this.verticesX[index1] - vx;
        float f892 = this.verticesY[index1] - vy;
        float f893 = this.verticesZ[index1] - vz;

        float f894 = this.verticesX[index2] - vx;
        float f895 = this.verticesY[index2] - vy;
        float f896 = this.verticesZ[index2] - vz;

        float f897 = f883 * f887 - f884 * f886;
        float f898 = f884 * f885 - f882 * f887;
        float f899 = f882 * f886 - f883 * f885;

        float f900 = f886 * f899 - f887 * f898;
        float f901 = f887 * f897 - f885 * f899;
        float f902 = f885 * f898 - f886 * f897;
        float denom = f900 * f882 + f901 * f883 + f902 * f884;
        float f903 = 1.0f / denom;

        float u0 = (f900 * f888 + f901 * f889 + f902 * f890) * f903;
        float u1 = (f900 * f891 + f901 * f892 + f902 * f893) * f903;
        float u2 = (f900 * f894 + f901 * f895 + f902 * f896) * f903;

        f900 = f883 * f899 - f884 * f898;
        f901 = f884 * f897 - f882 * f899;
        f902 = f882 * f898 - f883 * f897;
        denom = f900 * f885 + f901 * f886 + f902 * f887;
        f903 = 1.0f / denom;

        float v0 = (f900 * f888 + f901 * f889 + f902 * f890) * f903;
        float v1 = (f900 * f891 + f901 * f892 + f902 * f893) * f903;
        float v2 = (f900 * f894 + f901 * f895 + f902 * f896) * f903;

        return new float[] { u0, v0, u1, v1, u2, v2 };
    }

    public static final class VertexNormal {
        public int x;
        public int y;
        public int z;
        public int w;
    }

}
