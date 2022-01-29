package com.limjuhyg.blueberry.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.databinding.ActivityCustomizeSettingBinding
import com.limjuhyg.blueberry.models.CustomizeRepository
import com.limjuhyg.blueberry.models.room.entities.Customize
import com.limjuhyg.blueberry.models.room.entities.Widgets
import com.limjuhyg.blueberry.utils.addFragment
import com.limjuhyg.blueberry.utils.addFragmentWithAnimation
import com.limjuhyg.blueberry.viewmodels.CustomizeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomizeSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomizeSettingBinding
    private val customizeRepository by lazy { CustomizeRepository.getInstance(application) }
    private val customizeViewModel by lazy { ViewModelProvider(this).get(CustomizeViewModel::class.java) }
    private val widgetsContainerFragment by lazy { WidgetsContainerFragment() }
    private val keyboard by lazy { getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomizeSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editText.requestFocus()
        keyboard.showSoftInput(binding.editText, 0)
    }

    companion object CustomizeSet{
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
                            btnNext.setTextColor(ContextCompat.getColor(this@CustomizeSettingActivity, R.color.customBlack))
                            btnNext.background = ContextCompat.getDrawable(this@CustomizeSettingActivity, R.drawable.button_rounded_corners_purple)
                        }
                        else {
                            btnNext.isEnabled = false
                            btnNext.setTextColor(ContextCompat.getColor(this@CustomizeSettingActivity, R.color.gray))
                            btnNext.background = ContextCompat.getDrawable(this@CustomizeSettingActivity, R.drawable.button_rounded_corners_gray)
                        }
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        binding.apply {
            btnNext.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    val customize: Customize? = customizeRepository.getCustomize(editText.text.toString())

                    customize?.let {
                        Toast.makeText(this@CustomizeSettingActivity, "이미 생성된 커스텀 이름입니다", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        keyboard.hideSoftInputFromWindow(editText.windowToken, 0)
                        customizeName = editText.text.toString()
                        addFragment(fragmentContainer.id, widgetsContainerFragment, true)
                        editText.setText("")
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