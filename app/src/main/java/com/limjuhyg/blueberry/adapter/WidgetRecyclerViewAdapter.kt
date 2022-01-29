package com.limjuhyg.blueberry.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.adapter.items.WidgetRecyclerViewItem

class WidgetRecyclerViewAdapter(private val context: Context) : RecyclerView.Adapter<WidgetRecyclerViewAdapter.ViewHolder>() {
    private val widgetItems by lazy { ArrayList<WidgetRecyclerViewItem>() }
    private lateinit var onItemClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view)
        val title: TextView = view.findViewById(R.id.title_view)

        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION) onItemClickListener.onItemClick(view, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recycler_view_widget_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = widgetItems[position]

        viewHolder.imageView.setImageBitmap(item.image)
        viewHolder.title.text = item.title
    }

    override fun getItemCount() = widgetItems.size

    fun addItem(image: Bitmap, title: String) {
        widgetItems.add(WidgetRecyclerViewItem(image, title))
        notifyDataSetChanged()
    }

    fun getItem(position: Int) = widgetItems[position]

    fun addItemIgnoreNotify(image: Bitmap, title: String) {
        widgetItems.add(WidgetRecyclerViewItem(image, title))
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun clear() {
        widgetItems.clear()
        notifyDataSetChanged()
    }
}