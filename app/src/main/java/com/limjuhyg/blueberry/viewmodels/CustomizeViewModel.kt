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

/**
 * launch, async 는 동시실행
 * -> 루틴B 가 반드시 루틴A 가 끝나야 되는 경우라면 부적합할 수 있음
 * = 두 중단함수가 서로 의존성이 없을 때 사용
 */

/**
 * Join 은 해당 job 이 끝날때까지 대기한다
 * CoroutineScope 또는 runBlocking 에서 사용가능
 */

/**
 * CoroutineScope: 액티비티가 종료되거나 클래스의 인스턴스가 사라지면 자동으로 메모리에서 해제
 * GlobalScope: 앱이 시작될 때 생성, 앱이 종료되어야 메모리에서 해제
 */

class CustomizeViewModel(application: Application) : AndroidViewModel(application) {
    private val customizeRepository by lazy { CustomizeRepository.getInstance(application) }
    val customize by lazy { MutableLiveData<Customize>() }
    val customizeList by lazy { MutableLiveData<List<Customize>>() }
    val widgets by lazy { MutableLiveData<List<Widget>>() }
    val isCustomizeCreated by lazy { MutableLiveData<Boolean>() }
    val isCustomizeModified by lazy { MutableLiveData<Boolean>() }

    // 커스터마이즈 생성 순차 실행
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
    fun modifyCustomize(oldCustomizeName: String, newCustomizeName: String, newDeviceName: String?, newDeviceAddress: String?, widgets: ArrayList<Widget>) {
        CoroutineScope(Dispatchers.Main).launch {
            launch { // Job 1
                customizeRepository.deleteWidget(oldCustomizeName)
            }.join()

            launch { // Job 2
                customizeRepository.updateCustomize(oldCustomizeName,
                    newCustomizeName,
                    newDeviceName,
                    newDeviceAddress
                )
            }.join()

            launch { // Job 3
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