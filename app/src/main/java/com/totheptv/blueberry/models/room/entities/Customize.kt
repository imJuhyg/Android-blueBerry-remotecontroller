package com.totheptv.blueberry.models.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Customize(
    @PrimaryKey var customizeName: String,
    @ColumnInfo var deviceName: String? = null,
    @ColumnInfo var deviceAddress: String? = null
)