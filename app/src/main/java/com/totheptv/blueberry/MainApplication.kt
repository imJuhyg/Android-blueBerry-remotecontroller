package com.totheptv.blueberry

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log

class MainApplication : Application() {
    var bluetoothAdapter: BluetoothAdapter? = null

    companion object {
        lateinit var instance: MainApplication
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
}