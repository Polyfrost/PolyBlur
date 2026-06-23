#version 150

// without UBO support (1.21.5 and below)

uniform sampler2D DepthSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform mat4 Reproj;    // prevVP * inverse(curVP)
uniform vec4 InvRow3;   // 4th row of inverse(curVP)
uniform vec4 D;         // prevVP * vec4(cameraDelta, 0)
uniform float MaxVel;

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
