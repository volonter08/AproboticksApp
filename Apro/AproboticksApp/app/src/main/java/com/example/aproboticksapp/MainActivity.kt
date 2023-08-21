package com.example.aproboticksapp

import com.example.aproboticksapp.R
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.DialogInterface.OnShowListener
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import com.example.aproboticksapp.databinding.ActivityMainBinding
import com.example.aproboticksapp.fragments.AllowedActionsFragment
import com.example.aproboticksapp.fragments.FromComputerFragment
import com.example.aproboticksapp.fragments.LoadingFragment
import com.example.aproboticksapp.fragments.OpenGlFragment
import com.example.aproboticksapp.fragments.SignInFragment
import com.example.aproboticksapp.network.Utils
import com.example.aproboticksapp.opengl.Bin
import com.example.aproboticksapp.opengl.Box
import com.example.aproboticksapp.requests.HttpRequestManager
import com.example.aproboticksapp.requests.OnRequestListener
import com.example.aproboticksapp.websocket.WebSocketManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.Serializable
import java.util.concurrent.TimeUnit


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
    val client:OkHttpClient
    val webSocketManager: WebSocketManager
    lateinit var httpRequestManager: HttpRequestManager
    init {
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(10,TimeUnit.SECONDS) // connect timeout
            .writeTimeout(30, TimeUnit.SECONDS) // write timeout
            .readTimeout(30, TimeUnit.SECONDS) // read timeout
        client = builder.build()
        webSocketManager = WebSocketManager(client, onActivityReceiveMessage = ::onActivityReceiveMessage)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                        supportFragmentManager.commit {
                            replace(
                                R.id.fragment_container_view_tag,
                                FromComputerFragment(
                                    webSocketManager
                                )
                            )
                        }
                    } else {
                        if (!isLoggedIn)
                            supportFragmentManager.commit {
                                replace(
                                    R.id.fragment_container_view_tag,
                                    SignInFragment(httpRequestManager),"login_fragment"
                                )
                            }
                        else {
                            supportFragmentManager.commit {
                                replace(
                                    R.id.fragment_container_view_tag,
                                    AllowedActionsFragment(user, httpRequestManager),"allowed_action_fragment"
                                )
                            }
                        }
                    }
                }
            }

            override fun onError(errorMessage: String) {
                supportFragmentManager.apply {
                    if( findFragmentById(R.id.fragment_container_view_tag) == LoadingFragment) {
                        commitNow {
                            remove(LoadingFragment)
                        }
                        commitNow {
                            findFragmentById(R.id.fragment_container_view_tag)?.let {
                                show(it)
                            }
                        }
                    }
                }
                Snackbar.make(binding.root,errorMessage, Snackbar.LENGTH_LONG).setAction("OK",null).show()
            }
            override fun onLoading() {
                supportFragmentManager.commit {
                    supportFragmentManager.findFragmentById(R.id.fragment_container_view_tag)?.let {
                        hide(it)
                    }
                    add(
                        R.id.fragment_container_view_tag,
                        LoadingFragment,"loading_fragment"
                    )
                }
            }

            override fun onPutCrate(
                listBox: List<Box>, bin: Bin,
                idCrate: String,
                lockedList: MutableList<Int>
            ) {
                supportFragmentManager.commitNow {
                    remove(LoadingFragment)
                    supportFragmentManager.findFragmentByTag("receiving_fragment")?.let {
                        show(it)
                    }
                }
                val dialog = MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle("Положение коробки")
                    .setMessage("Расположите коробку в ячейке ${bin.id}")
                    .setNegativeButton("Выбрать другую ячейку") { _, _ ->
                        lockedList.add(bin.id)
                        httpRequestManager.requestPositionCrate(idCrate, lockedList)
                    }
                    .setPositiveButton("ОК") { _, _ ->
                        httpRequestManager.requestRegisterCrate(idCrate, bin.id.toString())
                    }
                    .setNeutralButton("Визуализировать ячейку", null)
                    .create()
                dialog.setOnShowListener { dialog -> //
                    val neutralButton: Button = (dialog as AlertDialog)
                        .getButton(AlertDialog.BUTTON_NEUTRAL)
                    neutralButton.setOnClickListener {
                        val intent = Intent(this@MainActivity, OpenGLActivity::class.java).apply {
                            putExtra("listBox", listBox as Serializable)
                            putExtra("bin", bin as Serializable)
                        }
                        startActivity(intent)
                    }
                }
                dialog.show()
            }

            override fun onGetUserData(user: User?) {
                supportFragmentManager.commitNow {
                    remove(LoadingFragment)
                    supportFragmentManager.findFragmentByTag("login_fragment")?.let {
                        show(it)
                    }
                }
                if (user != null) {
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle("Подтверждение")
                        .setMessage("Войти как ${user.name} ")
                        .setNegativeButton("НЕТ") { dialog, which ->
                            Toast.makeText(this@MainActivity,"Пропуск считался некорректно,попробуйте отсканировать еще раз.", Toast.LENGTH_LONG).show()
                        }
                        .setPositiveButton("ДА") { dialog, which ->
                            httpRequestManager.requestLogin(user)
                        }
                        .show()
                }
                else{
                    Toast.makeText(this@MainActivity,"Пропуск считался некорректно,попробуйте отсканировать еще раз.", Toast.LENGTH_LONG).show()
                }

            }
            override fun onLogin(user: User) {
                supportFragmentManager.commit {
                    replace(
                        R.id.fragment_container_view_tag,
                        AllowedActionsFragment(user, httpRequestManager)
                    )
                }
            }

            override fun onLogout() {
                supportFragmentManager.commit {
                    replace(
                        R.id.fragment_container_view_tag,
                        SignInFragment(httpRequestManager),"login_fragment"
                    )
                }
            }
            override fun onStopLoading() {
                supportFragmentManager.apply {
                    if( findFragmentById(R.id.fragment_container_view_tag) == LoadingFragment) {
                        commitNow {
                            remove(LoadingFragment)
                        }
                        commitNow {
                            findFragmentById(R.id.fragment_container_view_tag)?.let {
                                show(it)
                            }
                        }
                    }
                }
            }

        }
        httpRequestManager = HttpRequestManager(applicationContext, client, onRequestListener)
        setScanSetting()
        httpRequestManager.requestOnCreate()
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
        val ip = Utils.getIPAddress(true)
        webSocketManager.webSocket?.send(gson.toJson(CodeObject(101, DataObject(ip))))
        super.onStop()
    }

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