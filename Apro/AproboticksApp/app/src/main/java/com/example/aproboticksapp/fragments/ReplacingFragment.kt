package com.example.aproboticksapp.fragments

import android.R
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
import com.example.aproboticksapp.MainActivity
import com.example.aproboticksapp.databinding.ReplaceFragmentBinding
import com.example.aproboticksapp.requests.HttpRequestManager


class ReplacingFragment(val httpRequestManager: HttpRequestManager) : Fragment() {
    private lateinit var replacingReceiver: BroadcastReceiver
    private var idCrate:String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val replaceFragmentBinding = ReplaceFragmentBinding.inflate(inflater)
        val simpleArray = arrayOf("Производство", "Брак")
        val adapter = ArrayAdapter<String>(
            requireContext(), R.layout.simple_dropdown_item_1line, simpleArray
        )
        val filter =
            InputFilter { src, start, end, d, dstart, dend ->
                for (i in start until end) {
                    if (src[i].code !in (48..57)) {
                        return@InputFilter src.subSequence(start, i)
                    }
                }
                return@InputFilter src.subSequence(start, end)
            }
        replaceFragmentBinding.numberCell.filters = arrayOf(filter)
        replaceFragmentBinding.replaceButton.setOnClickListener {
            when {
                (idCrate==null) -> {
                    Toast.makeText(
                        requireContext(),
                        "Отсканируйте штрих-код коробки!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                replaceFragmentBinding.numberCell.text == null -> {
                    Toast.makeText(requireContext(), "Введите кол-во деталей!", Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                    httpRequestManager.requestReplace(idCrate!!,replaceFragmentBinding.numberCell.text.toString().toInt())
                    idCrate = null
                    replaceFragmentBinding.idCrate.text= null
                    replaceFragmentBinding.numberCell.text = null
                }
            }
        }
        replacingReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                idCrate = intent?.getStringExtra("EXTRA_BARCODE_DECODING_DATA")
                replaceFragmentBinding.idCrate.setText(idCrate)
            }
        }
        val intentFilter = IntentFilter(MainActivity.BROADCAST_ACTION)
        requireContext().registerReceiver(replacingReceiver, intentFilter)
        return replaceFragmentBinding.root
    }
    override fun onDestroyView() {
        requireContext().unregisterReceiver(replacingReceiver)
        super.onDestroyView()
    }
    private fun testRequest(){
        val intent = Intent(MainActivity.BROADCAST_ACTION)
        intent.putExtra("EXTRA_BARCODE_DECODING_DATA","00098")
        requireContext().sendBroadcast(intent)
    }
}