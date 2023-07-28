package com.example.aproboticksapp.opengl

import com.example.aproboticksapp.forGson.Read
import com.example.aproboticksapp.forGson.ReadAs

data class Bin @Read constructor(
    @ReadAs("id") val id: Int,
    @ReadAs("width") val width: Float,
    @ReadAs("height") val height: Float,
    @ReadAs("depth") val depth: Float
) {
    val leftBottomFront = VertexBox(
        0f,
        0f,
        0f
    )
    val rightBottomFront = VertexBox(
        width,
        0f,
        0f
    )
    val leftTopFront = VertexBox(
        0f,
        0f,
        depth
    )
    val rightTopFront = VertexBox(
        width,
        0f,
        depth
    )
    val leftBottomBack = VertexBox(
        0f,
        height,
        0f
    )
    val rightBottomBack = VertexBox(
        width,
        height,
        0f
    )
    val leftTopBack = VertexBox(
        0f,
        height,
        depth
    )
    val rightTopBack = VertexBox(
        width,
        height,
        depth
    )
}