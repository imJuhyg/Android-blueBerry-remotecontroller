package com.totheptv.blueberry.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.totheptv.blueberry.R
import com.totheptv.blueberry.recyclerviews.ChatRecyclerViewAdapter
import com.totheptv.blueberry.recyclerviews.DeviceRecyclerViewAdapter

// Device recycler view add item with (Activity)
fun AppCompatActivity.addDeviceItem(name: String?, address: String, recyclerViewAdapter: DeviceRecyclerViewAdapter) {
    if(recyclerViewAdapter.isNotExist(address))
        recyclerViewAdapter.addItem(ContextCompat.getDrawable(this, R.drawable.icon_remote_device_48), name ?: address, address)
}

// Device recycler view add item with animation (Fragment)
fun Fragment.addDeviceItemWithAnimation(name: String?, address: String, recyclerViewAdapter: DeviceRecyclerViewAdapter) {
    if(recyclerViewAdapter.isNotExist(address))
        recyclerViewAdapter.addItemWithAnimation(ContextCompat.getDrawable(requireContext(), R.drawable.icon_remote_device_48), name ?: address, address)
}

// Chat recycler view add item (Activity)
fun AppCompatActivity.addChatItem(direction: Int, message: String, time: Long, recyclerViewAdapter: ChatRecyclerViewAdapter) {
    recyclerViewAdapter.addItem(direction, message, time)
}