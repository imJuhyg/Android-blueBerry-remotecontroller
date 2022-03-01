package com.limjuhyg.blueberry.utils

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/* 순간적인 네트워크 상태 가져오기 */
fun AppCompatActivity.getNetworkState(): Boolean {
    val connectivityManager = ContextCompat.getSystemService(
        this,
        ConnectivityManager::class.java
    ) as ConnectivityManager

    if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected ?: false

    } else {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        if(activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return true
        else if(activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return true
    }
    return false
}