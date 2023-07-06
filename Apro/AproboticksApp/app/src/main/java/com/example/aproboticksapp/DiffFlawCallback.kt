package com.example.aproboticksapp

import androidx.recyclerview.widget.DiffUtil

class DiffFlawCallback: DiffUtil.ItemCallback<Flaw>() {
    override fun areItemsTheSame(oldItem: Flaw, newItem: Flaw): Boolean {
        return oldItem.equals(newItem)
    }

    override fun areContentsTheSame(oldItem: Flaw, newItem: Flaw): Boolean {
        return oldItem==newItem
    }
}