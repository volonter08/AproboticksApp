package com.example.aproboticksapp.list_flaw

import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.aproboticksapp.databinding.ItemListFlawBinding

class FlawHolder(val binding: ItemListFlawBinding,val remove: (Flaw)->Unit): ViewHolder(binding.root){
     fun bind(flaw: Flaw){
        binding.apply {
            nomenclatureFlaw.text = flaw.nomenclature
            amountFlaw.text = flaw.amount.toString()
            dateFlaw.text = flaw.date.toString()
            autoCompleteSolutionFlaw.setAdapter( ArrayAdapter<String>(
                    binding.root.context, android.R.layout.simple_dropdown_item_1line, arrayOf("Брак","Не брак")))
            autoCompleteSolutionFlaw.setOnClickListener{
                (it as AutoCompleteTextView).showDropDown()
            }
            sendButton.setOnClickListener{
                remove(flaw)
            }
        }
     }
}