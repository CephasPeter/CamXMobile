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

    suspend fun getItemModelWithoutFlow(id:String) : ItemModel {
        return dataDao.getWithIdsWithoutFlow(id)
    }

    suspend fun getItemModel(id:String) : Flow<ItemModel> {
        return dataDao.getWithIds(id)
    }

    suspend fun deleteItemModel(data : ItemModel) {
        dataDao.delete(data)
    }
}