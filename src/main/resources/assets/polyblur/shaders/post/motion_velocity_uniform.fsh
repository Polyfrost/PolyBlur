#version 150

// variant for versions without ubo support

uniform sampler2D DepthSampler;
uniform sampler2D HistorySampler;

in vec2 texCoord;

out vec4 fragColor;

uniform mat4 Reproj;    // prevVP times the inverse of curVP
uniform vec4 InvRow3;   // 4th row of the inverse of curVP
uniform vec4 D;         // prevVP applied to the camera delta as a direction
uniform float MaxVel;

const float VELOCITY_SMOOTHING = 0.35;

void main() {
    float depth = textureLod(DepthSampler, texCoord, 0.0).r;
    vec4 ndc = vec4(texCoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);

    vec4 clip = Reproj * ndc;
    vec4 prevClip;
    if (depth >= 0.99999) {
        prevClip = clip;
    } else {
        prevClip = clip / dot(InvRow3, ndc) + D;
    }

    vec2 prevUV = (prevClip.xy / prevClip.w) * 0.5 + 0.5;
    vec2 vel = texCoord - prevUV;
    vec2 enc = clamp(vel / MaxVel, -1.0, 1.0) * 0.5 + 0.5;

    vec2 hist = textureLod(HistorySampler, texCoord, 0.0).rg;
    fragColor = vec4(mix(hist, enc, VELOCITY_SMOOTHING), 0.0, 1.0);
}
