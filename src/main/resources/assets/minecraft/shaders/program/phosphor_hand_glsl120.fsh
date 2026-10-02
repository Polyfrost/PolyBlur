#version 120

uniform sampler2D DiffuseSampler; // current frame with blurred world and sharp hand on top
uniform sampler2D PrevSampler;    // previous final frame used as phosphor feedback history
uniform sampler2D WorldSampler;   // snapshot taken before the hand with blurred world only

varying vec2 texCoord;

uniform float BlendFactor = 0.7;
uniform float Mode = 1.0;

void main() {
    vec4 curr = texture2D(DiffuseSampler, texCoord);
    vec4 prev = texture2D(PrevSampler, texCoord);
    vec4 world = texture2D(WorldSampler, texCoord);

    float diff = length(curr.rgb - world.rgb);
    float mask = smoothstep(0.003, 0.02, diff);

    vec3 phosphor;
    if (Mode < 0.5) {
        // weighted max
        phosphor = max(prev.rgb * BlendFactor, curr.rgb);
    } else if (Mode < 1.5) {
        // linear mix
        phosphor = mix(curr.rgb, prev.rgb, BlendFactor);
    } else {
        // alpha decay
        float a = max(0.0, min(prev.a - 0.325, prev.a * BlendFactor * 0.95));
        phosphor = prev.rgb * a + curr.rgb * (1.0 - a);
    }

    gl_FragColor = vec4(mix(world.rgb, phosphor, mask), 1.0);
}
