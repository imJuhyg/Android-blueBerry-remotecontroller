package com.limjuhyg.blueberry.views.custom

import android.animation.*
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.limjuhyg.blueberry.applications.MainApplication
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.databinding.ActivityCustomizeNameSettingBinding
import com.limjuhyg.blueberry.dataclass.TempCustomizeSettingData
import com.limjuhyg.blueberry.models.CustomizeRepository
import com.limjuhyg.blueberry.models.room.entities.Customize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomizeNameSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomizeNameSettingBinding
    private val tempCustomizeSettingData by lazy { TempCustomizeSettingData.getInstance() }
    private val customizeRepository by lazy { CustomizeRepository.getInstance(application) }
    private val keyboard by lazy { getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager}
    private var editTextInitY = 0f

    companion object {
        lateinit var activity: CustomizeNameSettingActivity

        var mode: Int? = null
        const val CUSTOMIZE_CREATE_MODE = 101
        const val CUSTOMIZE_MODIFICATION_MODE = 100

        // Set existing customize name if modification mode
        var oldCustomizeName: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomizeNameSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.to_top_from_bottom_1, R.anim.none)

        activity = this

        mode = intent.getIntExtra("MODE", -1)
        // Modification mode
        if(mode == CUSTOMIZE_MODIFICATION_MODE) {
            oldCustomizeName = intent.getStringExtra("CUSTOMIZE_NAME")
            binding.guidelineTextView.text = getString(R.string.revise_customize_name)
            binding.editText.setText(oldCustomizeName)
            btnEditTextEnabled(true)
        }

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
                addListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        binding.editText.isFocusableInTouchMode = true
                        binding.editText.requestFocus()
                        val inputMethodManager =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.showSoftInput(binding.editText, 0)
                    }
                })
            })
            start()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.btnFinish.setOnClickListener { finish() }

        // edit text changed listener
        binding.editText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                text?.let {
                    if(it.isNotEmpty()) btnEditTextEnabled(true)
                    else btnEditTextEnabled(false)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 다음 단계
        binding.btnNext.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                keyboard.hideSoftInputFromWindow(binding.editText.windowToken, 0)
                val customize: Customize? = customizeRepository.getCustomize(binding.editText.text.toString())

                // Modification mode
                if(mode == CUSTOMIZE_MODIFICATION_MODE) {
                    // 변경하려는 이름이 이미 있을 경우
                    if(customize != null && customize.customizeName != oldCustomizeName) {
                        Toast.makeText(applicationContext, "이미 생성된 커스텀 이름입니다", Toast.LENGTH_SHORT).show()
                    }
                    // 변경하려는 이름이 없거나 변경하지 않는 경우
                    else startWidgetSettingActivity()
                }
                // Create mode
                else if(mode == CUSTOMIZE_CREATE_MODE) {
                    customize?.let {
                        Toast.makeText(applicationContext, "이미 생성된 커스텀 이름입니다", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        startWidgetSettingActivity()
                    }
                }
            }
        }
    }

    private fun btnEditTextEnabled(boolean: Boolean) {
        binding.apply {
            if(boolean) {
                btnNext.isEnabled = boolean
                btnNext.setTextColor(ContextCompat.getColor(this@CustomizeNameSettingActivity, R.color.customBlack))
                btnNext.background = ContextCompat.getDrawable(this@CustomizeNameSettingActivity, R.drawable.button_rounded_corners_purple)
            }
            else {
                btnNext.isEnabled = boolean
                btnNext.setTextColor(ContextCompat.getColor(this@CustomizeNameSettingActivity, R.color.gray))
                btnNext.background = ContextCompat.getDrawable(this@CustomizeNameSettingActivity, R.drawable.button_rounded_corners_gray)
            }
        }
    }

    private fun startWidgetSettingActivity() {
        val customizeName = binding.editText.text.toString()
        tempCustomizeSettingData.setCustomizeName(customizeName)

        val intent = Intent(applicationContext, WidgetSettingActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        tempCustomizeSettingData.instanceClear()
    }
}