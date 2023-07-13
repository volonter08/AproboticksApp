package com.example.aproboticksapp.list_flaw

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.aproboticksapp.databinding.ItemListFlawBinding

class FlawAdapter(val remove:(Flaw)->Unit) : androidx.recyclerview.widget.ListAdapter<Flaw, FlawHolder>(
    DiffFlawCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlawHolder {
        val binding= ItemListFlawBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FlawHolder(binding,remove)
    }

    override fun onBindViewHolder(holder: FlawHolder, position: Int) {
           holder.bind(getItem(position))
    }
}