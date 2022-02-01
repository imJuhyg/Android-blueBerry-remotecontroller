package com.limjuhyg.blueberry.dataclass

import android.graphics.Point
import com.limjuhyg.blueberry.customviews.CustomWidget
import com.limjuhyg.blueberry.models.room.entities.Customize
import com.limjuhyg.blueberry.models.room.entities.Widgets

class TempCustomizeSettingData private constructor() {
    private var customizeName: String? = null
    private var deviceName: String? = null
    private var deviceAddress: String? = null
    private var widgetList: ArrayList<CustomWidget>? = null

    companion object {
        private var instance: TempCustomizeSettingData? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: TempCustomizeSettingData().also {
                    instance = it
                }
            }
    }

    fun setCustomizeName(customizeName: String) {
        this.customizeName = customizeName
    }

    fun setDeviceInfo(deviceName: String, deviceAddress: String) {
        this.deviceName = deviceName
        this.deviceAddress = deviceAddress
    }

    fun setWidgetList(widgetList: ArrayList<CustomWidget>) {
        this.widgetList = widgetList
    }

    fun getCustomize() = Customize(customizeName ?: "null", deviceName, deviceAddress)
    // TODO getWidgetList() : ArrayList<Widgets> -> Entity Widgets 으로 가공하여 리턴
    // TODO Type Converter -> bitmap

    fun instanceClear() {
        instance = null
    }
}