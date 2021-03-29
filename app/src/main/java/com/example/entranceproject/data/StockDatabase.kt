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

    class StockCallback @Inject constructor(
        /* In order to create a database, we need a callback,
            but at the same time, for a callback, we need a database
            and there Provider<> helps to us*/
        private val database: Provider<StockDatabase>,
        @ApplicationScope private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val dao = database.get().stockDao()

            scope.launch {
                dao.insert(Stock("YNDX", isFavorite = true))
                dao.insert(Stock("AAPL", isFavorite = true))
                dao.insert(Stock("GOOGL", isFavorite = true))
                dao.insert(Stock("AMZN", isFavorite = true))
                dao.insert(Stock("BAC", isFavorite = true))
                dao.insert(Stock("MSFT", isFavorite = true))
                dao.insert(Stock("TSLA", isFavorite = true))
                dao.insert(Stock("MA", isFavorite = true))
            }
        }
    }
}