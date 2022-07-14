package com.limjuhyg.blueberry.communication.rfcomm

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import com.limjuhyg.blueberry.applications.MainApplication
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_FAIL
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_SUCCESS
import java.io.IOException
import java.util.*

/**
 * BluetoothChatActivity, CustomizeCommunicationActivity 에서 사용
 *
 * 연결 - ConnectThread 를 통해 수행
 * RFCOMM 클라이언트 소켓 생성 후, 서버 소켓으로 연결 요청
 * 연결 요청이 수락되면 메인 스레드로 SUCCESS 메시지와 RFCOMM socket 을 보낸다.
 * 실패 시 FAIL 메시지를 보내고, UI 업데이트를 통해 연결 실패를 알린다.
 *
 * 성공 시 RFCOMM socket 을 메인 스레드로 보내면, 메인 스레드는 다음 과정으로 CommunicationThread 호출
 */

class ConnectThread(private val bluetoothDevice: BluetoothDevice, private val messageHandler: Handler) : Thread() {
    private val bluetoothAdapter by lazy { MainApplication.instance.bluetoothAdapter }
    private val rfcommSocket: BluetoothSocket by lazy {
        // SerialPortServiceClass
        bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
        /** 시리얼 포트 서비스 클래스 UUID 로 클라이언트 소켓을 생성한다. **/
    }

    override fun run() {
        bluetoothAdapter!!.cancelDiscovery() // 연결을 시작하기 전에 주변 기기 탐색을 멈춘다.

        try {
            rfcommSocket.connect() // 연결 요청
            // 성공시 아래 코드 실행
            if(!isInterrupted)  { // 외부에서 인터럽트 요청을 보내지 않았으면
                // UI 스레드로 성공 메시지 & 소켓 전송
                val message = messageHandler.obtainMessage(CONNECT_SUCCESS, rfcommSocket)
                message.sendToTarget()
            }
        } catch (e: IOException) { // 연결 실패 시
            cancel() // 소켓 close
            if(!isInterrupted) { // 외부에서 인터럽트 요청을 보내지 않았으면
                val message = messageHandler.obtainMessage(CONNECT_FAIL) // 실패 메시지 전송
                message.sendToTarget()
            }
        }
    }

    fun cancel() {
        try { rfcommSocket.close() } catch (e: IOException) { }
    }
}