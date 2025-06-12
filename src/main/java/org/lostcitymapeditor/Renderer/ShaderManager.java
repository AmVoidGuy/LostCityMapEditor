package org.lostcitymapeditor.Renderer;

import static org.lwjgl.opengl.GL20.*;

public class ShaderManager {

    private int shaderProgram;

    private static final String VERTEX_SHADER_SOURCE =
            "#version 330 core\n" +
                    "layout (location = 0) in vec3 aPos;\n" +
                    "layout (location = 1) in vec3 aColor;\n" +
                    "layout (location = 2) in vec2 aTexCoord;\n" +
                    "layout (location = 3) in float aUseTexture;\n" +
                    "layout (location = 4) in float aTextureID;\n" +
                    "layout (location = 5) in float aIsHovered;\n" +
                    "\n" +
                    "out vec3 vertexColor;\n" +
                    "out vec2 TexCoord;\n" +
                    "out float useTexture;\n" +
                    "out float textureID;\n" +
                    "out float isHovered;\n" +
                    "\n" +
                    "uniform mat4 model;\n" +
                    "uniform mat4 view;\n" +
                    "uniform mat4 projection;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    gl_Position = projection * view * model * vec4(aPos, 1.0);\n" +
                    "    vertexColor = aColor;\n" +
                    "    TexCoord = aTexCoord;\n" +
                    "    useTexture = aUseTexture;\n" +
                    "    textureID = aTextureID;\n" +
                    "    isHovered = aIsHovered;\n" +
                    "}";

    private static final String FRAGMENT_SHADER_SOURCE =
            "#version 330 core\n" +
                    "in vec3 vertexColor;\n" +
                    "in vec2 TexCoord;\n" +
                    "in float useTexture;\n" +
                    "in float textureID;\n" +
                    "in float isHovered;\n" +
                    "out vec4 FragColor;\n" +
                    "uniform sampler2D currentTexture;\n" +
                    "uniform int expectedTextureID;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    vec3 baseColor = vertexColor;\n" +
                    "\n" +
                    "    if (isHovered > 0.5) { \n" +
                    "        baseColor = min(baseColor + vec3(0.2), vec3(1.0));\n" +
                    "    }\n" +
                    "\n" +
                    "    if (useTexture > 0.5) {\n" +
                    "        int texID = int(round(textureID));\n" +
                    "        if (texID == expectedTextureID) {\n" +
                    "            vec4 texColor = texture(currentTexture, TexCoord);\n" +
                    "            if (texColor.a < 0.1) discard;\n" +
                    "           if (isHovered > 0.5) { \n" +
                    "               FragColor = texColor * vec4(baseColor, 1.0);\n" +
                    "           } else {\n" +
                    "               FragColor = texColor; \n" +
                    "           }   } else {\n" +
                    "            // Fallback for wrong texture ID\n" +
                    "            FragColor = vec4(baseColor, 1.0);\n" +
                    "        }\n" +
                    "    } else {\n" +
                    "        FragColor = vec4(baseColor, 1.0);\n" +
                    "    }\n" +
                    "}";


    public ShaderManager() {

    }

    public int createProgram() {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, VERTEX_SHADER_SOURCE);
        glCompileShader(vertexShader);

        int success = glGetShaderi(vertexShader, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(vertexShader);
            System.err.println("ERROR::SHADER::VERTEX::COMPILATION_FAILED\n" + infoLog);
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, FRAGMENT_SHADER_SOURCE);
        glCompileShader(fragmentShader);

        success = glGetShaderi(fragmentShader, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(fragmentShader);
            System.err.println("ERROR::SHADER::FRAGMENT::COMPILATION_FAILED\n" + infoLog);
        }

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        success = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            String infoLog = glGetProgramInfoLog(shaderProgram);
            System.err.println("ERROR::SHADER::PROGRAM::LINKING_FAILED\n" + infoLog);
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        return shaderProgram;
    }
    public int getShaderProgram() {
        return shaderProgram;
    }
}