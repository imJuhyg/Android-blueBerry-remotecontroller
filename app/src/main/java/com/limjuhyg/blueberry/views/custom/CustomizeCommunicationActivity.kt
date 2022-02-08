package com.limjuhyg.blueberry.views.custom

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.applications.MainApplication
import com.limjuhyg.blueberry.applications.MainApplication.Companion.BUFFER_SIZE
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_CLOSE
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_FAIL
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_SUCCESS
import com.limjuhyg.blueberry.applications.MainApplication.Companion.MESSAGE_READ
import com.limjuhyg.blueberry.applications.MainApplication.Companion.MESSAGE_WRITE
import com.limjuhyg.blueberry.customviews.CustomWidget
import com.limjuhyg.blueberry.databinding.ActivityCustomizeCommunicationBinding
import com.limjuhyg.blueberry.models.room.entities.Widget
import com.limjuhyg.blueberry.rfcomm.client.ClientCommunicationThread
import com.limjuhyg.blueberry.rfcomm.client.ClientConnectThread
import com.limjuhyg.blueberry.utils.ProgressCircleAnimator
import com.limjuhyg.blueberry.viewmodels.CustomizeViewModel

class CustomizeCommunicationActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityCustomizeCommunicationBinding
    private lateinit var customizeName: String
    private var deviceAddress: String? = null
    private val bluetoothAdapter by lazy { MainApplication.instance.bluetoothAdapter }
    private lateinit var bluetoothDevice: BluetoothDevice
    private var isBonded = false
    private val customizeViewModel by lazy { ViewModelProvider(this).get(CustomizeViewModel::class.java) }
    private lateinit var connectMessageHandler: Handler
    private lateinit var communicationMessageHandler: Handler
    private var connectThread: ClientConnectThread? = null
    private var communicationThread: ClientCommunicationThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomizeCommunicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customizeName = intent.getStringExtra("CUSTOMIZE_NAME")!!
        deviceAddress = intent.getStringExtra("DEVICE_ADDRESS")

        val progressAnimator = ProgressCircleAnimator(
            binding.progressCircle1,
            binding.progressCircle2,
            binding.progressCircle3, 500)
        progressAnimator.startAnimation()

        Handler(Looper.getMainLooper()).postDelayed( // 연결상태 확인 메시지 2초간 표시
            {
                deviceAddress?.let { // 연결정보가 있는 경우
                    for(device in bluetoothAdapter!!.bondedDevices) {
                        if(it == device.address) {
                            isBonded = true
                            bluetoothDevice = device
                        }
                    }
                    if(!isBonded) { // 연결정보는 있지만 현재는 페어링되어있지 않은 경우
                        progressAnimator.cancelAnimation()
                        binding.checkConnectSettingGroup.visibility = View.GONE
                        binding.checkFailGroup.visibility = View.VISIBLE
                        binding.warningMessage1.text = getString(R.string.not_found_bonded_device)
                        binding.warningMessage2.text = getString(R.string.not_found_bonded_device_explain)
                    }

                    if(isBonded) { // 디바이스 연결 요청
                        binding.textMessage.text = getString(R.string.try_connect)
                        connectThread = ClientConnectThread(bluetoothDevice, connectMessageHandler)
                        connectThread!!.start()
                    }

                } ?: run { // 연결정보가 없는 경우
                    progressAnimator.cancelAnimation()
                    binding.checkConnectSettingGroup.visibility = View.GONE
                    binding.checkFailGroup.visibility = View.VISIBLE
                    binding.warningMessage1.text = getString(R.string.fail_check_connect_setting)
                    binding.warningMessage2.text = getString(R.string.fail_check_connect_setting_explain)
                }
            }, 2000
        )

        // Observer
        val widgetObserver = Observer<List<Widget>> {
            for(widget in it) {
                val customWidget = CustomWidget(this).apply {
                    widget.caption?.let { caption -> setWidgetCaption(caption) } ?: run { setCaptionVisibility(false) }
                    setWidgetData(widget.data)
                    setColorFilter(R.color.darkGray)
                    setWidgetImageBitmap(widget.icon)
                    setWidgetScale(widget.scale)
                    setWidgetCoordination(widget.x, widget.y)
                    setOnClickListener(this@CustomizeCommunicationActivity)
                }
                binding.communicationLayout.addView(customWidget, widget.width, widget.height)
                Log.d("debug", "addview")
                //customWidgetList.add(customWidget) // 필요 없을 듯?
            }
        }
        customizeViewModel.widgets.observe(this, widgetObserver)

        // Handlers
        // Connect process
        connectMessageHandler = object: Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                when(msg.what) {
                    CONNECT_SUCCESS -> {
                        val rfcommSocket = msg.obj as BluetoothSocket
                        progressAnimator.cancelAnimation()
                        binding.checkConnectSettingGroup.visibility = View.GONE
                        binding.communicationGroup.visibility = View.VISIBLE
                        binding.customizeNameTextView.text = customizeName // 이름 표시
                        customizeViewModel.getWidgets(customizeName) // 위젯 가져오기
                        // 스레드 실행
                        communicationThread = ClientCommunicationThread(rfcommSocket, communicationMessageHandler, BUFFER_SIZE)
                        communicationThread!!.start()
                    }

                    CONNECT_FAIL -> {
                        Log.d("debug", "connect fail")
                        progressAnimator.cancelAnimation()
                        // TODO 연결실패
                    }
                }
            }
        }
        // Communication process
        communicationMessageHandler = object: Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when(msg.what) {
                    MESSAGE_READ -> {
                        // TODO 커뮤니케이션 진행상황에 표시
                    }
                    MESSAGE_WRITE -> {
                        // TODO 커뮤니케이션 진행상황에 표시
                    }
                    CONNECT_CLOSE -> {
                        // TODO 연결이 끊기면 다이얼로그 메시지 표시하고 액티비티 종료
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.btnFinish.setOnClickListener { finish() }
        binding.btnNaviBefore.setOnClickListener { finish() }
    }

    // Widget click listener
    override fun onClick(view: View?) {
        val widget = view as CustomWidget
        Log.d("click", widget.getWidgetData())
    }

    override fun onDestroy() {
        super.onDestroy()

        connectThread?.let { thread ->
            if(!thread.isInterrupted) {
                thread.interrupt()
                thread.cancel()
            }
        }
        communicationThread?.let { thread ->
            if(!thread.isInterrupted) {
                thread.interrupt()
                thread.cancel()
            }
        }
    }
}