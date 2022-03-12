package com.limjuhyg.blueberry.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.limjuhyg.blueberry.models.CustomizeRepository
import com.limjuhyg.blueberry.models.room.entities.Customize
import com.limjuhyg.blueberry.models.room.entities.Widget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomizeViewModel(application: Application) : AndroidViewModel(application) {
    private val customizeRepository by lazy { CustomizeRepository.getInstance(application) }
    val customize by lazy { MutableLiveData<Customize>() }
    val customizeList by lazy { MutableLiveData<List<Customize>>() }
    val widgets by lazy { MutableLiveData<List<Widget>>() }
    val isCustomizeCreated by lazy { MutableLiveData<Boolean>() }
    val isCustomizeModified by lazy { MutableLiveData<Boolean>() }
    val orientation by lazy { MutableLiveData<String>() }

    // 커스터마이즈 생성
    fun createCustomize(customize: Customize, widgets: ArrayList<Widget>) {
        CoroutineScope(Dispatchers.Main).launch {
            launch { // Job 1
                customizeRepository.insertCustomize(customize)
            }.join()

            launch { // Job 2
                for(widget in widgets) customizeRepository.insertWidget(widget)
            }.join()
            isCustomizeCreated.value = true
        }
    }

    // 커스터마이즈 수정 순차 실행
    fun modifyCustomize(oldCustomizeName: String, newCustomizeName: String, newDeviceName: String?, newDeviceAddress: String?, newOrientation: String, widgets: ArrayList<Widget>) {
        CoroutineScope(Dispatchers.Main).launch {
            launch { // Job 1 (delete widget)
                customizeRepository.deleteWidget(oldCustomizeName)
            }.join()

            launch { // Job 2 (update customize)
                customizeRepository.updateCustomize(oldCustomizeName,
                    newCustomizeName,
                    newDeviceName,
                    newDeviceAddress,
                    newOrientation
                )
            }.join()

            launch { // Job 3 (insert widget)
                for(widget in widgets) customizeRepository.insertWidget(widget)
            }.join()
            isCustomizeModified.value = true
        }
    }

    // 모든 커스터마이즈 조회
    fun getAllCustomize() {
        viewModelScope.launch {
            customizeList.value = customizeRepository.getAllCustomize()
        }
    }

    // 일부 커스터마이즈 조회
    fun getCustomize(customizeName: String) {
        viewModelScope.launch {
            customize.value = customizeRepository.getCustomize(customizeName)
        }
    }

    // Orientation 조회
    fun getOrientation(customizeName: String) {
        viewModelScope.launch {
            orientation.value = customizeRepository.getOrientation(customizeName)
        }
    }

    // 커스터마이즈 삭제
    fun deleteCustomize(customizeName: String) {
        viewModelScope.launch {
            customizeRepository.deleteCustomize(customizeName)
        }
    }

    // 위젯 조회
    fun getWidgets(customizeName: String) {
        viewModelScope.launch {
            widgets.value = customizeRepository.getWidgets(customizeName)
        }
    }
}