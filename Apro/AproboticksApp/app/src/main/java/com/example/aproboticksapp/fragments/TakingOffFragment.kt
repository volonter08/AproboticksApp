package com.example.aproboticksapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.aproboticksapp.MainActivity.Companion.BROADCAST_ACTION
import com.example.aproboticksapp.databinding.TakeOffFragmentBinding
import com.example.aproboticksapp.requests.HttpRequestManager


class TakingOffFragment(val httpRequestManager: HttpRequestManager) : Fragment() {
    private lateinit var takingOffFragmentReceiver: BroadcastReceiver
    private var idCrate: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val takeOffFragmentBinding = TakeOffFragmentBinding.inflate(inflater)
        val simpleArray = arrayOf("Производство", "Брак")
        val adapter = ArrayAdapter<String>(
            requireContext(), android.R.layout.simple_dropdown_item_1line, simpleArray
        )
        takeOffFragmentBinding.takeOffButton.setOnClickListener {
            when {
                (idCrate == null) -> {
                    Toast.makeText(
                        requireContext(),
                        "Отсканируйте штрих-код коробки!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                (takeOffFragmentBinding.amountDetail.text == null) -> {
                    Toast.makeText(requireContext(), "Введите кол-во деталей!", Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                    httpRequestManager.requestTakeOff(idCrate!!,takeOffFragmentBinding.amountDetail.text.toString())
                    takeOffFragmentBinding.amountDetail.text.clear()
                    idCrate = null
                    takeOffFragmentBinding.idCrate.text = null
                }
            }
        }
        takingOffFragmentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                idCrate = intent?.getStringExtra("EXTRA_BARCODE_DECODING_DATA")
                takeOffFragmentBinding.idCrate.setText(idCrate)
            }
        }
        val intentFilter = IntentFilter(BROADCAST_ACTION)
        requireContext().registerReceiver(takingOffFragmentReceiver, intentFilter)
        return takeOffFragmentBinding.root
    }
    override fun onDestroyView() {
        requireContext().unregisterReceiver(takingOffFragmentReceiver)
        super.onDestroyView()
    }
    private fun testRequest(){
        val intent = Intent(MainActivity.BROADCAST_ACTION)
        intent.putExtra("EXTRA_BARCODE_DECODING_DATA","00098")
        requireContext().sendBroadcast(intent)
    }
}