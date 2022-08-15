package cc.polyfrost.polyblur.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.io.InputStream;

public class ShaderLoader {

    private static final String DOMAIN = "polyblur";
    private static final Logger LOGGER = LogManager.getLogger("PolyBlur ShaderLoader");

    public static Shader loadShader(String shaderPath) {
        ResourceLocation locationVertex = new ResourceLocation(DOMAIN, "shaders/" + shaderPath + ".vert");
        ResourceLocation locationFrag = new ResourceLocation(DOMAIN, "shaders/" + shaderPath + ".frag");

        String sourceVertex;
        String sourceFrag;
        try {
            sourceVertex = loadShaderSource(locationVertex);
            sourceFrag = loadShaderSource(locationFrag);
        } catch (Exception e) {
            e.printStackTrace();
            return new Shader(-1, true);
        }

        int vertex = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        int fragment = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(vertex, sourceVertex);
        GL20.glShaderSource(fragment, sourceFrag);
        GL20.glCompileShader(vertex);
        GL20.glCompileShader(fragment);

        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertex);
        GL20.glAttachShader(program, fragment);
        GL20.glLinkProgram(program);

        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);

        int status = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            LOGGER.error("Shader " + shaderPath + " link failed!");
            LOGGER.error(GL20.glGetProgramInfoLog(program, 10000));
            return new Shader(program, true);
        }

        return new Shader(program, false);
    }

    private static String loadShaderSource(ResourceLocation location) throws IOException {
        try (InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream()) {
            return IOUtils.toString(is);
        }
    }

}
