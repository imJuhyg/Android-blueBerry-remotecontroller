package com.limjuhyg.blueberry.models.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.limjuhyg.blueberry.models.room.converter.Converters
import com.limjuhyg.blueberry.models.room.dao.CustomizeDao
import com.limjuhyg.blueberry.models.room.dao.WidgetDao
import com.limjuhyg.blueberry.models.room.entities.Customize
import com.limjuhyg.blueberry.models.room.entities.Widget

@Database(entities = [Customize::class, Widget::class], version = 1)
@TypeConverters(Converters::class)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun customizeDao() : CustomizeDao
    abstract fun widgetDao() : WidgetDao
}