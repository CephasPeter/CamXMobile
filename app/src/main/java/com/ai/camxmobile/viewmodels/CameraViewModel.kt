package com.ai.camxmobile.viewmodels

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.collection.ArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.camxmobile.database.dao.ItemDetailRepo
import com.ai.camxmobile.models.ItemModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(private val itemDetailRepo: ItemDetailRepo): ViewModel(){
    var lensFacing = MutableLiveData<CameraSelector?>()
    var flashEnabled = MutableLiveData<Boolean?>()

    var capturedBitmap = MutableLiveData<Bitmap>()
    var capturedUri = MutableLiveData<Uri>()
    var capturedName = MutableLiveData<String>()

    private val _id = MutableLiveData<Long>()
    val id : LiveData<Long> =  _id
    fun insertRoomData(itemModel: ItemModel){
        viewModelScope.launch(Dispatchers.IO) {
            _id.postValue(itemDetailRepo.createItemModel(itemModel))
        }
    }

    var itemList = MutableLiveData<ArrayList<ItemModel>>()
    fun getAllStoredData(){
        viewModelScope.launch(Dispatchers.IO) {
            itemDetailRepo.getAllItemModel
                .catch { e->
                    e.printStackTrace()
                }
                .collect { list ->
                    itemList.value?.clear()

                    val innerList = ArrayList<ItemModel>()
                    innerList.addAll(list)
                    itemList.postValue(innerList)
                }
        }
    }

    fun getOneStoredData() : ItemModel?{
        var itemModel : ItemModel? = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                itemModel = itemDetailRepo.getOneItem
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        return itemModel
    }

    fun deleteRoomData(itemModel: ItemModel){
        viewModelScope.launch(Dispatchers.IO) {
            itemDetailRepo.deleteItemModel(itemModel)
        }
    }

    fun dataExist(id: String):Boolean{
        var exists = false
        viewModelScope.launch(Dispatchers.IO) {
            exists = itemDetailRepo.exists(id)
        }
        return exists
    }
}