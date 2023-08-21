package com.example.aproboticksapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.aproboticksapp.opengl.AproboticsOpenGLView
import com.example.aproboticksapp.opengl.Bin
import com.example.aproboticksapp.opengl.Box
import java.io.Serializable

class OpenGLActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val listBoxes = intent.getSerializableExtra("listBox") as List<Box>
        val bin = intent.getSerializableExtra("bin") as Bin
        val glSurfaceView = AproboticsOpenGLView(this, listBoxes = listBoxes, bin = bin)
        setContentView(glSurfaceView)
    }
}