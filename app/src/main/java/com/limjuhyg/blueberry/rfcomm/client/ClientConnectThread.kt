package com.limjuhyg.blueberry.rfcomm.client

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import com.limjuhyg.blueberry.applications.MainApplication
import java.io.IOException
import java.util.*

class ClientConnectThread(private val bluetoothDevice: BluetoothDevice, private val messageHandler: Handler) : Thread() {
    private val bluetoothAdapter by lazy { MainApplication.instance.bluetoothAdapter }
    private val rfcommSocket: BluetoothSocket by lazy {
        // SerialPortServiceClass
        bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
    }

    companion object {
        const val CONNECT_SUCCESS: Int = 1000
        const val CONNECT_FAIL: Int = 1001
    }

    override fun run() {
        super.run()

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
        try { rfcommSocket.close() } catch (e: IOException) { Log.e("Raise IOException", "Connect Thread -> Fail socket close") }
    }
}