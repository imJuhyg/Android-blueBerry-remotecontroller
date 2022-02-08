package com.limjuhyg.blueberry.views.testing

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.adapter.ChatRecyclerViewAdapter
import com.limjuhyg.blueberry.applications.MainApplication.Companion.BUFFER_SIZE
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_CLOSE
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_FAIL
import com.limjuhyg.blueberry.applications.MainApplication.Companion.CONNECT_SUCCESS
import com.limjuhyg.blueberry.applications.MainApplication.Companion.MESSAGE_READ
import com.limjuhyg.blueberry.applications.MainApplication.Companion.MESSAGE_WRITE
import com.limjuhyg.blueberry.databinding.ActivityBluetoothChatBinding
import com.limjuhyg.blueberry.rfcomm.client.ClientCommunicationThread
import com.limjuhyg.blueberry.rfcomm.client.ClientConnectThread
import com.limjuhyg.blueberry.utils.addChatItem
import java.util.*

class BluetoothChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBluetoothChatBinding
    private var bluetoothDevice: BluetoothDevice? = null
    private lateinit var messageHandler: Handler
    private val chatRecyclerViewAdapter by lazy { ChatRecyclerViewAdapter() }
    private var connectThread: ClientConnectThread? = null
    private var communicationThread: ClientCommunicationThread? = null
    private var isButtonAccessible: Boolean = false
    private var showDialog: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.to_top_from_bottom_1, R.anim.none)

        bluetoothDevice = intent.getParcelableExtra("BLUETOOTH_DEVICE")

        // Handler(Background thread -> UI thread)
        messageHandler = object: Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                when(msg.what) {
                    CONNECT_SUCCESS -> {
                        binding.apply {
                            val rfcommSocket = msg.obj as BluetoothSocket

                            connectProgressLayout.visibility = View.GONE
                            communicationLayout.visibility = View.VISIBLE

                            // set chatView adapter
                            binding.apply {
                                chatView.layoutManager = LinearLayoutManager(this@BluetoothChatActivity)
                                chatView.adapter = chatRecyclerViewAdapter
                            }

                            // Set remote panel
                            binding.apply {
                                bluetoothDevice!!.name?.let { name ->
                                    nameTextView.text = name
                                    addressTextView.text = bluetoothDevice!!.address

                                } ?: run {
                                    nameTextView.text = getString(R.string.remote_device_name)
                                    addressTextView.text = bluetoothDevice!!.address
                                }
                            }

                            communicationThread = ClientCommunicationThread(rfcommSocket, messageHandler, BUFFER_SIZE)
                            communicationThread!!.start()
                        }
                    }

                    CONNECT_FAIL -> {
                        binding.apply {
                            connectProgressLayout.visibility = View.GONE
                            connectFailLayout.visibility = View.VISIBLE
                        }
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
                        addChatItem(ChatRecyclerViewAdapter.DIRECTION_RECEIVE, message, System.currentTimeMillis(), chatRecyclerViewAdapter)
                        binding.chatView.smoothScrollToPosition(chatRecyclerViewAdapter.itemCount-1)
                    }

                    MESSAGE_WRITE -> {

                    }
                }
            }
        }

        connectThread = ClientConnectThread(bluetoothDevice!!, messageHandler)
        connectThread!!.start()
    }

    override fun onResume() {
        super.onResume()

        // Reconnect button listener
        binding.btnReConnect.setOnClickListener {
            binding.connectFailLayout.visibility = View.GONE
            binding.connectProgressLayout.visibility = View.VISIBLE

            connectThread = ClientConnectThread(bluetoothDevice!!, messageHandler)
            connectThread!!.start()
        }

        // 키보드 팝업시 자동 스크롤
        binding.chatView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if(bottom < oldBottom) {
                if(chatRecyclerViewAdapter.itemCount > 0) {
                    binding.chatView.smoothScrollToPosition(chatRecyclerViewAdapter.itemCount-1)
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

        // Send message to remote device
        binding.btnSend.setOnClickListener {
            // Update chat view
            if(isButtonAccessible) {
                val sendMessage = binding.editText.text.toString()
                addChatItem(ChatRecyclerViewAdapter.DIRECTION_SEND, sendMessage, System.currentTimeMillis(), chatRecyclerViewAdapter)

                binding.editText.setText("")
                binding.chatView.smoothScrollToPosition(chatRecyclerViewAdapter.itemCount-1) // 자동 스크롤
                communicationThread!!.write(sendMessage.toByteArray(Charsets.UTF_8))
            }
        }

        binding.btnFinish.setOnClickListener {
            finish()
        }
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
        showDialog = false
    }
}