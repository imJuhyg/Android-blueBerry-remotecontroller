package com.limjuhyg.blueberry.views.load

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.limjuhyg.blueberry.views.main.MainActivity
import com.limjuhyg.blueberry.applications.MainApplication
import com.limjuhyg.blueberry.R
import kotlin.system.exitProcess

class LoadActivity : AppCompatActivity() {
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)

        // Check bluetooth adapter
        MainApplication.instance.bluetoothAdapter ?: run {
            AlertDialog.Builder(this).apply {
                setMessage(R.string.bluetooth_adapter_isnull)
                setPositiveButton("확인") { _, _ ->
                    ActivityCompat.finishAffinity(this@LoadActivity)
                    exitProcess(0)
                }
                setCancelable(false)
                show()
            }
        }
    }

    private val launchActivity = Runnable {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()

        MainApplication.instance.bluetoothAdapter?.let {
            handler.postDelayed(launchActivity, 2000)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(launchActivity)
    }
}