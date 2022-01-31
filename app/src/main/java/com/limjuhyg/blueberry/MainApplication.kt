package com.limjuhyg.blueberry

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.view.WindowMetrics

class MainApplication : Application() {
    private val windowManager by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    var bluetoothAdapter: BluetoothAdapter? = null

    companion object {
        lateinit var instance: MainApplication
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        // Set bluetooth adapter
        bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    fun getWindowHeight(): Int {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.d("api 30", "ebu")
            windowManager.currentWindowMetrics.bounds.height()
        }
        else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }
}