package com.totheptv.blueberry.views

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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.totheptv.blueberry.R
import com.totheptv.blueberry.databinding.ActivityClientCommunicationBinding
import com.totheptv.blueberry.recyclerviews.ChatRecyclerViewAdapter
import com.totheptv.blueberry.rfcomm.client.ClientCommunicationThread
import com.totheptv.blueberry.rfcomm.client.ClientConnectThread
import com.totheptv.blueberry.utils.addChatItem
import java.util.*

class ClientCommunicationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClientCommunicationBinding
    private var bluetoothDevice: BluetoothDevice? = null
    private lateinit var messageHandler: Handler
    private val chatRecyclerViewAdapter by lazy { ChatRecyclerViewAdapter() }
    private var connectThread: ClientConnectThread? = null
    private var communicationThread: ClientCommunicationThread? = null
    private var isButtonAccessible: Boolean = false
    private var showDialog: Boolean = true

    companion object {
        const val BUFFER_SIZE: Int = 1024
        const val CONNECT_SUCCESS: Int = 1000
        const val CONNECT_FAIL: Int = 1001
        const val CONNECT_CLOSE: Int = 1002
        const val MESSAGE_WRITE: Int = 0
        const val MESSAGE_READ: Int = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientCommunicationBinding.inflate(layoutInflater)
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
                                chatView.layoutManager = LinearLayoutManager(this@ClientCommunicationActivity)
                                chatView.adapter = chatRecyclerViewAdapter
                            }

                            // Set remote panel
                            binding.apply {
                                bluetoothDevice!!.name?.let { name ->
                                    imageView.setImageDrawable(ContextCompat.getDrawable(this@ClientCommunicationActivity, R.drawable.icon_remote_device_48))
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
                            val dialogView = layoutInflater.inflate(R.layout.custom_alert_dialog, null, false)
                            val builder = AlertDialog.Builder(this@ClientCommunicationActivity)
                            builder.setView(dialogView)
                            builder.setCancelable(false)

                            val alertDialog = builder.create()
                            alertDialog.show()

                            val button: Button = dialogView.findViewById(R.id.btn_ok)
                            button.setOnClickListener {
                                this@ClientCommunicationActivity.finish()
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
        binding.apply {
            btnReConnect.setOnClickListener {
                connectFailLayout.visibility = View.GONE
                connectProgressLayout.visibility = View.VISIBLE

                connectThread = ClientConnectThread(bluetoothDevice!!, messageHandler)
                connectThread!!.start()
            }
        }

        /**
         * Communication process
         */
        // 키보드 팝업시 자동 스크롤
        binding.apply {
            chatView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if(bottom < oldBottom) {
                    if(chatRecyclerViewAdapter.itemCount > 0) {
                        chatView.smoothScrollToPosition(chatRecyclerViewAdapter.itemCount-1)
                    }
                }
            }
        }

        // Change send button color
        binding.apply {
            editText.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                    text?.let {
                        if(it.isNotEmpty()) {
                            isButtonAccessible = true
                            sendBtnImageView.visibility = View.GONE
                            btnSend.visibility = View.VISIBLE
                        }
                        else {
                            isButtonAccessible = false
                            btnSend.visibility = View.GONE
                            sendBtnImageView.visibility = View.VISIBLE
                        }
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        // Send message to remote device
        binding.apply {
            btnSend.setOnClickListener {
                // Update chat view
                if(isButtonAccessible) {
                    val sendMessage = editText.text.toString()
                    addChatItem(ChatRecyclerViewAdapter.DIRECTION_SEND, sendMessage, System.currentTimeMillis(), chatRecyclerViewAdapter)

                    editText.setText("")
                    chatView.smoothScrollToPosition(chatRecyclerViewAdapter.itemCount-1) // 자동 스크롤
                    communicationThread!!.write(sendMessage.toByteArray(Charsets.UTF_8))
                }
            }
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