package com.limjuhyg.blueberry.models

import android.app.Application
import androidx.room.Room
import com.limjuhyg.blueberry.models.room.database.LocalDatabase
import com.limjuhyg.blueberry.models.room.entities.Customize
import com.limjuhyg.blueberry.models.room.entities.Widgets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CustomizeRepository private constructor() {
    companion object {
        private var instance: CustomizeRepository? = null
        private lateinit var application: Application
        private lateinit var localDatabase: LocalDatabase

        fun getInstance(application: Application): CustomizeRepository =
            instance ?: synchronized(this) {
                instance ?: CustomizeRepository().also {
                    instance = it
                    localDatabase = Room.databaseBuilder(application, LocalDatabase::class.java, "LocalDB").build()
                }
            }
    }

    // 모든 커스터마이즈 조회
    suspend fun getAllCustomize(): List<Customize>? = withContext(Dispatchers.IO) {
        localDatabase.customizeDao().getAllCustomize()
    }

    // 커스터마이즈 조회
    suspend fun getCustomize(customizeName: String): Customize? = withContext(Dispatchers.IO) {
        localDatabase.customizeDao().getCustomize(customizeName)
    }

    // 커스터마이즈 생성
    suspend fun insertCustomize(customize: Customize) = withContext(Dispatchers.IO) {
        localDatabase.customizeDao().insertCustomize(customize)
    }

    // 커스터마이즈 삭제
    suspend fun deleteCustomize(customizeName: String) = withContext(Dispatchers.IO) {
        localDatabase.customizeDao().deleteCustomize(customizeName)
    }

    // 커스터마이즈 이름 업데이트
    suspend fun updateCustomizeName(customizeName: String, updateName: String) = withContext(
        Dispatchers.IO) {
        localDatabase.customizeDao().updateCustomizeName(customizeName, updateName)
    }

    // 커스터마이즈 연결정보 업데이트
    suspend fun updateCustomizeDevice(customizeName: String, updateDeviceName: String? = null, updateDeviceAddress: String)
            = withContext(Dispatchers.IO) {
        localDatabase.customizeDao().updateCustomizeDevice(customizeName, updateDeviceName, updateDeviceAddress)
    }

    // 위젯 조회
    suspend fun getWidget(customizeName: String): Widgets? = withContext(Dispatchers.IO) {
        localDatabase.widgetsDao().getWidget(customizeName)
    }

    // 위젯 생성
    suspend fun insertWidget(widget: Widgets) = withContext(Dispatchers.IO) {
        localDatabase.widgetsDao().insertWidget(widget)
    }

    // 위젯 업데이트
    suspend fun updateWidget(customizeName: String, widget: Widgets)
            = withContext(Dispatchers.IO) {
        localDatabase.widgetsDao().updateWidget(
            customizeName, widget.x, widget.y,
            widget.width, widget.height,
            widget.drawableId, widget.caption, widget.data)
    }

    // 위젯 삭제
    suspend fun deleteWidget(customizeName: String) = withContext(Dispatchers.IO) {
        localDatabase.widgetsDao().deleteWidget(customizeName)
    }
}