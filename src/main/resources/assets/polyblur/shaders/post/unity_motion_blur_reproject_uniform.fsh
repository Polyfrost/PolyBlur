#version 150

// Blur pass for versions without UBO support (1.21.5 and  below).
// Same as unity_motion_blur_reproject.fsh but with individual uniforms.

uniform sampler2D DiffuseSampler;
uniform sampler2D VelocitySampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float Intensity;
uniform float MaxSamples;
uniform float Jitter;
uniform float MaxVel;
uniform float InvIntensity;
uniform float MinLen;
uniform float ScreenW;
uniform float ScreenH;

float gnoise(vec2 p) {
    return fract(52.9829189 * fract(dot(p, vec2(0.06711056, 0.00583715))));
}

void main() {
    vec2 vel = (textureLod(VelocitySampler, texCoord, 0.0).rg * 2.0 - 1.0) * MaxVel;

    float len = length(vel);
    if (len < MinLen) {
        fragColor = textureLod(DiffuseSampler, texCoord, 0.0);
        return;
    }

    if (len > Intensity) {
        vel *= Intensity / len;
        len = Intensity;
    }

    float pixels = length(vel * vec2(ScreenW, ScreenH));
    int n = int(clamp(min(len * InvIntensity * MaxSamples, pixels), 2.0, MaxSamples));

    float j = (gnoise(gl_FragCoord.xy) - 0.5) * Jitter;
    float invN = 1.0 / float(n);

    float t = (0.5 + j) * invN - 0.5;
    vec2 uv = texCoord + vel * t;
    vec2 duv = vel * invN;

    vec3 acc = vec3(0.0);
    float total = 0.0;
    for (int i = 0; i < n; i++) {
        float w = 1.0 - abs(t) * 2.0;
        acc += textureLod(DiffuseSampler, uv, 0.0).rgb * w;
        total += w;
        uv += duv;
        t += invN;
    }

    fragColor = total > 0.0 ? vec4(acc / total, 1.0) : textureLod(DiffuseSampler, texCoord, 0.0);
}
