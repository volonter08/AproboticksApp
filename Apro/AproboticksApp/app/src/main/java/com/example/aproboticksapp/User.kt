package com.example.aproboticksapp

data class User(val ip:String,
    val name: String, val storageRight: Boolean = false, val planRight: Boolean = false,
    val qualityControlRight: Boolean = false
)