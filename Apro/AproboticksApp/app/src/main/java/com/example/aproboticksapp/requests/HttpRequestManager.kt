package com.example.aproboticksapp.requests

import android.util.Log
import com.example.aproboticksapp.User
import com.example.aproboticksapp.network.Utils
import kotlinx.coroutines.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class HttpRequestManager(val client: OkHttpClient, val onRequestListener: OnRequestListener) {
    suspend fun receiveJSONObjectOnCreate(): JSONObject? {
        val urlCheckBinding = "http://192.168.8.54:8000/API/THD-lock"
        val request = Request.Builder().url(urlCheckBinding).build()
        return CoroutineScope(Dispatchers.IO).async {
            val responses = client.newCall(request).execute()
            val jsonData = responses.body?.string()
            try {
                val jObject = jsonData?.let {
                    JSONObject(it)
                }
                return@async jObject
            } catch (e: Exception) {
                return@async null
            }
        }.await()
    }
    suspend fun requestOnCreate() {
        val jsonObject = receiveJSONObjectOnCreate()
        jsonObject?.apply {
            val status = getBoolean("status")
            val isComp = getBoolean("is_comp")
            val id = getString("id")
            val isLoggedIn = getBoolean("is_logged_in")
            if (isLoggedIn) {
                val loginObject = getJSONObject("login")
                val loginObjectId = loginObject.getString("id")
                val name = loginObject.getString("name")
                val storageRight = loginObject.getBoolean("storage_right")
                val planRight = loginObject.getBoolean("plan_right")
                val qualityControlRight = loginObject.getBoolean("quality_control_right")
                val user = User(loginObjectId, name, storageRight, planRight, qualityControlRight)
                onRequestListener.onRequestOnCreate(status, isComp, id, isLoggedIn, user)
            }
            else{
                onRequestListener.onRequestOnCreate(status, isComp, id, isLoggedIn)
            }

        }
    }
    suspend fun requestLogin(id:String): User? {
        val urlLogin = "http://192.168.8.54:8000/API/login"
        val formBody: RequestBody = FormBody.Builder()
            .add("id", id)
            .add("ip", Utils.getIPAddress(true))
            .build()
        val request = Request.Builder().url(urlLogin).post(formBody).build()
        return GlobalScope.async(Dispatchers.IO) {
            val responses = client.newCall(request).execute()
            val jsonData = responses.body?.string()
            try {
                val jObject = jsonData?.let {
                    JSONObject(it)
                }
                if (jObject != null) {
                    val obj = jObject as JSONObject
                    val status = obj.getBoolean("status")
                    if (!status) {
                        return@async null
                    } else {
                        val name = obj.getString("name")
                        val storageRight = obj.getBoolean("storage_right")
                        val planRight = obj.getBoolean("plan_right")
                        val qualityControlRight = obj.getBoolean("quality_control_right")
                        val user = User(id, name, storageRight, planRight, qualityControlRight)
                        Log.d("OoO", user.toString())
                        return@async user
                    }
                }
                return@async null
            } catch (e: Exception) {
                e.printStackTrace()
                return@async null
            }
        }.await()
    }
    fun requestLogout() {
        val urlLogout = "http://192.168.8.54:8000/API/logout"
        val formBody: RequestBody = FormBody.Builder()
            .add("ip", Utils.getIPAddress(true))
            .build()
        val request = Request.Builder().url(urlLogout).post(formBody).build()
        GlobalScope.launch(Dispatchers.IO) {
            client.newCall(request).execute()
        }
    }
}