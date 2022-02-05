package com.limjuhyg.blueberry.viewmodels

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.limjuhyg.blueberry.applications.MainApplication
import java.util.jar.Manifest

class BluetoothScanPair(application: Application) : AndroidViewModel(application) {
    val pairedDevices by lazy { MutableLiveData<ArrayList<BluetoothDevice>>() } // paired devices
    val foundDevice by lazy { MutableLiveData<BluetoothDevice>() } // found device
    val isDiscovering by lazy { MutableLiveData<Boolean>() } // scanning
    val isBonded by lazy { MutableLiveData<Boolean>() } // pair success
    private val bluetoothAdapter by lazy { getApplication<MainApplication>().bluetoothAdapter }
    private lateinit var bluetoothDevice: BluetoothDevice
    private val bondIntentFilter by lazy { IntentFilter() }
    private var isBondReceiverRegistered: Boolean = false
    private val scanIntentFilter by lazy { IntentFilter() }
    private var isScanReceiverRegistered: Boolean = false

    fun getPairedDevices() {
        pairedDevices.value = ArrayList(bluetoothAdapter!!.bondedDevices)
    }

    fun startScan() {
        if(bluetoothAdapter!!.isDiscovering) bluetoothAdapter!!.cancelDiscovery()

        scanIntentFilter.apply {
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }

        getApplication<Application>().registerReceiver(scanReceiver, scanIntentFilter)
        isScanReceiverRegistered = true
        bluetoothAdapter!!.startDiscovery()
    }

    fun stopScan() {
        if(bluetoothAdapter!!.isDiscovering) bluetoothAdapter!!.cancelDiscovery()
    }

    fun requestPair(bluetoothDevice: BluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice
        bondIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        getApplication<Application>().registerReceiver(bondReceiver, bondIntentFilter)
        isBondReceiverRegistered = true

        bluetoothDevice.createBond()
    }

    private val scanReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    isDiscovering.value = true
                }

                BluetoothDevice.ACTION_FOUND -> {
                    val bluetoothDevice: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    bluetoothDevice?.let { foundDevice.value = it }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    isDiscovering.value = false
                }
            }
        }
    }

    private val bondReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    when(bluetoothDevice.bondState) {
                        BluetoothDevice.BOND_NONE -> {
                            // 페어링 요청이 수락되지 않은 경우
                            isBonded.value = false
                            onCleared()
                        }

                        BluetoothDevice.BOND_BONDED -> {
                            isBonded.value = true
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        stopScan()
        if(isBondReceiverRegistered) getApplication<Application>().unregisterReceiver(bondReceiver)
        if(isScanReceiverRegistered) getApplication<Application>().unregisterReceiver(scanReceiver)
        isBondReceiverRegistered = false
        isScanReceiverRegistered = false
    }
}