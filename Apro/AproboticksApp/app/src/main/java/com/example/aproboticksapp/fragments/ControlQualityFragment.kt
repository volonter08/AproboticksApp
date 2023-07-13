package com.example.aproboticksapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.example.aproboticksapp.list_flaw.Flaw
import com.example.aproboticksapp.list_flaw.FlawAdapter
import com.example.aproboticksapp.databinding.ControlQualityFragmentBinding
import java.util.*

class ControlQualityFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var listFlaw = listOf<Flaw>(
            Flaw(0, "191-10-02-01 Гайка", 200, Date()),
            Flaw(1, "191-13-01 Запальник (задел исп-ть)", 1200, Date()),
            Flaw(2, "193-01-08 Втулка  (черная)", 2000, Date()),
        )
        var data = MutableLiveData<List<Flaw>>(listFlaw)
        fun remove(flaw: Flaw) {
            listFlaw = listFlaw.filter {
                it.id != flaw.id
            }
            data.value = listFlaw
        }

        super.onCreateView(inflater, container, savedInstanceState)
        val controlQualityFragmentBinding = ControlQualityFragmentBinding.inflate(inflater)
        val flawAdapter = FlawAdapter(::remove)
        controlQualityFragmentBinding.listFlaw.adapter = flawAdapter
        flawAdapter.submitList(listFlaw)
        data.observe(this.viewLifecycleOwner){
              flawAdapter.submitList(it)
        }
        return controlQualityFragmentBinding.root


    }
}