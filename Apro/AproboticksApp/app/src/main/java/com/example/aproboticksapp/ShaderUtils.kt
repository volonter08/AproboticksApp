package com.example.aproboticksapp

import android.opengl.GLES20
import android.opengl.GLES20.*

class ShaderUtils {
    companion object {
        fun createVertexShaderId(vertexShaderTextCode: String): Int {
            val shaderId = glCreateShader(GL_VERTEX_SHADER)
            if (shaderId == 0) {
                return 0
            }
            glShaderSource(shaderId, vertexShaderTextCode)
            glCompileShader(shaderId)
            val compileStatus = IntArray(1)
            glGetShaderiv(shaderId, GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                glDeleteShader(shaderId)
                return 0
            }
            return shaderId
        }

        fun createFragmentShaderId(fragmentShaderText: String): Int {
            val fragmentId = glCreateShader(GL_FRAGMENT_SHADER)
            if (fragmentId == 0) {
                return 0
            }
            glShaderSource(fragmentId, fragmentShaderText)
            glCompileShader(fragmentId)
            val compileStatus = IntArray(1)
            glGetShaderiv(fragmentId, GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == GL_FALSE) {
                glDeleteShader(fragmentId)
                return 0
            }
            return fragmentId
        }
        fun createProgram(vertexShaderId:Int,fragmentShaderId:Int):Int{
            val program = glCreateProgram()
            if(program== GL_FALSE){
                return 0
            }
            glAttachShader(program,vertexShaderId)
            glAttachShader(program,fragmentShaderId)
            glLinkProgram(program)
            val linkStatus = IntArray(1)
            glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                glDeleteProgram(program)
                return 0
            }
            return program
        }
    }
}