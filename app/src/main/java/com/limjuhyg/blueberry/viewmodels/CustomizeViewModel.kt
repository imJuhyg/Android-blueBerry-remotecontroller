package com.limjuhyg.blueberry.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.limjuhyg.blueberry.models.CustomizeRepository
import com.limjuhyg.blueberry.models.room.entities.Customize
import com.limjuhyg.blueberry.models.room.entities.Widgets
import kotlinx.coroutines.launch

class CustomizeViewModel(application: Application) : AndroidViewModel(application) {
    private val customizeRepository by lazy { CustomizeRepository.getInstance(application) }
    val customize by lazy { MutableLiveData<Customize>() }
    val customizeList by lazy { MutableLiveData<List<Customize>>() }
    val widget by lazy { MutableLiveData<Widgets>() }

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

    // 커스터마이즈 생성
    fun insertCustomize(customize: Customize) {
        viewModelScope.launch {
            customizeRepository.insertCustomize(customize)
        }
    }

    // 커스터마이즈 삭제
    fun deleteCustomize(customizeName: String) {
        viewModelScope.launch {
            customizeRepository.deleteCustomize(customizeName)
        }
    }

    // 커스터마이즈 이름 업데이트
    fun updateCustomizeName(customizeName: String, updateName: String) {
        viewModelScope.launch {
            customizeRepository.updateCustomizeName(customizeName, updateName)
        }
    }

    // 커스터마이즈 연결정보 업데이트
    fun updateCustomizeDevice(customizeName: String, deviceName: String? = null, deviceAddress: String) {
        viewModelScope.launch {
            customizeRepository.updateCustomizeDevice(customizeName, deviceName, deviceAddress)
        }
    }

    // 일부 위젯 조회
    fun getWidget(customizeName: String) {
        viewModelScope.launch {
            widget.value = customizeRepository.getWidget(customizeName)
        }
    }

    // 위젯 생성
    fun insertWidget(widget: Widgets) {
        viewModelScope.launch {
            customizeRepository.insertWidget(widget)
        }
    }

    // 위젯 업데이트
    fun updateWidget(customizeName: String, widget: Widgets) {
        viewModelScope.launch {
            customizeRepository.updateWidget(customizeName, widget)
        }
    }
}