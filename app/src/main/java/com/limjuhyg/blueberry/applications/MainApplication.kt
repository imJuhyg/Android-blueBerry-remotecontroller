package com.limjuhyg.blueberry.applications

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

class MainApplication : Application() {
    private val windowManager by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    var bluetoothAdapter: BluetoothAdapter? = null

    companion object {
        lateinit var instance: MainApplication

        // Communication code
        const val BUFFER_SIZE: Int = 1024
        const val CONNECT_SUCCESS: Int = 1000
        const val CONNECT_FAIL: Int = 1001
        const val CONNECT_CLOSE: Int = 1002
        const val MESSAGE_WRITE: Int = 0
        const val MESSAGE_READ: Int = 1
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
            windowManager.currentWindowMetrics.bounds.height()
        }
        else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }

    fun getWindowWidth(): Int {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.width()
        }
        else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }
}