package com.limjuhyg.blueberry.models.room.entities

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Widget",
    // 외래키 설정
    foreignKeys = [
        ForeignKey(entity = Customize::class,
            parentColumns = ["customizeName"],
            childColumns = ["customizeName"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Widget(
    @ColumnInfo var customizeName: String,
    @ColumnInfo var x: Float,
    @ColumnInfo var y: Float,
    @ColumnInfo var width: Int,
    @ColumnInfo var height: Int,
    @ColumnInfo var icon: Bitmap,
    @ColumnInfo var caption: String? = null,
    @ColumnInfo var data: String
){
    @PrimaryKey(autoGenerate = true) var widgetId: Int = 0
}