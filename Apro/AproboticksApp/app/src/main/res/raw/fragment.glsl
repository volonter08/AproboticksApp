precision mediump float;
uniform sampler2D u_Texture;
varying vec4 v_Color;
varying vec2 v_TexCoord;
varying float v_Vertex_Id;
void main() {
    if (int(v_Vertex_Id) < 26) {
        gl_FragColor = v_Color;
    }
    else {
        gl_FragColor = texture2D(u_Texture, v_TexCoord);
    }

}