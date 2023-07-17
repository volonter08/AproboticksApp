package com.example.aproboticksapp.requests

import android.content.Context
import android.util.Log
import com.example.aproboticksapp.User
import com.example.aproboticksapp.network.Utils
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.net.ConnectException
import java.net.InetAddress

class HttpRequestManager(
    context: Context,
    val client: OkHttpClient,
    val onRequestListener: OnRequestListener
) {
    companion object {
        const val PORT = 8000
    }

    val prefs = context.getSharedPreferences("application", Context.MODE_PRIVATE)
    var ipServer = prefs.getString("ip_server", "") ?: ""
    var httpURL = "http://$ipServer:$PORT/"

    suspend fun receiveJSONObjectOnCreate(): JSONObject? {
        return CoroutineScope(Dispatchers.IO).async {
            var urlCheckBinding = httpURL + "API/THD-lock"
            val request = Request.Builder().url(urlCheckBinding).build()
            lateinit var response: Response
            try {
                response = client.newCall(request).execute()
            } catch (e: java.lang.Exception) {
                findServer()
                urlCheckBinding = httpURL + "API/THD-lock"
                response = client.newCall(Request.Builder().url(urlCheckBinding).build()).execute()
            }
            val jsonData = response.body?.string()
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
                onRequestListener.onRequestOnCreate(ipServer, status, isComp, id, isLoggedIn, user)
            } else {
                onRequestListener.onRequestOnCreate(ipServer, status, isComp, id, isLoggedIn)
            }

        }
    }

    suspend fun requestLogin(id: String): User? {
        val urlLogin = httpURL + "API/login"
        val formBody: RequestBody = FormBody.Builder()
            .add("id", id)
            .add("ip", Utils.getIPAddress(true))
            .build()
        val request = Request.Builder().url(urlLogin).post(formBody).build()
        lateinit var response: Response
        return GlobalScope.async(Dispatchers.IO) {
            response = client.newCall(request).execute()
            val jsonData = response.body?.string()
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
        val urlLogout = httpURL + "API/logout"
        val formBody: RequestBody = FormBody.Builder()
            .add("ip", Utils.getIPAddress(true))
            .build()
        val request = Request.Builder().url(urlLogout).post(formBody).build()
        GlobalScope.launch(Dispatchers.IO) {
            client.newCall(request).execute().close()
        }
    }

    fun findServer() {
        val prefix: String = Utils.getIPAddress(true).let {
            it.substring(0, it.lastIndexOf(".") + 1)
        }
        for (i in 0..254) {
            val testIp: String = prefix + i.toString()
            println(testIp)
            var reachable = false
            val address: InetAddress = InetAddress.getByName(testIp)
            reachable = address.isReachable(100)
            val hostName: String = address.getCanonicalHostName()
            if (reachable) {
                val testUrlHttp = "http://$testIp:$PORT/"
                val request = Request.Builder().url(testUrlHttp + "API/check-connection").build()
                if (testIp != ipServer) {
                    try {
                        val jsonData = client.newCall(request).execute().use {
                            it.body?.string()
                        }
                        try {
                            val jsonObject = jsonData?.let {
                                JSONObject(it)
                            }
                            if(jsonObject!= null && jsonObject.getBoolean("status")) {
                                ipServer = testIp
                                httpURL = "http://$ipServer:$PORT/"
                                prefs.edit().putString("ip_server", ipServer).apply()
                                return
                            }
                            else continue
                        } catch (e: Exception) {
                            continue
                        }
                    } catch (e: ConnectException) {
                        continue
                    }
                }
            }
        }
    }

    fun requestTakeOff(id: String, amount: Int) {
        val urlLogout = httpURL + "API/crate/$id"
        val formBody: RequestBody = FormBody.Builder()
            .add("amount",amount.toString())
            .build()
        val request = Request.Builder().url(urlLogout).post(formBody).build()
        GlobalScope.launch(Dispatchers.IO) {
            client.newCall(request).execute().close()
        }
    }
    fun requestReplace(id:String,idCell:Int){
        val urlLogout = httpURL + "API/move-crate/$id"
        val formBody: RequestBody = FormBody.Builder()
            .add("cell",idCell.toString())
            .build()
        val request = Request.Builder().url(urlLogout).post(formBody).build()
        GlobalScope.launch(Dispatchers.IO) {
            client.newCall(request).execute().close()
        }
    }
}