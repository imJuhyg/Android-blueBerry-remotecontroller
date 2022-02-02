package com.limjuhyg.blueberry.views.custom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.limjuhyg.blueberry.databinding.ActivityCustomizeConnectSettingBinding
import com.limjuhyg.blueberry.dataclass.TempCustomizeSettingData
import com.limjuhyg.blueberry.models.room.entities.Widget
import com.limjuhyg.blueberry.viewmodels.CustomizeViewModel
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity.Companion.CUSTOMIZE_CREATE_MODE
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity.Companion.CUSTOMIZE_MODIFICATION_MODE
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity.Companion.existCustomizeName

class CustomizeConnectSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomizeConnectSettingBinding
    private val tempCustomizeSettingData by lazy { TempCustomizeSettingData.getInstance() }
    private val customizeViewModel by lazy { ViewModelProvider(this).get(CustomizeViewModel::class.java) }
    private var mode: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomizeConnectSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = CustomizeNameSettingActivity.mode
    }

    override fun onResume() {
        super.onResume()

        binding.btnBefore.setOnClickListener {
            finish()
        }

        // Save only customize name, widget settings
        binding.btnSkip.setOnClickListener {
            if(mode == CUSTOMIZE_CREATE_MODE) {
                customizeViewModel.insertCustomize(tempCustomizeSettingData.getCustomize()) // Insert Customize in DB
                val customizeName = tempCustomizeSettingData.getCustomize().customizeName // Get customize name from temp data class
                val userCreatedWidgets = tempCustomizeSettingData.getWidgetList()   // Get widget settings from temp data class

                // Insert widgets
                for(userCreatedWidget in userCreatedWidgets) {
                    val widget = Widget(
                        customizeName,
                        userCreatedWidget.x,
                        userCreatedWidget.y,
                        userCreatedWidget.width,
                        userCreatedWidget.height,
                        userCreatedWidget.getWidgetImageBitmap(),
                        userCreatedWidget.getWidgetCaption(),
                        userCreatedWidget.getWidgetData()
                    )
                    customizeViewModel.insertWidget(widget)
                }
            }

            else if(mode == CUSTOMIZE_MODIFICATION_MODE) {
                val newCustomizeName = tempCustomizeSettingData.getCustomize().customizeName
                customizeViewModel.updateCustomizeName(existCustomizeName!!, newCustomizeName) // Update customize name
                // TODO Foreign key 로 지정된 속성을 바꾸면 자동으로 사라지나?
                // TODO FOREIGN KEY constraint failed (code 787 SQLITE_CONSTRAINT_FOREIGNKEY)
                // TODO 위젯을 업데이트 하는 방식으로 바꿔보기
                val userCreatedWidgets = tempCustomizeSettingData.getWidgetList()   // Get widget settings from temp data class

                // Update widgets
                customizeViewModel.deleteWidget(existCustomizeName!!) // Delete exist widgets
                for(userCreatedWidget in userCreatedWidgets) {
                    val widget = Widget(
                        newCustomizeName,
                        userCreatedWidget.x,
                        userCreatedWidget.y,
                        userCreatedWidget.width,
                        userCreatedWidget.height,
                        userCreatedWidget.getWidgetImageBitmap(),
                        userCreatedWidget.getWidgetCaption(),
                        userCreatedWidget.getWidgetData()
                    )
                    customizeViewModel.insertWidget(widget) // Insert updated widgets
                }
            }

            // customize create process finish
            WidgetSettingActivity.activity.finish()
            CustomizeNameSettingActivity.activity.finish()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("CustomizeConnectSetAct", "onDestroy")
    }
}