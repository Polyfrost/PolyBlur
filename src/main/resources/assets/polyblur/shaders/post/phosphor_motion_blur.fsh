#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;

out vec4 fragColor;

layout(std140) uniform BlurConfig {
    float Strength;
    float Mode;
};

void main() {
    vec4 curr = texture(DiffuseSampler, texCoord);
    vec4 prev = texture(PrevSampler, texCoord);

    if (Mode < 0.5) {
        // Weighted Max: keep the brightest of the decayed previous frame and the current frame.
        fragColor = vec4(max(prev.rgb * Strength, curr.rgb), 1.0);
    } else if (Mode < 1.5) {
        // Linear Mix: blend linearly towards the previous frame.
        fragColor = vec4(mix(curr.rgb, prev.rgb, Strength), 1.0);
    } else {
        // Alpha Decay: accumulate with a decaying feedback alpha.
        float a = max(0.0, min(prev.a - 0.325, prev.a * Strength * 0.95));
        fragColor = vec4(prev.rgb * a + curr.rgb * (1.0 - a), 1.0);
    }
}
