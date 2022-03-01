package com.limjuhyg.blueberry.views.guidelines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.limjuhyg.blueberry.databinding.ActivityAppGuidelineBinding

class AppGuidelineActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppGuidelineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppGuidelineBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        binding.btnClose.setOnClickListener { finish() }
    }
}