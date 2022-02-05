package com.limjuhyg.blueberry.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.ArrayList


@RequiresApi(31)
fun AppCompatActivity.requestPermission(requestCode: Int): Boolean {
    val permissions: Array<String> = arrayOf (
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
        )

    val requirePermissions = ArrayList<String>()

    /*
    val permissions: Array<String> = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN)

    val permissions: Array<String> = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // api level 29
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION)
    }
    else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION) // under api level 28
    }

     */

    // 필요한 권한 확인
    for(permission in permissions) {
        if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
            requirePermissions.add(permission) // require permission
    }

    // 권한 요청
    return if(requirePermissions.size > 0) {
        val permissionArray = requirePermissions.toArray(arrayOfNulls<String>(requirePermissions.size))
        ActivityCompat.requestPermissions(this, permissionArray, requestCode)
        false
    }
    else true
}