package com.limjuhyg.blueberry.views.custom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity.Companion.oldCustomizeName
import com.limjuhyg.blueberry.views.fragments.PairedDevicesFragment

class CustomizeConnectSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomizeConnectSettingBinding
    private val tempCustomizeSettingData by lazy { TempCustomizeSettingData.getInstance() }
    private val customizeViewModel by lazy { ViewModelProvider(this).get(CustomizeViewModel::class.java) }
    private val pairedDevicesFragment by lazy { PairedDevicesFragment() }
    private var mode: Int? = null
    private var oldDeviceName: String? = null
    private var oldDeviceAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomizeConnectSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = CustomizeNameSettingActivity.mode
        if(mode == CUSTOMIZE_MODIFICATION_MODE) {
            binding.deviceInfo.visibility = View.VISIBLE
            customizeViewModel.getCustomize(oldCustomizeName!!) // 수정 모드에서 저장된 커스터마이즈 정보 불러옴(customizeObserver)
        }

        addFragment(binding.fragmentContainer.id, pairedDevicesFragment, false)

        // Observers
        val customizeObserver = Observer<Customize> {
            oldDeviceName = it.deviceName
            oldDeviceAddress = it.deviceAddress
            binding.deviceInfo.append(" ${oldDeviceAddress ?: "없음"}") // 현재 연결정보 표시
        }
        customizeViewModel.customize.observe(this, customizeObserver)

        val isCreatedObserver = Observer<Boolean> { finishAll() } // 데이터베이스 조작 종료 후 액티비티 모두 종료
        customizeViewModel.isCustomizeCreated.observe(this, isCreatedObserver)

        val isModifiedObserver = Observer<Boolean> { finishAll() }
        customizeViewModel.isCustomizeModified.observe(this, isModifiedObserver)

        // click listener
        binding.btnBefore.setOnClickListener { finish() }

        // (Skip button) Save only customize name, widgets, orientation
        binding.btnSkip.setOnClickListener {
            if(mode == CUSTOMIZE_CREATE_MODE) { // 연결정보 스킵 && 커스터마이즈 생성 모드
                val customize = tempCustomizeSettingData.getCustomize()
                val customizeName = tempCustomizeSettingData.getCustomizeName()
                val widgets = createWidgetList(customizeName)
                customizeViewModel.createCustomize(customize, widgets)
            }

            else if(mode == CUSTOMIZE_MODIFICATION_MODE) {
                val newCustomizeName = tempCustomizeSettingData.getCustomize().customizeName
                val newOrientation = tempCustomizeSettingData.getCustomize().orientation
                val widgets: ArrayList<Widget> = createWidgetList(newCustomizeName)
                customizeViewModel.modifyCustomize(
                    oldCustomizeName!!,
                    newCustomizeName,
                    oldDeviceName,
                    oldDeviceAddress,
                    newOrientation,
                    widgets
                )
            }
        }
    }

    // Called from PairedDevicesFragment
    fun setBluetoothDevice(deviceName: String, deviceAddress: String) {
        // Get customize name from temp data class
        val customizeName = tempCustomizeSettingData.getCustomize().customizeName
        val orientation = tempCustomizeSettingData.getCustomize().orientation
        val customize = Customize(customizeName, deviceName, deviceAddress, orientation)
        val widgets = createWidgetList(customizeName)

        if(mode == CUSTOMIZE_CREATE_MODE) {
            customizeViewModel.createCustomize(customize, widgets)
        }
        else if(mode == CUSTOMIZE_MODIFICATION_MODE) {
            customizeViewModel.modifyCustomize(
                oldCustomizeName!!,
                customizeName,
                deviceName,
                deviceAddress,
                orientation,
                widgets
            )
        }
    }

    private fun createWidgetList(customizeName: String): ArrayList<Widget> {
        // Get widget settings from temp data class
        val userCreatedWidgets = tempCustomizeSettingData.getWidgetList()
        val widgetList = ArrayList<Widget>()
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
            widgetList.add(widget)
        }
        return widgetList
    }

    private fun finishAll() {
        WidgetSettingActivity.activity.finish()
        CustomizeNameSettingActivity.activity.finish()
        finish()
    }
}