package com.example.aproboticksapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.example.aproboticksapp.MainActivity.Companion.ACTION_OPEN_SCAN
import com.example.aproboticksapp.MainActivity.Companion.KEYCODE_FRONT_BUTTON
import com.example.aproboticksapp.MainActivity.Companion.SCANKEY
import com.example.aproboticksapp.databinding.FromComputerFragmentBinding

class FromComputerFragment(val action: MutableLiveData<String?>) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val fromComputerFragmentBinding = FromComputerFragmentBinding.inflate(layoutInflater)
        val actionTextView =  fromComputerFragmentBinding.action
        action.observe(viewLifecycleOwner){
            if(it==null){
                actionTextView.text = "ЗАГРУЗКА..."
            }
            else{
                actionTextView.text = it
                turnOnScanner()
            }
        }
        return fromComputerFragmentBinding.root
    }
    private fun turnOnScanner() {
        val intentTurnOn = Intent();
        intentTurnOn.setAction(ACTION_OPEN_SCAN);
        intentTurnOn.putExtra(SCANKEY, KEYCODE_FRONT_BUTTON);
        context?.sendBroadcast(intentTurnOn);
    }
}