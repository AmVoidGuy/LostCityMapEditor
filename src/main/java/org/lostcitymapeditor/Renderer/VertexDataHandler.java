package org.lostcitymapeditor.Renderer;

import org.lwjgl.BufferUtils;
import org.lostcitymapeditor.OriginalCode.Pix3D;
import org.lostcitymapeditor.DataObjects.newTriangle;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class VertexDataHandler {
    private int vao;
    private int vbo;
    private static final int STRIDE = 11;

    public void setupVertexDataWithTriangles(List<newTriangle> triangles, int[] vaoAndVbo) {
        if (triangles == null || triangles.isEmpty()) {
            System.out.println("No triangles available");
            return;
        }

        int totalTriangles = triangles.size();
        int totalVertices = totalTriangles * 3;

        float[] interleavedData = new float[totalVertices * STRIDE];

        float[][] defaultTexCoords = {
                {0.0f, 0.0f},
                {1.0f, 0.0f},
                {0.0f, 1.0f}
        };

        int dataIndex = 0;

        for (int triangleIndex = 0; triangleIndex < totalTriangles; triangleIndex++) {
            newTriangle triangle = triangles.get(triangleIndex);

            for (int vertexIndex = 0; vertexIndex < 3; vertexIndex++) {
                interleavedData[dataIndex++] = triangle.vertices[vertexIndex * 3];
                interleavedData[dataIndex++] = triangle.vertices[vertexIndex * 3 + 1];
                interleavedData[dataIndex++] = triangle.vertices[vertexIndex * 3 + 2];

                if (triangle.colors != null) {
                    int color = Pix3D.colourTable[triangle.colors[vertexIndex]];
                    interleavedData[dataIndex++] = ((color >> 16) & 0xFF) / 255.0f;
                    interleavedData[dataIndex++] = ((color >> 8) & 0xFF) / 255.0f;
                    interleavedData[dataIndex++] = (color & 0xFF) / 255.0f;
                } else {
                    interleavedData[dataIndex++] = 1.0f;
                    interleavedData[dataIndex++] = 0.5f;
                    interleavedData[dataIndex++] = 0.2f;
                }

                if (triangle.textureId > -1 && triangle.textureCoordinates != null) {
                    interleavedData[dataIndex++] = triangle.textureCoordinates[vertexIndex * 2];
                    interleavedData[dataIndex++] = triangle.textureCoordinates[vertexIndex * 2 + 1];
                } else if (triangle.textureId > -1) {
                    interleavedData[dataIndex++] = defaultTexCoords[vertexIndex][0];
                    interleavedData[dataIndex++] = defaultTexCoords[vertexIndex][1];
                } else {
                    interleavedData[dataIndex++] = 0.0f;
                    interleavedData[dataIndex++] = 0.0f;
                }

                interleavedData[dataIndex++] = (triangle.textureId > -1) ? 1.0f : 0.0f;

                interleavedData[dataIndex++] = (triangle.textureId > -1) ? triangle.textureId : -1.0f;

                interleavedData[dataIndex++] = 0.0f;
            }
        }

        if (vaoAndVbo[0] != 0) {
            glDeleteVertexArrays(vaoAndVbo[0]);
        }
        if (vaoAndVbo[1] != 0) {
            glDeleteBuffers(vaoAndVbo[1]);
        }

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(interleavedData.length);
        buffer.put(interleavedData).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        int floatSize = Float.BYTES;
        int strideBytes = STRIDE * floatSize;

        glVertexAttribPointer(0, 3, GL_FLOAT, false, strideBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, strideBytes, 3 * floatSize);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 2, GL_FLOAT, false, strideBytes, 6 * floatSize);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, 1, GL_FLOAT, false, strideBytes, 8 * floatSize);
        glEnableVertexAttribArray(3);

        glVertexAttribPointer(4, 1, GL_FLOAT, false, strideBytes, 9 * floatSize);
        glEnableVertexAttribArray(4);

        glVertexAttribPointer(5, 1, GL_FLOAT, false, strideBytes, 10 * floatSize);
        glEnableVertexAttribArray(5);

        glBindVertexArray(0);
        vaoAndVbo[0] = vao;
        vaoAndVbo[1] = vbo;
    }
    public int[] getVaoVbo() {
        return new int[]{vao, vbo};
    }
}