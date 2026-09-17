package org.polyfrost.polyblur.mixin.client;

import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
//? if =1.8.9 {
/*import net.minecraft.client.render.PostPass;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
*///?}

@Mixin(PostChain.class)
public interface PostChainAccessor {
    //? if =1.8.9 {
    /*@Accessor
    List<PostPass> getPasses();
    *///?}
}
