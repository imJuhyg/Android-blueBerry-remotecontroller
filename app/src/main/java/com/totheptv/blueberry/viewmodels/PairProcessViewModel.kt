package com.totheptv.blueberry.viewmodels

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.totheptv.blueberry.MainApplication

class PairProcessViewModel(application: Application) : AndroidViewModel(application) {
    val isBonded : MutableLiveData<Boolean> by lazy { MutableLiveData() }
    private lateinit var bluetoothDevice: BluetoothDevice
    private val bondIntentFilter by lazy { IntentFilter() }
    private var isReceiverRegistered: Boolean = false

    fun requestPair(bluetoothDevice: BluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice
        bondIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        getApplication<Application>().registerReceiver(bondReceiver, bondIntentFilter)
        isReceiverRegistered = true

        bluetoothDevice.createBond()
    }

    private val bondReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    when(bluetoothDevice.bondState) {
                        BluetoothDevice.BOND_NONE -> {
                            // 페어링 요청이 수락되지 않은 경우
                            Log.d("debug", "bond none")
                            isBonded.value = false
                            onCleared()
                        }

                        BluetoothDevice.BOND_BONDED -> {
                            Log.d("debug", "bond bonded")
                            isBonded.value = true
                        }

                        BluetoothDevice.ERROR -> {
                            Log.d("debug", "error")
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        if(isReceiverRegistered) getApplication<Application>().unregisterReceiver(bondReceiver)
        isReceiverRegistered = false
    }
}