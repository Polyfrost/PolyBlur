#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float Strength;

void main() {
    // Blur by TheKodeToad in Sol-Client, Credit to them
    // Copied three letters from a stackoverflow question (mix), but that's all I needed to create motion blur.
    // https://stackoverflow.com/questions/37913286/glsl-motion-blur-post-processing-2-textures-going-to-the-shader-are-the-same

    fragColor = mix(texture(DiffuseSampler, texCoord), texture(PrevSampler, texCoord), Strength);
//    fragColor.a = 1.0;
}
