package com.example.aproboticksapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aproboticksapp.opengl.AproboticsOpenGLView
import com.example.aproboticksapp.opengl.Bin
import com.example.aproboticksapp.opengl.Box

class OpenGlFragment(val listBoxes: List<Box>, val bin: Bin):Fragment() {
    lateinit var glSurfaceView: AproboticsOpenGLView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        glSurfaceView = AproboticsOpenGLView(requireContext(), listBoxes = listBoxes, bin = bin)
        return glSurfaceView
    }
    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }
}