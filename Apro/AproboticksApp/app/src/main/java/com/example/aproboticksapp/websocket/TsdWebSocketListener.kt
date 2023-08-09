package com.example.aproboticksapp.websocket

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
import com.example.aproboticksapp.CodeObject
import com.example.aproboticksapp.DataObject
import com.example.aproboticksapp.network.Utils
import com.google.gson.GsonBuilder
import okhttp3.Response
import okhttp3.WebSocket
import org.json.JSONException
import org.json.JSONObject


class TsdWebSocketListener(val onReceiveMessage: (Int)->Unit) : okhttp3.WebSocketListener() {
    val gson = GsonBuilder().create()
    val ip= Utils.getIPAddress(true)
    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        Log.d("OOO","onOpen")
        webSocket.send( gson.toJson(CodeObject(11, DataObject(ip))))
    }
    override fun onMessage(webSocket: WebSocket, text: String) {
        var textMessage:String? = null
        super.onMessage(webSocket, text)
        val jsonObjectMessage= JSONObject(text)
        val message = JSONObject(jsonObjectMessage.getString("message"))
        val code = message.getInt("code")
        try {
            textMessage = message.getString("user-message")
        }
        catch(_:JSONException){
        }
        if(textMessage!=null) {
            TsdStatusWebSocket.action.postValue(textMessage)
        }
        onReceiveMessage(code)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.send( gson.toJson(CodeObject(101, DataObject(ip))))
        super.onClosing(webSocket, code, reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        webSocket.send( gson.toJson(CodeObject(101, DataObject(ip))))
        Log.d("OOO", "onFailure: ${t.message} $response")
        super.onFailure(webSocket, t, response)
    }
}