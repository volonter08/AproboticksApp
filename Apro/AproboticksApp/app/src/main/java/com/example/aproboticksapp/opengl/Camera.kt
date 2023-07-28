package com.example.aproboticksapp.opengl

import glm_.func.common.abs
import glm_.glm
import glm_.glm.PIf
import glm_.glm.asin
import glm_.mat3x3.Mat3
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.properties.Delegates

class Camera(_eyeVec3: Vec3, centerVec3: Vec3) {
    companion object {
        const val TRANSLATE_SPEED = 32
    }
    lateinit var rightLocalVec3: Vec3
    lateinit var upLocalVec3: Vec3
    lateinit var mViewMatrix: Mat4
    lateinit var mLinearOperatorMatrix: Mat3
    var pitch = 0f
    var roll = 0f
    var yaw = 0f
    var eyeVec3: Vec3 by Delegates.observable(Vec3(_eyeVec3)) { _, _, newValue ->
        mViewMatrix = glm.lookAt(newValue, newValue - guideVec3, upLocalVec3)
        /*mLinearOperatorMatrix = rightLocalVec3.let {
            upLocalVec3.run {
                Mat3(floatArrayOf(it.x, it.y, it.z, x, y, z, guideVec3.x, guideVec3.y, guideVec3.z))
            }
        }.transpose()

         */

    }
    var guideVec3: Vec3 by Delegates.observable(_eyeVec3 - centerVec3) { _, oldValue, newValue ->
        this.eyeVec3.apply {
            x = (x - oldValue.x) + newValue.x
            y = (y - oldValue.y) + newValue.y
            z = (z - oldValue.z) + newValue.z
        }
        rightLocalVec3 =
            glm.cross(Vec3(0, 0, 1), newValue).normalize()
        upLocalVec3 = glm.cross(newValue, rightLocalVec3).normalize()
        mViewMatrix = glm.lookAt(eyeVec3, eyeVec3 - newValue, upLocalVec3)
        /*
        mLinearOperatorMatrix = rightLocalVec3.let {
            upLocalVec3.run {
                Mat3(floatArrayOf(it.x, it.y, it.z, x, y, z, newValue.x, newValue.y, newValue.z))
            }
        }.transpose()

         */
    }


    init {
        this.guideVec3 = (_eyeVec3 - centerVec3).normalize() *10
        this.eyeVec3 = _eyeVec3
        pitch = asin(guideVec3.run { z / length() })
        roll = guideVec3.run{
            if(y>=0f) acos(x / (length() * cos(pitch))) else -acos(x / (length() * cos(pitch)))
        }
    }

    fun rotateSphere(translateX: Float, translateY: Float) {
        val radius = guideVec3.length()
        if ((pitch + translateY * PIf / 20).abs < PIf / 2)
            pitch += translateY * PIf / 20
        roll += translateX * PIf / 20
        val newGuideX = cos(pitch) * cos(roll) * radius
        val newGuideY = cos(pitch) * sin(roll) * radius
        val newGuideZ = sin(pitch) * radius
        guideVec3 = Vec3(newGuideX, newGuideY, newGuideZ)
        println(guideVec3)
    }

    /*fun translateFromLocalToGlobal(localVec: Vec3): Vec3 {
        return Vec3().apply {
            x =
                mLinearOperatorMatrix[0, 0] * localVec.x + mLinearOperatorMatrix[0, 1] * localVec.y + mLinearOperatorMatrix[0, 2] * localVec.z + centerVec3.x
            y =
                mLinearOperatorMatrix[1, 0] * localVec.x * mLinearOperatorMatrix[1, 1] * localVec.y + mLinearOperatorMatrix[1, 2] * localVec.z + centerVec3.y
            z =
                mLinearOperatorMatrix[2, 0] * localVec.x * mLinearOperatorMatrix[2, 1] * localVec.y + mLinearOperatorMatrix[2, 2] * localVec.z + centerVec3.z
        }
    }

     */
    fun translateCamera(translateX: Float, translateY: Float) {
        val newEyeX = rightLocalVec3 * translateX * TRANSLATE_SPEED
        val newEyeY = upLocalVec3 * translateY * TRANSLATE_SPEED
        val newEye = newEyeX + newEyeY
        eyeVec3 = eyeVec3 + newEye
    }
    fun scale(scaleFactor:Float){
        eyeVec3 = eyeVec3 + (guideVec3 * (if(scaleFactor>1)scaleFactor else -scaleFactor )*10)
    }

}