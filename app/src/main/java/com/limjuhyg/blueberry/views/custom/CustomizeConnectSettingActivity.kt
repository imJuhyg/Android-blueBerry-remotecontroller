package com.limjuhyg.blueberry.views.custom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.limjuhyg.blueberry.databinding.ActivityCustomizeConnectSettingBinding
import com.limjuhyg.blueberry.dataclass.TempCustomizeSettingData
import com.limjuhyg.blueberry.models.room.entities.Customize
import com.limjuhyg.blueberry.models.room.entities.Widget
import com.limjuhyg.blueberry.utils.addFragment
import com.limjuhyg.blueberry.viewmodels.CustomizeViewModel
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity.Companion.CUSTOMIZE_CREATE_MODE
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity.Companion.CUSTOMIZE_MODIFICATION_MODE
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity.Companion.existCustomizeName
import com.limjuhyg.blueberry.views.fragments.PairedDevicesFragment

class CustomizeConnectSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomizeConnectSettingBinding
    private val tempCustomizeSettingData by lazy { TempCustomizeSettingData.getInstance() }
    private val customizeViewModel by lazy { ViewModelProvider(this).get(CustomizeViewModel::class.java) }
    private val pairedDevicesFragment by lazy { PairedDevicesFragment() }
    private var mode: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomizeConnectSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = CustomizeNameSettingActivity.mode

        addFragment(binding.fragmentContainer.id, pairedDevicesFragment, false)
    }

    override fun onResume() {
        super.onResume()

        binding.btnBefore.setOnClickListener {
            finish()
        }

        // (Skip button) Save only customize name, widgets
        binding.btnSkip.setOnClickListener {
            if(mode == CUSTOMIZE_CREATE_MODE) {
                customizeViewModel.insertCustomize(tempCustomizeSettingData.getCustomize()) // Insert Customize in DB
                val customizeName = tempCustomizeSettingData.getCustomize().customizeName // Get customize name from temp data class

                insertWidgetToDatabase(customizeName)
            }

            else if(mode == CUSTOMIZE_MODIFICATION_MODE) {
                val newCustomizeName = tempCustomizeSettingData.getCustomize().customizeName
                customizeViewModel.deleteWidget(existCustomizeName!!) // Delete exist widgets
                customizeViewModel.updateCustomizeName(existCustomizeName!!, newCustomizeName) // Update customize name

                insertWidgetToDatabase(newCustomizeName)
            }

            // Finish customize create process
            WidgetSettingActivity.activity.finish()
            CustomizeNameSettingActivity.activity.finish()
            finish()
        }
    }

    private fun insertWidgetToDatabase(customizeName: String) {
        // Get widget settings from temp data class
        val userCreatedWidgets = tempCustomizeSettingData.getWidgetList()

        // Insert widgets
        for(userCreatedWidget in userCreatedWidgets) {
            val widget = Widget(
                customizeName,
                userCreatedWidget.x,
                userCreatedWidget.y,
                userCreatedWidget.width,
                userCreatedWidget.height,
                userCreatedWidget.getWidgetScale(),
                userCreatedWidget.getWidgetImageBitmap(),
                userCreatedWidget.getWidgetCaption(),
                userCreatedWidget.getWidgetData()
            )
            customizeViewModel.insertWidget(widget)
        }
    }

    fun setBluetoothDevice(deviceName: String, deviceAddress: String) {
        if(mode == CUSTOMIZE_CREATE_MODE) {
            tempCustomizeSettingData.setDeviceInfo(deviceName, deviceAddress) // Set device information
            customizeViewModel.insertCustomize(tempCustomizeSettingData.getCustomize()) // Insert Customize in DB
            val customizeName = tempCustomizeSettingData.getCustomize().customizeName // Get customize name from temp data class

            insertWidgetToDatabase(customizeName)
        }
        else if(mode == CUSTOMIZE_MODIFICATION_MODE) {
            val newCustomizeName = tempCustomizeSettingData.getCustomize().customizeName
            customizeViewModel.deleteWidget(existCustomizeName!!) // Delete exist widgets
            customizeViewModel.updateCustomizeName(existCustomizeName!!, newCustomizeName) // Update customize name
            customizeViewModel.updateCustomizeDevice(newCustomizeName, deviceName, deviceAddress) // Update customize device

            insertWidgetToDatabase(newCustomizeName)
        }

        // Finish customize create process
        WidgetSettingActivity.activity.finish()
        CustomizeNameSettingActivity.activity.finish()
        finish()
    }
}