package com.example.aproboticksapp.requests

import com.example.aproboticksapp.User

interface OnRequestListener {
    fun onRequestOnCreate(status:Boolean,isComp:Boolean,id:String,isLoggedIn:Boolean, user: User? = null)

}