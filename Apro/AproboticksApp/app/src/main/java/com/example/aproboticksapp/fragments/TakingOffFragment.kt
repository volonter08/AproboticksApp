package com.example.aproboticksapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.aproboticksapp.MainActivity.Companion.BROADCAST_ACTION
import com.example.aproboticksapp.databinding.TakeOffFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject


class TakingOffFragment() : Fragment() {
    private lateinit var takingOffFragmentReceiver: BroadcastReceiver
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
        takeOffFragmentBinding.autoCompleteTextView.setAdapter(adapter)
        takeOffFragmentBinding.layoutAutoCompleteTextView.setOnClickListener {
            takeOffFragmentBinding.autoCompleteTextView.showDropDown()

        }
        takeOffFragmentBinding.autoCompleteTextView.setOnClickListener {
            takeOffFragmentBinding.autoCompleteTextView.showDropDown()
        }
        takingOffFragmentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val barCode = intent?.getStringExtra("EXTRA_BARCODE_DECODING_DATA")
                takeOffFragmentBinding.takinOffArticul.setText(barCode)
                requestBaseData(takeOffFragmentBinding,barCode?:"")
                println("onReceive")
            }

        }
        val intentFilter = IntentFilter(BROADCAST_ACTION)
        requireContext().registerReceiver(takingOffFragmentReceiver, intentFilter)
        return takeOffFragmentBinding.root
    }

    override fun onDestroy() {
        requireContext().unregisterReceiver(takingOffFragmentReceiver)
        super.onDestroy()
    }

    fun requestBaseData(takeOffFragmentBinding:TakeOffFragmentBinding,article:String) {
        val client = OkHttpClient()
        val url =
            "http://192.168.107.215:5555/API/get_nomenclature?article=$article"
        val request = Request.Builder().url(url).build()
        CoroutineScope(Dispatchers.IO).launch {
            val responses = client.newCall(request).execute()
            val jsonData = responses.body?.string()
            try {
                val jObject = jsonData?.let {

                    JSONObject(it)
                }
                withContext(Dispatchers.Main) {
                    if (jObject != null) {
                        val obj = jObject as JSONObject
                        val nomenclature = obj.getString("nomenclature")
                        takeOffFragmentBinding.takingOffNomenclature.setText(nomenclature)
                    }
                }
            }
            catch (e: JSONException){
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(),"Данного артикула нет в Базе",Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}