package com.totheptv.blueberry.models.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.totheptv.blueberry.models.room.dao.CustomizeDao
import com.totheptv.blueberry.models.room.dao.WidgetsDao
import com.totheptv.blueberry.models.room.entities.Customize
import com.totheptv.blueberry.models.room.entities.Widgets

@Database(entities = [Customize::class, Widgets::class], version = 1)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun customizeDao() : CustomizeDao
    abstract fun widgetsDao() : WidgetsDao
}