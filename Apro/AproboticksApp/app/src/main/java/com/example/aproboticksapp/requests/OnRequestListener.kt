package com.example.aproboticksapp.requests

import android.widget.Toast
import com.example.aproboticksapp.User

interface OnRequestListener {
    fun onRequestOnCreate(ipServer:String,status:Boolean,isComp:Boolean,id:String,isLoggedIn:Boolean, user: User? = null)
    fun onRequestShowToast(errorMessage:String)
    fun onFindServer()
}