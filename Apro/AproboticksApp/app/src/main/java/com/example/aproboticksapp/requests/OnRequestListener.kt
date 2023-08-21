package com.example.aproboticksapp.requests

import com.example.aproboticksapp.User
import com.example.aproboticksapp.opengl.Bin
import com.example.aproboticksapp.opengl.Box

interface OnRequestListener {
    fun onRequestOnCreate(ipServer:String,status:Boolean,isComp:Boolean,id:String,isLoggedIn:Boolean, user: User? = null)
    fun onError(errorMessage:String = "Запрос не удачен.Попробуйте еще раз")
    fun onLoading()
    fun onPutCrate(listBox:List<Box>, bin: Bin,idCrate:String,lockedList:MutableList<Int>)
    fun onGetUserData(user: User?)
    fun onLogin(user:User)
    fun onLogout()
    fun onStopLoading()
}