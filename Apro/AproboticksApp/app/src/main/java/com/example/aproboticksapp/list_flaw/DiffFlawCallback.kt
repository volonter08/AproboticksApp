package com.example.aproboticksapp.list_flaw

import androidx.recyclerview.widget.DiffUtil
import com.example.aproboticksapp.list_flaw.Flaw

class DiffFlawCallback: DiffUtil.ItemCallback<Flaw>() {
    override fun areItemsTheSame(oldItem: Flaw, newItem: Flaw): Boolean {
        return oldItem.equals(newItem)
    }

    override fun areContentsTheSame(oldItem: Flaw, newItem: Flaw): Boolean {
        return oldItem==newItem
    }
}