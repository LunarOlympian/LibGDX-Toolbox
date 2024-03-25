#version 400 core

in vec4 a_position;
uniform vec4 proj;



void main() {
    gl_Position = proj * a_position;
}
