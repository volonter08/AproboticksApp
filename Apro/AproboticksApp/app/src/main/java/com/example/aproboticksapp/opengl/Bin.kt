package com.example.aproboticksapp.opengl

import com.example.aproboticksapp.forGson.Read
import com.example.aproboticksapp.forGson.ReadAs
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Bin @Read constructor(
    @ReadAs("id")
    @SerializedName("id ")
    @Expose
    val id: Int,
    @ReadAs("width")
    @SerializedName("width ")
    @Expose
    val width: Float,
    @ReadAs("height")
    @SerializedName("height ")
    @Expose
    val height: Float,
    @ReadAs("depth")
    @SerializedName("height ")
    @Expose
    val depth: Float
):Serializable {
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