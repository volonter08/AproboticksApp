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
import com.example.aproboticksapp.MainActivity
import com.example.aproboticksapp.MainActivity.Companion.ACTION_OPEN_SCAN
import com.example.aproboticksapp.MainActivity.Companion.KEYCODE_FRONT_BUTTON
import com.example.aproboticksapp.MainActivity.Companion.SCANKEY
import com.example.aproboticksapp.databinding.FromComputerFragmentBinding
import com.example.aproboticksapp.websocket.TsdStatusWebSocket
import com.example.aproboticksapp.websocket.WebSocketManager

class FromComputerFragment(val webSocketManager: WebSocketManager) : Fragment() {
    private lateinit var fromComputerFragmentReceiver: BroadcastReceiver
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val fromComputerFragmentBinding = FromComputerFragmentBinding.inflate(layoutInflater)
        val actionTextView =  fromComputerFragmentBinding.action
        TsdStatusWebSocket.action.observe(viewLifecycleOwner){
            if(it==null){
                actionTextView.text = "ЗАГРУЗКА..."
            }
            else{
                actionTextView.text = it
            }
        }
        fromComputerFragmentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getStringExtra("EXTRA_BARCODE_DECODING_DATA")
                webSocketManager.sendIdForLogin(id!!)
            }
        }
        val intentFilter = IntentFilter(MainActivity.BROADCAST_ACTION)
        requireContext().registerReceiver(fromComputerFragmentReceiver, intentFilter)
        return fromComputerFragmentBinding.root
    }

    override fun onDestroyView() {
        requireContext().unregisterReceiver(fromComputerFragmentReceiver)
        super.onDestroyView()
    }
    private fun turnOnScanner() {
        val intentTurnOn = Intent();
        intentTurnOn.setAction(ACTION_OPEN_SCAN);
        intentTurnOn.putExtra(SCANKEY, KEYCODE_FRONT_BUTTON);
        context?.sendBroadcast(intentTurnOn);
    }
}