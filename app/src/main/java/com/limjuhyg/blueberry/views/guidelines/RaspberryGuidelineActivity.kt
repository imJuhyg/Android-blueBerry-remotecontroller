package com.limjuhyg.blueberry.views.guidelines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.limjuhyg.blueberry.databinding.ActivityRaspberryGuidelineBinding

class RaspberryGuidelineActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRaspberryGuidelineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRaspberryGuidelineBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        binding.btnClose.setOnClickListener { finish() }
    }
}