#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;
uniform float Strength;
uniform float Mode;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 curr = texture(DiffuseSampler, texCoord);
    vec4 prev = texture(PrevSampler, texCoord);

    if (Mode < 0.5) {
        // Weighted Max
        fragColor = vec4(max(prev.rgb * Strength, curr.rgb), 1.0);
    } else if (Mode < 1.5) {
        // Linear Mix
        fragColor = vec4(mix(curr.rgb, prev.rgb, Strength), 1.0);
    } else {
        // Alpha Decay
        float a = max(0.0, min(prev.a - 0.325, prev.a * Strength * 0.95));
        fragColor = vec4(prev.rgb * a + curr.rgb * (1.0 - a), 1.0);
    }
}
