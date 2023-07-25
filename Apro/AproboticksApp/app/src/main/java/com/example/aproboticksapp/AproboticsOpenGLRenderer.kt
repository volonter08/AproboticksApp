package com.example.aproboticksapp

import android.content.Context
import android.graphics.PointF
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_LINE_STRIP
import android.opengl.GLES20.GL_TRIANGLE_STRIP
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glLineWidth
import android.opengl.GLES20.glUniform4f
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix
import android.os.Build.VERSION
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.LinkedList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.mat4x4.Mat4
import glm_.glm
import glm_.pow
import java.lang.Exception
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

class AproboticsOpenGLRenderer(private val context: Context) : Renderer {

    var mProjectionMatrix = FloatArray(16)
    var mResultMatrix = FloatArray(16)
    lateinit var vertexData: FloatBuffer
    lateinit var fragmentData: FloatBuffer
    var width = 0
    var height = 0
    var mScaleFactor = 1f
    var translateX = 0f
    var transLateY = 0f
    var program = 0

    //for camera
    val camera = Camera(centerVec3 = Vec3(0, 0, 0) ,eyeVec3 = Vec3(0, 0,4))

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glEnable(GL_DEPTH_TEST)
        glClearColor(0f, 0f, 0f, 0f)
        initShadersAndProgram()
        initDataForBox(Box(0.5f, 0.5f, 1f, Position(0f, 0f, 0f)))

        bindData()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height);
        this.width = width
        this.height = height
        createProjectionMatrix(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        bindMatrix()
        glClear(GL_COLOR_BUFFER_BIT)
        glClear(GL_DEPTH_BUFFER_BIT)
        glLineWidth(10f)
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 18)
        glDrawArrays(GL_LINE_STRIP, 18, 20)
        glDrawArrays(GL_LINE_STRIP, 38, 2)
        glDrawArrays(GL_LINE_STRIP, 40, 2)
        glDrawArrays(GL_LINE_STRIP, 42, 2)

    }

    fun initDataForBox(box: Box) {
        val leftBottomFront = VertexBox(
            box.position.x - box.width / 2,
            box.position.y - box.height / 2,
            box.position.z + box.depth / 2
        )
        val rightBottomFront = VertexBox(
            box.position.x + box.width / 2,
            box.position.y - box.height / 2,
            box.position.z + box.depth / 2
        )
        val leftTopFront = VertexBox(
            box.position.x - box.width / 2,
            box.position.y + box.height / 2,
            box.position.z + box.depth / 2
        )
        val rightTopFront = VertexBox(
            box.position.x + box.width / 2,
            box.position.y + box.height / 2,
            box.position.z + box.depth / 2
        )
        val leftBottomBack = VertexBox(
            box.position.x - box.width / 2,
            box.position.y - box.height / 2,
            box.position.z - box.depth / 2
        )
        val rightBottomBack = VertexBox(
            box.position.x + box.width / 2,
            box.position.y - box.height / 2,
            box.position.z - box.depth / 2
        )
        val leftTopBack = VertexBox(
            box.position.x - box.width / 2,
            box.position.y + box.height / 2,
            box.position.z - box.depth / 2
        )
        val rightTopBack = VertexBox(
            box.position.x + box.width / 2,
            box.position.y + box.height / 2,
            box.position.z - box.depth / 2
        )
        val listForDrawingBorder = listOf(
            leftTopFront,
            leftBottomFront,
            rightBottomFront,
            rightTopFront,
            leftTopFront,
            rightTopFront,
            rightBottomFront,
            rightBottomBack,
            rightTopBack,
            rightTopFront,
            rightTopBack,
            rightBottomBack,
            leftBottomBack,
            leftTopBack,
            rightTopBack,
            leftTopBack,
            leftBottomBack,
            leftBottomFront,
            leftTopFront,
            leftTopBack
        )
        Border(leftBottomFront, rightBottomFront)
        Border(leftBottomFront, rightBottomFront)
        val arraySideFaces = listOf<VertexBox>(
            leftTopFront,
            leftBottomFront,
            rightTopFront,
            rightBottomFront,
            rightTopBack,
            rightBottomBack,
            leftTopBack,
            leftBottomBack,
            leftTopFront,
            leftBottomFront
        )
        val arrayTopFaces =
            listOf<VertexBox>(leftTopFront, rightTopFront, leftTopBack, rightTopBack)
        val arrayBottomFaces =
            listOf<VertexBox>(leftBottomFront, rightBottomFront, leftBottomBack, rightBottomBack)

        prepareBuffersForBox(arraySideFaces, arrayTopFaces, arrayBottomFaces, listForDrawingBorder)
    }

    fun initShadersAndProgram() {
        val vertexTextCode = context.resources.openRawResource(R.raw.vertex).bufferedReader().use {
            val stringBuilder = StringBuilder()
            do {
                val line = it.readLine()
                if (line != null)
                    stringBuilder.append(line)
            } while (line != null)
            stringBuilder.toString()
        }
        val vertexShaderId = ShaderUtils.createVertexShaderId(vertexTextCode)
        val fragmentTextCode =
            context.resources.openRawResource(R.raw.fragment).bufferedReader().use {
                val stringBuilder = StringBuilder()
                do {
                    val line = it.readLine()
                    if (line != null)
                        stringBuilder.append(line)
                } while (line != null)
                stringBuilder.toString()
            }
        val fragmentShaderId = ShaderUtils.createFragmentShaderId(fragmentTextCode)
        program = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
        glUseProgram(program)
    }

    private fun createProjectionMatrix(
        width: Int, height: Int
    ) {
        var ratio: Float
        var left = -1.0f
        var right = 1.0f
        var bottom = -1.0f
        var top = 1.0f
        val near = 1f * mScaleFactor
        val far = 1000f
        if (width > height) {
            ratio = (height.toFloat() / width.toFloat())
            top *= ratio
            bottom *= ratio
        } else if (height > width) {
            ratio = (width.toFloat() / height.toFloat())
            left *= ratio
            right *= ratio
        }
        Matrix.frustumM(mProjectionMatrix,0,left,right, bottom, top, near, far)

    }

    private fun createResultMatrix() {

         Matrix.multiplyMM(mResultMatrix,0,mProjectionMatrix,0,camera.mViewMatrix.toFloatArray(),0)
    }

    private fun bindMatrix() {
        val uMatrixLocation = glGetUniformLocation(program, "u_Matrix")
        createResultMatrix()
        glUniformMatrix4fv(uMatrixLocation, 1, false, mResultMatrix,0)

    }

    private fun prepareBuffersForBox(
        arraySideFaces: List<VertexBox>,
        arrayTobFaces: List<VertexBox>,
        arrayBottomFaces: List<VertexBox>,
        listForDrawingBorder: List<VertexBox>
    ) {
        val arrayForAxeX = arrayOf(
            Position(0f, 0f, 0f),
            Position(100f, 0f, 0f),
        )
        val arrayForAxeY = arrayOf(
            Position(0f, 0f, 0f),
            Position(0f, 100f, 0f),
        )
        val arrayForAxeZ = arrayOf(
            Position(0f, 0f, 0f),
            Position(0f, 0f, 100f),
        )
        val arrayForVertexData = LinkedList<Float>().apply {
            arraySideFaces.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
            }
            arrayTobFaces.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
            }
            arrayBottomFaces.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
            }
            listForDrawingBorder.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
            }
            arrayForAxeX.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
            }
            arrayForAxeY.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
            }
            arrayForAxeZ.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
            }
        }.toFloatArray()
        val arrayForFragmentData = LinkedList<Float>().apply {
            arraySideFaces.forEach {
                add(1f)
                add(0f)
                add(0.5f)
            }
            arrayTobFaces.forEach {
                add(1f)
                add(0f)
                add(0.5f)
            }
            arrayBottomFaces.forEach {
                add(1f)
                add(0f)
                add(0.5f)
            }
            listForDrawingBorder.forEach {
                add(1f)
                add(1f)
                add(1f)
            }
            arrayForAxeX.forEach {
                add(1f)
                add(0f)
                add(0f)
            }
            arrayForAxeY.forEach {
                add(0f)
                add(1f)
                add(0f)
            }
            arrayForAxeZ.forEach {
                add(0f)
                add(0f)
                add(1f)
            }
        }.toFloatArray()
        vertexData =
            ByteBuffer.allocateDirect(arrayForVertexData.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        vertexData.put(arrayForVertexData)
        fragmentData =
            ByteBuffer.allocateDirect(arrayForVertexData.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        fragmentData.put(arrayForFragmentData)
    }

    private fun bindData() {
        val uColor = glGetUniformLocation(program, "u_Color")
        glUniform4f(uColor, 0f, 0f, 1f, 1f)
        val aPositionLocation = glGetAttribLocation(program, "a_Position")
        val aColor = glGetAttribLocation(program, "a_Color")
        vertexData.position(0)
        glVertexAttribPointer(aPositionLocation, 3, GL_FLOAT, false, 0, vertexData)
        glEnableVertexAttribArray(aPositionLocation)
        fragmentData.position(0)
        glVertexAttribPointer(aColor, 3, GL_FLOAT, false, 0, fragmentData)
        glEnableVertexAttribArray(aColor)
    }
    fun calculateRotation() {
        val radius = camera.eyeVec3.length()
        val tetta = acos((camera.eyeVec3.z / radius).toDouble()) * if(camera.eyeVec3.y>=0)1 else -1
        val angleVertical = asin((sqrt(2f) * radius * transLateY).toDouble() / radius)
        camera.eyeVec3 = Vec3((radius * sin(tetta + angleVertical) * cos(PI/2)).toFloat(),
            (radius * sin(tetta + angleVertical) * sin(PI/2)).toFloat(),
            (radius * cos(tetta + angleVertical)).toFloat()
        )
    }
}