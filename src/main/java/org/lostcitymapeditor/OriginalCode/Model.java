package org.lostcitymapeditor.OriginalCode;

public class Model extends DoublyLinkable {
    public int vertexCount;
    public int[] verticesX;
    public int[] verticesY;
    public int[] verticesZ;
    public int faceCount;
    public int[] faceIndicesA;
    public int[] faceIndicesB;
    public int[] faceIndicesC;
    private int[] faceColorA;
    private int[] faceColorB;
    private int[] faceColorC;
    public int[] faceInfos;
    private int[] facePriorities;
    private int[] faceAlphas;
    public int[] faceColors;
    private int modelPriority;
    private int texturedFaceCount;
    private int[] texturePCoordinate;
    private int[] textureMCoordinate;
    private int[] textureNCoordinate;
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
    private int[] vertexLabels;
    private int[] faceLabels;
    public int[][] labelVertices;
    public int[][] labelFaces;
    public boolean pickable = false;
    public VertexNormal[] vertexNormal;
    public VertexNormal[] vertexNormalOriginal;
    public static Metadata[] metadata;
    public static boolean[] faceClippedX = new boolean[4096];
    public static boolean[] faceNearClipped = new boolean[4096];
    public static int[] vertexScreenX = new int[4096];
    public static int[] vertexScreenY = new int[4096];
    public static int[] vertexScreenZ = new int[4096];
    public static int[] vertexViewSpaceX = new int[4096];
    public static int[] vertexViewSpaceY = new int[4096];
    public static int[] vertexViewSpaceZ = new int[4096];
    public static int[] tmpDepthFaceCount = new int[1500];
    public static int[][] tmpDepthFaces = new int[1500][512];
    public static int[] tmpPriorityFaceCount = new int[12];
    public static int[][] tmpPriorityFaces = new int[12][2000];
    public static int[] tmpPriority10FaceDepth = new int[2000];
    public static int[] tmpPriority11FaceDepth = new int[2000];
    public static int[] tmpPriorityDepthSum = new int[12];
    public static final int[] clippedX = new int[10];
    public static final int[] clippedY = new int[10];
    public static final int[] clippedColor = new int[10];
    public static int baseX;
    public static int baseY;
    public static int baseZ;
    public static boolean checkHover;
    public static int mouseX;
    public static int mouseZ;
    public static int pickedCount;
    public static final int[] pickedBitsets = new int[1000];
    public static int[] sinTable = Pix3D.sinTable;
    public static int[] cosTable = Pix3D.cosTable;
    public static int[] colourTable = Pix3D.colourTable;
    public static int[] divTable2 = Pix3D.divTable2;

//    public Model( int id) {
//        if (metadata == null) {
//            return;
//        }
//
//        Metadata meta = metadata[id];
//        if (meta == null) {
//            System.out.println("Error model:" + id + " not found!");
//        } else {
//            this.vertexCount = meta.vertexCount;
//            this.faceCount = meta.faceCount;
//            this.texturedFaceCount = meta.texturedFaceCount;
//
//            this.verticesX = new int[this.vertexCount];
//            this.verticesY = new int[this.vertexCount];
//            this.verticesZ = new int[this.vertexCount];
//
//            this.faceIndicesA = new int[this.faceCount];
//            this.faceIndicesB = new int[this.faceCount];
//            this.faceIndicesC = new int[this.faceCount];
//
//            this.texturePCoordinate = new int[this.texturedFaceCount];
//            this.textureMCoordinate = new int[this.texturedFaceCount];
//            this.textureNCoordinate = new int[this.texturedFaceCount];
//
//            if (meta.vertexLabelsOffset >= 0) {
//                this.vertexLabels = new int[this.vertexCount];
//            }
//
//            if (meta.faceInfosOffset >= 0) {
//                this.faceInfos = new int[this.faceCount];
//            }
//
//            if (meta.facePrioritiesOffset >= 0) {
//                this.facePriorities = new int[this.faceCount];
//            } else {
//                this.modelPriority = -meta.facePrioritiesOffset - 1;
//            }
//
//            if (meta.faceAlphasOffset >= 0) {
//                this.faceAlphas = new int[this.faceCount];
//            }
//
//            if (meta.faceLabelsOffset >= 0) {
//                this.faceLabels = new int[this.faceCount];
//            }
//
//            this.faceColors = new int[this.faceCount];
//
//            point1.pos = meta.vertexFlagsOffset;
//            point2.pos = meta.verticesXOffset;
//            point3.pos = meta.verticesYOffset;
//            point4.pos = meta.verticesZOffset;
//            point5.pos = meta.vertexLabelsOffset;
//
//            int dx = 0;
//            int db = 0;
//            int dc = 0;
//            int a;
//            int b;
//            int c;
//
//            for ( int v = 0; v < this.vertexCount; v++) {
//                int flags = point1.g1();
//
//                a = 0;
//                if ((flags & 0x1) != 0) {
//                    a = point2.gsmart();
//                }
//
//                b = 0;
//                if ((flags & 0x2) != 0) {
//                    b = point3.gsmart();
//                }
//
//                c = 0;
//                if ((flags & 0x4) != 0) {
//                    c = point4.gsmart();
//                }
//
//                this.verticesX[v] = dx + a;
//                this.verticesY[v] = db + b;
//                this.verticesZ[v] = dc + c;
//                dx = this.verticesX[v];
//                db = this.verticesY[v];
//                dc = this.verticesZ[v];
//
//                if (this.vertexLabels != null) {
//                    this.vertexLabels[v] = point5.g1();
//                }
//            }
//
//            face1.pos = meta.faceColorsOffset;
//            face2.pos = meta.faceInfosOffset;
//            face3.pos = meta.facePrioritiesOffset;
//            face4.pos = meta.faceAlphasOffset;
//            face5.pos = meta.faceLabelsOffset;
//
//            for (int f = 0; f < this.faceCount; f++) {
//                this.faceColors[f] = face1.g2();
//
//                if (this.faceInfos != null) {
//                    this.faceInfos[f] = face2.g1();
//                }
//
//                if (this.facePriorities != null) {
//                    this.facePriorities[f] = face3.g1();
//                }
//
//                if (this.faceAlphas != null) {
//                    this.faceAlphas[f] = face4.g1();
//                }
//
//                if (this.faceLabels != null) {
//                    this.faceLabels[f] = face5.g1();
//                }
//            }
//
//            vertex1.pos = meta.faceIndicesOffset;
//            vertex2.pos = meta.faceIndicesFlagsOffset;
//
//            a = 0;
//            b = 0;
//            c = 0;
//            int last = 0;
//
//            for ( int f = 0; f < this.faceCount; f++) {
//                int orientation = vertex2.g1();
//
//                if (orientation == 1) {
//                    a = vertex1.gsmart() + last;
//                    b = vertex1.gsmart() + a;
//                    c = vertex1.gsmart() + b;
//                    last = c;
//                    this.faceIndicesA[f] = a;
//                    this.faceIndicesB[f] = b;
//                    this.faceIndicesC[f] = c;
//                } else if (orientation == 2) {
//                    a = a;
//                    b = c;
//                    c = vertex1.gsmart() + last;
//                    last = c;
//                    this.faceIndicesA[f] = a;
//                    this.faceIndicesB[f] = b;
//                    this.faceIndicesC[f] = c;
//                } else if (orientation == 3) {
//                    a = c;
//                    b = b;
//                    c = vertex1.gsmart() + last;
//                    last = c;
//                    this.faceIndicesA[f] = a;
//                    this.faceIndicesB[f] = b;
//                    this.faceIndicesC[f] = c;
//                } else if (orientation == 4) {
//                    int tmp = a;
//                    a = b;
//                    b = tmp;
//                    c = vertex1.gsmart() + last;
//                    last = c;
//                    this.faceIndicesA[f] = a;
//                    this.faceIndicesB[f] = tmp;
//                    this.faceIndicesC[f] = c;
//                }
//            }
//
//            axis.pos = meta.projectionPlanePointsOffset * 6;
//            for (int f = 0; f < this.texturedFaceCount; f++) {
//                this.texturePCoordinate[f] = axis.g2();
//                this.textureMCoordinate[f] = axis.g2();
//                this.textureNCoordinate[f] = axis.g2();
//            }
//        }
//    }

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
    }

    public Model( Model src,  boolean copyVertexY,  boolean copyFaces) {
        this.vertexCount = src.vertexCount;
        this.faceCount = src.faceCount;
        this.texturedFaceCount = src.texturedFaceCount;

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
    }

//    public static void unload() {
//        metadata = null;
//        head = null;
//        face1 = null;
//        face2 = null;
//        face3 = null;
//        face4 = null;
//        face5 = null;
//        point1 = null;
//        point2 = null;
//        point3 = null;
//        point4 = null;
//        point5 = null;
//        vertex1 = null;
//        vertex2 = null;
//        axis = null;
//        faceClippedX = null;
//        faceNearClipped = null;
//        vertexScreenX = null;
//        vertexScreenY = null;
//        vertexScreenZ = null;
//        vertexViewSpaceX = null;
//        vertexViewSpaceY = null;
//        vertexViewSpaceZ = null;
//        tmpDepthFaceCount = null;
//        tmpDepthFaces = null;
//        tmpPriorityFaceCount = null;
//        tmpPriorityFaces = null;
//        tmpPriority10FaceDepth = null;
//        tmpPriority11FaceDepth = null;
//        tmpPriorityDepthSum = null;
//        sinTable = null;
//        cosTable = null;
//        colourTable = null;
//        divTable2 = null;
//    }
//
//    public static void unpack( Jagfile models) {
//        try {
//            head = new Packet(models.read("ob_head.dat", null));
//            face1 = new Packet(models.read("ob_face1.dat", null));
//            face2 = new Packet(models.read("ob_face2.dat", null));
//            face3 = new Packet(models.read("ob_face3.dat", null));
//            face4 = new Packet(models.read("ob_face4.dat", null));
//            face5 = new Packet(models.read("ob_face5.dat", null));
//            point1 = new Packet(models.read("ob_point1.dat", null));
//            point2 = new Packet(models.read("ob_point2.dat", null));
//            point3 = new Packet(models.read("ob_point3.dat", null));
//            point4 = new Packet(models.read("ob_point4.dat", null));
//            point5 = new Packet(models.read("ob_point5.dat", null));
//            vertex1 = new Packet(models.read("ob_vertex1.dat", null));
//            vertex2 = new Packet(models.read("ob_vertex2.dat", null));
//            axis = new Packet(models.read("ob_axis.dat", null));
//
//            head.pos = 0;
//            point1.pos = 0;
//            point2.pos = 0;
//            point3.pos = 0;
//            point4.pos = 0;
//            vertex1.pos = 0;
//            vertex2.pos = 0;
//
//            int count = head.g2();
//            metadata = new Metadata[count + 100];
//
//            int projectionPlanePointsOffset = 0;
//            int vertexLabelsOffset = 0;
//            int faceColorsOffset = 0;
//            int faceInfosOffset = 0;
//            int facePrioritiesOffset = 0;
//            int faceAlphasOffset = 0;
//            int faceLabelsOffset = 0;
//
//            for ( int i = 0; i < count; i++) {
//                int index = head.g2();
//                Metadata meta = metadata[index] = new Metadata();
//
//                meta.vertexCount = head.g2();
//                meta.faceCount = head.g2();
//                meta.texturedFaceCount = head.g1();
//
//                meta.vertexFlagsOffset = point1.pos;
//                meta.verticesXOffset = point2.pos;
//                meta.verticesYOffset = point3.pos;
//                meta.verticesZOffset = point4.pos;
//                meta.faceIndicesOffset = vertex1.pos;
//                meta.faceIndicesFlagsOffset = vertex2.pos;
//
//                int faceInfosFlag = head.g1();
//                int facePrioritiesFlag = head.g1();
//                int faceAlphasFlag = head.g1();
//                int faceLabelsFlag = head.g1();
//                int vertexLabelsFlag = head.g1();
//
//                for ( int v = 0; v < meta.vertexCount; v++) {
//                    int flags = point1.g1();
//                    if ((flags & 0x1) != 0) {
//                        point2.gsmart();
//                    }
//
//                    if ((flags & 0x2) != 0) {
//                        point3.gsmart();
//                    }
//
//                    if ((flags & 0x4) != 0) {
//                        point4.gsmart();
//                    }
//                }
//
//                for (int f = 0; f < meta.faceCount; f++) {
//                    int type = vertex2.g1();
//                    if (type == 1) {
//                        vertex1.gsmart();
//                        vertex1.gsmart();
//                    }
//                    vertex1.gsmart();
//                }
//
//                meta.faceColorsOffset = faceColorsOffset;
//                faceColorsOffset += meta.faceCount * 2;
//
//                if (faceInfosFlag == 1) {
//                    meta.faceInfosOffset = faceInfosOffset;
//                    faceInfosOffset += meta.faceCount;
//                } else {
//                    meta.faceInfosOffset = -1;
//                }
//
//                if (facePrioritiesFlag == 255) {
//                    meta.facePrioritiesOffset = facePrioritiesOffset;
//                    facePrioritiesOffset += meta.faceCount;
//                } else {
//                    meta.facePrioritiesOffset = -facePrioritiesFlag - 1;
//                }
//
//                if (faceAlphasFlag == 1) {
//                    meta.faceAlphasOffset = faceAlphasOffset;
//                    faceAlphasOffset += meta.faceCount;
//                } else {
//                    meta.faceAlphasOffset = -1;
//                }
//
//                if (faceLabelsFlag == 1) {
//                    meta.faceLabelsOffset = faceLabelsOffset;
//                    faceLabelsOffset += meta.faceCount;
//                } else {
//                    meta.faceLabelsOffset = -1;
//                }
//
//                if (vertexLabelsFlag == 1) {
//                    meta.vertexLabelsOffset = vertexLabelsOffset;
//                    vertexLabelsOffset += meta.vertexCount;
//                } else {
//                    meta.vertexLabelsOffset = -1;
//                }
//
//                meta.projectionPlanePointsOffset = projectionPlanePointsOffset;
//                projectionPlanePointsOffset += meta.texturedFaceCount;
//            }
//        } catch ( Exception ex) {
//            System.out.println("Error loading model index");
//            ex.printStackTrace();
//        }
//    }

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

    public void createLabelReferences() {
        if (this.vertexLabels != null) {
            int[] labelVertexCount = new int[256];

            int count = 0;
            for (int v = 0; v < this.vertexCount; v++) {
                int label = this.vertexLabels[v];
                int countDebug = labelVertexCount[label]++;

                if (label > count) {
                    count = label;
                }
            }

            this.labelVertices = new int[count + 1][];
            for (int label = 0; label <= count; label++) {
                this.labelVertices[label] = new int[labelVertexCount[label]];
                labelVertexCount[label] = 0;
            }

            int v = 0;
            while (v < this.vertexCount) {
                int label = this.vertexLabels[v];
                this.labelVertices[label][labelVertexCount[label]++] = v++;
            }

            this.vertexLabels = null;
        }

        if (this.faceLabels != null) {
            int[] labelFaceCount = new int[256];

            int count = 0;
            for (int f = 0; f < this.faceCount; f++) {
                int label = this.faceLabels[f];
                int countDebug = labelFaceCount[label]++;
                if (label > count) {
                    count = label;
                }
            }

            this.labelFaces = new int[count + 1][];
            for (int label = 0; label <= count; label++) {
                this.labelFaces[label] = new int[labelFaceCount[label]];
                labelFaceCount[label] = 0;
            }

            int face = 0;
            while (face < this.faceCount) {
                int label = this.faceLabels[face];
                this.labelFaces[label][labelFaceCount[label]++] = face++;
            }

            this.faceLabels = null;
        }
    }

//    public void applyTransform( int id) {
//        if (this.labelVertices != null && id != -1) {
//            AnimFrame frame = AnimFrame.instances[id];
//            AnimBase base = frame.base;
//
//            baseX = 0;
//            baseY = 0;
//            baseZ = 0;
//
//            for ( int i = 0; i < frame.length; i++) {
//                int group = frame.groups[i];
//                this.applyTransform(frame.x[i], frame.y[i], frame.z[i], base.labels[group], base.types[group]);
//            }
//        }
//    }

//    public void applyTransforms( int id,  int id2,  int[] walkmerge) {
//        if (id == -1) {
//            return;
//        }
//
//        if (walkmerge == null || id2 == -1) {
//            this.applyTransform(id);
//        } else {
//            AnimFrame frame = AnimFrame.instances[id];
//            AnimFrame frame2 = AnimFrame.instances[id2];
//            AnimBase base = frame.base;
//
//            baseX = 0;
//            baseY = 0;
//            baseZ = 0;
//
//            int length = 0;
//            int merge = walkmerge[length++];
//
//            for ( int i = 0; i < frame.length; i++) {
//                int group = frame.groups[i];
//                while (group > merge) {
//                    merge = walkmerge[length++];
//                }
//
//                if (group != merge || base.types[group] == AnimBase.OP_BASE) {
//                    this.applyTransform(frame.x[i], frame.y[i], frame.z[i], base.labels[group], base.types[group]);
//                }
//            }
//
//            baseX = 0;
//            baseY = 0;
//            baseZ = 0;
//
//            length = 0;
//            merge = walkmerge[length++];
//
//            for (int i = 0; i < frame2.length; i++) {
//                int group = frame2.groups[i];
//                while (group > merge) {
//                    merge = walkmerge[length++];
//                }
//
//                if (group == merge || base.types[group] == AnimBase.OP_BASE) {
//                    this.applyTransform(frame2.x[i], frame2.y[i], frame2.z[i], base.labels[group], base.types[group]);
//                }
//            }
//        }
//    }

//    private void applyTransform( int x,  int y,  int z,  int[] labels,  int type) {
//        int labelCount = labels.length;
//
//        if (type == AnimBase.OP_BASE) {
//            int count = 0;
//            baseX = 0;
//            baseY = 0;
//            baseZ = 0;
//
//            for (int g = 0; g < labelCount; g++) {
//                int label = labels[g];
//                if (label < this.labelVertices.length) {
//                    int[] vertices = this.labelVertices[label];
//                    for (int i = 0; i < vertices.length; i++) {
//                        int v = vertices[i];
//                        baseX += this.verticesX[v];
//                        baseY += this.verticesY[v];
//                        baseZ += this.verticesZ[v];
//                        count++;
//                    }
//                }
//            }
//
//            if (count > 0) {
//                baseX = baseX / count + x;
//                baseY = baseY / count + y;
//                baseZ = baseZ / count + z;
//            } else {
//                baseX = x;
//                baseY = y;
//                baseZ = z;
//            }
//        } else if (type == AnimBase.OP_TRANSLATE) {
//            for (int g = 0; g < labelCount; g++) {
//                int label = labels[g];
//                if (label >= this.labelVertices.length) {
//                    continue;
//                }
//
//                int[] vertices = this.labelVertices[label];
//                for (int i = 0; i < vertices.length; i++) {
//                    int v = vertices[i];
//                    this.verticesX[v] += x;
//                    this.verticesY[v] += y;
//                    this.verticesZ[v] += z;
//                }
//            }
//        } else if (type == AnimBase.OP_ROTATE) {
//            for (int g = 0; g < labelCount; g++) {
//                int label = labels[g];
//                if (label >= this.labelVertices.length) {
//                    continue;
//                }
//
//                int[] vertices = this.labelVertices[label];
//                for (int i = 0; i < vertices.length; i++) {
//                    int v = vertices[i];
//                    this.verticesX[v] -= baseX;
//                    this.verticesY[v] -= baseY;
//                    this.verticesZ[v] -= baseZ;
//
//                    int pitch = (x & 0xFF) * 8;
//                    int yaw = (y & 0xFF) * 8;
//                    int roll = (z & 0xFF) * 8;
//
//                    int sin;
//                    int cos;
//
//                    if (roll != 0) {
//                        sin = Model.sinTable[roll];
//                        cos = Model.cosTable[roll];
//                        int x_ = this.verticesY[v] * sin + this.verticesX[v] * cos >> 16;
//                        this.verticesY[v] = this.verticesY[v] * cos - this.verticesX[v] * sin >> 16;
//                        this.verticesX[v] = x_;
//                    }
//
//                    if (pitch != 0) {
//                        sin = Model.sinTable[pitch];
//                        cos = Model.cosTable[pitch];
//                        int y_ = this.verticesY[v] * cos - this.verticesZ[v] * sin >> 16;
//                        this.verticesZ[v] = this.verticesY[v] * sin + this.verticesZ[v] * cos >> 16;
//                        this.verticesY[v] = y_;
//                    }
//
//                    if (yaw != 0) {
//                        sin = Model.sinTable[yaw];
//                        cos = Model.cosTable[yaw];
//                        int x_ = this.verticesZ[v] * sin + this.verticesX[v] * cos >> 16;
//                        this.verticesZ[v] = this.verticesZ[v] * cos - this.verticesX[v] * sin >> 16;
//                        this.verticesX[v] = x_;
//                    }
//
//                    this.verticesX[v] += baseX;
//                    this.verticesY[v] += baseY;
//                    this.verticesZ[v] += baseZ;
//                }
//            }
//        } else if (type == AnimBase.OP_SCALE) {
//            for (int g = 0; g < labelCount; g++) {
//                int label = labels[g];
//                if (label >= this.labelVertices.length) {
//                    continue;
//                }
//
//                int[] vertices = this.labelVertices[label];
//                for (int i = 0; i < vertices.length; i++) {
//                    int v = vertices[i];
//
//                    this.verticesX[v] -= baseX;
//                    this.verticesY[v] -= baseY;
//                    this.verticesZ[v] -= baseZ;
//
//                    this.verticesX[v] = this.verticesX[v] * x / 128;
//                    this.verticesY[v] = this.verticesY[v] * y / 128;
//                    this.verticesZ[v] = this.verticesZ[v] * z / 128;
//
//                    this.verticesX[v] += baseX;
//                    this.verticesY[v] += baseY;
//                    this.verticesZ[v] += baseZ;
//                }
//            }
//        } else if (type == AnimBase.OP_ALPHA && (this.labelFaces != null && this.faceAlphas != null)) {
//            for (int g = 0; g < labelCount; g++) {
//                int label = labels[g];
//                if (label >= this.labelFaces.length) {
//                    continue;
//                }
//
//                int[] triangles = this.labelFaces[label];
//                for (int i = 0; i < triangles.length; i++) {
//                    int t = triangles[i];
//
//                    this.faceAlphas[t] += x * 8;
//                    if (this.faceAlphas[t] < 0) {
//                        this.faceAlphas[t] = 0;
//                    }
//
//                    if (this.faceAlphas[t] > 255) {
//                        this.faceAlphas[t] = 255;
//                    }
//                }
//            }
//        }
//    }

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

    public void drawSimple( int pitch,  int yaw,  int roll,  int eyePitch,  int eyeX,  int eyeY,  int eyeZ) {
        int centerX = Pix3D.centerW3D;
        int centerY = Pix3D.centerH3D;
        int sinPitch = sinTable[pitch];
        int cosPitch = cosTable[pitch];
        int sinYaw = sinTable[yaw];
        int cosYaw = cosTable[yaw];
        int sinRoll = sinTable[roll];
        int cosRoll = cosTable[roll];
        int sinEyePitch = sinTable[eyePitch];
        int cosEyePitch = cosTable[eyePitch];
        int midZ = eyeY * sinEyePitch + eyeZ * cosEyePitch >> 16;

        for ( int v = 0; v < this.vertexCount; v++) {
            int x = this.verticesX[v];
            int y = this.verticesY[v];
            int z = this.verticesZ[v];

            int temp;
            if (roll != 0) {
                temp = y * sinRoll + x * cosRoll >> 16;
                y = y * cosRoll - x * sinRoll >> 16;
                x = temp;
            }

            if (pitch != 0) {
                temp = y * cosPitch - z * sinPitch >> 16;
                z = y * sinPitch + z * cosPitch >> 16;
                y = temp;
            }

            if (yaw != 0) {
                temp = z * sinYaw + x * cosYaw >> 16;
                z = z * cosYaw - x * sinYaw >> 16;
                x = temp;
            }

            x += eyeX;
            y += eyeY;
            z += eyeZ;

            temp = y * cosEyePitch - z * sinEyePitch >> 16;
            z = y * sinEyePitch + z * cosEyePitch >> 16;

            vertexScreenZ[v] = z - midZ;
            vertexScreenX[v] = centerX + (x << 9) / z;
            vertexScreenY[v] = centerY + (temp << 9) / z;

            if (this.texturedFaceCount > 0) {
                vertexViewSpaceX[v] = x;
                vertexViewSpaceY[v] = temp;
                vertexViewSpaceZ[v] = z;
            }
        }

        try {
            this.draw(false, false, 0);
        } catch ( Exception ex) {
        }
    }

    public void draw( int yaw,  int sinEyePitch,  int cosEyePitch,  int sinEyeYaw,  int cosEyeYaw,  int relativeX,  int relativeY,  int relativeZ,  int bitset) {
        int zPrime = relativeZ * cosEyeYaw - relativeX * sinEyeYaw >> 16;
        int midZ = relativeY * sinEyePitch + zPrime * cosEyePitch >> 16;
        int radiusCosEyePitch = this.radius * cosEyePitch >> 16;

        int maxZ = midZ + radiusCosEyePitch;
        if (maxZ <= 50 || midZ >= 3500) {
            return;
        }

        int midX = relativeZ * sinEyeYaw + relativeX * cosEyeYaw >> 16;
        int leftX = midX - this.radius << 9;
        if (leftX / maxZ >= Pix2D.centerW2D) {
            return;
        }

        int rightX = midX + this.radius << 9;
        if (rightX / maxZ <= -Pix2D.centerW2D) {
            return;
        }

        int midY = relativeY * cosEyePitch - zPrime * sinEyePitch >> 16;
        int radiusSinEyePitch = this.radius * sinEyePitch >> 16;

        int bottomY = midY + radiusSinEyePitch << 9;
        if (bottomY / maxZ <= -Pix2D.centerH2D) {
            return;
        }

        int yPrime = radiusSinEyePitch + (this.maxY * cosEyePitch >> 16);
        int topY = midY - yPrime << 9;
        if (topY / maxZ >= Pix2D.centerH2D) {
            return;
        }

        int radiusZ = radiusCosEyePitch + (this.maxY * sinEyePitch >> 16);

        boolean clipped = midZ - radiusZ <= 50;
        boolean picking = false;

        if (bitset > 0 && checkHover) {
            int z = midZ - radiusCosEyePitch;
            if (z <= 50) {
                z = 50;
            }

            if (midX > 0) {
                leftX /= maxZ;
                rightX /= z;
            } else {
                rightX /= maxZ;
                leftX /= z;
            }

            if (midY > 0) {
                topY /= maxZ;
                bottomY /= z;
            } else {
                bottomY /= maxZ;
                topY /= z;
            }

            int mouseX = Model.mouseX - Pix3D.centerW3D;
            int mouseY = mouseZ - Pix3D.centerH3D;
            if (mouseX > leftX && mouseX < rightX && mouseY > topY && mouseY < bottomY) {
                if (this.pickable) {
                    pickedBitsets[pickedCount++] = bitset;
                } else {
                    picking = true;
                }
            }
        }

        int centerX = Pix3D.centerW3D;
        int centerY = Pix3D.centerH3D;

        int sinYaw = 0;
        int cosYaw = 0;
        if (yaw != 0) {
            sinYaw = sinTable[yaw];
            cosYaw = cosTable[yaw];
        }

        for ( int v = 0; v < this.vertexCount; v++) {
            int x = this.verticesX[v];
            int y = this.verticesY[v];
            int z = this.verticesZ[v];

            int temp;
            if (yaw != 0) {
                temp = z * sinYaw + x * cosYaw >> 16;
                z = z * cosYaw - x * sinYaw >> 16;
                x = temp;
            }

            x += relativeX;
            y += relativeY;
            z += relativeZ;

            temp = z * sinEyeYaw + x * cosEyeYaw >> 16;
            z = z * cosEyeYaw - x * sinEyeYaw >> 16;
            x = temp;

            temp = y * cosEyePitch - z * sinEyePitch >> 16;
            z = y * sinEyePitch + z * cosEyePitch >> 16;

            vertexScreenZ[v] = z - midZ;

            if (z >= 50) {
                vertexScreenX[v] = centerX + (x << 9) / z;
                vertexScreenY[v] = centerY + (temp << 9) / z;
            } else {
                vertexScreenX[v] = -5000;
                clipped = true;
            }

            if (clipped || this.texturedFaceCount > 0) {
                vertexViewSpaceX[v] = x;
                vertexViewSpaceY[v] = temp;
                vertexViewSpaceZ[v] = z;
            }
        }

        try {
            this.draw(clipped, picking, bitset);
        } catch ( Exception ex) {
        }
    }

    private void draw( boolean clipped,  boolean picking,  int bitset) {
        for ( int depth = 0; depth < this.maxDepth; depth++) {
            tmpDepthFaceCount[depth] = 0;
        }

        for ( int f = 0; f < this.faceCount; f++) {
            if (this.faceInfos == null || this.faceInfos[f] != -1) {
                int a = this.faceIndicesA[f];
                int b = this.faceIndicesB[f];
                int c = this.faceIndicesC[f];

                int xA = vertexScreenX[a];
                int xB = vertexScreenX[b];
                int xC = vertexScreenX[c];

                if (clipped && (xA == -5000 || xB == -5000 || xC == -5000)) {
                    faceNearClipped[f] = true;
                    int depthAverage = (vertexScreenZ[a] + vertexScreenZ[b] + vertexScreenZ[c]) / 3 + this.minDepth;
                    tmpDepthFaces[depthAverage][tmpDepthFaceCount[depthAverage]++] = f;
                } else {
                    if (picking && this.pointWithinTriangle(mouseX, mouseZ, vertexScreenY[a], vertexScreenY[b], vertexScreenY[c], xA, xB, xC)) {
                        pickedBitsets[pickedCount++] = bitset;
                        picking = false;
                    }

                    if ((xA - xB) * (vertexScreenY[c] - vertexScreenY[b]) - (vertexScreenY[a] - vertexScreenY[b]) * (xC - xB) > 0) {
                        faceNearClipped[f] = false;
                        faceClippedX[f] = xA < 0 || xB < 0 || xC < 0 || xA > Pix2D.safeWidth || xB > Pix2D.safeWidth || xC > Pix2D.safeWidth;
                        int depthAverage = (vertexScreenZ[a] + vertexScreenZ[b] + vertexScreenZ[c]) / 3 + this.minDepth;
                        tmpDepthFaces[depthAverage][tmpDepthFaceCount[depthAverage]++] = f;
                    }
                }
            }
        }

        if (this.facePriorities == null) {
            for (int depth = this.maxDepth - 1; depth >= 0; depth--) {
                int count = tmpDepthFaceCount[depth];
                if (count > 0) {
                    int[] faces = tmpDepthFaces[depth];
                    for (int f = 0; f < count; f++) {
                        this.drawFace(faces[f]);
                    }
                }
            }
            return;
        }

        for (int priority = 0; priority < 12; priority++) {
            tmpPriorityFaceCount[priority] = 0;
            tmpPriorityDepthSum[priority] = 0;
        }

        for (int depth = this.maxDepth - 1; depth >= 0; depth--) {
            int faceCount = tmpDepthFaceCount[depth];
            if (faceCount > 0) {
                int[] faces = tmpDepthFaces[depth];
                for (int i = 0; i < faceCount; i++) {
                    int priorityDepth = faces[i];
                    int priorityFace = this.facePriorities[priorityDepth];
                    int priorityFaceCount = tmpPriorityFaceCount[priorityFace]++;
                    tmpPriorityFaces[priorityFace][priorityFaceCount] = priorityDepth;
                    if (priorityFace < 10) {
                        tmpPriorityDepthSum[priorityFace] += depth;
                    } else if (priorityFace == 10) {
                        tmpPriority10FaceDepth[priorityFaceCount] = depth;
                    } else {
                        tmpPriority11FaceDepth[priorityFaceCount] = depth;
                    }
                }
            }
        }

        int averagePriorityDepthSum1_2 = 0;
        if (tmpPriorityFaceCount[1] > 0 || tmpPriorityFaceCount[2] > 0) {
            averagePriorityDepthSum1_2 = (tmpPriorityDepthSum[1] + tmpPriorityDepthSum[2]) / (tmpPriorityFaceCount[1] + tmpPriorityFaceCount[2]);
        }
        int averagePriorityDepthSum3_4 = 0;
        if (tmpPriorityFaceCount[3] > 0 || tmpPriorityFaceCount[4] > 0) {
            averagePriorityDepthSum3_4 = (tmpPriorityDepthSum[3] + tmpPriorityDepthSum[4]) / (tmpPriorityFaceCount[3] + tmpPriorityFaceCount[4]);
        }
        int averagePriorityDepthSum6_8 = 0;
        if (tmpPriorityFaceCount[6] > 0 || tmpPriorityFaceCount[8] > 0) {
            averagePriorityDepthSum6_8 = (tmpPriorityDepthSum[6] + tmpPriorityDepthSum[8]) / (tmpPriorityFaceCount[6] + tmpPriorityFaceCount[8]);
        }

        int priorityFace = 0;
        int priorityFaceCount = tmpPriorityFaceCount[10];

        int[] priorityFaces = tmpPriorityFaces[10];
        int[] priorithFaceDepths = tmpPriority10FaceDepth;
        if (priorityFace == priorityFaceCount) {
            priorityFace = 0;
            priorityFaceCount = tmpPriorityFaceCount[11];
            priorityFaces = tmpPriorityFaces[11];
            priorithFaceDepths = tmpPriority11FaceDepth;
        }

        int priorityDepth;
        if (priorityFace < priorityFaceCount) {
            priorityDepth = priorithFaceDepths[priorityFace];
        } else {
            priorityDepth = -1000;
        }

        for ( int priority = 0; priority < 10; priority++) {
            while (priority == 0 && priorityDepth > averagePriorityDepthSum1_2) {
                this.drawFace(priorityFaces[priorityFace++]);

                if (priorityFace == priorityFaceCount && priorityFaces != tmpPriorityFaces[11]) {
                    priorityFace = 0;
                    priorityFaceCount = tmpPriorityFaceCount[11];
                    priorityFaces = tmpPriorityFaces[11];
                    priorithFaceDepths = tmpPriority11FaceDepth;
                }

                if (priorityFace < priorityFaceCount) {
                    priorityDepth = priorithFaceDepths[priorityFace];
                } else {
                    priorityDepth = -1000;
                }
            }

            while (priority == 3 && priorityDepth > averagePriorityDepthSum3_4) {
                this.drawFace(priorityFaces[priorityFace++]);

                if (priorityFace == priorityFaceCount && priorityFaces != tmpPriorityFaces[11]) {
                    priorityFace = 0;
                    priorityFaceCount = tmpPriorityFaceCount[11];
                    priorityFaces = tmpPriorityFaces[11];
                    priorithFaceDepths = tmpPriority11FaceDepth;
                }

                if (priorityFace < priorityFaceCount) {
                    priorityDepth = priorithFaceDepths[priorityFace];
                } else {
                    priorityDepth = -1000;
                }
            }

            while (priority == 5 && priorityDepth > averagePriorityDepthSum6_8) {
                this.drawFace(priorityFaces[priorityFace++]);

                if (priorityFace == priorityFaceCount && priorityFaces != tmpPriorityFaces[11]) {
                    priorityFace = 0;
                    priorityFaceCount = tmpPriorityFaceCount[11];
                    priorityFaces = tmpPriorityFaces[11];
                    priorithFaceDepths = tmpPriority11FaceDepth;
                }

                if (priorityFace < priorityFaceCount) {
                    priorityDepth = priorithFaceDepths[priorityFace];
                } else {
                    priorityDepth = -1000;
                }
            }

            int count = tmpPriorityFaceCount[priority];
            int[] faces = tmpPriorityFaces[priority];
            for ( int i = 0; i < count; i++) {
                this.drawFace(faces[i]);
            }
        }

        while (priorityDepth != -1000) {
            this.drawFace(priorityFaces[priorityFace++]);

            if (priorityFace == priorityFaceCount && priorityFaces != tmpPriorityFaces[11]) {
                priorityFace = 0;
                priorityFaces = tmpPriorityFaces[11];
                priorityFaceCount = tmpPriorityFaceCount[11];
                priorithFaceDepths = tmpPriority11FaceDepth;
            }

            if (priorityFace < priorityFaceCount) {
                priorityDepth = priorithFaceDepths[priorityFace];
            } else {
                priorityDepth = -1000;
            }
        }
    }

    private void drawFace( int face) {
        if (faceNearClipped[face]) {
            this.drawNearClippedFace(face);
            return;
        }

        int a = this.faceIndicesA[face];
        int b = this.faceIndicesB[face];
        int c = this.faceIndicesC[face];

        Pix3D.hclip = faceClippedX[face];

        if (this.faceAlphas == null) {
            Pix3D.trans = 0;
        } else {
            Pix3D.trans = this.faceAlphas[face];
        }

        int type;
        if (this.faceInfos == null) {
            type = 0;
        } else {
            type = this.faceInfos[face] & 0x3;
        }

        if (type == 0) {
            Pix3D.gouraudTriangle(vertexScreenX[a], vertexScreenX[b], vertexScreenX[c], vertexScreenY[a], vertexScreenY[b], vertexScreenY[c], this.faceColorA[face], this.faceColorB[face], this.faceColorC[face]);
        } else if (type == 1) {
            Pix3D.flatTriangle(vertexScreenX[a], vertexScreenX[b], vertexScreenX[c], vertexScreenY[a], vertexScreenY[b], vertexScreenY[c], colourTable[this.faceColorA[face]]);
        } else if (type == 2) {
            int texturedFace = this.faceInfos[face] >> 2;
            int tA = this.texturePCoordinate[texturedFace];
            int tB = this.textureMCoordinate[texturedFace];
            int tC = this.textureNCoordinate[texturedFace];
            Pix3D.textureTriangle(vertexScreenX[a], vertexScreenX[b], vertexScreenX[c], vertexScreenY[a], vertexScreenY[b], vertexScreenY[c], this.faceColorA[face], this.faceColorB[face], this.faceColorC[face], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
        } else if (type == 3) {
            int texturedFace = this.faceInfos[face] >> 2;
            int tA = this.texturePCoordinate[texturedFace];
            int tB = this.textureMCoordinate[texturedFace];
            int tC = this.textureNCoordinate[texturedFace];
            Pix3D.textureTriangle(vertexScreenX[a], vertexScreenX[b], vertexScreenX[c], vertexScreenY[a], vertexScreenY[b], vertexScreenY[c], this.faceColorA[face], this.faceColorA[face], this.faceColorA[face], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
        }
    }

    private void drawNearClippedFace( int face) {
        int centerX = Pix3D.centerW3D;
        int centerY = Pix3D.centerH3D;
        int elements = 0;

        int a = this.faceIndicesA[face];
        int b = this.faceIndicesB[face];
        int c = this.faceIndicesC[face];

        int zA = vertexViewSpaceZ[a];
        int zB = vertexViewSpaceZ[b];
        int zC = vertexViewSpaceZ[c];

        if (zA >= 50) {
            clippedX[elements] = vertexScreenX[a];
            clippedY[elements] = vertexScreenY[a];
            clippedColor[elements++] = this.faceColorA[face];
        } else {
            int xA = vertexViewSpaceX[a];
            int yA = vertexViewSpaceY[a];
            int colorA = this.faceColorA[face];

            if (zC >= 50) {
                int scalar = (50 - zA) * divTable2[zC - zA];
                clippedX[elements] = centerX + (xA + ((vertexViewSpaceX[c] - xA) * scalar >> 16) << 9) / 50;
                clippedY[elements] = centerY + (yA + ((vertexViewSpaceY[c] - yA) * scalar >> 16) << 9) / 50;
                clippedColor[elements++] = colorA + ((this.faceColorC[face] - colorA) * scalar >> 16);
            }

            if (zB >= 50) {
                int scalar = (50 - zA) * divTable2[zB - zA];
                clippedX[elements] = centerX + (xA + ((vertexViewSpaceX[b] - xA) * scalar >> 16) << 9) / 50;
                clippedY[elements] = centerY + (yA + ((vertexViewSpaceY[b] - yA) * scalar >> 16) << 9) / 50;
                clippedColor[elements++] = colorA + ((this.faceColorB[face] - colorA) * scalar >> 16);
            }
        }

        if (zB >= 50) {
            clippedX[elements] = vertexScreenX[b];
            clippedY[elements] = vertexScreenY[b];
            clippedColor[elements++] = this.faceColorB[face];
        } else {
            int xB = vertexViewSpaceX[b];
            int yB = vertexViewSpaceY[b];
            int colorB = this.faceColorB[face];

            if (zA >= 50) {
                int scalar = (50 - zB) * divTable2[zA - zB];
                clippedX[elements] = centerX + (xB + ((vertexViewSpaceX[a] - xB) * scalar >> 16) << 9) / 50;
                clippedY[elements] = centerY + (yB + ((vertexViewSpaceY[a] - yB) * scalar >> 16) << 9) / 50;
                clippedColor[elements++] = colorB + ((this.faceColorA[face] - colorB) * scalar >> 16);
            }

            if (zC >= 50) {
                int scalar = (50 - zB) * divTable2[zC - zB];
                clippedX[elements] = centerX + (xB + ((vertexViewSpaceX[c] - xB) * scalar >> 16) << 9) / 50;
                clippedY[elements] = centerY + (yB + ((vertexViewSpaceY[c] - yB) * scalar >> 16) << 9) / 50;
                clippedColor[elements++] = colorB + ((this.faceColorC[face] - colorB) * scalar >> 16);
            }
        }

        if (zC >= 50) {
            clippedX[elements] = vertexScreenX[c];
            clippedY[elements] = vertexScreenY[c];
            clippedColor[elements++] = this.faceColorC[face];
        } else {
            int xC = vertexViewSpaceX[c];
            int yC = vertexViewSpaceY[c];
            int colorC = this.faceColorC[face];

            if (zB >= 50) {
                int scalar = (50 - zC) * divTable2[zB - zC];
                clippedX[elements] = centerX + (xC + ((vertexViewSpaceX[b] - xC) * scalar >> 16) << 9) / 50;
                clippedY[elements] = centerY + (yC + ((vertexViewSpaceY[b] - yC) * scalar >> 16) << 9) / 50;
                clippedColor[elements++] = colorC + ((this.faceColorB[face] - colorC) * scalar >> 16);
            }

            if (zA >= 50) {
                int scalar = (50 - zC) * divTable2[zA - zC];
                clippedX[elements] = centerX + (xC + ((vertexViewSpaceX[a] - xC) * scalar >> 16) << 9) / 50;
                clippedY[elements] = centerY + (yC + ((vertexViewSpaceY[a] - yC) * scalar >> 16) << 9) / 50;
                clippedColor[elements++] = colorC + ((this.faceColorA[face] - colorC) * scalar >> 16);
            }
        }

        int x0 = clippedX[0];
        int x1 = clippedX[1];
        int x2 = clippedX[2];
        int y0 = clippedY[0];
        int y1 = clippedY[1];
        int y2 = clippedY[2];

        if ((x0 - x1) * (y2 - y1) - (y0 - y1) * (x2 - x1) <= 0) {
            return;
        }

        Pix3D.hclip = false;

        if (elements == 3) {
            if (x0 < 0 || x1 < 0 || x2 < 0 || x0 > Pix2D.safeWidth || x1 > Pix2D.safeWidth || x2 > Pix2D.safeWidth) {
                Pix3D.hclip = true;
            }

            int type;
            if (this.faceInfos == null) {
                type = 0;
            } else {
                type = this.faceInfos[face] & 0x3;
            }

            if (type == 0) {
                Pix3D.gouraudTriangle(x0, x1, x2, y0, y1, y2, clippedColor[0], clippedColor[1], clippedColor[2]);
            } else if (type == 1) {
                Pix3D.flatTriangle(x0, x1, x2, y0, y1, y2, colourTable[this.faceColorA[face]]);
            } else if (type == 2) {
                int texturedFace = this.faceInfos[face] >> 2;
                int tA = this.texturePCoordinate[texturedFace];
                int tB = this.textureMCoordinate[texturedFace];
                int tC = this.textureNCoordinate[texturedFace];
                Pix3D.textureTriangle(x0, x1, x2, y0, y1, y2, clippedColor[0], clippedColor[1], clippedColor[2], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
            } else if (type == 3) {
                int texturedFace = this.faceInfos[face] >> 2;
                int tA = this.texturePCoordinate[texturedFace];
                int tB = this.textureMCoordinate[texturedFace];
                int tC = this.textureNCoordinate[texturedFace];
                Pix3D.textureTriangle(x0, x1, x2, y0, y1, y2, this.faceColorA[face], this.faceColorA[face], this.faceColorA[face], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
            }
        } else if (elements == 4) {
            if (x0 < 0 || x1 < 0 || x2 < 0 || x0 > Pix2D.safeWidth || x1 > Pix2D.safeWidth || x2 > Pix2D.safeWidth || clippedX[3] < 0 || clippedX[3] > Pix2D.safeWidth) {
                Pix3D.hclip = true;
            }

            int type;
            if (this.faceInfos == null) {
                type = 0;
            } else {
                type = this.faceInfos[face] & 0x3;
            }

            if (type == 0) {
                Pix3D.gouraudTriangle(x0, x1, x2, y0, y1, y2, clippedColor[0], clippedColor[1], clippedColor[2]);
                Pix3D.gouraudTriangle(x0, x2, clippedX[3], y0, y2, clippedY[3], clippedColor[0], clippedColor[2], clippedColor[3]);
            } else if (type == 1) {
                int colorA = colourTable[this.faceColorA[face]];
                Pix3D.flatTriangle(x0, x1, x2, y0, y1, y2, colorA);
                Pix3D.flatTriangle(x0, x2, clippedX[3], y0, y2, clippedY[3], colorA);
            } else if (type == 2) {
                int texturedFace = this.faceInfos[face] >> 2;
                int tA = this.texturePCoordinate[texturedFace];
                int tB = this.textureMCoordinate[texturedFace];
                int tC = this.textureNCoordinate[texturedFace];
                Pix3D.textureTriangle(x0, x1, x2, y0, y1, y2, clippedColor[0], clippedColor[1], clippedColor[2], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
                Pix3D.textureTriangle(x0, x2, clippedX[3], y0, y2, clippedY[3], clippedColor[0], clippedColor[2], clippedColor[3], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
            } else if (type == 3) {
                int texturedFace = this.faceInfos[face] >> 2;
                int tA = this.texturePCoordinate[texturedFace];
                int tB = this.textureMCoordinate[texturedFace];
                int tC = this.textureNCoordinate[texturedFace];
                Pix3D.textureTriangle(x0, x1, x2, y0, y1, y2, this.faceColorA[face], this.faceColorA[face], this.faceColorA[face], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
                Pix3D.textureTriangle(x0, x2, clippedX[3], y0, y2, clippedY[3], this.faceColorA[face], this.faceColorA[face], this.faceColorA[face], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
            }
        }
    }

    private boolean pointWithinTriangle( int x,  int y,  int yA,  int yB,  int yC,  int xA,  int xB,  int xC) {
        if (y < yA && y < yB && y < yC) {
            return false;
        } else if (y > yA && y > yB && y > yC) {
            return false;
        } else if (x < xA && x < xB && x < xC) {
            return false;
        } else {
            return x <= xA || x <= xB || x <= xC;
        }
    }


    public static final class Metadata {
        public int vertexCount;
        public int faceCount;
        public int texturedFaceCount;
        public int vertexFlagsOffset;
        public int verticesXOffset;
        public int verticesYOffset;
        public int verticesZOffset;
        public int vertexLabelsOffset;
        public int faceIndicesOffset;
        public int faceIndicesFlagsOffset;
        public int faceColorsOffset;
        public int faceInfosOffset;
        public int facePrioritiesOffset;
        public int faceAlphasOffset;
        public int faceLabelsOffset;
        public int projectionPlanePointsOffset;
    }

    public static final class VertexNormal {
        public int x;
        public int y;
        public int z;
        public int w;
    }

}
