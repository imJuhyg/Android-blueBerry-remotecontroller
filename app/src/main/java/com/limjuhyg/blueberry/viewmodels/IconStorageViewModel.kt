package com.limjuhyg.blueberry.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.limjuhyg.blueberry.applications.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.launch

class IconStorageViewModel : ViewModel() {
    private val storage by lazy { FirebaseStorage.getInstance() }
    val fileReferences by lazy { MutableLiveData<ArrayList<StorageReference>>() }
    val iconUriList by lazy { MutableLiveData<ArrayList<Uri>>() }
    val onProgress by lazy { MutableLiveData<Boolean>() }

    init {
        // 설정해야 Failure listener 를 받을 수 있음
        storage.maxDownloadRetryTimeMillis = 100
        storage.maxOperationRetryTimeMillis = 100
    }

    fun getFileReferences() {
        viewModelScope.launch {
            onProgress.value = true

            val deviceDpi: String = MainApplication.instance.deviceDpi!!
            val directoryPath = "google_icons/android/black/drawable-$deviceDpi/"
            val storageAllReference: StorageReference = storage.reference.child(directoryPath)
            val listAllTask: Task<ListResult> = storageAllReference.listAll()
            listAllTask.addOnCompleteListener {
                if(it.isSuccessful) {
                    onProgress.value = false
                    fileReferences.value = ArrayList(it.result.items)
                }

            }.addOnFailureListener { // 네트워크가 끊어진 경우
                fileReferences.value = ArrayList()
                onProgress.value = false
            }
        }
    }

    fun getIconUriList(fileReferences: List<StorageReference>, fileName: String) {
        viewModelScope.launch {
            onProgress.value = true

            var itemCount = 0
            val uriList: ArrayList<Uri> = ArrayList()

            for(reference in fileReferences) {
                if(reference.name.contains(fileName)) {
                    ++itemCount
                }
            }

            if(itemCount == 0) { // 검색 결과가 없는 경우
                iconUriList.value = ArrayList()
                onProgress.value = false
            }

            for(reference in fileReferences) {
                if(reference.name.contains(fileName)) {
                    reference.downloadUrl.addOnSuccessListener { uri ->
                        uriList.add(uri)
                        if(uriList.size == itemCount) {
                            onProgress.value = false
                            iconUriList.value = uriList
                        }

                    }.addOnFailureListener { // 네트워크가 끊어진 경우
                        onProgress.value = false
                    }
                }
            }
        }
    }
}