package com.example.aproboticksapp.requests

import android.widget.Toast
import com.example.aproboticksapp.User
import com.example.aproboticksapp.opengl.Bin
import com.example.aproboticksapp.opengl.Box

interface OnRequestListener {
    fun onRequestOnCreate(ipServer:String,status:Boolean,isComp:Boolean,id:String,isLoggedIn:Boolean, user: User? = null)
    fun onRequestShowToast(errorMessage:String)
    fun onFindServer()
    fun onVisualiseBin(listBox:List<Box>, bin: Bin)
}