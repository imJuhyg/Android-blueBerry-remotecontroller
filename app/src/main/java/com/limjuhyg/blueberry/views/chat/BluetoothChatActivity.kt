package com.limjuhyg.blueberry.views.chat

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.adapter.ChatRecyclerViewAdapter
import com.limjuhyg.blueberry.applications.MainApplication
import com.limjuhyg.blueberry.applications.MainApplication.Companion.BUFFER_SIZE
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_CLOSE
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_FAIL
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_SUCCESS
import com.limjuhyg.blueberry.applications.MainApplication.Companion.MESSAGE_READ
import com.limjuhyg.blueberry.applications.MainApplication.Companion.MESSAGE_WRITE
import com.limjuhyg.blueberry.databinding.ActivityBluetoothChatBinding
import com.limjuhyg.blueberry.communication.rfcomm.CommunicationThread
import com.limjuhyg.blueberry.communication.rfcomm.ConnectThread
import com.limjuhyg.blueberry.utils.ProgressCircleAnimator
import com.limjuhyg.blueberry.utils.addChatItem
import java.lang.Exception
import java.util.*

class BluetoothChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBluetoothChatBinding
    private val bluetoothAdapter by lazy { MainApplication.instance.bluetoothAdapter }
    private var bluetoothDevice: BluetoothDevice? = null
    private lateinit var messageHandler: Handler
    private lateinit var requestBluetoothEnable: ActivityResultLauncher<Intent>
    private var chatRecyclerViewAdapter: ChatRecyclerViewAdapter? = null
    private var connectThread: ConnectThread? = null
    private var communicationThread: CommunicationThread? = null
    private var isButtonAccessible: Boolean = false
    private var showDialog: Boolean = true
    private var progressAnimator: ProgressCircleAnimator? = null
    private var startTime = SystemClock.elapsedRealtime()
    private var isThreadStarted: Boolean = false
    private var hasPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.to_top_from_bottom_1, R.anim.none)

        progressAnimator = ProgressCircleAnimator(
            binding.progressCircle1,
            binding.progressCircle2,
            binding.progressCircle3, 500)
        progressAnimator?.startAnimation()

        bluetoothDevice = intent.getParcelableExtra("BLUETOOTH_DEVICE")

        // Handler(Background thread -> UI thread)
        messageHandler = object: Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                when(msg.what) {
                    CONNECT_SUCCESS -> {
                        val rfcommSocket = msg.obj as BluetoothSocket

                        progressAnimator?.cancelAnimation()
                        binding.connectProgressLayout.visibility = View.GONE
                        binding.communicationLayout.visibility = View.VISIBLE

                        // Set chatView adapter
                        chatRecyclerViewAdapter = ChatRecyclerViewAdapter()
                        binding.chatView.layoutManager = LinearLayoutManager(this@BluetoothChatActivity)
                        binding.chatView.adapter = chatRecyclerViewAdapter

                        // Set remote panel
                        bluetoothDevice!!.name?.let { name ->
                            binding.nameTextView.text = name
                            binding.addressTextView.text = bluetoothDevice!!.address

                        } ?: run {
                            binding.nameTextView.text = getString(R.string.remote_device_name)
                            binding.addressTextView.text = bluetoothDevice!!.address
                        }

                        communicationThread = CommunicationThread(rfcommSocket, messageHandler, BUFFER_SIZE)
                        communicationThread!!.start()

                        isThreadStarted = true
                    }

                    CONNECT_FAIL -> {
                        progressAnimator?.cancelAnimation()
                        binding.connectProgressLayout.visibility = View.GONE
                        binding.connectFailLayout.visibility = View.VISIBLE
                    }

                    CONNECT_CLOSE -> {
                        if(showDialog) {
                            val dialogView = layoutInflater.inflate(R.layout.custom_alert_dialog_ok, null, false)
                            val title: TextView = dialogView.findViewById(R.id.title)
                            val subtitle: TextView = dialogView.findViewById(R.id.subtitle)
                            title.text = getString(R.string.socket_close_message)
                            subtitle.text = getString(R.string.test_close_message)

                            val builder = AlertDialog.Builder(this@BluetoothChatActivity)
                            builder.setView(dialogView)
                            builder.setCancelable(false)

                            val alertDialog = builder.create()
                            alertDialog.show()

                            val button: Button = dialogView.findViewById(R.id.btn_ok)
                            button.setOnClickListener {
                                this@BluetoothChatActivity.finish()
                                alertDialog.dismiss()
                            }
                        }
                    }

                    MESSAGE_READ -> {
                        try {
                            val message = msg.obj as String
                            addChatItem(ChatRecyclerViewAdapter.DIRECTION_RECEIVE, message, System.currentTimeMillis(), chatRecyclerViewAdapter!!)
                            binding.chatView.smoothScrollToPosition(chatRecyclerViewAdapter!!.itemCount-1)
                        } catch (e: Exception) {

                        }
                    }

                    MESSAGE_WRITE -> {

                    }
                }
            }
        }

        // Change send button color
        binding.editText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                text?.let {
                    if(it.isNotEmpty()) {
                        isButtonAccessible = true
                        binding.sendBtnImageView.visibility = View.GONE
                        binding.btnSend.visibility = View.VISIBLE
                    }
                    else {
                        isButtonAccessible = false
                        binding.btnSend.visibility = View.GONE
                        binding.sendBtnImageView.visibility = View.VISIBLE
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        // Request bluetooth enable
        requestBluetoothEnable = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_OK) {
                connectThread = ConnectThread(bluetoothDevice!!, messageHandler)
                connectThread!!.start()

            } else if(it.resultCode == RESULT_CANCELED) {
                Toast.makeText(applicationContext, "블루투스를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                finish()
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

        } else if(hasPermission){
            connectThread = ConnectThread(bluetoothDevice!!, messageHandler)
            connectThread!!.start()

        } else { // 퍼미션이 없는 경우
            showPermissionAlertDialog()
        }
    }

    override fun onResume() {
        super.onResume()

        // Reconnect button listener
        binding.btnReconnect.setOnClickListener {
            progressAnimator?.startAnimation()
            binding.connectFailLayout.visibility = View.GONE
            binding.connectProgressLayout.visibility = View.VISIBLE

            connectThread = ConnectThread(bluetoothDevice!!, messageHandler)
            connectThread!!.start()
        }

        binding.btnNaviBefore.setOnClickListener { finish() }

        // 키보드 팝업시 자동 스크롤
        binding.chatView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if(bottom < oldBottom) {
                if(chatRecyclerViewAdapter!!.itemCount > 0) {
                    binding.chatView.smoothScrollToPosition(chatRecyclerViewAdapter!!.itemCount-1)
                }
            }
        }

        // Send message to remote device
        binding.btnSend.setOnClickListener {
            // Update chat view
            if(isButtonAccessible) {
                val sendMessage = binding.editText.text.toString()
                addChatItem(ChatRecyclerViewAdapter.DIRECTION_SEND, sendMessage, System.currentTimeMillis(), chatRecyclerViewAdapter!!)

                binding.editText.setText("")
                binding.chatView.smoothScrollToPosition(chatRecyclerViewAdapter!!.itemCount-1) // 자동 스크롤
                communicationThread!!.write(sendMessage.toByteArray(Charsets.UTF_8))
            }
        }

        binding.btnFinish.setOnClickListener {
            finish()
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

    override fun onDestroy() {
        super.onDestroy()
        progressAnimator?.cancelAnimation()
        progressAnimator = null
        chatRecyclerViewAdapter?.clear()
        chatRecyclerViewAdapter = null

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