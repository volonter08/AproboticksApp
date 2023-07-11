package com.example.aproboticksapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aproboticksapp.databinding.RecieveFragmentBinding

class ReceivingFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val receivingBinding= RecieveFragmentBinding.inflate(inflater)
        return receivingBinding.root
    }
}