package com.ai.camxmobile.database.dao

import androidx.room.*
import com.ai.camxmobile.models.ItemModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDetailDao {
    @Transaction
    @Query("SELECT * FROM ItemModel")
    fun getAll(): Flow<List<ItemModel>>

    @Transaction
    @Query("SELECT * FROM ItemModel LIMIT 1")
    fun getOneItem(): ItemModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: ItemModel) : Long

    @Query("SELECT EXISTS (SELECT 1 FROM ItemModel WHERE id = :itemID)")
    fun exists(itemID: String): Boolean

    @Delete
    fun delete(data: ItemModel)
}