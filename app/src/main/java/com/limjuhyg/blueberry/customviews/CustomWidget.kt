package com.limjuhyg.blueberry.customviews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.limjuhyg.blueberry.R

class CustomWidget constructor(context: Context) : ConstraintLayout(context) {
    private lateinit var bitmap: Bitmap
    private var imageView: ImageView
    private var dataTextView: TextView
    private var captionTextView: TextView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_view_widget, this, false)
        addView(view)

        imageView = view.findViewById(R.id.image_view)
        dataTextView = view.findViewById(R.id.data_text_view)
        captionTextView = view.findViewById(R.id.caption_text_view)
    }

    fun setWidgetImageBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        imageView.setImageBitmap(this.bitmap)
    }

    fun getWidgetImageBitmap() = bitmap

    fun setWidgetData(data: String) {
        dataTextView.text = data
    }

    fun getWidgetData() = dataTextView.text.toString()

    fun setWidgetCaption(text: String) {
        captionTextView.text = text
    }

    fun getWidgetCaption() = captionTextView.text.toString()

    fun setWidgetCoordination(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun getWidgetCoordination(): Point {
        val point = Point()
        point.set(this.x.toInt(), this.y.toInt())
        return point
    }

    fun setDataVisibility(visible: Boolean) {
        if(visible) dataTextView.visibility = View.VISIBLE
        else dataTextView.visibility = View.INVISIBLE
    }

    fun setCaptionVisibility(visible: Boolean) {
        if(visible) captionTextView.visibility = View.VISIBLE
        else captionTextView.visibility = View.GONE
    }

    fun setColorFilter(color: Int) {
        imageView.setColorFilter(ContextCompat.getColor(context, color))
    }

    fun getWidgetWidth() = this.width
    fun getWidgetHeight() = this.height
}