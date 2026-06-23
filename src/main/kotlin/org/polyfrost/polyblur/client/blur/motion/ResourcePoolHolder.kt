package org.polyfrost.polyblur.client.blur.motion

//? if >=1.21.5 {
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.CrossFrameResourcePool

object ResourcePoolHolder {
    var pool: CrossFrameResourcePool? = null
    var mainTarget: RenderTarget? = null
}
//?}
