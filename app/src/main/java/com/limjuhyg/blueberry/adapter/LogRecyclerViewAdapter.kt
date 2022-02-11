package com.limjuhyg.blueberry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.adapter.items.LogRecyclerViewItem
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LogRecyclerViewAdapter : RecyclerView.Adapter<LogRecyclerViewAdapter.ViewHolder>() {
    private val logItems by lazy { ArrayList<LogRecyclerViewItem>() }

    companion object {
        const val TYPE_SEND = 0
        const val TYPE_RECEIVE = 1
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val sendOrReceiveTextView: TextView = view.findViewById(R.id.send_or_receive)
        val timeTextView: TextView = view.findViewById(R.id.time)
        val dataTextView: TextView = view.findViewById(R.id.data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_log_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = logItems[position]
        if(item.type == TYPE_SEND) viewHolder.sendOrReceiveTextView.text = "(전송)"
        //else viewHolder.sendOrReceiveTextView.text = "(받음)"
        viewHolder.timeTextView.text = formatTime(item.time)
        viewHolder.dataTextView.text = item.data

    }

    override fun getItemCount() = logItems.size

    fun addItem(type: Int, time: Long, data: String) {
        logItems.add(LogRecyclerViewItem(type, data, time))
        notifyItemInserted(logItems.size-1)
    }

    private fun formatTime(time: Long) = SimpleDateFormat("kk:mm:ss", Locale.getDefault()).format(time)

    fun clear() {
        logItems.clear()
        notifyDataSetChanged()
    }
}