package com.example.entranceproject.data.model

import androidx.room.*
import com.example.entranceproject.ui.main.PagerAdapter
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {

    fun getStocks(): Flow<List<Stock>> = getAllStocks()

    @Query("select * from stocks_table")
    fun getAllStocks(): Flow<List<Stock>>

    @Query("select * from stocks_table where (isFavorite = 1)")
    fun getFavoriteStocks(): Flow<List<Stock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<Stock>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stock: Stock)

    @Delete
    suspend fun delete(stock: Stock)

    @Update()
    suspend fun update(stock: Stock)

}