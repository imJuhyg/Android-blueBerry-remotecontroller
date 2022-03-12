package com.limjuhyg.blueberry.models.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.limjuhyg.blueberry.models.room.entities.Customize

@Dao
interface CustomizeDao {
    // 모든 커스터마이즈 조회
    @Query("SELECT * FROM Customize")
    fun getAllCustomize(): List<Customize>?

    // 일부 커스터마이즈 조회
    @Query("SELECT * FROM Customize WHERE customizeName = :customizeName")
    fun getCustomize(customizeName: String): Customize?

    // Orientation 조회
    @Query("SELECT orientation FROM Customize WHERE customizeName = :customizeName")
    fun getOrientation(customizeName: String): String

    // 커스터마이즈 생성
    @Insert
    fun insertCustomize(customize: Customize)

    // 커스터마이즈 삭제(cascade: Widgets 테이블 자동 삭제)
    @Query("DELETE FROM Customize WHERE customizeName =:deleteKey")
    fun deleteCustomize(deleteKey: String)

    // 커스텀 수정
    @Query("UPDATE Customize SET customizeName =:customizeName, deviceName =:deviceName, deviceAddress =:deviceAddress, orientation =:orientation WHERE customizeName =:updateKey")
    fun updateCustomize(updateKey: String, customizeName: String, deviceName: String?, deviceAddress: String?, orientation: String)
}