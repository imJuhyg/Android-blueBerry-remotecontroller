package com.totheptv.blueberry.rfcomm.client

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ClientCommunicationThread(
    private val rfcommSocket: BluetoothSocket,
    private val messageHandler: Handler,
    private val bufferSize: Int
) : Thread() {

    private val inputStream: InputStream = rfcommSocket.inputStream
    private val outputStream: OutputStream = rfcommSocket.outputStream
    private val readBuffer: ByteArray = ByteArray(bufferSize)

    companion object {
        const val CONNECT_CLOSE: Int = 1002
        const val MESSAGE_WRITE: Int = 0
        const val MESSAGE_READ: Int = 1
    }

    // Read message
    override fun run() {
        var messageLength: Int
        while(true) {
            if(isInterrupted) break

            // read
            messageLength = try {
                inputStream.read(readBuffer)

            } catch (e: IOException) { // 연결이 끊기면 발생
                cancel()
                val message = messageHandler.obtainMessage(CONNECT_CLOSE)
                message.sendToTarget()
                break
            }

            // Send message to Main Thread
            val receivedMessage = String(readBuffer.copyOf(messageLength))
            val message = messageHandler.obtainMessage(MESSAGE_READ, receivedMessage)
            message.sendToTarget()
        }
    }

    // Write message
    fun write(bytes: ByteArray) {
        try {
            outputStream.write(bytes)

            // 전송 성공 메시지
            val message = messageHandler.obtainMessage(MESSAGE_WRITE)
            message.sendToTarget()

        } catch (e: IOException) {
            e.printStackTrace()
            cancel()
        }
    }

    fun cancel() {
        try { rfcommSocket.close() } catch (e: IOException) { Log.e("ConnectionThread ", "Raise IOException") }
    }
}