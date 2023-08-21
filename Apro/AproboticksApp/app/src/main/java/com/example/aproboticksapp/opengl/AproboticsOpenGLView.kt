package com.example.aproboticksapp.opengl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.example.aproboticksapp.opengl.renderer.AproboticsOpenGLRenderer
import glm_.func.common.abs
import kotlin.math.sign


class AproboticsOpenGLView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, listBoxes: List<Box>?= null, bin: Bin? = null
) :
    GLSurfaceView(context, attrs) {
    val renderer = AproboticsOpenGLRenderer(context,listBoxes!!,bin!!)
    val scaleDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
                val scaleFactor =
                    Math.max(0.5f, Math.min((scaleGestureDetector.getScaleFactor().let { it ->
                        2 - it
                    }), 2f));
                println(scaleFactor)
                renderer.camera.scale(scaleFactor)
                return true
            }
        })
    var currentTouchState = 0
    var startTouchOneFinger: PointF? = null
    var startTouchTwoFinger: PointF? = null

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
                        startTouchOneFinger = PointF(event.x, event.y)
                    }
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (event.pointerCount == 2) {
                        currentTouchState = 2
                        startTouchTwoFinger = PointF(event.x, event.y)
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    when (currentTouchState) {
                        1 -> {
                            if (startTouchOneFinger != null) {
                                renderer.apply {
                                    val translateX = ((event.x - startTouchOneFinger!!.x) / width)
                                    val translateY = -(event.y - startTouchOneFinger!!.y) / height
                                    renderer.camera.rotateSphere(translateX, translateY)
                                }
                            }
                        }

                        2 -> {
                            if (startTouchTwoFinger != null) {
                                renderer.apply {
                                    val translateX =
                                        (event.x - startTouchTwoFinger!!.x) / width.run {
                                            sign * (this.abs + 1)
                                        }
                                    val translateY =
                                        -(event.y - startTouchTwoFinger!!.y) / height.run {
                                            sign * (this.abs + 1)
                                        }
                                    renderer.camera.translateCamera(translateX, translateY)
                                    scaleDetector.onTouchEvent(event)
                                }
                            }
                        }
                    }
                }
            }
        }
        return true
    }
}
