#version 150

// Pass 1 pre-GUI

uniform sampler2D DepthSampler;
uniform sampler2D HistorySampler;

in vec2 texCoord;

out vec4 fragColor;

layout(std140) uniform VelocityConfig {
    mat4 Reproj;
    vec4 InvRow3;
    vec4 D;
    float MaxVel;
    float TimeScale;
    float ZZeroToOne;
    float Alpha;
};

void main() {
    float depth = textureLod(DepthSampler, texCoord, 0.0).r;
    float ndcZ = (ZZeroToOne > 0.5) ? depth : (depth * 2.0 - 1.0);
    vec4 ndc = vec4(texCoord * 2.0 - 1.0, ndcZ, 1.0);

    vec4 clip = Reproj * ndc;
    vec4 prevClip;
    if (depth >= 0.99999) {
        prevClip = clip;
    } else {
        prevClip = clip / dot(InvRow3, ndc) + D;
    }

    vec2 prevUV = (prevClip.xy / prevClip.w) * 0.5 + 0.5;

    vec2 vel = (texCoord - prevUV) * TimeScale;
    vec2 enc = clamp(vel / MaxVel, -1.0, 1.0) * 0.5 + 0.5;

    vec2 hist = textureLod(HistorySampler, texCoord, 0.0).rg;
    fragColor = vec4(mix(hist, enc, Alpha), 0.0, 1.0);
}
