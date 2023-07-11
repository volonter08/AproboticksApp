package com.example.aproboticksapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.commit
import androidx.lifecycle.MutableLiveData
import com.example.aproboticksapp.databinding.ActivityMainBinding
import com.example.aproboticksapp.fragments.*
import com.example.aproboticksapp.websocket.TsdStatus
import com.example.aproboticksapp.websocket.TsdWebSocketListener
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import org.json.JSONObject

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

        const val URLBASE = "://192.168.8.54:5555/"
    }

    var isBusyTsd = MutableLiveData<Boolean>()
    var tsdStatus = TsdStatus()
    val client = OkHttpClient()
    var webSocketListener = TsdWebSocketListener(tsdStatus)
    private var webSocket: WebSocket? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setScanSetting()
        val receivingFragment = ReceivingFragment()
        val takingOffFragment = TakingOffFragment()
        val signInFragment = SignInFragment()
        val controlQualityFragment = ControlQualityFragment()
        val allowedActionsFragment = AllowedActionsFragment()
    }
    override fun onStart() {
        super.onStart()
        CoroutineScope(Dispatchers.Main).launch {
            requestOnOpen()
        }
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

    override fun onDestroy() {
        super.onDestroy()

    }

    suspend fun requestOnOpen() {
        val urlCheckBinding = "http://" + URLBASE + "API/check-binding"
        val request = Request.Builder().url(urlCheckBinding).build()
        isBusyTsd.value = CoroutineScope(Dispatchers.IO).async {
            val responses = client.newCall(request).execute()
            val jsonData = responses.body?.string()
            try {
                val jObject = jsonData?.let {
                    JSONObject(it)
                }
                if (jObject != null) {
                    val obj = jObject as JSONObject
                    val status = obj.getBoolean("is_comp")
                    webSocket = client.newWebSocket(
                        createWebSocketRequest(obj.getString("id")),
                        webSocketListener
                    )
                    if (status) {
                        withContext(Dispatchers.Main) {
                            supportFragmentManager.commit {
                                add(
                                    R.id.fragment_container_view_tag,
                                    FromComputerFragment(tsdStatus.action)
                                )
                            }
                        }
                    } else
                        supportFragmentManager.commit {
                            add(
                                R.id.fragment_container_view_tag,
                                SignInFragment()
                            )
                        }
                    return@async status
                } else return@async false
            } catch (e: Exception) {
                return@async false
            }
        }.await()
    }

    private fun createWebSocketRequest(id: String): Request {
        val websocketURL = "ws://192.168.8.54:5555/ws/THD-ws/$id"
        return Request.Builder()
            .url(websocketURL)
            .build()
    }
}