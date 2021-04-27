package com.example.entranceproject.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.data.model.StockDao
import com.example.entranceproject.data.model.Ticker
import com.example.entranceproject.data.model.TickerDao

@Database(
    entities = [Stock::class, Ticker::class],
    version = 1,
    exportSchema = false
)
abstract class StockDatabase : RoomDatabase() {

    abstract fun stockDao(): StockDao

    abstract fun tickerDao(): TickerDao

}