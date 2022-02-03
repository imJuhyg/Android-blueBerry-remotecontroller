package com.limjuhyg.blueberry.models.room.dao

import android.graphics.Bitmap
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

    // 위젯 업데이트
    @Query("UPDATE Widget " +
            "SET x = :x, y = :y, width = :width, height = :height, " +
            "scale = :scale, icon = :icon, caption = :caption, data = :data " +
            "WHERE customizeName = :updateKey")
    fun updateWidget(updateKey: String, x: Float, y: Float, width: Int, height: Int, scale:Float, icon: Bitmap, caption: String? = null, data: String)

    // 위젯 삭제
    @Query("DELETE FROM Widget WHERE customizeName = :deleteKey")
    fun deleteWidget(deleteKey: String)
}