#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
out vec4 fragColor;

uniform float VelocityX;
uniform float VelocityY;
uniform float Samples;
uniform float Jitter;

#define Velocity vec2(VelocityX, VelocityY)

float gnoise(vec2 p) {
    return fract(52.9829189 * fract(dot(p, vec2(0.06711056, 0.00583715))));
}

void main() {
    int n = int(Samples);
    if (n < 2 || dot(Velocity, Velocity) < 1e-9) {
        fragColor = vec4(textureLod(DiffuseSampler, texCoord, 0.0).rgb, 1.0);
        return;
    }

    float j = (gnoise(gl_FragCoord.xy) - 0.5) * Jitter;
    float invN = 1.0 / float(n);

    float t = (0.5 + j) * invN - 0.5;
    vec2 uv = texCoord + Velocity * t;
    vec2 duv = Velocity * invN;

    vec3 acc = vec3(0.0);
    float total = 0.0;
    for (int i = 0; i < n; i++) {
        float w = 1.0 - abs(t) * 2.0;
        acc += textureLod(DiffuseSampler, uv, 0.0).rgb * w;
        total += w;
        uv += duv;
        t += invN;
    }

    fragColor = vec4(total > 0.0 ? acc / total : textureLod(DiffuseSampler, texCoord, 0.0).rgb, 1.0);
}
