#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

varying vec2 texCoord;

uniform vec2 InSize;

uniform float BlendFactor = 0.7;
uniform float Mode = 1.0;

void main() {
    // blur by TheKodeToad in Sol Client credit to them
    // https://stackoverflow.com/questions/37913286/glsl-motion-blur-post-processing-2-textures-going-to-the-shader-are-the-same

    vec4 curr = texture2D(DiffuseSampler, texCoord);
    vec4 prev = texture2D(PrevSampler, texCoord);

    if (Mode < 0.5) {
        // weighted max
        gl_FragColor = vec4(max(prev.rgb * BlendFactor, curr.rgb), 1.0);
    } else if (Mode < 1.5) {
        // linear mix
        gl_FragColor = vec4(mix(curr.rgb, prev.rgb, BlendFactor), 1.0);
    } else {
        // alpha decay
        float a = max(0.0, min(prev.a - 0.325, prev.a * BlendFactor * 0.95));
        gl_FragColor = vec4(prev.rgb * a + curr.rgb * (1.0 - a), 1.0);
    }
}
