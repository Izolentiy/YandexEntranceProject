package com.example.entranceproject.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.data.model.StockDao

@Database(
    entities = [Stock::class],
    version = 1,
    exportSchema = false
)
abstract class StockDatabase : RoomDatabase() {

    abstract fun stockDao(): StockDao

}