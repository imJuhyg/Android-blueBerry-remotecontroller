package com.limjuhyg.blueberry.views.custom

import android.animation.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.limjuhyg.blueberry.applications.MainApplication
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.databinding.ActivityCustomizeNameSettingBinding
import com.limjuhyg.blueberry.dataclass.TempCustomizeSettingData
import com.limjuhyg.blueberry.models.CustomizeRepository
import com.limjuhyg.blueberry.models.room.entities.Customize
import com.limjuhyg.blueberry.viewmodels.CustomizeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomizeNameSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomizeNameSettingBinding
    private val tempCustomizeSettingData by lazy { TempCustomizeSettingData.getInstance() }
    private val customizeRepository by lazy { CustomizeRepository.getInstance(application) }
    private val customizeViewModel by lazy { ViewModelProvider(this).get(CustomizeViewModel::class.java) }
    private val keyboard by lazy { getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager}
    private var editTextInitY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomizeNameSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.to_top_from_bottom_1, R.anim.none)

        binding.editText.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                editTextInitY = binding.editText.y
                binding.editText.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        binding.editText.translationY = MainApplication.instance.getWindowWidth().toFloat()

        AnimatorSet().apply {
            play(ObjectAnimator.ofFloat(binding.guidelineTextView, "alpha", 1.0f).apply {
                duration = 850
            }).before(ObjectAnimator.ofFloat(binding.editText, "translationY", editTextInitY).apply {
                duration = 250
            })
            start()
        }
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
                            btnNext.setTextColor(ContextCompat.getColor(this@CustomizeNameSettingActivity, R.color.customBlack))
                            btnNext.background = ContextCompat.getDrawable(this@CustomizeNameSettingActivity, R.drawable.button_rounded_corners_purple)
                        }
                        else {
                            btnNext.isEnabled = false
                            btnNext.setTextColor(ContextCompat.getColor(this@CustomizeNameSettingActivity, R.color.gray))
                            btnNext.background = ContextCompat.getDrawable(this@CustomizeNameSettingActivity, R.drawable.button_rounded_corners_gray)
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
                        Toast.makeText(this@CustomizeNameSettingActivity, "이미 생성된 커스텀 이름입니다", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        keyboard.hideSoftInputFromWindow(editText.windowToken, 0)

                        val customizeName = editText.text.toString()
                        tempCustomizeSettingData.setCustomizeName(customizeName)

                        val intent = Intent(this@CustomizeNameSettingActivity, WidgetContainerActivity::class.java)
                        startActivity(intent)
                        editText.setText("")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("CustomizeSettingAct", "onDestroy")
        tempCustomizeSettingData.instanceClear()
    }
}