#version 150

// without UBO support (1.21.5 and below)

uniform sampler2D DepthSampler;
uniform sampler2D HistorySampler;

in vec2 texCoord;

out vec4 fragColor;

uniform mat4 Reproj;    // prevVP * inverse(curVP)
uniform vec4 InvRow3;   // 4th row of inverse(curVP)
uniform vec4 D;         // prevVP * vec4(cameraDelta, 0)
uniform float MaxVel;

// See motion_velocity.fsh for the rationale behind temporal smoothing.
const float VELOCITY_SMOOTHING = 0.35;

void main() {
    float depth = textureLod(DepthSampler, texCoord, 0.0).r;
    vec4 ndc = vec4(texCoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);

    vec4 clip = Reproj * ndc;
    vec4 prevClip = (depth >= 0.99999) ? clip : (clip / dot(InvRow3, ndc) + D);

    vec2 prevUV = (prevClip.xy / prevClip.w) * 0.5 + 0.5;
    vec2 vel = texCoord - prevUV;
    vec2 enc = clamp(vel / MaxVel, -1.0, 1.0) * 0.5 + 0.5;

    vec2 hist = textureLod(HistorySampler, texCoord, 0.0).rg;
    fragColor = vec4(mix(hist, enc, VELOCITY_SMOOTHING), 0.0, 1.0);
}
