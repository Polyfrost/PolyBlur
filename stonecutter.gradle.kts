plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3" /* [SC] DO NOT EDIT */

stonecutter {
    parameters {
        // shaders only used on targets below 1.21.9 have no directives and must keep #version 150
        filters.exclude(
            "resources/assets/minecraft/shaders/**",
            "resources/assets/polyblur/shaders/core/**",
            "resources/assets/polyblur/shaders/post/*_legacy.fsh",
            "resources/assets/polyblur/shaders/post/*_uniform.fsh",
        )
        replacements {
            // vanilla raised its GLSL version to 330 in 1.21.9
            string(eval(current.version, ">=1.21.10")) {
                replace("#version 150", "#version 330")
            }
            string(eval(current.version, "= 1.8.9")) {
                replace("com.mojang.blaze3d.pipeline.RenderTarget", "net.minecraft.client.render.pipeline.RenderTarget")
                replace("com.mojang.blaze3d.platform.GlStateManager", "net.minecraft.client.render.platform.GlStateManager")
                replace("net.minecraft.client.renderer.GameRenderer", "net.minecraft.client.render.GameRenderer")
                replace("net.minecraft.client.renderer.PostChain", "net.minecraft.client.render.PostChain")
                replace("net.minecraft.server.Bootstrap", "net.minecraft.Bootstrap")
                replace("import net.minecraft.util.Mth\n", "import net.minecraft.util.math.MathHelper as Mth\n")
            }
            string(eval(current.version, ">= 26.3")) {
                replace("com.mojang.blaze3d.GpuFormat", "com.mojang.renderpearl.api.GpuFormat")
                replace("com.mojang.blaze3d.PrimitiveTopology", "com.mojang.renderpearl.api.pipeline.PrimitiveTopology")
                replace("com.mojang.blaze3d.buffers.GpuBuffer", "com.mojang.renderpearl.api.buffers.GpuBuffer")
                replace("com.mojang.blaze3d.pipeline.BindGroupLayout", "com.mojang.renderpearl.api.pipeline.BindGroupLayout")
                replace("com.mojang.blaze3d.pipeline.ColorTargetState", "com.mojang.renderpearl.api.pipeline.ColorTargetState")
                replace("com.mojang.blaze3d.pipeline.DepthStencilState", "com.mojang.renderpearl.api.pipeline.DepthStencilState")
                replace("com.mojang.blaze3d.pipeline.RenderPipeline", "com.mojang.renderpearl.api.pipeline.RenderPipeline")
                replace("com.mojang.blaze3d.platform.CompareOp", "com.mojang.renderpearl.api.pipeline.CompareOp")
                replace("com.mojang.blaze3d.platform.GlStateManager", "com.mojang.renderpearl.backend.opengl.GlStateManager")
                replace("com.mojang.blaze3d.shaders.UniformType", "com.mojang.renderpearl.api.pipeline.UniformType")
                replace("com.mojang.blaze3d.systems.RenderPass", "com.mojang.renderpearl.api.commands.RenderPass")
                replace("com.mojang.blaze3d.textures.AddressMode", "com.mojang.renderpearl.api.textures.AddressMode")
                replace("com.mojang.blaze3d.textures.FilterMode", "com.mojang.renderpearl.api.textures.FilterMode")
                replace("com.mojang.blaze3d.textures.GpuSampler", "com.mojang.renderpearl.api.textures.GpuSampler")
                replace("com.mojang.blaze3d.vertex.VertexFormat", "com.mojang.renderpearl.api.vertex.VertexFormat")
            }
        }
    }
}

stonecutter tasks {
    order("publishModrinth")
}
