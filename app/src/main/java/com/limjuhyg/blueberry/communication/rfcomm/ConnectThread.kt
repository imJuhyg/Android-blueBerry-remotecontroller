package com.limjuhyg.blueberry.communication.rfcomm

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import com.limjuhyg.blueberry.applications.MainApplication
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_FAIL
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_SUCCESS
import java.io.IOException
import java.util.*

class ConnectThread(private val bluetoothDevice: BluetoothDevice, private val messageHandler: Handler) : Thread() {
    private val bluetoothAdapter by lazy { MainApplication.instance.bluetoothAdapter }
    private val rfcommSocket: BluetoothSocket by lazy {
        // SerialPortServiceClass
        bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
    }

    override fun run() {
        bluetoothAdapter!!.cancelDiscovery()

        try {
            rfcommSocket.connect()
            if(!isInterrupted)  {
                val message = messageHandler.obtainMessage(CONNECT_SUCCESS, rfcommSocket)
                message.sendToTarget()
            }
        } catch (e: IOException) {
            cancel()
            if(!isInterrupted) {
                val message = messageHandler.obtainMessage(CONNECT_FAIL)
                message.sendToTarget()
            }
        }
    }

    fun cancel() {
        try { rfcommSocket.close() } catch (e: IOException) { }
    }
}