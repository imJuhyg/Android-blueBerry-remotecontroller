package com.totheptv.blueberry.models.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.totheptv.blueberry.models.room.entities.Customize

@Dao
interface CustomizeDao {
    // 모든 커스터마이즈 조회
    @Query("SELECT * FROM Customize")
    fun getAllCustomize(): List<Customize>?

    // 일부 커스터마이즈 조회
    @Query("SELECT * FROM Customize WHERE customizeName = :customizeName")
    fun getCustomize(customizeName: String): Customize?

    // 커스터마이즈 생성
    @Insert
    fun insertCustomize(customize: Customize)

    // 커스터마이즈 삭제(cascade: Widgets 테이블 자동 삭제)
    @Query("DELETE FROM Customize WHERE customizeName =:deleteKey")
    fun deleteCustomize(deleteKey: String)

    // 커스텀명 수정(cascade: Widgets 테이블 자동 업데이트)
    @Query("UPDATE Customize SET customizeName =:customizeName WHERE customizeName =:updateKey")
    fun updateCustomizeName(updateKey: String, customizeName: String)

    // 커스텀 연결정보 수정
    @Query("UPDATE Customize SET deviceName =:deviceName, deviceAddress =:deviceAddress WHERE customizeName =:updateKey")
    fun updateCustomizeDevice(updateKey: String, deviceName: String? = null, deviceAddress: String)
}