package com.limjuhyg.blueberry.models.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Widgets",
    // 외래키 설정
    foreignKeys = [
        ForeignKey(entity = Customize::class,
            parentColumns = ["customizeName"],
            childColumns = ["customizeName"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class Widgets(
    @PrimaryKey(autoGenerate = true) var widgetId: Int,
    @ColumnInfo var customizeName: String,
    @ColumnInfo var x: Float,
    @ColumnInfo var y: Float,
    @ColumnInfo var width: Int,
    @ColumnInfo var height: Int,
    @ColumnInfo var drawableId: Int,
    @ColumnInfo var caption: String? = null,
    @ColumnInfo var data: String
)