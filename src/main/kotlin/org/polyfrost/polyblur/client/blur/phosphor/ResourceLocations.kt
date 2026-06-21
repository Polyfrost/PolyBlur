package org.polyfrost.polyblur.client.blur.phosphor

//? if <1.21.11 {
import net.minecraft.resources.ResourceLocation

fun location(namespace: String, path: String): ResourceLocation =
    ResourceLocation.fromNamespaceAndPath(namespace, path)
//?}

//? if >=1.21.11 {
/*import net.minecraft.resources.Identifier

fun location(namespace: String, path: String): Identifier =
    Identifier.fromNamespaceAndPath(namespace, path)
*///?}
