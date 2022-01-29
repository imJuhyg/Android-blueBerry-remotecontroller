package com.limjuhyg.blueberry.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.adapter.ChatRecyclerViewAdapter
import com.limjuhyg.blueberry.adapter.DeviceRecyclerViewAdapter
import com.limjuhyg.blueberry.adapter.WidgetRecyclerViewAdapter

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

// Widget recycler view add item (Fragment)
fun Fragment.addWidgetItem(bitmap: Bitmap, title: String, recyclerViewAdapter: WidgetRecyclerViewAdapter) {
    recyclerViewAdapter.addItem(bitmap, title)
}
fun Fragment.addDefaultWidgetItems(recyclerViewAdapter: WidgetRecyclerViewAdapter) {
    recyclerViewAdapter.apply {
        addItemIgnoreNotify(BitmapFactory.decodeResource(requireContext().resources, R.drawable.icon_default_button_48)!!, "기본")
        addItemIgnoreNotify(BitmapFactory.decodeResource(requireContext().resources, R.drawable.icon_circle_button_48)!!, "원형")
        addItemIgnoreNotify(BitmapFactory.decodeResource(requireContext().resources, R.drawable.icon_play_48)!!, "재생")
        addItemIgnoreNotify(BitmapFactory.decodeResource(requireContext().resources, R.drawable.icon_stop_48)!!, "중지")
        addItemIgnoreNotify(BitmapFactory.decodeResource(requireContext().resources, R.drawable.icon_pause_48)!!, "일시정지")
        addItemIgnoreNotify(BitmapFactory.decodeResource(requireContext().resources, R.drawable.icon_arrow_left_48)!!, "왼쪽")
        addItemIgnoreNotify(BitmapFactory.decodeResource(requireContext().resources, R.drawable.icon_arrow_drop_up_48)!!, "위쪽")
        addItemIgnoreNotify(BitmapFactory.decodeResource(requireContext().resources, R.drawable.icon_arrow_right_48)!!, "오른쪽")
        addItemIgnoreNotify(BitmapFactory.decodeResource(requireContext().resources, R.drawable.icon_arrow_drop_down_48)!!, "아래쪽")
        addItemIgnoreNotify(BitmapFactory.decodeResource(requireContext().resources, R.drawable.icon_volume_up_48)!!, "볼륨업")
        addItemIgnoreNotify(BitmapFactory.decodeResource(requireContext().resources, R.drawable.icon_volume_down_48)!!, "볼륨다운")
        addItemIgnoreNotify(BitmapFactory.decodeResource(requireContext().resources, R.drawable.icon_volume_off_48)!!, "음소거")
        addItemIgnoreNotify(BitmapFactory.decodeResource(requireContext().resources, R.drawable.icon_power_settings_48)!!, "전원")
    }
}