package com.example.entranceproject.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.data.model.StockDao
import com.example.entranceproject.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Stock::class], version = 1, exportSchema = false)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
}