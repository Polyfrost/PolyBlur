package org.polyfrost.polyblur.client.blur.phosphor

enum class BlitPlan {
    COPY,
    DRAW,
    SKIP,
}

fun planBlit(
    srcWidth: Int,
    srcHeight: Int,
    srcTextureWidth: Int,
    srcTextureHeight: Int,
    dstWidth: Int,
    dstHeight: Int,
    dstTextureWidth: Int,
    dstTextureHeight: Int,
): BlitPlan {
    if (srcTextureWidth <= 0 || srcTextureHeight <= 0 || dstTextureWidth <= 0 || dstTextureHeight <= 0) {
        return BlitPlan.SKIP
    }

    if (srcTextureWidth != srcWidth || srcTextureHeight != srcHeight) {
        return BlitPlan.SKIP
    }

    if (dstTextureWidth != dstWidth || dstTextureHeight != dstHeight) {
        return BlitPlan.SKIP
    }

    return if (srcTextureWidth == dstTextureWidth && srcTextureHeight == dstTextureHeight) {
        BlitPlan.COPY
    } else {
        BlitPlan.DRAW
    }
}
