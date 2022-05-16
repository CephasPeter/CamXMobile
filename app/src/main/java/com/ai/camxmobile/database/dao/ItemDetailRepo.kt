package com.ai.camxmobile.database.dao

import com.ai.camxmobile.models.ItemModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ItemDetailRepo @Inject constructor(private val dataDao: ItemDetailDao)  {
    suspend fun createItemModel(data : ItemModel) : Long {
        return dataDao.insert(data)
    }

    suspend fun exists(id : String) : Boolean {
        return dataDao.exists(id)
    }

    val getAllItemModel: Flow<List<ItemModel>> get() =  dataDao.getAll()

    val getOneItem: ItemModel get() =  dataDao.getOneItem()

    suspend fun deleteItemModel(data : ItemModel) {
        dataDao.delete(data)
    }
}