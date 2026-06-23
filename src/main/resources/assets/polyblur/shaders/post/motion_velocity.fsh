#version 150

// Pass 1 pre-GUI

uniform sampler2D DepthSampler;

in vec2 texCoord;

out vec4 fragColor;

layout(std140) uniform VelocityConfig {
    mat4 Reproj;
    vec4 InvRow3;
    vec4 D;
    float MaxVel;
};

void main() {
    float depth = texture(DepthSampler, texCoord).r;
    vec4 ndc = vec4(texCoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);

    vec4 prevClip;
    if (depth >= 0.99999) {
        prevClip = Reproj * ndc;
    } else {
        float Hw = dot(InvRow3, ndc);
        prevClip = (Reproj * ndc) / Hw + D;
    }

    vec2 prevUV = (prevClip.xy / prevClip.w) * 0.5 + 0.5;

    vec2 vel = texCoord - prevUV;
    vec2 enc = clamp(vel / MaxVel, -1.0, 1.0);
    fragColor = vec4(enc * 0.5 + 0.5, 0.0, 1.0);
}
