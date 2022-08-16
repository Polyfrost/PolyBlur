#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

varying vec2 texCoord;

uniform float Weight = 1.0;

// This is the phosphor shader in super secret settings with a few minor adjustments:
// 1. i have no idea why phosphor was here in the first place i also had no clue that you can times vec3 by a float why is opengl like this
// 2. used mix instead of max so like it goes linearly towards the next color or whatever
// 3. why was there a LerpFactor uniform that wasn't used at all
// i have no idea how to use opengl help me

void main() {
    vec4 CurrTexel = texture2D(DiffuseSampler, texCoord);
    vec4 PrevTexel = texture2D(PrevSampler, texCoord);

    gl_FragColor = vec4(mix(PrevTexel.rgb, CurrTexel.rgb, Weight), 1.0);
}
