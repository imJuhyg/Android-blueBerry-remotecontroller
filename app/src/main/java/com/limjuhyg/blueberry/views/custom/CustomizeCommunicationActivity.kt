package com.limjuhyg.blueberry.views.custom

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.adapter.LogRecyclerViewAdapter.Companion.TYPE_SEND
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
import com.limjuhyg.blueberry.rfcomm.CommunicationThread
import com.limjuhyg.blueberry.rfcomm.ConnectThread
import com.limjuhyg.blueberry.utils.ProgressCircleAnimator
import com.limjuhyg.blueberry.utils.addFragment
import com.limjuhyg.blueberry.viewmodels.CustomizeViewModel
import com.limjuhyg.blueberry.views.fragments.CommunicationLogFragment

class CustomizeCommunicationActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityCustomizeCommunicationBinding
    private lateinit var customizeName: String
    private var deviceName: String? = null
    private var deviceAddress: String? = null
    private val bluetoothAdapter by lazy { MainApplication.instance.bluetoothAdapter }
    private lateinit var bluetoothDevice: BluetoothDevice
    private val widgetList by lazy { ArrayList<CustomWidget>() }
    private var isBonded = false
    private val customizeViewModel by lazy { ViewModelProvider(this).get(CustomizeViewModel::class.java) }
    private lateinit var connectMessageHandler: Handler
    private lateinit var communicationMessageHandler: Handler
    private var connectThread: ConnectThread? = null
    private var communicationThread: CommunicationThread? = null
    private var progressAnimator: ProgressCircleAnimator? = null
    private var showDialog: Boolean = true
    private var isDataVisible: Boolean = false
    private var isLogVisible: Boolean = false
    private var isThreadStarted: Boolean = false
    private val communicationLogFragment by lazy { CommunicationLogFragment() }
    private var startTime = SystemClock.elapsedRealtime()

    companion object {
        const val DEFAULT_WARNING = 301
        const val CONNECT_WARNING = 300
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomizeCommunicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customizeName = intent.getStringExtra("CUSTOMIZE_NAME")!!
        deviceName = intent.getStringExtra("DEVICE_NAME")
        deviceAddress = intent.getStringExtra("DEVICE_ADDRESS")

        progressAnimator = ProgressCircleAnimator(
            binding.progressCircle1,
            binding.progressCircle2,
            binding.progressCircle3, 500)
        progressAnimator?.startAnimation()

        Handler(Looper.getMainLooper()).postDelayed( // 연결상태 확인 메시지 2초간 표시
            {
                if(!isDestroyed) {
                    deviceAddress?.let { // 연결정보가 있는 경우
                        for(device in bluetoothAdapter!!.bondedDevices) {
                            if(it == device.address) {
                                isBonded = true
                                bluetoothDevice = device
                            }
                        }
                        if(!isBonded) { // 연결정보는 있지만 현재는 페어링되어있지 않은 경우
                            showWarningMessage(
                                DEFAULT_WARNING,
                                getString(R.string.not_found_bonded_device),
                                getString(R.string.not_found_bonded_device_explain)
                            )
                        }

                        if(isBonded) {
                            binding.textMessage.text = getString(R.string.try_connect)
                            // 연결 요청
                            connectThread = ConnectThread(bluetoothDevice, connectMessageHandler)
                            connectThread!!.start()
                        }

                    } ?: run { // 연결정보가 없는 경우
                        showWarningMessage(
                            DEFAULT_WARNING,
                            getString(R.string.fail_check_connect_setting),
                            getString(R.string.fail_check_connect_setting_explain)
                        )
                    }
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
                widgetList.add(customWidget)
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
                        progressAnimator?.cancelAnimation()

                        // Add Log fragment
                        val bundle = Bundle()
                        bundle.putString("DEVICE_NAME", deviceName ?: deviceAddress)
                        communicationLogFragment.arguments = bundle

                        addFragment(
                            binding.logFragmentContainer.id,
                            communicationLogFragment,
                            false
                        )

                        binding.checkConnectSettingGroup.visibility = View.GONE
                        binding.communicationGroup.visibility = View.VISIBLE
                        binding.customizeNameTextView.text = customizeName // 이름 표시
                        customizeViewModel.getWidgets(customizeName) // 위젯 가져오기

                        // 스레드 실행
                        communicationThread = CommunicationThread(rfcommSocket, communicationMessageHandler, BUFFER_SIZE)
                        communicationThread!!.start()
                        isThreadStarted = true
                    }

                    CONNECT_FAIL -> {
                        showWarningMessage(
                            CONNECT_WARNING,
                            getString(R.string.connect_fail),
                            getString(R.string.try_reconnect)
                        )
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

                    }

                    MESSAGE_WRITE -> {
                        val writtenMessage = msg.obj as String
                        communicationLogFragment.addLogItem(TYPE_SEND, System.currentTimeMillis(), writtenMessage)
                    }

                    CONNECT_CLOSE -> {
                        if(showDialog) {
                            val dialogView = layoutInflater.inflate(R.layout.custom_alert_dialog_ok, null, false)
                            val title: TextView = dialogView.findViewById(R.id.title)
                            val subtitle: TextView = dialogView.findViewById(R.id.subtitle)
                            title.text = getString(R.string.socket_close_message)
                            subtitle.text = getString(R.string.custom_close_message)

                            val builder = AlertDialog.Builder(this@CustomizeCommunicationActivity)
                            builder.setView(dialogView)
                            builder.setCancelable(false)

                            val alertDialog = builder.create()
                            alertDialog.show()

                            val button: Button = dialogView.findViewById(R.id.btn_ok)
                            button.setOnClickListener {
                                this@CustomizeCommunicationActivity.finish()
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.btnFinish.setOnClickListener { finish() }
        binding.btnNaviBefore.setOnClickListener { finish() }

        binding.btnReconnect.setOnClickListener {
            progressAnimator?.startAnimation()
            binding.checkWarningGroup.visibility = View.GONE
            binding.checkWarningMessage3.visibility = View.GONE
            binding.btnReconnect.visibility = View.GONE
            binding.checkConnectSettingGroup.visibility = View.VISIBLE

            connectThread = ConnectThread(bluetoothDevice, connectMessageHandler)
            connectThread!!.start()
        }

        binding.btnVisibility.setOnClickListener { // 데이터 표시
            if(!isDataVisible) {
                isDataVisible = true
                binding.btnVisibility.setColorFilter(ContextCompat.getColor(this, R.color.identityColor))

                for(widget in widgetList) {
                    widget.setDataVisibility(true)
                }

            } else {
                isDataVisible = false
                binding.btnVisibility.clearColorFilter()

                for(widget in widgetList) {
                    widget.setDataVisibility(false)
                }
            }
        }

        binding.btnCommunicationLog.setOnClickListener { // 커뮤니케이션 로그 표시
            if(!isLogVisible) {
                isLogVisible = true
                binding.btnCommunicationLog.setColorFilter(ContextCompat.getColor(this, R.color.identityColor))
                communicationLogFragment.show()

            } else {
                isLogVisible = false
                binding.btnCommunicationLog.clearColorFilter()
                communicationLogFragment.hide()
            }
        }
    }

    override fun onBackPressed() {
        val endTime = SystemClock.elapsedRealtime()
        if(isThreadStarted) {
            if(endTime - startTime <= 2000) {
                super.onBackPressed()
            } else {
                Toast.makeText(this, "한 번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
                startTime = SystemClock.elapsedRealtime()
            }
        } else {
            super.onBackPressed()
        }
    }

    // Widget click listener
    override fun onClick(view: View?) {
        val widget = view as CustomWidget
        communicationThread?.write(widget.getWidgetData().toByteArray(Charsets.UTF_8))
    }

    private fun showWarningMessage(warningType: Int, message1: String, message2: String) {
        progressAnimator?.cancelAnimation()
        binding.checkConnectSettingGroup.visibility = View.GONE

        if(warningType == DEFAULT_WARNING) {
            binding.btnReconnect.visibility = View.GONE
            binding.checkWarningGroup.visibility = View.VISIBLE
            binding.checkWarningMessage3.visibility = View.GONE

        } else if(warningType == CONNECT_WARNING) {
            binding.btnReconnect.visibility = View.VISIBLE
            binding.checkWarningGroup.visibility = View.VISIBLE
            binding.checkWarningMessage3.visibility = View.VISIBLE
        }

        binding.checkWarningMessage1.text = message1
        binding.checkWarningMessage2.text = message2
    }

    override fun onDestroy() {
        super.onDestroy()
        progressAnimator?.cancelAnimation()
        progressAnimator = null

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
        showDialog = false
    }
}