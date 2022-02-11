package com.limjuhyg.blueberry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.adapter.items.ChatRecyclerViewItem
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatRecyclerViewAdapter : RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder>() {
    private val chatItems by lazy { ArrayList<ChatRecyclerViewItem>() }

    companion object {
        const val DIRECTION_SEND: Int = 0
        const val DIRECTION_RECEIVE: Int = 1
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // send
        val sendLayout: Group = view.findViewById(R.id.send_layout)
        val sendMessage: TextView = view.findViewById(R.id.send_text_view)
        val sendTime: TextView = view.findViewById(R.id.send_time)

        // receive
        val receiveLayout: Group = view.findViewById(R.id.receive_layout)
        val receiveMessage: TextView = view.findViewById(R.id.receive_text_view)
        val receiveTime: TextView = view.findViewById(R.id.receive_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_chat_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = chatItems[position]

        viewHolder.apply {
            if(item.direction == DIRECTION_SEND) { // Send
                receiveLayout.visibility = View.GONE
                sendLayout.visibility = View.VISIBLE
                sendMessage.text = item.message
                sendTime.text = formatTime(item.time)
            }
            else if(item.direction == DIRECTION_RECEIVE) { // Receive
                sendLayout.visibility = View.GONE
                receiveLayout.visibility = View.VISIBLE
                receiveMessage.text = item.message
                receiveTime.text = formatTime(item.time)
            }
        }
    }

    override fun getItemCount() = chatItems.size

    override fun getItemViewType(position: Int) = position

    fun addItem(direction: Int, message: String, time: Long) {
        val item = ChatRecyclerViewItem(direction, message, time)
        chatItems.add(item)
        notifyDataSetChanged()
        // TODO notifyItemInserted()
    }

    private fun formatTime(time: Long) = SimpleDateFormat("kk:mm:ss", Locale.getDefault()).format(time)

    fun clear() {
        chatItems.clear()
        notifyDataSetChanged()
    }
}