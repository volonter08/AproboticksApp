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
import com.example.aproboticksapp.MainActivity.Companion.BROADCAST_ACTION
import com.example.aproboticksapp.databinding.SignInFragmentBinding
import com.example.aproboticksapp.requests.HttpRequestManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import okhttp3.OkHttpClient


class SignInFragment(val httpRequestManager: HttpRequestManager) : Fragment() {
    val client:OkHttpClient = OkHttpClient()
    private lateinit var signInFragmentReceiver: BroadcastReceiver
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val signInFragmentBinding = SignInFragmentBinding.inflate(inflater)
        signInFragmentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getStringExtra("EXTRA_BARCODE_DECODING_DATA")
                httpRequestManager.requestGetUserData(id!!)
            }
        }
        val intentFilter = IntentFilter(MainActivity.BROADCAST_ACTION)
        requireContext().registerReceiver(signInFragmentReceiver, intentFilter)
        return signInFragmentBinding.root
    }
    override fun onDestroyView() {
        requireContext().unregisterReceiver(signInFragmentReceiver)
        super.onDestroyView()
    }
    private fun testRequest(){
        val intent = Intent(BROADCAST_ACTION)
        intent.putExtra("EXTRA_BARCODE_DECODING_DATA","1111111")
        requireContext().sendBroadcast(intent)
    }
}