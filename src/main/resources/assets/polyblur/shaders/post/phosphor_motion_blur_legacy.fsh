#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;
uniform float Strength;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    fragColor = mix(texture(DiffuseSampler, texCoord), texture(PrevSampler, texCoord), Strength);
}
