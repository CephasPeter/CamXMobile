package com.ai.camxmobile.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ai.camxmobile.database.dao.ItemDetailDao
import com.ai.camxmobile.database.di.ApplicationScope
import com.ai.camxmobile.database.typeconverters.ImageLabelConverter
import com.ai.camxmobile.models.ItemModel
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [ItemModel::class], version = 1, exportSchema = false)
@TypeConverters(ImageLabelConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataDao(): ItemDetailDao

    class Callback @Inject constructor(private val dataDatabase: Provider<AppDatabase>, @ApplicationScope private val applicationScope: CoroutineScope) : RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
        }
    }

}