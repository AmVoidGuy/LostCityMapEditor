package org.lostcitymapeditor.OriginalCode;

import org.lostcitymapeditor.Loaders.FileLoader;
import org.lostcitymapeditor.OriginalCode.Model;
import org.lostcitymapeditor.Transformers.FloFileTransformer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocType {

    public static final int WALL_STRAIGHT = 0;
    public static final int WALL_DIAGONALCORNER = 1;
    public static final int WALL_L = 2;
    public static final int WALL_SQUARECORNER = 3;
    public static final int WALL_DIAGONAL = 9;
    public static final int WALLDECOR_STRAIGHT_NOOFFSET = 4;
    public static final int WALLDECOR_STRAIGHT_OFFSET = 5;
    public static final int WALLDECOR_DIAGONAL_OFFSET = 6;
    public static final int WALLDECOR_DIAGONAL_NOOFFSET = 7;
    public static final int WALLDECOR_DIAGONAL_BOTH = 8;
    public static final int CENTREPIECE_STRAIGHT = 10;
    public static final int CENTREPIECE_DIAGONAL = 11;
    public static final int GROUNDDECOR = 22;
    public static final int ROOF_STRAIGHT = 12;
    public static final int ROOF_DIAGONAL_WITH_ROOFEDGE = 13;
    public static final int ROOF_DIAGONAL = 14;
    public static final int ROOF_L_CONCAVE = 15;
    public static final int ROOF_L_CONVEX = 16;
    public static final int ROOF_FLAT = 17;
    public static final int ROOFEDGE_STRAIGHT = 18;
    public static final int ROOFEDGE_DIAGONALCORNER = 19;
    public static final int ROOFEDGE_L = 20;
    public static final int ROOFEDGE_SQUARECORNER = 21;
    public static boolean reset;
    private static int count;
    private static int[] offsets;
    private static LocType[] cache;
    private static int cachePos;
    public int index = -1;
    private int[] models;
    private int[] shapes;
    public String name;
    public String desc;
    private int[] recol_s;
    private int[] recol_d;
    public int width = 1;
    public int length = 1;
    public boolean blockwalk = true;
    public boolean blockrange = true;
    public boolean active = false;
    private boolean hillskew = false;
    private boolean sharelight = false;
    public boolean occlude = false;
    public int anim = -1;
    public int wallwidth = 16;
    private byte ambient = 0;
    private byte contrast = 0;
    public String[] op;
    private boolean animHasAlpha = false;
    public int mapfunction = -1;
    public int mapscene = -1;
    private boolean mirror = false;
    public boolean shadow = true;
    private int resizex = 128;
    private int resizey = 128;
    private int resizez = 128;
    private int offsetx = 0;
    private int offsety = 0;
    private int offsetz = 0;
    public String model;
    public int forceapproach = 0;
    public boolean forcedecor = false;
    private static final Map<Integer, LocType> locTypeCache = new ConcurrentHashMap<>();
    private static final Map<Long, Model> modelCache = new ConcurrentHashMap<>();

    public static LocType get(int id) {
        LocType loc = locTypeCache.get(id);
        if (loc != null) {
            return loc;
        }
        loc = new LocType();
        String locName = FileLoader.getLocMap().get(id);
        loc.index = id;
        if (FileLoader.getAllLocMap().containsKey(locName)) {
            Map<String, Object> locData = (Map<String, Object>) FileLoader.getAllLocMap().get(locName);
            loc.name = (String) locData.getOrDefault("name", locName);
            loc.desc = (String) locData.get("desc");
            loc.model = (String) locData.get("model");
            loc.wallwidth = (int) locData.get("wallwidth");
            loc.active = (Boolean) locData.getOrDefault("active", false);
            loc.mapscene = (Integer) locData.getOrDefault("mapscene", 0);
            Map<Integer, int[]> recols = (Map<Integer, int[]>) locData.get("recols");
            if (recols != null) {
                int maxIndex = 0;
                for (Integer index : recols.keySet()) {
                    maxIndex = Math.max(maxIndex, index);
                }
                loc.recol_s = new int[maxIndex + 1];
                loc.recol_d = new int[maxIndex + 1];
                for (Map.Entry<Integer, int[]> entry : recols.entrySet()) {
                    int index = entry.getKey();
                    int[] values = entry.getValue();

                    loc.recol_s[index] = values[0];
                    loc.recol_d[index] = values[1];
                }
            }
            loc.resizex = (Integer) locData.get("resizex");
            loc.resizey = (Integer) locData.get("resizey");
            loc.resizez = (Integer) locData.get("resizez");
            loc.offsetx = (Integer) locData.get("offsetx");
            loc.offsety = (Integer) locData.get("offsety");
            loc.offsetz = (Integer) locData.get("offsetz");
            loc.forcedecor = (Boolean) locData.getOrDefault("forcedecor", false);
            loc.mirror = (Boolean) locData.getOrDefault("mirror", false);
            loc.hillskew = (Boolean) locData.getOrDefault("hillskew", false);
            loc.sharelight = (Boolean) locData.getOrDefault("sharelight", false);
            loc.ambient = (byte) locData.get("ambient");
            loc.contrast = (byte) locData.get("contrast");
            loc.mapfunction = (Integer) locData.get("mapfunction");
            loc.width = (int) locData.getOrDefault("width", 1);
            loc.length = (int) locData.getOrDefault("length", 1);
            loc.mirror = (Boolean) locData.getOrDefault("mirror", false);
            loc.shadow = (Boolean) locData.getOrDefault("shadow", false);
        } else {
            locTypeCache.put(id, loc);
            loc.model = FileLoader.getLocMap().get(id);
        }
        return loc;
    }

    public Model getModel(int modelId, int rotation, int heightmapSW, int heightmapSE, int heightmapNE, int heightmapNW, int transformId, int x, int z) {

        long bitset = ((long) this.index << 6) + ((long) modelId << 12) + ((long) rotation << 3) + ((long) (transformId + 1) << 32);

        if (modelId == -1) {
            return null;
        }

        Model cachedModel = modelCache.get(bitset);

        if (cachedModel != null) {
            cachedModel.baseX = x;
            cachedModel.baseZ = z;
            return cachedModel;
        }

        boolean flipped = this.mirror ^ rotation > 3;
         if (flipped) {
            modelId += 65536;
        }
        Model model = new Model(modelId & 0xFFFF);
        if(model.faceCount == 0)
            return null;
        if (flipped) {
            model.rotateY180();
        }
        model.baseX = x;
        model.baseZ = z;
        boolean scaled = this.resizex != 128 || this.resizey != 128 || this.resizez != 128;
        boolean translated = this.offsetx != 0 || this.offsety != 0 || this.offsetz != 0;

        Model modified = new Model(model, this.recol_s == null, !this.animHasAlpha, rotation == 0 && transformId == -1 && !scaled && !translated);
        while (rotation-- > 0) {
            modified.rotateY90();
        }

        if (this.recol_s != null) {
            for (int i = 0; i < this.recol_s.length; i++) {
                modified.recolor(this.recol_s[i], this.recol_d[i]);
            }
        }

        if (scaled) {
            modified.scale(this.resizex, this.resizey, this.resizez);
        }

        if (translated) {
            modified.translate(this.offsety, this.offsetx, this.offsetz);
        }

        modified.calculateNormals(this.ambient + 64, this.contrast * 5 + 768, -50, -10, -50, !this.sharelight);

        if (this.blockwalk) {
            modified.objRaise = modified.maxY;
        }

        if (this.hillskew || this.sharelight) {
            modified = new Model(modified, this.hillskew, this.sharelight);
        }

        if (this.hillskew) {
            int groundY = (heightmapSW + heightmapSE + heightmapNE + heightmapNW) / 4;

            for (int i = 0; i < modified.vertexCount; i++) {
                int x1 = modified.verticesX[i];
                int z1 = modified.verticesZ[i];

                int heightS = heightmapSW + (heightmapSE - heightmapSW) * (x1 + 64) / 128;
                int heightN = heightmapNW + (heightmapNE - heightmapNW) * (x1 + 64) / 128;
                int y = heightS + (heightN - heightS) * (z1 + 64) / 128;

                modified.verticesY[i] += y - groundY;
            }

            modified.calculateBoundsY();
        }

        return modified;
    }
}
