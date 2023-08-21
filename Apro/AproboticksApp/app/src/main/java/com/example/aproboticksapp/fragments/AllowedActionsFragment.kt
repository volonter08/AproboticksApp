package com.example.aproboticksapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.aproboticksapp.requests.HttpRequestManager
import com.example.aproboticksapp.R
import com.example.aproboticksapp.TakingOffFragment
import com.example.aproboticksapp.User
import com.example.aproboticksapp.databinding.PageOfAllowedActionsBinding

class AllowedActionsFragment(val user: User?,val httpRequestManager: HttpRequestManager) : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val pageOfAllowedActionsBinding = PageOfAllowedActionsBinding.inflate(inflater)
        pageOfAllowedActionsBinding.logout.visibility = View.VISIBLE
        pageOfAllowedActionsBinding.logout.setOnClickListener {
            httpRequestManager.requestLogout(user!!.id)
        }
        user?.also{
            when{
                it.storageRight-> {
                    pageOfAllowedActionsBinding.apply {
                        receiveMcButton.visibility = View.VISIBLE
                        replaceMc.visibility = View.VISIBLE
                        takeOffMcButton.visibility = View.VISIBLE
                    }
                }
                it.qualityControlRight->{
                    pageOfAllowedActionsBinding.apply {
                        qualityControlButton.visibility = View.VISIBLE
                    }
                }
            }
        }
        pageOfAllowedActionsBinding.receiveMcButton.setOnClickListener {
            activity?.supportFragmentManager?.commit {
                replace(R.id.fragment_container_view_tag,ReceivingFragment(httpRequestManager),"receiving_fragment")
                addToBackStack(null)
            }
        }
        pageOfAllowedActionsBinding.takeOffMcButton.setOnClickListener {
            activity?.supportFragmentManager?.commit {
                replace(R.id.fragment_container_view_tag,TakingOffFragment(httpRequestManager),"taking_off_fragment")
                addToBackStack(null)
            }
        }
        pageOfAllowedActionsBinding.replaceMc.setOnClickListener {
            activity?.supportFragmentManager?.commit {
                replace(R.id.fragment_container_view_tag,ReplacingFragment(httpRequestManager),"replacing_fragment")
                addToBackStack(null)
            }
        }
        return pageOfAllowedActionsBinding.root
    }
}