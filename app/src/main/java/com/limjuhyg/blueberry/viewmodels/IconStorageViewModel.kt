package com.limjuhyg.blueberry.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.StorageReference
import com.limjuhyg.blueberry.models.firebase.storage.IconStorage
import kotlinx.coroutines.launch

class IconStorageViewModel : ViewModel() {
    private val iconStorage by lazy { IconStorage.getInstance() }
    val fileReferences by lazy { MutableLiveData<List<StorageReference>>() }
    val iconUriList by lazy { MutableLiveData<ArrayList<Uri>>() }

    fun getFileReferences() {
        viewModelScope.launch {
            fileReferences.value = iconStorage.getFileReferences()
        }
    }

    fun getIconUriList(fileReferences: List<StorageReference>, fileName: String) {
        viewModelScope.launch {
            iconUriList.value = iconStorage.getIconUriList(fileReferences, fileName)
        }
    }
}