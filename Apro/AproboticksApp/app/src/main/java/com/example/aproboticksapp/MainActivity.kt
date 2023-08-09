package com.example.aproboticksapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.commit
import androidx.lifecycle.MutableLiveData
import com.example.aproboticksapp.databinding.ActivityMainBinding
import com.example.aproboticksapp.fragments.AllowedActionsFragment
import com.example.aproboticksapp.fragments.FromComputerFragment
import com.example.aproboticksapp.fragments.OpenGlFragment
import com.example.aproboticksapp.fragments.SignInFragment
import com.example.aproboticksapp.network.Utils
import com.example.aproboticksapp.opengl.AproboticsOpenGLView
import com.example.aproboticksapp.opengl.Bin
import com.example.aproboticksapp.opengl.Box
import com.example.aproboticksapp.requests.HttpRequestManager
import com.example.aproboticksapp.requests.OnRequestListener
import com.example.aproboticksapp.websocket.WebSocketManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val BROADCAST_ACTION = "com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST"

        const val KEYCODE_FRONT_BUTTON = 301
        const val KEYCODE_LEFT_TRIGGER_BUTTON = 27
        const val KEYCODE_RIGHT_TRIGGER_BUTTON = 80
        const val DATA_RECV_NONE = "NONE";
        const val DATA_RECV_BROADCAST_EVENT = "BROADCAST_EVENT";
        const val DATA_RECV_KEYBOARD_EVENT = "KEYBOARD_EVENT";
        const val DATA_RECV_KEYBOARD_BROADCAST_EVENT = "KEYBOARD/BORADCAST";
        const val ACTION_CONTROL_DATA_EVENT =
            "com.xcheng.scanner.action.CONTROL_DATA_EVENT";
        const val EXTRA_DATA_EVENT = "extra_data_event";
        const val ACTION_CONTROL_SCANKEY =
            "com.xcheng.scanner.action.ACTION_CONTROL_SCANKEY"
        const val EXTRA_SCANKEY_CODE = "extra_scankey_code"
        const val EXTRA_SCANKEY_STATUS = "extra_scankey_STATUS";
        const val ACTION_OPEN_SCAN = "com.xcheng.scanner.action.OPEN_SCAN_BROADCAST"
        const val SCANKEY = "scankey"
        val TRIGGER_KEY = intArrayOf(
            KEYCODE_FRONT_BUTTON,
            KEYCODE_LEFT_TRIGGER_BUTTON,
            KEYCODE_RIGHT_TRIGGER_BUTTON
        )

    }

    var isBusyTsd = MutableLiveData<Boolean>()
    val client = OkHttpClient()
    lateinit var webSocketManager:WebSocketManager
    lateinit var httpRequestManager: HttpRequestManager
    lateinit var glSurfaceView: AproboticsOpenGLView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        webSocketManager =
            WebSocketManager(applicationContext,client, onActivityReceiveMessage = ::onActivityReceiveMessage)
        val onRequestListener = object : OnRequestListener {
            override fun onRequestOnCreate(
                ipServer: String,
                status: Boolean,
                isComp: Boolean,
                id: String,
                isLoggedIn: Boolean,
                user: User?
            ) {
                if (status) {
                    webSocketManager.connectWebSocket(id, ipServer)
                    if (isComp) {
                        GlobalScope.launch(Dispatchers.Main) {
                            supportFragmentManager.commit {
                                replace(
                                    R.id.fragment_container_view_tag,
                                    FromComputerFragment(
                                        webSocketManager
                                    )
                                )
                            }
                        }
                    } else {
                        if (!isLoggedIn)
                            supportFragmentManager.commit {
                                replace(
                                    R.id.fragment_container_view_tag,
                                    SignInFragment(httpRequestManager)
                                )
                            }
                        else {
                            supportFragmentManager.commit {
                                replace(
                                    R.id.fragment_container_view_tag,
                                    AllowedActionsFragment(user, httpRequestManager)
                                )
                            }
                        }
                    }
                }
            }

            override fun onRequestShowToast(errorMessage: String) {
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
            }

            override fun onFindServer() {
                binding.searchServerProgressBar.visibility = View.VISIBLE
            }
            override fun onPutCrate(listBox:List<Box>, bin: Bin){
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle("Положение коробки")
                    .setMessage("Расположите коробку в ячейке ${bin.id}")
                    .setNegativeButton("Визуализировать ячейку") { dialog, which ->
                        supportFragmentManager.commit {
                            replace(
                                R.id.fragment_container_view_tag,
                                OpenGlFragment(listBox,bin)
                            )
                            addToBackStack("opengl_fragment")
                        }
                    }
                    .setPositiveButton("ОК") { dialog, which ->
                    }
                    .show()
            }
        }
        httpRequestManager = HttpRequestManager(applicationContext, client, onRequestListener)
        setScanSetting()
        CoroutineScope(Dispatchers.Main).launch {
            httpRequestManager.requestOnCreate()
        }
    }

    override fun onStart() {
        super.onStart()
        val gson = GsonBuilder().create()
        val ip = Utils.getIPAddress(true)
        webSocketManager.webSocket?.send(gson.toJson(CodeObject(11, DataObject(ip))))
    }

    fun setScanSetting() {
        for (key in (TRIGGER_KEY)) {
            val intentTriggerButton = Intent();
            intentTriggerButton.setAction(ACTION_CONTROL_SCANKEY);
            intentTriggerButton.putExtra(EXTRA_SCANKEY_CODE, key);
            intentTriggerButton.putExtra(EXTRA_SCANKEY_STATUS, true);
            this.sendBroadcast(intentTriggerButton)
        }
        val intent: Intent = Intent()
        intent.setAction(ACTION_CONTROL_DATA_EVENT)
        intent.putExtra(EXTRA_DATA_EVENT, DATA_RECV_BROADCAST_EVENT)
        this.sendBroadcast(intent)
    }

    override fun onStop() {
        val gson = GsonBuilder().create()
        val ip = "10.241.95.34"
        webSocketManager.webSocket?.send(gson.toJson(CodeObject(101, DataObject(ip))))
        super.onStop()
    }
    /*
    suspend fun requestOnOpen() {
        val urlCheckBinding = "http://192.168.8.54:8000/API/THD-lock"
        val request = Request.Builder().url(urlCheckBinding).build()
        isBusyTsd.value = CoroutineScope(Dispatchers.IO).async {
            val responses = client.newCall(request).execute()
            val jsonData = responses.body?.string()
            try {
                val jObject = jsonData?.let {
                    JSONObject(it)
                }
                Log.d("Jobject",jObject.toString())
                if (jObject != null) {
                    val obj = jObject as JSONObject
                    val status = obj.getBoolean("status")
                    if (status) {
                        val isComp = obj.getBoolean("is_comp")
                        val id  = obj.getString("id")
                        webSocketManager.connectWebSocket(id)
                        if (isComp) {
                            withContext(Dispatchers.Main) {
                                supportFragmentManager.commit {
                                    replace(
                                        R.id.fragment_container_view_tag,
                                        FromComputerFragment(webSocketManager.tsdStatusWebSocket.action,webSocketManager.webSocket!!)
                                    )
                                }
                            }
                        } else {
                            val isLoggedIn = obj.getBoolean("is_logged_in")
                            if (!isLoggedIn)
                                supportFragmentManager.commit {
                                    replace(
                                        R.id.fragment_container_view_tag,
                                        SignInFragment()
                                    )
                                }
                            else {
                                val loginObject = obj.getJSONObject("login")
                                val id  = loginObject.getString("id")
                                val name = loginObject.getString("name")
                                val storageRight = loginObject.getBoolean("storage_right")
                                val planRight =  loginObject.getBoolean("plan_right")
                                val qualityControlRight =  loginObject.getBoolean("quality_control_right")
                                val user = User(id,name,storageRight,planRight,qualityControlRight)
                                supportFragmentManager.commit {
                                    replace(
                                        R.id.fragment_container_view_tag,
                                        AllowedActionsFragment(user)
                                    )
                                }
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "ТСД занято!", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                    return@async status
                } else return@async false
            } catch (e: Exception) {
                return@async false
            }
        }.await()
    }



    private fun createWebSocketRequest(id: String): Request {
        val websocketURL = "ws://192.168.8.54:8000/ws/THD-ws/$id"
        return Request.Builder()
            .url(websocketURL)
            .build()
    }

     */

    private fun onActivityReceiveMessage(code: Int) {
        when (code) {
            0 -> supportFragmentManager.commit {
                replace(
                    R.id.fragment_container_view_tag,
                    FromComputerFragment(webSocketManager)
                )
            }

            1010 -> supportFragmentManager.commit {
                replace(
                    R.id.fragment_container_view_tag,
                    SignInFragment(httpRequestManager)
                )
            }
        }
    }
}