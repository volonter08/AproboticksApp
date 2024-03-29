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
import com.example.aproboticksapp.databinding.RecieveFragmentBinding
import com.example.aproboticksapp.requests.HttpRequestManager
import java.util.LinkedList

class ReceivingFragment(val httpRequestManager: HttpRequestManager) : Fragment() {
    private lateinit var receiveFragmentReceiver: BroadcastReceiver
    private var lockedCratesList= LinkedList<Int>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val receivingBinding = RecieveFragmentBinding.inflate(inflater)
        receiveFragmentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val idCrate = intent?.getStringExtra("EXTRA_BARCODE_DECODING_DATA")
                httpRequestManager.requestPositionCrate(idCrate!!,lockedCratesList)
            }
        }
        val intentFilter = IntentFilter(MainActivity.BROADCAST_ACTION)
        requireContext().registerReceiver(receiveFragmentReceiver, intentFilter)
        return receivingBinding.root
    }
    override fun onDestroyView() {
        requireContext().unregisterReceiver(receiveFragmentReceiver)
        super.onDestroyView()
    }
    private fun testRequest(){
        val intent = Intent(MainActivity.BROADCAST_ACTION)
        intent.putExtra("EXTRA_BARCODE_DECODING_DATA","00100")
        requireContext().sendBroadcast(intent)
    }
}