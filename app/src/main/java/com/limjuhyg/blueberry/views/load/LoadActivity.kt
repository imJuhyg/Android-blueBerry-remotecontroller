package com.limjuhyg.blueberry.views.load

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
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

        // 버전 코드 표시
        val versionCodeTextView: TextView = findViewById(R.id.version_code)
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            versionCodeTextView.text = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {

        }

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
            handler.postDelayed(launchActivity, 1500)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(launchActivity)
    }
}