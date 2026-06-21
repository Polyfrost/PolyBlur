#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;

out vec4 fragColor;

layout(std140) uniform BlurConfig {
    float Strength;
};

void main() {
    fragColor = mix(texture(DiffuseSampler, texCoord), texture(PrevSampler, texCoord), Strength);
}
