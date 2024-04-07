#version 400 core

out vec4 FragColor;
// uniform sampler2D inputTexture;

void main() {
    // This may fix a bug when changing screen size it may not.
    // vec2 fragCoord = gl_FragCoord.xy / res;

    // FragColor = texture(inputTexture, fragCoord);
    FragColor = vec4(1, 0, 0, 1);
}