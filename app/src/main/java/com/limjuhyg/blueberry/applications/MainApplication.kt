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
    var deviceDpi: String? = null

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

        // Set device dpi
        deviceDpi = when(resources.displayMetrics.densityDpi) {
            in 0 .. 160 -> "mdpi"
            in 161 .. 240 -> "hdpi"
            in 241 .. 320 -> "xhdpi"
            in 321 .. 480 -> "xxhdpi"
            else -> "xxxhdpi"
        }
    }

    fun getStatusBarHeight(): Int {
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if(resourceId > 0)
            statusBarHeight = resources.getDimensionPixelSize(resourceId)

        return statusBarHeight
    }

    fun getBottomNavigationBarHeight(): Int {
        var height = 0
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if(resourceId > 0)
            height = resources.getDimensionPixelSize(resourceId)

        return height
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