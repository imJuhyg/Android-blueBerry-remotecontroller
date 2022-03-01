package com.limjuhyg.blueberry.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.adapter.items.DeviceRecyclerViewItem

class DeviceRecyclerViewAdapter(private val context: Context) : RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder>() {
    private val deviceItems by lazy { ArrayList<DeviceRecyclerViewItem>() }
    private lateinit var onItemClickListener: OnItemClickListener
    private var startOffsetValue: Long = 0

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerView: ConstraintLayout = view.findViewById(R.id.device_recycler_view)
        val imageView: ImageView = view.findViewById(R.id.image_view)
        val nameView: TextView = view.findViewById(R.id.name_text_view)
        val addressView: TextView = view.findViewById(R.id.address_text_view)

        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION) onItemClickListener.onItemClick(view, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_device_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = deviceItems[position]
        viewHolder.imageView.setImageDrawable(item.image)
        viewHolder.nameView.text = item.name
        viewHolder.addressView.text = item.address
    }

    override fun getItemCount() = deviceItems.size

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun addItem(image: Drawable?, name: String?, address: String) {
        val item = DeviceRecyclerViewItem(image, name, address)
        deviceItems.add(item)
        notifyDataSetChanged()
    }

    fun getItem(position: Int) = deviceItems[position]

    fun isNotExist(mac: String): Boolean {
        for(device in deviceItems) {
            if(device.address == mac) return false
        }
        return true
    }

    fun clear() {
        deviceItems.clear()
        startOffsetValue = 0
        notifyDataSetChanged()
    }
}