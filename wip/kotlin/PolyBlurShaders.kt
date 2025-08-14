package org.polyfrost.polyblur.client.blur

import dev.deftu.omnicore.client.OmniClient
import dev.deftu.omnicore.client.render.state.OmniManagedBlendState
import dev.deftu.omnicore.client.shaders.OmniShader
import dev.deftu.omnicore.common.OmniIdentifier
import dev.deftu.omnicore.common.resources.findFirst
import net.minecraft.util.ResourceLocation
import org.polyfrost.polyblur.PolyBlurConstants

object PolyBlurShaders {
    const val DOMAIN = PolyBlurConstants.ID

    @JvmStatic
    fun loadShader(name: String): OmniShader {
        val vert = loadSource(OmniIdentifier.create(DOMAIN, "shaders/$name.vert"))
        val frag = loadSource(OmniIdentifier.create(DOMAIN, "shaders/$name.frag"))
        return OmniShader.fromLegacyShader(
            vert = vert,
            frag = frag,
            blend = OmniManagedBlendState.ALPHA,
            vertexFormat = null
        )
    }

    @JvmStatic
    fun loadSource(location: ResourceLocation): String {
        val resource = OmniClient.getInstance()
            .resourceManager
            .findFirst(location)
            .orElseThrow { IllegalStateException("Shader source file not found: $location") }
        return resource.inputStream.reader().readText()
    }
}
