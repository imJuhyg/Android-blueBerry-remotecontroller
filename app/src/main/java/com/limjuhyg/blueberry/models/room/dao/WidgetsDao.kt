package com.limjuhyg.blueberry.models.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.limjuhyg.blueberry.models.room.entities.Widgets

@Dao
interface WidgetsDao {
    // 위젯 조회
    @Query("SELECT * FROM Widgets WHERE customizeName = :customizeName")
    fun getWidget(customizeName: String): Widgets?

    // 위젯 생성
    @Insert
    fun insertWidget(widget: Widgets)

    // 위젯 업데이트
    @Query("UPDATE Widgets " +
            "SET x = :x, y = :y, width = :width, height = :height, " +
            "drawableId = :drawableId, caption = :caption, data = :data " +
            "WHERE customizeName = :updateKey")
    fun updateWidget(updateKey: String, x: Float, y: Float, width: Int, height: Int, drawableId: Int, caption: String? = null, data: String)

    // 위젯 삭제
    @Query("DELETE FROM Widgets WHERE customizeName = :deleteKey")
    fun deleteWidget(deleteKey: String)
}