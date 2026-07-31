package org.polyfrost.polyblur.client.blur.phosphor

//? if >1.21.5 {
import com.mojang.blaze3d.systems.RenderPass
//? if <1.21.10 {
/*import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
*///?}

object FullscreenPass {
    fun draw(renderPass: RenderPass) {
        //? if <1.21.10 {
        /*val sequential = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS)
        renderPass.setVertexBuffer(0, FullscreenQuad.vertexBuffer)
        renderPass.setIndexBuffer(sequential.getBuffer(6), sequential.type())
        renderPass.drawIndexed(0, 0, 6, 1)
        *///?}
        //? if >=1.21.10 && <26.2 {
        renderPass.draw(0, 3)
        //?}
        //? if >=26.2 {
        /*renderPass.draw(3, 1, 0, 0)
        *///?}
    }
}
//?}
