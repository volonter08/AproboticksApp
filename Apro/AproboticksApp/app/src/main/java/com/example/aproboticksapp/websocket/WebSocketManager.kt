package com.example.aproboticksapp.websocket

import android.content.Context
import android.text.TextUtils.replace
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.aproboticksapp.CodeObject
import com.example.aproboticksapp.DataObject
import com.example.aproboticksapp.R
import com.example.aproboticksapp.fragments.FromComputerFragment
import com.example.aproboticksapp.network.Utils
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

class WebSocketManager(val context: Context,val client:OkHttpClient,val onActivityReceiveMessage:(Int)->Unit) {

    var tsdStatusWebSocket = TsdStatusWebSocket()
    var webSocketListener = TsdWebSocketListener(context,onActivityReceiveMessage)
    var webSocket: WebSocket? = null

    private fun createWebSocketRequest(id: String,ipServer:String): Request {
        val websocketURL = "ws://$ipServer:8000/ws/THD-ws/$id"
        return Request.Builder()
            .url(websocketURL)
            .build()
    }
    fun connectWebSocket(id:String,ipServer: String){
        webSocket = client.newWebSocket(
            createWebSocketRequest(id,ipServer),
            webSocketListener
        )
    }
    private fun onReceiveMessage(code: Int) {
        when (code) {
            0 -> {
            }
            10 -> {
            }
            1000 -> {

            }
            1001 -> {

            }
        }
    }
    fun sendIdForLogin(id:String){
        val gson = GsonBuilder().create()
        val ip = Utils.getIPAddress(true)
        webSocket?.send( gson.toJson(CodeObject(111, DataObject(id = id, ip = ip))))
    }
}