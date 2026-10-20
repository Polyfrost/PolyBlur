#version 330
//? if >=26.3
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

//? if >=26.3 {
layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;
//?}
//? if <26.3 {
//in vec2 texCoord;
//
//out vec4 fragColor;
//?}

layout(std140) uniform BlurConfig {
    float Strength;
    float Mode;
};

void main() {
    vec4 curr = textureLod(DiffuseSampler, texCoord, 0.0);
    vec4 prev = textureLod(PrevSampler, texCoord, 0.0);

    float feedback = clamp(Strength, 0.0, 0.99);

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

    fragColor = vec4(blended, 1.0);
}
