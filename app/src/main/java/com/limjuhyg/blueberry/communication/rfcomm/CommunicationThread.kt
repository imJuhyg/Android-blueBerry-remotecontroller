package com.limjuhyg.blueberry.communication.rfcomm

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_CLOSE
import com.limjuhyg.blueberry.applications.MainApplication.Companion.MESSAGE_READ
import com.limjuhyg.blueberry.applications.MainApplication.Companion.MESSAGE_WRITE
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class CommunicationThread(
    private val rfcommSocket: BluetoothSocket,
    private val messageHandler: Handler,
    private val bufferSize: Int
) : Thread() {

    private val inputStream: InputStream = rfcommSocket.inputStream
    private val outputStream: OutputStream = rfcommSocket.outputStream
    private val readBuffer: ByteArray = ByteArray(bufferSize)

    // Read message
    override fun run() {
        var messageLength: Int
        while(true) {
            if(isInterrupted) break

            // read
            messageLength = try {
                inputStream.read(readBuffer)

            } catch (e: IOException) { // 연결이 끊기면 발생
                Log.d("read error", e.stackTraceToString())
                cancel()
                val message = messageHandler.obtainMessage(CONNECT_CLOSE)
                message.sendToTarget()
                break
            }

            // Send message to Main Thread
            val receivedMessage = String(readBuffer.copyOf(messageLength))
            Log.d("receive", receivedMessage)
            val message = messageHandler.obtainMessage(MESSAGE_READ, receivedMessage)
            message.sendToTarget()
        }
    }

    // Write message
    fun write(bytes: ByteArray) {
        try {
            Log.d("debug", String(bytes, Charsets.UTF_8))
            outputStream.write(bytes)

            // 전송 성공 메시지
            val message = messageHandler.obtainMessage(MESSAGE_WRITE, String(bytes, Charsets.UTF_8))
            message.sendToTarget()

        } catch (e: IOException) {
            Log.d("write error", e.stackTraceToString())
            cancel()
        }
    }

    fun cancel() {
        try { rfcommSocket.close() } catch (e: IOException) { Log.e("ConnectionThread ", "Raise IOException") }
    }
}