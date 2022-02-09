package com.limjuhyg.blueberry.models.room.dao

import android.graphics.Bitmap
import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.limjuhyg.blueberry.models.room.entities.Widget

@Dao
interface WidgetDao {
    // 위젯 조회
    @Query("SELECT * FROM Widget WHERE customizeName = :customizeName")
    fun getWidgets(customizeName: String): List<Widget>?

    // 위젯 생성
    @Insert
    fun insertWidget(widget: Widget)

    // 위젯 삭제
    @Query("DELETE FROM Widget WHERE customizeName = :deleteKey")
    fun deleteWidget(deleteKey: String)
}