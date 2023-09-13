package com.example.aproboticksapp.requests

import android.content.Context
import com.example.aproboticksapp.User
import com.example.aproboticksapp.forGson.ConstructorTypeAdapterFactory
import com.example.aproboticksapp.network.Utils
import com.example.aproboticksapp.opengl.VisualisationObject
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
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
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onRequestListener.onLoading()
                }
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

    fun requestOnCreate() {
        onRequestListener.onLoading()
        GlobalScope.launch(Dispatchers.IO) {
            var urlCheckBinding = httpURL + "API/THD-lock"
            val request = Request.Builder().url(urlCheckBinding).build()
            lateinit var response: Response
            try {
                response = client.newCall(request).execute()
            } catch (e: Exception) {
                findServer()
                try {
                    urlCheckBinding = httpURL + "API/THD-lock"
                    response =
                        client.newCall(Request.Builder().url(urlCheckBinding).build()).execute()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        onRequestListener.onError("Ошибка.Перезарузите приложение")
                    }
                }
            }
            try {
                val jsonData = response.body?.string()
                val jsonObject = jsonData?.let {
                    JSONObject(it)
                }
                if (response.isSuccessful) {
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
                            val qualityControlRight =
                                loginObject.getBoolean("quality_control_right")
                            val user =
                                User(
                                    loginObjectId,
                                    name,
                                    storageRight,
                                    planRight,
                                    qualityControlRight
                                )
                            withContext(Dispatchers.Main) {
                                onRequestListener.onRequestOnCreate(
                                    ipServer,
                                    status,
                                    isComp,
                                    id,
                                    isLoggedIn,
                                    user
                                )
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onRequestListener.onRequestOnCreate(
                                    ipServer,
                                    status,
                                    isComp,
                                    id,
                                    isLoggedIn
                                )
                            }
                        }

                    }
                } else {
                    jsonObject?.let {
                        val errorMessage = it.getString("error")
                        withContext(Dispatchers.Main) {
                            onRequestListener.onError(errorMessage)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onRequestListener.onError("Ошибка.Перезапустите приложение.")
                }
            }
        }
    }

    fun requestGetUserData(id: String) {
        onRequestListener.onLoading()
        val urlGetUserData = httpURL + "API/get-user-data?id=$id"
        val request = Request.Builder().url(urlGetUserData).build()
        lateinit var response: Response
        GlobalScope.launch(Dispatchers.IO) {
            response = client.newCall(request).execute()
            val jsonData = response.body?.string()
            try {
                val jObject = jsonData?.let {
                    JSONObject(it)
                }
                var user: User? = null
                if (jObject != null) {
                    val status = jObject.getBoolean("status")
                    if (status) {
                        val name = jObject.getString("name")
                        val storageRight = jObject.getBoolean("storage_right")
                        val planRight = jObject.getBoolean("plan_right")
                        val qualityControlRight = jObject.getBoolean("quality_control_right")
                        user = User(id, name, storageRight, planRight, qualityControlRight)
                    }
                }
                withContext(Dispatchers.Main) {
                    onRequestListener.onGetUserData(user)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onRequestListener.onError()
                }
            }
        }
    }

    fun requestLogin(user: User) {
        onRequestListener.onLoading()
        val urlLogin = httpURL + "API/login"
        val formBody: RequestBody = FormBody.Builder()
            .add("id", user.id)
            .build()
        val request = Request.Builder().url(urlLogin).post(formBody).build()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                client.newCall(request).execute().close()
                withContext(Dispatchers.Main) {
                    onRequestListener.onLogin(user)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onRequestListener.onError()
                }
            }
        }
    }

    fun requestLogout(id: String) {
        onRequestListener.onLoading()
        val urlLogout = httpURL + "API/logout"
        val formBody: RequestBody = FormBody.Builder()
            .add("id", id)
            .build()
        val request = Request.Builder().url(urlLogout).post(formBody).build()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                client.newCall(request).execute()
                withContext(Dispatchers.Main) {
                    onRequestListener.onLogout()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onRequestListener.onError()
                }
            }
        }
    }

    fun findServer() {
        val prefix: String = Utils.getIPAddress(true).let {
            it.substring(0, it.lastIndexOf(".") + 1)
        }
        for (i in 0..254) {
            val testIp: String = prefix + i.toString()
            val address: InetAddress = InetAddress.getByName(testIp)
            val reachable = address.isReachable(100)
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
                            if (jsonObject != null && jsonObject.getBoolean("status")) {
                                ipServer = testIp
                                httpURL = "http://$ipServer:$PORT/"
                                prefs.edit().putString("ip_server", ipServer).apply()
                                return
                            } else continue
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
        onRequestListener.onLoading()
        val urlLogout = httpURL + "API/crate/$id/"
        val request = Request.Builder().url(urlLogout)
            .patch(("{\"amount\":$amount}").toRequestBody("application/json".toMediaType())).build()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    response.body?.string()?.let {
                        val jsonObject = JSONObject(it)
                        val status = jsonObject.getBoolean("status")
                        if (!status) {
                            val errorMessage = jsonObject.getString("error")
                            withContext(Dispatchers.Main) {
                                onRequestListener.onError(errorMessage = errorMessage)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onRequestListener.onStopLoading()
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                withContext(Dispatchers.Main) {
                    onRequestListener.onError()
                }
            }
        }
    }

    fun requestReplace(id: String, idCell: Int) {
        onRequestListener.onLoading()
        val urlLogout = httpURL + "API/move-crate/$id/"
        val formBody: RequestBody = FormBody.Builder()
            .add("cell", idCell.toString())
            .build()
        val request = Request.Builder().url(urlLogout)
            .patch(("{\"cell\":$idCell}").toRequestBody("application/json".toMediaType())).build()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    response.body?.string()?.let {
                        val jsonObject = JSONObject(it)
                        val status = jsonObject.getBoolean("status")
                        if (!status) {
                            val errorMessage = jsonObject.getString("error")
                            withContext(Dispatchers.Main) {
                                onRequestListener.onError(errorMessage = errorMessage)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onRequestListener.onStopLoading()
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                withContext(Dispatchers.Main) {
                    onRequestListener.onError()
                }
            }
        }
    }

    fun requestPositionCrate(idCrate: String, lockedList: MutableList<Int>) {
        onRequestListener.onLoading()
        val postRequestPositionCrateObject = object {
            val id = idCrate
            val blocked = lockedList
        }
        val gson =
            GsonBuilder().registerTypeAdapterFactory(ConstructorTypeAdapterFactory).create()
        val requestBody =
            RequestBody.create(
                "application/json".toMediaType(),
                gson.toJson(postRequestPositionCrateObject)
            )
        val request = Request.Builder()
            .url(httpURL + "API/crate-positioning")
            .post(requestBody).build()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val jsonData = response.body?.string()
                    val visualisationObject = jsonData?.let {
                        gson.fromJson(it, VisualisationObject::class.java)
                    }
                    if (visualisationObject != null)
                        withContext(Dispatchers.Main) {
                            onRequestListener.onPutCrate(
                                visualisationObject.listBoxes,
                                visualisationObject.bin,
                                idCrate,
                                lockedList
                            )
                        }
                } else {
                    response.body?.string()?.let {
                        val errorMessage = JSONObject(it).getString("error")
                        withContext(Dispatchers.Main) {
                            onRequestListener.onError(errorMessage = errorMessage)
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onRequestListener.onError()
                }
            }
        }
    }

    fun requestRegisterCrate(idCrate: String, idCell: String) {
        onRequestListener.onLoading()
        GlobalScope.launch(Dispatchers.IO) {
            val postRequestRegisterCrateObject = object {
                val crateId = idCrate
                val cellId = idCell
            }
            val gson =
                GsonBuilder().registerTypeAdapterFactory(ConstructorTypeAdapterFactory).create()
            val requestBody =
                RequestBody.create(
                    "application/json".toMediaType(),
                    gson.toJson(postRequestRegisterCrateObject)
                )
            val request = Request.Builder()
                .url(httpURL + "API/register-crate")
                .post(requestBody).build()
            try {
                client.newCall(request).execute()
                withContext(Dispatchers.Main) {
                    onRequestListener.onStopLoading()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onRequestListener.onError()
                }
            }
        }
    }
}