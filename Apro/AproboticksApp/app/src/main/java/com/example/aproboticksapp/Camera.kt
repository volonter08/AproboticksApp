package com.example.aproboticksapp

import androidx.lifecycle.MutableLiveData
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import kotlin.properties.Delegates

class Camera(eyeVec3: Vec3,centerVec3: Vec3) {
    lateinit var rightLocalVec3:Vec3
    lateinit var upLocalVec3: Vec3
    lateinit var guideVec3: Vec3
    lateinit var mViewMatrix: Mat4
    var eyeVec3:Vec3 by Delegates.observable(Vec3(eyeVec3)){ _, _, newValue->
        guideVec3 = newValue - this.centerVec3
        rightLocalVec3 = glm.cross( if(guideVec3.z>=0)Vec3(0, 1, 0)else Vec3(0,-1,0),newValue).also(Vec3::normalize)
        upLocalVec3 = glm.cross(guideVec3,rightLocalVec3)
        mViewMatrix = glm.lookAt(newValue, centerVec3,upLocalVec3)
    }
    var centerVec3:Vec3 by Delegates.observable(centerVec3){_,_,newValue->
        guideVec3 = this.eyeVec3 - newValue
        rightLocalVec3 = glm.cross( if(guideVec3.z>=0)Vec3(0, 1, 0)else Vec3(0,-1,0),eyeVec3).also(Vec3::normalize)
        upLocalVec3 = glm.cross(guideVec3,rightLocalVec3)
        mViewMatrix = glm.lookAt(eyeVec3,newValue,upLocalVec3)
    }
    init {
        this.eyeVec3 = eyeVec3
        this.centerVec3 = centerVec3
    }
    
}