package com.example.aproboticksapp.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aproboticksapp.MainActivity
import com.example.aproboticksapp.MainActivity.Companion.ACTION_OPEN_SCAN
import com.example.aproboticksapp.MainActivity.Companion.KEYCODE_FRONT_BUTTON
import com.example.aproboticksapp.MainActivity.Companion.SCANKEY
import com.example.aproboticksapp.ScannerHandler
import com.example.aproboticksapp.databinding.SignInFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import okhttp3.Request
import org.json.JSONObject


class SignInFragment : Fragment() {
    private lateinit var signInFragmentReceiver: BroadcastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            /*
        CoroutineScope(Dispatchers.IO).launch {
            webSocket = client.newWebSocket(createWebSocketRequest(), webSocketListener)
            withContext(Dispatchers.Main) {
                tsdStatus.isRequestToTurnOffScannerFromComputer.observe(viewLifecycleOwner) {
                    if (it == "true")
                        turnOnScanner()
                }
            }
        }

             */
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStartFragment")
            /*
        CoroutineScope(Dispatchers.Main).launch {
            requestOnOpen()
            println("${isBusyTsd.value}")

        }

             */
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val signInFragmentBinding = SignInFragmentBinding.inflate(inflater)
        signInFragmentBinding.scanButton.setOnClickListener {
            if (ScannerHandler.scanUsers()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Подтверждение")
                    .setMessage("Войти как ")
                    .setNegativeButton("НЕТ") { dialog, which ->
                    }
                    .setPositiveButton("ДА") { dialog, which ->
                    }
                    .show()
            } else {
                signInFragmentBinding.notificationTextView.text =
                    "Пропуск считался некорректно Попробуйте еще раз"
            }
        }
        signInFragmentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val barCode = intent?.getStringExtra("EXTRA_BARCODE_DECODING_DATA")
            }
        }
        /*
        val progressBar = signInFragmentBinding.progressbar
        progressBar.visibility = View.VISIBLE
        isBusyTsd.observe(viewLifecycleOwner) {
            progressBar.visibility = View.GONE
        }
        */
        val intentFilter = IntentFilter(MainActivity.BROADCAST_ACTION)
        requireContext().registerReceiver(signInFragmentReceiver, intentFilter)
        return signInFragmentBinding.root
    }
    /*
    suspend fun requestOnOpen() {
        val url =
            "http://192.168.8.54:5555/API/check-binding"
        val request = Request.Builder().url(url).build()
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
                    return@async status
                } else return@async false
            } catch (e: Exception) {
                return@async false
            }
        }.await()
    }
    fun requestUrl(){

    }
    override fun onDestroy() {
        super.onDestroy()
    }

    private fun createWebSocketRequest(): Request {
        val websocketURL = "ws://192.168.8.54:5555/ws/test/1"
        return Request.Builder()
            .url(websocketURL)
            .build()
    }

    private fun turnOnScanner() {
        val intentTurnOn = Intent();
        intentTurnOn.setAction(ACTION_OPEN_SCAN);
        intentTurnOn.putExtra(SCANKEY, KEYCODE_FRONT_BUTTON);
        context?.sendBroadcast(intentTurnOn);
    }

     */
}