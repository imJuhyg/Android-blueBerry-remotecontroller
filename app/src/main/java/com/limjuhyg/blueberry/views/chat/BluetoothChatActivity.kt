package com.limjuhyg.blueberry.views.chat

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.adapter.ChatRecyclerViewAdapter
import com.limjuhyg.blueberry.applications.MainApplication.Companion.BUFFER_SIZE
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_CLOSE
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_FAIL
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_SUCCESS
import com.limjuhyg.blueberry.applications.MainApplication.Companion.FROM_CLIENT
import com.limjuhyg.blueberry.applications.MainApplication.Companion.MESSAGE_READ
import com.limjuhyg.blueberry.applications.MainApplication.Companion.MESSAGE_WRITE
import com.limjuhyg.blueberry.applications.MainApplication.Companion.TO_SERVER
import com.limjuhyg.blueberry.databinding.ActivityBluetoothChatBinding
import com.limjuhyg.blueberry.rfcomm.client.ClientCommunicationThread
import com.limjuhyg.blueberry.rfcomm.client.ClientConnectThread
import com.limjuhyg.blueberry.utils.ProgressCircleAnimator
import com.limjuhyg.blueberry.utils.addChatItem
import com.limjuhyg.blueberry.utils.addFragment
import com.limjuhyg.blueberry.views.fragments.ConnectWayFragment
import java.util.*

class BluetoothChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBluetoothChatBinding
    private var bluetoothDevice: BluetoothDevice? = null
    private val connectWayFragment by lazy { ConnectWayFragment() }
    private lateinit var messageHandler: Handler
    private var chatRecyclerViewAdapter: ChatRecyclerViewAdapter? = null
    private var connectThread: ClientConnectThread? = null
    private var communicationThread: ClientCommunicationThread? = null
    private var isButtonAccessible: Boolean = false
    private var showDialog: Boolean = true
    private var progressAnimator: ProgressCircleAnimator? = null
    private var startTime = SystemClock.elapsedRealtime()
    private var isThreadStarted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.to_top_from_bottom_1, R.anim.none)

        addFragment(binding.fragmentContainer.id, connectWayFragment, false)

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

                        communicationThread = ClientCommunicationThread(rfcommSocket, messageHandler, BUFFER_SIZE)
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
                        val message = msg.obj as String
                        addChatItem(ChatRecyclerViewAdapter.DIRECTION_RECEIVE, message, System.currentTimeMillis(), chatRecyclerViewAdapter!!)
                        binding.chatView.smoothScrollToPosition(chatRecyclerViewAdapter!!.itemCount-1)
                    }

                    MESSAGE_WRITE -> {

                    }
                }
            }
        }

        // 연결 방식 선택
        val fragmentResultListener = FragmentResultListener { requestKey, result ->
            val connectWay = result.getInt("CONNECT_WAY")
            if(connectWay == TO_SERVER) {
                binding.fragmentContainer.visibility = View.GONE
                connectThread = ClientConnectThread(bluetoothDevice!!, messageHandler)
                connectThread!!.start()

            } else if(connectWay == FROM_CLIENT) {
                // TODO 연결 요청 받는 경우, server thread 호출
                binding.fragmentContainer.visibility = View.GONE
            }
        }
        supportFragmentManager.setFragmentResultListener("CONNECT_WAY_RESULT", this, fragmentResultListener)

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
    }

    override fun onResume() {
        super.onResume()

        // Reconnect button listener
        binding.btnReconnect.setOnClickListener {
            progressAnimator?.startAnimation()
            binding.connectFailLayout.visibility = View.GONE
            binding.connectProgressLayout.visibility = View.VISIBLE

            connectThread = ClientConnectThread(bluetoothDevice!!, messageHandler)
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