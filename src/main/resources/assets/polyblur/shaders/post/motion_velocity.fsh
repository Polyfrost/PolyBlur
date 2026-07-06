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
};

// Temporal blend factor for the velocity buffer: fraction of the freshly
// computed velocity mixed into the running history each frame. Lower values
// smooth harder, suppressing the frame-to-frame flicker that camera
// reprojection produces on geometry that moves independently of the camera
// (the first-person hand, the third-person player head).
const float VELOCITY_SMOOTHING = 0.35;

void main() {
    float depth = texture(DepthSampler, texCoord).r;
    float ndcZ = (ZZeroToOne > 0.5) ? depth : (depth * 2.0 - 1.0);
    vec4 ndc = vec4(texCoord * 2.0 - 1.0, ndcZ, 1.0);

    vec4 prevClip;
    if (depth >= 0.99999) {
        prevClip = Reproj * ndc;
    } else {
        float Hw = dot(InvRow3, ndc);
        prevClip = (Reproj * ndc) / Hw + D;
    }

    vec2 prevUV = (prevClip.xy / prevClip.w) * 0.5 + 0.5;

    vec2 vel = (texCoord - prevUV) * TimeScale;
    vec2 enc = clamp(vel / MaxVel, -1.0, 1.0) * 0.5 + 0.5;

    float alpha = 1.0 - pow(1.0 - VELOCITY_SMOOTHING, 1.0 / max(TimeScale, 1e-6));
    vec2 hist = texture(HistorySampler, texCoord).rg;
    fragColor = vec4(mix(hist, enc, alpha), 0.0, 1.0);
}
