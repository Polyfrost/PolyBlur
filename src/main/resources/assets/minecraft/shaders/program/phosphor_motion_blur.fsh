#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
out vec4 fragColor;

uniform vec2 InSize;

uniform float BlendFactor = 0.7;
uniform float Mode = 1.0;

void main() {
    // blur by TheKodeToad in Sol Client credit to them
    // https://stackoverflow.com/questions/37913286/glsl-motion-blur-post-processing-2-textures-going-to-the-shader-are-the-same

    vec4 curr = textureLod(DiffuseSampler, texCoord, 0.0);
    vec4 prev = textureLod(PrevSampler, texCoord, 0.0);

    float feedback = clamp(BlendFactor, 0.0, 0.95);

    vec3 blended;
    if (Mode < 0.5) {
        // weighted max
        blended = max(prev.rgb * feedback, curr.rgb);
    } else if (Mode < 1.5) {
        // linear mix
        blended = mix(curr.rgb, prev.rgb, feedback);
    } else {
        // alpha decay
        float a = max(0.0, min(prev.a - 0.325, prev.a * feedback * 0.95));
        blended = prev.rgb * a + curr.rgb * (1.0 - a);
    }

    // shrink the residual by one 8-bit step so static pixels converge instead of sticking
    vec3 delta = blended - curr.rgb;
    fragColor = vec4(curr.rgb + sign(delta) * max(abs(delta) - 1.0 / 255.0, 0.0), 1.0);
}
