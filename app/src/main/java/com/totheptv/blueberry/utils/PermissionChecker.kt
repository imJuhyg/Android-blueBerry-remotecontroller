package com.totheptv.blueberry.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.ArrayList

// 권한 요청이 필요하면 사용자에게 권한 요청 후 true 를 반환
// 필요하지 않으면(이미 권한이 추가된 경우) false 를 반환
// TODO 스캔 작업시 Location 권한 요청
fun AppCompatActivity.requestPermission(requestCode: Int): Boolean {
    val permissions: Array<String> = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN)
    /*
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

    val requirePermissionList = ArrayList<String>()

    // 필요한 권한 확인
    for(permission in permissions) {
        if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
            requirePermissionList.add(permission) // require permission
    }

    // 권한 요청
    return if(requirePermissionList.size > 0) {
        val permissionArray = requirePermissionList.toArray(arrayOfNulls<String>(requirePermissionList.size))
        ActivityCompat.requestPermissions(this, permissionArray, requestCode)
        true
    }
    else false
}