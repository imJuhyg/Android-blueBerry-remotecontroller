package com.limjuhyg.blueberry.views.custom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.databinding.ActivitySearchGoogleIconsBinding

class SearchGoogleIconsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchGoogleIconsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchGoogleIconsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}