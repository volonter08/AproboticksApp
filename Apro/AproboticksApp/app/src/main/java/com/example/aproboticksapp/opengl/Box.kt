package com.example.aproboticksapp.opengl

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Box(
    @SerializedName("width")
    @Expose
    val width: Float,
    @SerializedName("height")
    @Expose
    val height:Float,
    @SerializedName("depth")
    @Expose
    val depth: Float,
    @SerializedName("position")
    @Expose
    val position: Position,
    @SerializedName("img")
    @Expose
    val img:String):Serializable{
    val a = 15151
}