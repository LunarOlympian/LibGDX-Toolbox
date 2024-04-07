#version 400 core

// Input values
in vec4 a_position;
uniform mat4 u_projTrans;

void main() {
    gl_Position = u_projTrans * a_position;
}