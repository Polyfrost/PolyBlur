#version 150

uniform sampler2D DiffuseSampler; // current frame with blurred world and sharp hand on top
uniform sampler2D PrevSampler;    // previous final frame used as phosphor feedback history
uniform sampler2D WorldSampler;   // snapshot taken before the hand with blurred world only

in vec2 texCoord;

out vec4 fragColor;

layout(std140) uniform BlurConfig {
    float Strength;
    float Mode;
};

void main() {
    vec4 curr = textureLod(DiffuseSampler, texCoord, 0.0);
    vec4 prev = textureLod(PrevSampler, texCoord, 0.0);
    vec4 world = textureLod(WorldSampler, texCoord, 0.0);

    float diff = length(curr.rgb - world.rgb);
    float mask = smoothstep(0.003, 0.02, diff);

    vec3 phosphor;
    if (Mode < 0.5) {
        // weighted max
        phosphor = max(prev.rgb * Strength, curr.rgb);
    } else if (Mode < 1.5) {
        // linear mix
        phosphor = mix(curr.rgb, prev.rgb, Strength);
    } else {
        // alpha decay
        float a = max(0.0, min(prev.a - 0.325, prev.a * Strength * 0.95));
        phosphor = prev.rgb * a + curr.rgb * (1.0 - a);
    }

    fragColor = vec4(mix(world.rgb, phosphor, mask), 1.0);
}
