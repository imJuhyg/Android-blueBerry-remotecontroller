package com.limjuhyg.blueberry

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.limjuhyg.blueberry.customviews.CustomWidget
import com.limjuhyg.blueberry.models.room.entities.Customize
import com.limjuhyg.blueberry.models.room.entities.Widget
import com.limjuhyg.blueberry.viewmodels.CustomizeViewModel

class TestModuleActivity : AppCompatActivity() {
    private val customizeViewModel by lazy { ViewModelProvider(this).get(CustomizeViewModel::class.java) }
    private lateinit var container: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_module)

        container = findViewById(R.id.container)

        val customizeObserver = Observer<List<Customize>> {
            for(customize in it) {
                Log.d("customize name", customize.customizeName)
                Log.d("device name", customize.deviceName?: "null")
                Log.d("device address", customize.deviceAddress?: "null")
                // customizeList 를 구해서 해당 customizeName 과 연결된 위젯 정보를 불러온다
                customizeViewModel.getWidgets(customize.customizeName)
            }
        }
        customizeViewModel.customizeList.observe(this, customizeObserver)
        customizeViewModel.getAllCustomize()

        val widgetObserver = Observer<List<Widget>> {
            // 위젯 생성
            for(widget in it) {
                Log.d("debug", "위젯 생성")
                val customWidget = CustomWidget(this).apply {
                    widget.caption?.let { caption -> setWidgetCaption(caption) } ?: run { setCaptionVisibility(false) }
                    setWidgetData(widget.data)
                    setColorFilter(R.color.darkGray)
                    setWidgetImageBitmap(widget.icon)
                    setWidgetCoordination(widget.x, widget.y)
                    // TODO setOnClickListener 로 터치시 데이터 전송
                }
                container.addView(customWidget, widget.width, widget.height)
            }
        }
        customizeViewModel.widgets.observe(this, widgetObserver)
    }
}