package com.totheptv.blueberry.recyclerviews

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.totheptv.blueberry.R
import com.totheptv.blueberry.recyclerviews.items.DeviceRecyclerViewItem

class DeviceRecyclerViewAdapter(private val context: Context) : RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder>() {
    private val deviceRecyclerViewItems by lazy { ArrayList<DeviceRecyclerViewItem>() }
    private lateinit var onItemClickListener: OnItemClickListener
    private var startOffsetValue: Long = 0
    private var isCalledAnimation: Boolean = false

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
        val item = deviceRecyclerViewItems[position]
        viewHolder.imageView.setImageDrawable(item.image)
        viewHolder.nameView.text = item.name
        viewHolder.addressView.text = item.address

        if(isCalledAnimation) {
            val animation = AnimationUtils.loadAnimation(context, R.anim.to_top_from_bottom_2)
            startOffsetValue += 300
            animation.startOffset = startOffsetValue
            viewHolder.recyclerView.animation = animation
        }
    }

    override fun getItemCount() = deviceRecyclerViewItems.size

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun addItemWithAnimation(image: Drawable?, name: String?, address: String) {
        isCalledAnimation = true
        val item = DeviceRecyclerViewItem(image, name, address)
        deviceRecyclerViewItems.add(item)
        notifyDataSetChanged()
    }

    fun addItem(image: Drawable?, name: String?, address: String) {
        isCalledAnimation = false
        val item = DeviceRecyclerViewItem(image, name, address)
        deviceRecyclerViewItems.add(item)
        notifyDataSetChanged()
    }

    fun getItem(position: Int) = deviceRecyclerViewItems[position]

    fun isNotExist(mac: String): Boolean {
        for(device in deviceRecyclerViewItems) {
            if(device.address == mac) return false
        }
        return true
    }

    fun clear() {
        deviceRecyclerViewItems.clear()
        startOffsetValue = 0
        notifyDataSetChanged()
    }
}