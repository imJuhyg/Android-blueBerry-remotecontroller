package com.limjuhyg.blueberry.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.adapter.items.CustomizeRecyclerViewItem

class CustomizeRecyclerViewAdapter(private val context: Context) : RecyclerView.Adapter<CustomizeRecyclerViewAdapter.ViewHolder>() {
    private val customizeItems by lazy { ArrayList<CustomizeRecyclerViewItem>() }
    private var startOffsetValue: Long = 0
    private lateinit var onViewClickListener: OnViewClickListener
    private lateinit var onButtonClickListener: OnButtonClickListener

    interface OnViewClickListener {
        fun onViewClick(view: View, position: Int)
    }

    interface OnButtonClickListener {
        fun onButtonClick(view: View, position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerView: ConstraintLayout = view.findViewById(R.id.recycler_view_frame)
        val customizeName: TextView = view.findViewById(R.id.title)
        val deviceImage: ImageView = view.findViewById(R.id.device_image)
        val deviceName: TextView = view.findViewById(R.id.device_name)
        val deviceAddress: TextView = view.findViewById(R.id.device_address)
        val btnSetting: ImageButton = view.findViewById(R.id.btn_customize_setting_change)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_customize_delete)

        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION) onViewClickListener.onViewClick(view, position)
            }
            btnSetting.setOnClickListener {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION) onButtonClickListener.onButtonClick(it, position)
            }
            btnDelete.setOnClickListener {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION) onButtonClickListener.onButtonClick(it, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_customize_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = customizeItems[position]
        viewHolder.apply {
            deviceImage.setImageDrawable(item.image)
            customizeName.text = item.customizeName
            deviceName.text = item.deviceName
            deviceAddress.text = item.deviceAddress ?: "연결정보 없음"
        }

        val animation = AnimationUtils.loadAnimation(context, R.anim.to_top_from_bottom_2)
        startOffsetValue += 300
        animation.startOffset = startOffsetValue
        viewHolder.recyclerView.animation = animation
    }

    override fun getItemCount() = customizeItems.size

    fun addItem(customizeName: String, deviceImage: Drawable?, deviceName: String?, deviceAddress: String?) {
        val item = CustomizeRecyclerViewItem(customizeName, deviceImage, deviceName, deviceAddress)
        customizeItems.add(item)
    }

    fun getItem(position: Int) = customizeItems[position]

    fun removeItem(position: Int) {
        customizeItems.removeAt(position)
        startOffsetValue = 0
        notifyItemRemoved(position)
    }

    fun setOnViewClickListener(listener: OnViewClickListener) {
        onViewClickListener = listener
    }

    fun setOnButtonClickListener(listener: OnButtonClickListener) {
        onButtonClickListener = listener
    }

    fun clear() {
        customizeItems.clear()
        startOffsetValue = 0
        notifyDataSetChanged()
    }
}