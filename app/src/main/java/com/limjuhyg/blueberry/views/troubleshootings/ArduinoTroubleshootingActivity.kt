package com.limjuhyg.blueberry.views.troubleshootings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.limjuhyg.blueberry.databinding.ActivityArduinoTroubleshootingBinding

class ArduinoTroubleshootingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArduinoTroubleshootingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArduinoTroubleshootingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClose.setOnClickListener { finish() }
    }
}