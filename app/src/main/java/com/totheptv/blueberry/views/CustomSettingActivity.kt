package com.totheptv.blueberry.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.totheptv.blueberry.R
import com.totheptv.blueberry.databinding.ActivityCustomSettingBinding
import com.totheptv.blueberry.models.CustomizeRepository
import com.totheptv.blueberry.models.room.entities.Customize
import com.totheptv.blueberry.viewmodels.CustomizeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class CustomSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomSettingBinding
    private val customizeRepository by lazy { CustomizeRepository.getInstance(application) }
    private val customizeViewModel by lazy { ViewModelProvider(this).get(CustomizeViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    companion object CustomSet{
        lateinit var customizeName: String
        var deviceName: String? = null
        lateinit var deviceAddress: String
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            editText.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                    text?.let {
                        if(it.isNotEmpty()) {
                            btnNext.isEnabled = true
                            btnNext.setTextColor(ContextCompat.getColor(this@CustomSettingActivity, R.color.customBlack))
                            btnNext.background = ContextCompat.getDrawable(this@CustomSettingActivity, R.drawable.button_round_corners_purple)
                        }
                        else {
                            btnNext.isEnabled = false
                            btnNext.setTextColor(ContextCompat.getColor(this@CustomSettingActivity, R.color.gray))
                            btnNext.background = ContextCompat.getDrawable(this@CustomSettingActivity, R.drawable.button_round_corners_gray)
                        }
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        binding.apply {
            btnNext.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    // IO thread
                    val customize: Customize? = customizeRepository.getCustomize(editText.text.toString())

                    // Main thread
                    launch(Dispatchers.Main) {
                        customize?.let {
                            Toast.makeText(this@CustomSettingActivity, "이미 생성된 커스텀 이름입니다", Toast.LENGTH_SHORT).show()
                        } ?: let {
                            customizeName = editText.text.toString()
                            // TODO 다음 단계 진행
                            Log.d("DEBUG", "다음 단계")
                        }
                    }
                }
            }
        }
    }

    fun createCustomize() {
        // TODO 연결정보가 있는 경우와 없는 경우
    }

    override fun onDestroy() {
        super.onDestroy()

        // TODO customName, 컴포넌트 xml, deviceName?, deviceAddress
        // TODO setResult(RESULT_OK)
    }
}