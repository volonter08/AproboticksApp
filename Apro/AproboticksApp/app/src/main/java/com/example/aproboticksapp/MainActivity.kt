package com.example.aproboticksapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.commit
import com.example.aproboticksapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val receivingFragment = ReceivingFragment()
        val takingOffFragment = TakingOffFragment()
        val signInFragment = SignInFragment()
        val controlQualityFragment = ControlQualityFragment()
        supportFragmentManager.commit{
            add(R.id.fragment_container_view_tag,controlQualityFragment)
        }
    }
}