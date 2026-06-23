#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

out vec4 fragColor;

layout(std140) uniform MotionBlurConfig {
    vec2 Velocity;
    float Samples;
    float Jitter;
};

float gnoise(vec2 p) {
    return fract(52.9829189 * fract(dot(p, vec2(0.06711056, 0.00583715))));
}

void main() {
    int n = int(Samples);
    if (n < 2 || dot(Velocity, Velocity) < 1e-9) {
        fragColor = texture(DiffuseSampler, texCoord);
        return;
    }

    float j = (gnoise(gl_FragCoord.xy) - 0.5) * Jitter;

    vec4 acc = vec4(0.0);
    float total = 0.0;
    for (int i = 0; i < n; i++) {
        float t = (float(i) + 0.5 + j) / float(n) - 0.5;
        float w = 1.0 - abs(t) * 2.0;
        acc += texture(DiffuseSampler, texCoord + Velocity * t) * w;
        total += w;
    }

    fragColor = total > 0.0 ? acc / total : texture(DiffuseSampler, texCoord);
}
