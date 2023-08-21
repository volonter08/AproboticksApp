package com.example.aproboticksapp.opengl.renderer

import android.content.Context
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_LINE_STRIP
import android.opengl.GLES20.GL_MAX_TEXTURE_IMAGE_UNITS
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TRIANGLE_STRIP
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetIntegerv
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glLineWidth
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix
import com.example.aproboticksapp.R
import com.example.aproboticksapp.opengl.Bin
import com.example.aproboticksapp.opengl.Box
import com.example.aproboticksapp.opengl.Camera
import com.example.aproboticksapp.opengl.Position
import com.example.aproboticksapp.opengl.VertexBox
import com.example.aproboticksapp.opengl.utils.ShaderUtils
import com.example.aproboticksapp.opengl.utils.TextureUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.LinkedList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import glm_.vec3.Vec3

class AproboticsOpenGLRenderer(private val context: Context, val listBox: List<Box>, val bin: Bin) :
    Renderer {
    companion object {
        const val FLOAT_SIZE = 4
        const val STRIDE_VERTEX_DATA = 4 * FLOAT_SIZE
    }

    var mProjectionMatrix = FloatArray(16)
    var mResultMatrix = FloatArray(16)
    lateinit var vertexData: FloatBuffer
    lateinit var fragmentData: FloatBuffer
    lateinit var textureData: FloatBuffer
    var width = 0
    var height = 0
    var program = 0

    //for camera
    val camera =
        Camera(centerVec3 = Vec3(0, 0, 0), _eyeVec3 = Vec3(-bin.width, -bin.height, bin.depth))
    var vertexId = 0f

    //for locations
    var aTextureLocation = 0

    //for data
    val listForVertexData = LinkedList<Float>()
    val listForFragmentData = LinkedList<Float>()
    val listForTextureData = LinkedList<Float>()

    //for texture
    var listTextureId = emptyList<Int>()
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glEnable(GL_DEPTH_TEST)
        glClearColor(0f, 0f, 0f, 0f)
        initShadersAndProgram()
        prepareDataForAxes()
        prepareDataForBin()
        prepareDataForBoxes()
        putDataInBuffers()
        bindData()
        val cnt = IntArray(1)
        glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, cnt, 0)
        cnt[0]
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height);
        this.width = width
        this.height = height
    }

    override fun onDrawFrame(gl: GL10?) {
        createProjectionMatrix(width, height)
        bindMatrix()
        glClear(GL_COLOR_BUFFER_BIT)
        glClear(GL_DEPTH_BUFFER_BIT)
        glLineWidth(10f)
        drawAxes()
        drawBin()
        drawBoxes()
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
        val listFrontFacesBox = listOf<VertexBox>(
            leftTopFront,
            leftBottomFront,
            rightTopFront,
            rightBottomFront,

            )
        val listRightFacesBox = listOf<VertexBox>(
            rightTopFront,
            rightBottomFront,
            rightTopBack,
            rightBottomBack,

            )
        val listBackFacesBox = listOf<VertexBox>(
            rightTopBack,
            rightBottomBack,
            leftTopBack,
            leftBottomBack,

            )
        val listLeftFacesBox = listOf<VertexBox>(
            leftTopBack,
            leftBottomBack,
            leftTopFront,
            leftBottomFront
        )
        val listTopFacesBox = listOf(
            leftTopBack, leftTopFront, rightTopBack, rightTopFront)
        val listBottomFacesBox = listOf(
            leftBottomFront, leftBottomBack, rightBottomFront, rightBottomBack
        )

        prepareDataForBox(
            listFrontFacesBox,
            listRightFacesBox,
            listBackFacesBox,
            listLeftFacesBox,
            listTopFacesBox,
            listBottomFacesBox
        )
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
        val near = 1f
        val far = 100000f
        if (width > height) {
            ratio = (height.toFloat() / width.toFloat())
            top *= ratio
            bottom *= ratio
        } else if (height > width) {
            ratio = (width.toFloat() / height.toFloat())
            left *= ratio
            right *= ratio
        }
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far)

    }

    private fun createResultMatrix() {
        Matrix.multiplyMM(
            mResultMatrix,
            0,
            mProjectionMatrix,
            0,
            camera.mViewMatrix.toFloatArray(),
            0
        )
    }

    private fun bindMatrix() {
        val uMatrixLocation = glGetUniformLocation(program, "u_Matrix")
        createResultMatrix()
        glUniformMatrix4fv(uMatrixLocation, 1, false, mResultMatrix, 0)
    }

    private fun prepareDataForBox(
        listFrontFacesBox: List<VertexBox>,
        listRightFacesBox: List<VertexBox>,
        listBackFacesBox: List<VertexBox>,
        listLeftFacesBox: List<VertexBox>,
        listTopFacesBox: List<VertexBox>,
        listBottomFacesBox: List<VertexBox>
    ) {
        listForVertexData.apply {
            listFrontFacesBox.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
                add(vertexId++)
            }

            listRightFacesBox.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
                add(vertexId++)
            }
            listBackFacesBox.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
                add(vertexId++)
            }
            listLeftFacesBox.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
                add(vertexId++)
            }
            listTopFacesBox.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
                add(vertexId++)
            }
            listBottomFacesBox.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
                add(vertexId++)
            }
        }.toFloatArray()
        listForFragmentData.apply {
            listFrontFacesBox.forEach {
                add(1f)
                add(0f)
                add(0.5f)
            }
            listRightFacesBox.forEach {
                add(1f)
                add(0f)
                add(0.5f)
            }
            listBackFacesBox.forEach {
                add(1f)
                add(0f)
                add(0.5f)
            }
            listLeftFacesBox.forEach {
                add(1f)
                add(0f)
                add(0.5f)
            }
            listTopFacesBox.forEach {
                add(1f)
                add(0f)
                add(0.5f)
            }
            listBottomFacesBox.forEach {
                add(1f)
                add(0f)
                add(0.5f)
            }
        }.toFloatArray()
        for(i in(0..5)){
            listForTextureData.addAll(listOf(0f, 0f, 0f, 1f, 1f, 0f, 1f, 1f))
        }
    }

    private fun putDataInBuffers() {
        vertexData =
            ByteBuffer.allocateDirect(listForVertexData.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().apply {
                    put(listForVertexData.toFloatArray())
                }
        fragmentData =
            ByteBuffer.allocateDirect(listForFragmentData.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().apply {
                    put(listForFragmentData.toFloatArray())
                }
        textureData =
            ByteBuffer.allocateDirect(listForTextureData.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().apply {
                    put(listForTextureData.toFloatArray())
                }
        listTextureId = listBox.map {
            TextureUtils.loadTexture(context,it.img)
        }
    }

    private fun bindData() {
        val uTextureUnitLocation = glGetUniformLocation(program, "u_Texture");
        val aPositionLocation = glGetAttribLocation(program, "a_Position")
        val aColor = glGetAttribLocation(program, "a_Color")
        val aVertexId = glGetAttribLocation(program, "a_Vertex_Id")
        aTextureLocation = glGetAttribLocation(program, "a_TexCoord");
        vertexData.position(0)
        glVertexAttribPointer(aPositionLocation, 3, GL_FLOAT, false, STRIDE_VERTEX_DATA, vertexData)
        glEnableVertexAttribArray(aPositionLocation)
        vertexData.position(3)
        glVertexAttribPointer(aVertexId, 1, GL_FLOAT, false, STRIDE_VERTEX_DATA, vertexData)
        glEnableVertexAttribArray(aVertexId)
        fragmentData.position(0)
        glVertexAttribPointer(aColor, 3, GL_FLOAT, false, 0, fragmentData)
        glEnableVertexAttribArray(aColor)
        textureData.position(0)
        glVertexAttribPointer(aTextureLocation, 2, GL_FLOAT, false, 0, textureData)
        glEnableVertexAttribArray(aTextureLocation)


        // юнит текстуры
        glUniform1i(uTextureUnitLocation, 0);
    }

    private fun prepareDataForAxes() {
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
        listForVertexData.apply {
            arrayForAxeX.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
                add(vertexId++)
            }
            arrayForAxeY.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
                add(vertexId++)
            }
            arrayForAxeZ.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
                add(vertexId++)
            }
        }
        listForFragmentData.apply {
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
        }
    }

    private fun prepareDataForBin() {
        val listForBorderBin = bin.run {
            listOf(
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
        }
        listForVertexData.apply {
            listForBorderBin.forEach {
                add(it.x)
                add(it.y)
                add(it.z)
                add(vertexId++)
            }
        }
        listForFragmentData.apply {
            listForBorderBin.forEach {
                add(1f)
                add(1f)
                add(1f)
            }
        }
    }

    private fun prepareDataForBoxes() {
        listForTextureData.apply {
            for (i in (0..25)) {
                addAll(listOf(0f, 0f))
            }
        }
        listBox.forEach {
            initDataForBox(it)
        }
    }

    private fun drawAxes() {
        glDrawArrays(GL_LINE_STRIP, 0, 2)
        glDrawArrays(GL_LINE_STRIP, 2, 2)
        glDrawArrays(GL_LINE_STRIP, 4, 2)
    }

    private fun drawBin() {
        glDrawArrays(GL_LINE_STRIP, 6, 20)
    }

    private fun drawBoxes() {
        for (i in listBox.indices) {
            glBindTexture(GL_TEXTURE_2D, listTextureId[i]);
            glDrawArrays(GL_TRIANGLE_STRIP, 26 + i * 24, 24)
        }
    }
}