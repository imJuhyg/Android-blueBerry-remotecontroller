package com.limjuhyg.blueberry.communication.rfcomm

import android.bluetooth.BluetoothSocket
import android.os.Handler
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_CLOSE
import com.limjuhyg.blueberry.applications.MainApplication.Companion.MESSAGE_READ
import com.limjuhyg.blueberry.applications.MainApplication.Companion.MESSAGE_WRITE
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * I/O 스트림과 버퍼를 생성하고 통신 수행
 * InputStream: 상대방으로부터 온 버퍼에 담긴 메시지를 String 객체로 가공하여 UI 스레드로 전송
 * 버퍼는 Byte 단위의 Array 이기때문에 String 객체로 바꿈
 *
 * OutputStream: 문자열을 버퍼에 담아(ByteArray 로 변경하여) 상대편으로 전송한다.
 */

class CommunicationThread(
    private val rfcommSocket: BluetoothSocket,
    private val messageHandler: Handler,
    private val bufferSize: Int
) : Thread() {

    private val inputStream: InputStream = rfcommSocket.inputStream
    private val outputStream: OutputStream = rfcommSocket.outputStream
    private val readBuffer: ByteArray = ByteArray(bufferSize) // -> 버퍼: ByteArray

    // Read message
    override fun run() {
        var messageLength: Int
        while(true) {
            if(isInterrupted) break // 외부에서 중단 요청이 오면 읽기 정지

            // read
            messageLength = try {
                inputStream.read(readBuffer) // inputStream

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
            val message = messageHandler.obtainMessage(MESSAGE_WRITE, String(bytes, Charsets.UTF_8))
            message.sendToTarget()

        } catch (e: IOException) {
            cancel()
        }
    }

    fun cancel() {
        try { rfcommSocket.close() } catch (e: IOException) { }
    }
}