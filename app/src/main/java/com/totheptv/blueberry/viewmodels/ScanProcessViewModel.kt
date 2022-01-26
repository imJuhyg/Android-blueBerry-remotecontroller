package com.totheptv.blueberry.viewmodels

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.totheptv.blueberry.MainApplication

class ScanProcessViewModel(application: Application) : AndroidViewModel(application) {
    val mutableBluetoothDevice: MutableLiveData<BluetoothDevice> = MutableLiveData()
    var isDiscovering: MutableLiveData<Boolean> = MutableLiveData()
    private val bluetoothAdapter by lazy { MainApplication.instance.bluetoothAdapter }
    private val filter by lazy { IntentFilter() }
    private var isReceiverRegistered: Boolean = false

    fun startScan() {
        if(bluetoothAdapter!!.isDiscovering) bluetoothAdapter!!.cancelDiscovery()

        filter.apply {
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }

        getApplication<Application>().registerReceiver(receiver, filter)
        isReceiverRegistered = true
        bluetoothAdapter!!.startDiscovery()
    }

    fun stopScan() {
        if(bluetoothAdapter!!.isDiscovering) bluetoothAdapter!!.cancelDiscovery()
    }

    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    isDiscovering.value = true
                }

                BluetoothDevice.ACTION_FOUND -> {
                    Log.d("debug", "found")
                    val bluetoothDevice: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    bluetoothDevice?.run { mutableBluetoothDevice.value = this }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("debug", "finished")
                    isDiscovering.value = false
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        stopScan()
        if(isReceiverRegistered) getApplication<Application>().unregisterReceiver(receiver)
        isReceiverRegistered = false
    }
}