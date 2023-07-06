package com.example.aproboticksapp

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.aproboticksapp.databinding.TakeOffFragmentBinding
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class TakingOffFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val takeOffFragmentBinding = TakeOffFragmentBinding.inflate(inflater)
        val simpleArray = arrayOf("Производство","Брак")
        val adapter = ArrayAdapter<String>(
            requireContext(), android.R.layout.simple_dropdown_item_1line, simpleArray
        )
        takeOffFragmentBinding.autoCompleteTextView.setAdapter(adapter)
        takeOffFragmentBinding.layoutAutoCompleteTextView.setOnClickListener{
            takeOffFragmentBinding.autoCompleteTextView.showDropDown()

        }
        takeOffFragmentBinding.autoCompleteTextView.setOnClickListener{
            takeOffFragmentBinding.autoCompleteTextView.showDropDown()
        }
        return takeOffFragmentBinding.root
    }
}