package com.limjuhyg.blueberry.models.firebase.storage

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.limjuhyg.blueberry.applications.MainApplication
import kotlinx.coroutines.*

class IconStorage {
    private val storage by lazy { FirebaseStorage.getInstance() }

    companion object {
        private var instance: IconStorage? = null

        fun getInstance(): IconStorage =
            instance ?: synchronized(this) {
                instance ?: IconStorage().also {
                    instance = it
                }
            }
    }

    suspend fun getFileReferences(): List<StorageReference>? = withContext(Dispatchers.IO) {
        val deviceDpi: String = MainApplication.instance.deviceDpi!!
        val directoryPath = "google_icons/android/black/drawable-$deviceDpi/"
        val storageAllReference: StorageReference = storage.reference.child(directoryPath)
        var fileReferences: List<StorageReference>? = null
        var isFailure = false

        val listAllTask: Task<ListResult> = storageAllReference.listAll()
        listAllTask.addOnFailureListener {
            isFailure = true

        }.addOnCompleteListener {
            if(it.isSuccessful) {
                fileReferences = it.result.items
            }
        }

        while(fileReferences == null) {
            if(isFailure) {
                break
            }
        }

        fileReferences
    }

    suspend fun getIconUriList(fileReferences: List<StorageReference>, fileName: String): ArrayList<Uri> = withContext(Dispatchers.IO) {
        var itemCount = 0
        val uriList: ArrayList<Uri> = ArrayList()
        var isFailure = false

        for(reference in fileReferences) {
            if(reference.name.contains(fileName)) {
                ++itemCount
            }
        }

        if(itemCount == 0) return@withContext ArrayList<Uri>()

        for(reference in fileReferences) {
            if(reference.name.contains(fileName)) {
                reference.downloadUrl.addOnFailureListener {
                    isFailure = true

                }.addOnSuccessListener { uri ->
                    uriList.add(uri)
                }

            }
        }

        while (uriList.size != itemCount) {
            if(isFailure) {
                break
            }
        }

        uriList
    }
}