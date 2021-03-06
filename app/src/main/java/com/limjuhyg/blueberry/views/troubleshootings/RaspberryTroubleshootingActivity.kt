package com.limjuhyg.blueberry.views.troubleshootings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.limjuhyg.blueberry.databinding.ActivityRaspberryTroubleshootingBinding

class RaspberryTroubleshootingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRaspberryTroubleshootingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRaspberryTroubleshootingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClose.setOnClickListener { finish() }
    }
}