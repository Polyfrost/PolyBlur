#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
out vec4 fragColor;

uniform vec2 InSize;

uniform float BlendFactor = 0.7;
uniform float Mode = 1.0;

void main() {
    // Blur by TheKodeToad in Sol-Client, Credit to them
    // https://stackoverflow.com/questions/37913286/glsl-motion-blur-post-processing-2-textures-going-to-the-shader-are-the-same

    vec4 curr = texture(DiffuseSampler, texCoord);
    vec4 prev = texture(PrevSampler, texCoord);

    if (Mode < 0.5) {
        // Weighted Max
        fragColor = vec4(max(prev.rgb * BlendFactor, curr.rgb), 1.0);
    } else if (Mode < 1.5) {
        // Linear Mix
        fragColor = vec4(mix(curr.rgb, prev.rgb, BlendFactor), 1.0);
    } else {
        // Alpha Decay
        float a = max(0.0, min(prev.a - 0.325, prev.a * BlendFactor * 0.95));
        fragColor = vec4(prev.rgb * a + curr.rgb * (1.0 - a), 1.0);
    }
}
