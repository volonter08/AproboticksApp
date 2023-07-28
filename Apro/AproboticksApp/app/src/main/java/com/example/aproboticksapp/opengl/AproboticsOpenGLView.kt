package com.example.aproboticksapp.opengl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Base64
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.example.aproboticksapp.forGson.ConstructorTypeAdapterFactory
import com.example.aproboticksapp.requests.HttpRequestManager
import com.google.gson.GsonBuilder
import glm_.func.common.abs
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.sign


class AproboticsOpenGLView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) :
    GLSurfaceView(context, attrs) {
    val fileInputStream = context.assets.open("boxes.json")
    val gson = GsonBuilder().registerTypeAdapterFactory(ConstructorTypeAdapterFactory).create()
    val visObj = gson.fromJson(fileInputStream.bufferedReader(), VisualisationObject::class.java)
    val renderer = AproboticsOpenGLRenderer(context, visObj.listBoxes, visObj.bin)
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

    @OptIn(ExperimentalEncodingApi::class)
    fun readFileAndConvertHimToBitmap(){
       val string = context.assets.open("123 (2).txt").bufferedReader().use {
           val stringBuilder = StringBuilder()
           do{
               val line = it.readLine()
               if(line!=null)
                   stringBuilder.append(line)
           }while (line!=null)
           stringBuilder.toString()
       }
       val bitmap = convertStringToBitmap(string)
    }
    fun BitMapToString(bitmap:Bitmap):String
    {
        val baos = ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        val b = baos.toByteArray ();
        val temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
    fun convertStringToBitmap(string: String?): Bitmap? {
        val byteArray1: ByteArray
        byteArray1 = Base64.decode(string, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(
            byteArray1, 0,
            byteArray1.size
        )
    }
    fun requestBitmap():Bitmap?{
            var string:String? = null
            val cliient = OkHttpClient()
            val formBody = RequestBody.create("application/json".toMediaType(),"{\"id\":\"0000101\" }")
            val request = Request.Builder()
                .url("http://192.168.8.54:${HttpRequestManager.PORT}/API/crate-positioning").post(formBody).build()
            val response = cliient.newCall(request).execute()
            val jsonData = response.body?.string()
        return try {
            val visualisationObject = jsonData?.let {
                gson.fromJson(it,VisualisationObject::class.java)
            }
            convertStringToBitmap(visualisationObject!!.listBoxes[0].img)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}