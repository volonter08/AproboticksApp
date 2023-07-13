package com.example.aproboticksapp.websocket

import androidx.lifecycle.MutableLiveData
import com.example.aproboticksapp.network.Utils
import com.google.gson.GsonBuilder

class TsdStatusWebSocket {
    companion object {
        val gson = GsonBuilder().create()
        val ip = Utils.getIPAddress(true)
        val action: MutableLiveData<String?> = MutableLiveData(null)
    }
}