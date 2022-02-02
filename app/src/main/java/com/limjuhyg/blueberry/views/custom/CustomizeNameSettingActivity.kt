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

        // Set customize name to load widget settings in case of modification mode
        var existCustomizeName: String? = null
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
            existCustomizeName = intent.getStringExtra("CUSTOMIZE_NAME")
            binding.guidelineTextView.text = getString(R.string.revise_customize_name)
            binding.editText.setText(existCustomizeName)
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
            })
            start()
        }
    }

    override fun onResume() {
        super.onResume()

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

        // Next stage
        binding.btnNext.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                keyboard.hideSoftInputFromWindow(binding.editText.windowToken, 0)
                // Modification mode
                if(mode == CUSTOMIZE_MODIFICATION_MODE) {
                    startWidgetSettingActivity()
                }
                // Create mode
                else if(mode == CUSTOMIZE_CREATE_MODE) {
                    val customize: Customize? = customizeRepository.getCustomize(binding.editText.text.toString())

                    customize?.let {
                        Toast.makeText(this@CustomizeNameSettingActivity, "이미 생성된 커스텀 이름입니다", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        startWidgetSettingActivity()
                        binding.editText.setText("")
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

        val intent = Intent(this@CustomizeNameSettingActivity, WidgetSettingActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("CustomizeSettingAct", "onDestroy")
        tempCustomizeSettingData.instanceClear()
    }
}