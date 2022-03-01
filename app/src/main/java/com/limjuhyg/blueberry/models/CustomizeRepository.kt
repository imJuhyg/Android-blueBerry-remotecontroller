package com.limjuhyg.blueberry.models

import android.app.Application
import androidx.room.Room
import com.limjuhyg.blueberry.models.room.database.LocalDatabase
import com.limjuhyg.blueberry.models.room.entities.Customize
import com.limjuhyg.blueberry.models.room.entities.Widget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CustomizeRepository private constructor() {
    companion object {
        private var instance: CustomizeRepository? = null
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

    // 커스터마이즈 업데이트
    suspend fun updateCustomize(customizeName: String, updateName: String, updateDeviceName: String?, updateDeviceAddress: String?) =
        withContext(Dispatchers.IO) {
            localDatabase.customizeDao().updateCustomize(customizeName, updateName, updateDeviceName, updateDeviceAddress)
        }

    // 위젯 조회
    suspend fun getWidgets(customizeName: String): List<Widget>? = withContext(Dispatchers.IO) {
        localDatabase.widgetDao().getWidgets(customizeName)
    }

    // 위젯 생성
    suspend fun insertWidget(widget: Widget) = withContext(Dispatchers.IO) {
        localDatabase.widgetDao().insertWidget(widget)
    }

    // 위젯 삭제
    suspend fun deleteWidget(customizeName: String) = withContext(Dispatchers.IO) {
        localDatabase.widgetDao().deleteWidget(customizeName)
    }
}