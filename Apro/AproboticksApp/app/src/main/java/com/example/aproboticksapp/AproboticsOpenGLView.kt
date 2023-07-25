package com.example.aproboticksapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import glm_.func.common.abs

class AproboticsOpenGLView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) :
    GLSurfaceView(context, attrs) {
    val renderer = AproboticsOpenGLRenderer(context)
    val scaleDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
                renderer.mScaleFactor *= scaleGestureDetector.getScaleFactor();
                renderer.mScaleFactor = Math.max(0.5f, Math.min(renderer.mScaleFactor, 2f));
                return true
            }
        })
    var currentTouchState = 0
    var startTouch: PointF? = null

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (event.pointerCount == 1) {
                        currentTouchState = 1
                        startTouch = PointF(event.x, event.y)
                    }
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (event.pointerCount == 2) {
                        currentTouchState = 2
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    when (currentTouchState) {
                        1 -> {
                            if (startTouch != null) {
                                renderer.apply {
                                    translateX = (event.x - startTouch!!.x) /width
                                    transLateY = -(event.y - startTouch!!.y)/height
                                    calculateRotation()
                                }
                            }
                        }

                        2 -> {
                            scaleDetector.onTouchEvent(event)
                        }
                    }
                }
            }
        }
        return true
    }
}