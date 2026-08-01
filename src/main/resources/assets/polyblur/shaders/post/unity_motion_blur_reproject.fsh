#version 150

// post-gui pass 2: directional blur along the per-pixel velocity buffer
// produced by motion_velocity.fsh

uniform sampler2D DiffuseSampler;
uniform sampler2D VelocitySampler;

in vec2 texCoord;

out vec4 fragColor;

layout(std140) uniform BlurConfig {
    float Intensity;
    float MaxSamples;
    float Jitter;
    float MaxVel;
    float InvIntensity;
    float MinLen;
    float ScreenW;
    float ScreenH;
};

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
    vec3 acc = vec3(0.0);
    float total = 0.0;
    for (int i = 0; i < n; i++) {
        float t = (float(i) + 0.5 + j) / float(n) - 0.5;
        float w = 1.0 - abs(t) * 2.0;
        acc += textureLod(DiffuseSampler, texCoord + vel * t, 0.0).rgb * w;
        total += w;
    }

    fragColor = total > 0.0 ? vec4(acc / total, 1.0) : textureLod(DiffuseSampler, texCoord, 0.0);
}
