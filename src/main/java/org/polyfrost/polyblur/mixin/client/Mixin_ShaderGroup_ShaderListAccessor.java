package org.polyfrost.polyblur.mixin.client;

import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ShaderGroup.class)
public interface Mixin_ShaderGroup_ShaderListAccessor {
    @Accessor("listShaders") List<Shader> getListShaders();
}
