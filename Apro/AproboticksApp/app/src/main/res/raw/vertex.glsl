precision mediump float;
uniform mat4 u_Matrix;
attribute vec4 a_Position;
attribute vec4 a_Color;
attribute vec2 a_TexCoord;
attribute float a_Vertex_Id;
varying vec4 v_Color;
varying vec2 v_TexCoord;
varying float v_Vertex_Id;
void main() {
    gl_Position = u_Matrix * a_Position;
    v_Color = a_Color;
    v_TexCoord = a_TexCoord;
    v_Vertex_Id = a_Vertex_Id;
}