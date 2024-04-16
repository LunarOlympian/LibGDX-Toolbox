#version 400 core

// Input values
in vec4 a_position;
uniform mat4 u_projTrans;
uniform vec4 coord_offset;
uniform float scale;

void main() {
    vec4 aPos = vec4(a_position.x * scale, a_position.y * scale, a_position.z * scale, a_position.w);
    aPos = aPos + coord_offset;
    gl_Position = u_projTrans * aPos;
}