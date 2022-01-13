#version 130

uniform sampler2D texture;
uniform sampler2D depthtex;

uniform mat4 modelViewInverse;
uniform mat4 projectionInverse;
uniform mat4 previousProjection;
uniform mat4 previousModelView;

uniform vec3 cameraPosition;
uniform vec3 previousCameraPosition;

varying vec4 texcoord;

float maxDepth(vec2 texcoord, sampler2D tex) {
    vec2 t = 1 / textureSize(tex, 0);

//    float depth = texture2D(tex, texcoord.st).x;
//    float depth1 = texture2D(tex, vec2(texcoord.s + t.s, texcoord.t)).x;
//    float depth2 = texture2D(tex, vec2(texcoord.s - t.s, texcoord.t)).x;
//    float depth3 = texture2D(tex, vec2(texcoord.s, texcoord.t + t.t)).x;
//    float depth4 = texture2D(tex, vec2(texcoord.s, texcoord.t - t.t)).x;

//    return max(depth, max(depth1, max(depth2, max(depth3, depth4))));
//    return t.s;

    float depth = 1;

    for (int x = -2; x < 3; x++) {
        for (int y = -2; y < 3; y++) {
            float d = texture2D(tex, texcoord + t * vec2(x, y)).x;
            depth = max(depth, d);
        }
    }

    return depth;
}

void main() {
    vec2 texelSize = textureSize(depthtex, 0);

    vec3 color = texture2D(texture, texcoord.st).rgb;

    float depth = texture2D(depthtex, texcoord.st).x;
    float depth2 = maxDepth(texcoord.st, depthtex);

    /*vec4 H = vec4(texcoord.st, depth, 1.0) * 2.0 - 1.0;

    vec4 worldpos = projectionInverse * H;
    worldpos = modelViewInverse * worldpos;
    worldpos /= worldpos.w;
    worldpos.xyz += cameraPosition;

    vec4 prevpos = worldpos;
    prevpos.xyz -= previousCameraPosition;
    prevpos = previousModelView * prevpos;
    prevpos = previousProjection * prevpos;
    prevpos /= prevpos.w;

    vec2 vel = (H - prevpos).st * 0.01;
    vec2 coord = texcoord.st + vel;

    int count = 1;
    for (int i = 0; i < 30; ++i, coord += vel) {
        if (coord.s > 1.0 || coord.t > 1.0 || coord.s < 0.0 || coord.t < 0.0) break;
        color += texture2D(texture, coord).xyz;
        ++count;
    }
    color /= count;*/

    gl_FragColor = vec4(color.rg, depth, 1);
}