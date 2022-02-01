package com.limjuhyg.blueberry.views.custom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.databinding.ActivityCustomizeConnectSettingBinding
import com.limjuhyg.blueberry.dataclass.TempCustomizeSettingData

class CustomizeConnectSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomizeConnectSettingBinding
    private val tempCustomizeSettingData by lazy { TempCustomizeSettingData.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomizeConnectSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        binding.btnBefore.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO 건너뛰기 or 완료 시 이전 액티비티 모두 종료, Customize 저장
        // TODO 건너뛰기

    }
}