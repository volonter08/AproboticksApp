package com.example.aproboticksapp.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.aproboticksapp.*
import com.example.aproboticksapp.MainActivity.Companion.ACTION_OPEN_SCAN
import com.example.aproboticksapp.MainActivity.Companion.BROADCAST_ACTION
import com.example.aproboticksapp.MainActivity.Companion.KEYCODE_FRONT_BUTTON
import com.example.aproboticksapp.MainActivity.Companion.SCANKEY
import com.example.aproboticksapp.databinding.SignInFragmentBinding
import com.example.aproboticksapp.requests.HttpRequestManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request


class SignInFragment(val httpRequestManager: HttpRequestManager) : Fragment() {
    val client:OkHttpClient = OkHttpClient()
    private lateinit var signInFragmentReceiver: BroadcastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val signInFragmentBinding = SignInFragmentBinding.inflate(inflater)
        fun onOpenMaterialDialog(user: User){
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Подтверждение")
                .setMessage("Войти как ${user.name} ")
                .setNegativeButton("НЕТ") { dialog, which ->
                }
                .setPositiveButton("ДА") { dialog, which ->
                    activity?.supportFragmentManager?.commit {
                        replace(R.id.fragment_container_view_tag,AllowedActionsFragment(user,httpRequestManager))
                    }
                }
                .show()
        }
        fun onOpenWithRepeatingAuthorization(){
            signInFragmentBinding.notificationTextView.text =
                "Пропуск считался некорректно Попробуйте еще раз"
        }
        signInFragmentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getStringExtra("EXTRA_BARCODE_DECODING_DATA")
                GlobalScope.launch(Dispatchers.Main){
                    authorization(id!!,::onOpenMaterialDialog,::onOpenWithRepeatingAuthorization)
                }
            }
        }
        val intentFilter = IntentFilter(MainActivity.BROADCAST_ACTION)
        requireContext().registerReceiver(signInFragmentReceiver, intentFilter)
        testRequest()
        return signInFragmentBinding.root
    }
    suspend fun authorization(id:String,onOpenMaterialDialog:(User)->Unit,onOpenWithRepeatingAuthorization:()->Unit ) {
        val user = httpRequestManager.requestLogin(id)
        if(user ==null){
           onOpenWithRepeatingAuthorization()
        }
        else
            onOpenMaterialDialog(user)
    }

    override fun onPause() {
        super.onPause()
        println("onPause")
    }
    override fun onDestroyView() {
        requireContext().unregisterReceiver(signInFragmentReceiver)
        println("onDestroyView")
        super.onDestroyView()
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
    private fun testRequest(){
        val intent = Intent(BROADCAST_ACTION)
        intent.putExtra("EXTRA_BARCODE_DECODING_DATA","1111111")
        requireContext().sendBroadcast(intent)
    }
}