package com.example.aproboticksapp

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ListAdapter
import com.example.aproboticksapp.databinding.ItemListFlawBinding

class FlawAdapter(val remove:(Flaw)->Unit) : androidx.recyclerview.widget.ListAdapter<Flaw,FlawHolder>(DiffFlawCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlawHolder {
        val binding= ItemListFlawBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FlawHolder(binding,remove)
    }

    override fun onBindViewHolder(holder: FlawHolder, position: Int) {
           holder.bind(getItem(position))
    }
}