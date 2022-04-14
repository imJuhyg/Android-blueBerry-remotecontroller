package com.limjuhyg.blueberry.views.custom

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Group
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
import com.limjuhyg.blueberry.models.room.entities.Widget
import com.limjuhyg.blueberry.communication.rfcomm.CommunicationThread
import com.limjuhyg.blueberry.communication.rfcomm.ConnectThread
import com.limjuhyg.blueberry.utils.ProgressCircleAnimator
import com.limjuhyg.blueberry.utils.addFragment
import com.limjuhyg.blueberry.viewmodels.CustomizeViewModel
import com.limjuhyg.blueberry.views.fragments.CommunicationLogFragment

class CustomizeCommunicationActivity : AppCompatActivity(), View.OnClickListener {
    private val checkConnectSettingGroup: Group by lazy { findViewById(R.id.check_connect_setting_group) }
    private val textMessage: TextView by lazy { findViewById(R.id.text_message) }
    private val progressCircle1: View by lazy { findViewById(R.id.progress_circle_1) }
    private val progressCircle2: View by lazy { findViewById(R.id.progress_circle_2) }
    private val progressCircle3: View by lazy { findViewById(R.id.progress_circle_3) }
    private val checkWarningGroup: Group by lazy { findViewById(R.id.check_warning_group) }
    private val checkWarningImage: ImageView by lazy { findViewById(R.id.check_warning_image) }
    private val checkWarningMessage1: TextView by lazy { findViewById(R.id.check_warning_message_1) }
    private val checkWarningMessage2: TextView by lazy { findViewById(R.id.check_warning_message_2) }
    private val btnNaviBefore: ImageButton by lazy { findViewById(R.id.btn_navi_before) }
    private val btnReconnect: Button by lazy { findViewById(R.id.btn_reconnect) }
    private val communicationGroup: Group by lazy { findViewById(R.id.communication_group) }
    private val btnFinish: ImageButton by lazy { findViewById(R.id.btn_finish) }
    private val customizeNameTextView: TextView by lazy { findViewById(R.id.customize_name_text_view) }
    private val btnVisibility: ImageButton by lazy { findViewById(R.id.btn_visibility) }
    private val btnCommunicationLog: ImageButton by lazy { findViewById(R.id.btn_communication_log) }
    private val communicationLayout: FrameLayout by lazy { findViewById(R.id.communication_layout) }
    private val logFragmentContainer: FrameLayout by lazy { findViewById(R.id.log_fragment_container) }
    private val troubleshootingGroup: Group by lazy { findViewById(R.id.troubleshooting_group) }
    private val troubleshootingFragmentContainer: FrameLayout by lazy { findViewById(R.id.troubleshooting_fragment_container) }

    private lateinit var customizeName: String
    private var deviceName: String? = null
    private var deviceAddress: String? = null
    private lateinit var orientation: String
    private lateinit var requestBluetoothEnable: ActivityResultLauncher<Intent>
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
    private var hasPermission = false

    companion object {
        const val DEFAULT_WARNING = 301
        const val CONNECT_WARNING = 300
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        orientation = intent.getStringExtra("ORIENTATION")!!
        if(orientation == "landscape") {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            setContentView(R.layout.activity_customize_communication_landscape)
        } else {
            setContentView(R.layout.activity_customize_communication)
        }

        customizeName = intent.getStringExtra("CUSTOMIZE_NAME")!!
        deviceName = intent.getStringExtra("DEVICE_NAME")
        deviceAddress = intent.getStringExtra("DEVICE_ADDRESS")

        progressAnimator = ProgressCircleAnimator(progressCircle1, progressCircle2, progressCircle3, 500)
        progressAnimator?.startAnimation()

        // Request bluetooth enable
        requestBluetoothEnable = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_OK) {
                startConnect()

            } else if(it.resultCode == RESULT_CANCELED) {
                Toast.makeText(applicationContext, "블루투스를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

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
                communicationLayout.addView(customWidget, widget.width, widget.height)
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
                        addFragment(logFragmentContainer.id, communicationLogFragment, false)

                        checkConnectSettingGroup.visibility = View.GONE
                        communicationGroup.visibility = View.VISIBLE
                        customizeNameTextView.text = customizeName // 이름 표시
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

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R) { // more than api 31
            requestBluetoothPermission()

        } else {
            hasPermission = true
        }

        if(hasPermission && !bluetoothAdapter!!.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetoothEnable.launch(intent)

        } else if(hasPermission) {
            startConnect()

        } else { // 퍼미션이 없는 경우
            showPermissionAlertDialog()
        }


        // click listener
        btnFinish.setOnClickListener { finish() }
        btnNaviBefore.setOnClickListener { finish() }

        btnReconnect.setOnClickListener {
            progressAnimator?.startAnimation()
            checkWarningGroup.visibility = View.GONE
            btnReconnect.visibility = View.GONE
            checkConnectSettingGroup.visibility = View.VISIBLE

            connectThread = ConnectThread(bluetoothDevice, connectMessageHandler)
            connectThread!!.start()
        }

        btnVisibility.setOnClickListener { // 데이터 표시
            if(!isDataVisible) {
                isDataVisible = true
                btnVisibility.setColorFilter(ContextCompat.getColor(this, R.color.identityColor))

                for(widget in widgetList) {
                    widget.setDataVisibility(true)
                }

            } else {
                isDataVisible = false
                btnVisibility.clearColorFilter()

                for(widget in widgetList) {
                    widget.setDataVisibility(false)
                }
            }
        }

        btnCommunicationLog.setOnClickListener { // 커뮤니케이션 로그 표시
            if(!isLogVisible) {
                isLogVisible = true
                btnCommunicationLog.setColorFilter(ContextCompat.getColor(this, R.color.identityColor))
                communicationLogFragment.show()

            } else {
                isLogVisible = false
                btnCommunicationLog.clearColorFilter()
                communicationLogFragment.hide()
            }
        }
    }

    @RequiresApi(31)
    private fun requestBluetoothPermission() {
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if(isGranted) {
                // BLUETOOTH_CONNECT 권한을 얻은 경우
                hasPermission = true

            } else {
                // BLUETOOTH_CONNECT 권한을 얻지 못한 경우
                Toast.makeText(this, "블루투스 권한이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED) {
            // BLUETOOTH_CONNECT 권한을 요청합니다.
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)

        } else {
            // BLUETOOTH_CONNECT 권한이 이미 있는 경우
            hasPermission = true
        }
    }

    override fun onBackPressed() {
        val endTime = SystemClock.elapsedRealtime()
        if(isThreadStarted) {
            if(endTime - startTime <= 2000) {
                super.onBackPressed()
            } else {
                Toast.makeText(applicationContext, "한 번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
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

    private fun startConnect() {
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
                            textMessage.text = getString(R.string.try_connect)

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
    }

    private fun showPermissionAlertDialog() {
        val dialogView = layoutInflater.inflate(R.layout.custom_alert_dialog_ok, null, false)
        val title: TextView = dialogView.findViewById(R.id.title)
        val subtitle: TextView = dialogView.findViewById(R.id.subtitle)
        title.text = getString(R.string.require_permission)
        subtitle.text = getString(R.string.request_connect_permission)

        val builder = AlertDialog.Builder(this).apply {
            setView(dialogView)
            setCancelable(false)
        }
        val alertDialog = builder.create()
        alertDialog.show()

        val buttonOk: Button = dialogView.findViewById(R.id.btn_ok)
        buttonOk.setOnClickListener {
            alertDialog.dismiss()
            finish()
        }
    }

    private fun showWarningMessage(warningType: Int, message1: String, message2: String) {
        progressAnimator?.cancelAnimation()
        checkConnectSettingGroup.visibility = View.GONE

        if(warningType == DEFAULT_WARNING) {
            btnReconnect.visibility = View.GONE
            checkWarningGroup.visibility = View.VISIBLE

        } else if(warningType == CONNECT_WARNING) {
            btnReconnect.visibility = View.VISIBLE
            checkWarningGroup.visibility = View.VISIBLE
        }

        checkWarningMessage1.text = message1
        checkWarningMessage2.text = message2
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