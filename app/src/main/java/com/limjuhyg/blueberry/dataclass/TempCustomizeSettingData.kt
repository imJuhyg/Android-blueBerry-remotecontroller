package com.limjuhyg.blueberry.dataclass

import com.limjuhyg.blueberry.customviews.CustomWidget
import com.limjuhyg.blueberry.models.room.entities.Customize

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

    fun getWidgetList(): ArrayList<CustomWidget> = widgetList ?: ArrayList()

    fun getCustomize() = Customize(customizeName!!, deviceName, deviceAddress)

    fun instanceClear() {
        instance = null
    }
}