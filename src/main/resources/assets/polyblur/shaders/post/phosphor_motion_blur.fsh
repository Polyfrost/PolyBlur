//? if >=26.3 {
#version 330
#extension GL_ARB_separate_shader_objects : require
//?}
//? if <26.3 {
//#version 150
//?}

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

    if (Mode < 0.5) {
        // weighted max
        fragColor = vec4(max(prev.rgb * Strength, curr.rgb), 1.0);
    } else if (Mode < 1.5) {
        // linear mix
        fragColor = vec4(mix(curr.rgb, prev.rgb, Strength), 1.0);
    } else {
        // alpha decay
        float a = max(0.0, min(prev.a - 0.325, prev.a * Strength * 0.95));
        fragColor = vec4(prev.rgb * a + curr.rgb * (1.0 - a), 1.0);
    }
}
