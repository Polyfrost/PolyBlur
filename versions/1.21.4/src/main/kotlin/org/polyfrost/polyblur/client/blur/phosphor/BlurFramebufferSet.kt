package org.polyfrost.polyblur.client.blur.phosphor

class BlurFramebufferSet(
    mainHandle: ResourceHandle<RenderTarget>,
    prevHandle: ResourceHandle<RenderTarget>
) : PostChain.TargetBundle {
    companion object {
        val MAIN_LOCATION = PostChain.MAIN_TARGET_ID
        val PREVIOUS_LOCATION = locationOrThrow(PolyBlurConstants.ID, "previous")

        val TARGETS = LevelTargetBundle.MAIN_TARGETS + listOf(PREVIOUS_LOCATION)
    }

    private var mainTarget: ResourceHandle<RenderTarget> = mainHandle
    private var prevTarget: ResourceHandle<RenderTarget> = prevHandle

    override fun replace(identifier: Identifier, newHandle: ResourceHandle<RenderTarget>) {
        when (identifier) {
            MAIN_LOCATION -> mainTarget = newHandle
            PREVIOUS_LOCATION -> prevTarget = newHandle
            else -> throw IllegalArgumentException("Unknown location: $identifier")
        }
    }

    override fun get(identifier: Identifier): ResourceHandle<RenderTarget> {
        return when (identifier) {
            MAIN_LOCATION -> mainTarget
            PREVIOUS_LOCATION -> prevTarget
            else -> throw IllegalArgumentException("Unknown location: $identifier")
        }
    }
}