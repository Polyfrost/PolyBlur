package me.djtheredstoner.monkeyblur.shader;

import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class Shader {

    public int programID;
    public boolean errored;

    private final Map<String, Integer> uniformCache = new HashMap<>();

    public Shader(int programID, boolean errored) {
        this.programID = programID;
        this.errored = errored;
    }

    public void bindShader() {
        if (errored) return;
        GL20.glUseProgram(programID);
    }

    public void unbindShader() {
        GL20.glUseProgram(0);
    }

    public void delete() {
        if (programID > 0) {
            GL20.glDeleteProgram(programID);
        }
    }

    public int getUniformLocation(String name) {
        if (errored || programID <= 0) return -1;
        return uniformCache.computeIfAbsent(name, uniform -> GL20.glGetUniformLocation(programID, uniform));
    }

    public void setUniform1i(String name, int v1) {
        int location = getUniformLocation(name);
        if (location >= 0) GL20.glUniform1i(location, v1);
    }

    public void setUniform3f(String name, float v1, float v2, float v3) {
        int location = getUniformLocation(name);
        if (location >= 0) GL20.glUniform3f(location, v1, v2, v3);
    }

    public void setUniformMat4(String name, FloatBuffer mat) {
        int location = getUniformLocation(name);
        if (location >= 0) GL20.glUniformMatrix4(location, false, mat);
    }
}
