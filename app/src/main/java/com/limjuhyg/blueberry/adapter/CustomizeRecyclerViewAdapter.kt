package com.limjuhyg.blueberry.adapter

import android.content.Context
import android.graphics.drawable.Drawable
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

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerView: ConstraintLayout = view.findViewById(R.id.recycler_view_frame)
        val customizeName: TextView = view.findViewById(R.id.title)
        val deviceImage: ImageView = view.findViewById(R.id.device_image)
        val deviceName: TextView = view.findViewById(R.id.device_name)
        val deviceAddress: TextView = view.findViewById(R.id.device_address)
        val btnSetting: ImageButton = view.findViewById(R.id.btn_setting) // TODO 연결재설정, 컴포넌트 리디자인
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete) // TODO delete시 데이터베이스에서 삭제
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
            deviceAddress.text = item.deviceAddress
        }

        val animation = AnimationUtils.loadAnimation(context, R.anim.to_top_from_bottom_2)
        startOffsetValue += 300
        animation.startOffset = startOffsetValue
        viewHolder.recyclerView.animation = animation
    }

    override fun getItemCount() = customizeItems.size

    fun addItem(customizeName: String, deviceImage: Drawable?, deviceName: String, deviceAddress: String) {
        val item = CustomizeRecyclerViewItem(customizeName, deviceImage, deviceName, deviceAddress)
        customizeItems.add(item)
        notifyDataSetChanged()
    }

    fun clear() {
        customizeItems.clear()
        startOffsetValue = 0
        notifyDataSetChanged()
    }
}