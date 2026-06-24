package org.polyfrost.polyblur.client.blur.phosphor

//? if >=1.21.5 {
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.systems.RenderSystem
//? if >=26.2
/*import com.mojang.blaze3d.PrimitiveTopology*/
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat

object FullscreenQuad {
    val vertexBuffer: GpuBuffer by lazy {
        val storage = ByteBufferBuilder(DefaultVertexFormat.POSITION.vertexSize * 4)
        val builder = BufferBuilder(
            storage,
            //? if >=26.2
            /*PrimitiveTopology.QUADS,*/
            //? if <26.2
            VertexFormat.Mode.QUADS,
            DefaultVertexFormat.POSITION
        )

        builder.addVertex(-1f, -1f, 0f)
        builder.addVertex(1f, -1f, 0f)
        builder.addVertex(1f, 1f, 0f)
        builder.addVertex(-1f, 1f, 0f)

        builder.buildOrThrow().use { mesh ->
            //? if =1.21.5 {
            /*DefaultVertexFormat.POSITION.uploadImmediateVertexBuffer(mesh.vertexBuffer())
            *///?} else {
            RenderSystem.getDevice().createBuffer({ "PolyBlur fullscreen quad" }, GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer())
            //?}
        }
    }
}
//?}
