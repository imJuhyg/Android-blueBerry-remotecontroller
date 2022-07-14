package com.limjuhyg.blueberry.viewmodels

/**
 * 블루투스 스캔 및 페어링 요청 뷰모델
 * 기능
 * 1. getPairedDevices(): 페어링된 디바이스 찾기
 * 2. startScan(): 주변 기기 스캔
 * 3. stopScan(): 주변 기기 스캔 중지
 * 4. requestPair(): 탐색된 기기에 페어링 요청
 */

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.limjuhyg.blueberry.applications.MainApplication

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
        // pairedDevices Live Data 로 전달
        pairedDevices.value = ArrayList(bluetoothAdapter!!.bondedDevices)
    }

    fun startScan() {
        if(bluetoothAdapter!!.isDiscovering) bluetoothAdapter!!.cancelDiscovery()

        scanIntentFilter.apply { // 스캔 인텐트 필터 설정
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) // 주변 기기 검색 시작 알림 받기
            addAction(BluetoothDevice.ACTION_FOUND) // 주변 기기 찾았을 대 알림 받기
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) // 검색이 끝나면 알림 받기
        }

        // 리시버와 인텐트 필터 등록
        getApplication<Application>().registerReceiver(scanReceiver, scanIntentFilter)
        isScanReceiverRegistered = true //  리시버 해지 시 오류 방지
        bluetoothAdapter!!.startDiscovery() // 주변 기기 검색 시작(scanReceiver 로 알림 받음)
    }

    fun stopScan() {
        if(bluetoothAdapter!!.isDiscovering) bluetoothAdapter!!.cancelDiscovery()
    }

    // DeviceScanActivity 에서 사용
    fun requestPair(bluetoothDevice: BluetoothDevice) { // 블루투스 디바이스 외부에서 넘겨 받기
        this.bluetoothDevice = bluetoothDevice
        /**
         * BluetoothDevice.ACTION_BOND_STATE_CHANGED
         * 페어링 요청이 수락되지 않은 경우와 수락된 경우를 알림받을 수 있다.
         */
        bondIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        getApplication<Application>().registerReceiver(bondReceiver, bondIntentFilter) // 리시버 등록
        isBondReceiverRegistered = true // 리시버 해지 시 오류 방지

        bluetoothDevice.createBond() // 페어링 수행
    }

    private val scanReceiver = object: BroadcastReceiver() { // 스캔 작업
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> { // 검색을 시작하면
                    isDiscovering.value = true // Live Data 변경
                }

                BluetoothDevice.ACTION_FOUND -> { // 주변 기기를 찾았으면
                    // 블루투스 디바이스 객체를 생성하고
                    val bluetoothDevice: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    bluetoothDevice?.let { foundDevice.value = it } // 블루투스 디바이스가 null 이 아닌 경우
                    // foundDevice Live Data 에 블루투스 디바이스 등록
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> { // 탐색이 종료되면
                    isDiscovering.value = false // Live Data 변경
                }
            }
        }
    }

    private val bondReceiver = object: BroadcastReceiver() { // 페어링 요청 작업
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    when(bluetoothDevice.bondState) {
                        BluetoothDevice.BOND_NONE -> {
                            // 페어링 요청이 수락되지 않은 경우
                            isBonded.value = false
                            onCleared() // 뷰 모델 종료
                        }

                        BluetoothDevice.BOND_BONDED -> { // 페어링 요청이 수락된 경우
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