package cc.polyfrost.polyblur.blurs.phosphor;

import net.minecraft.client.shader.ShaderGroup;

public interface EntityRendererHook {
    ShaderGroup getPhosphorShader();
    void setPhosphorShader(ShaderGroup phosphorShader);
}
