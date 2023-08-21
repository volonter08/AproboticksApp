package com.example.aproboticksapp.opengl

import com.example.aproboticksapp.opengl.Bin
import com.example.aproboticksapp.opengl.Box

data class VisualisationObject(
    val status: Boolean,
    val listBoxes: List<Box>,
    val bin: Bin,
    val error:String?=null
)