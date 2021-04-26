package com.example.entranceproject.data.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TickerDao {

    @Query("select * from tickers_table")
    fun getTickers(): Flow<List<Ticker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickers(tickers: List<Ticker>)

}