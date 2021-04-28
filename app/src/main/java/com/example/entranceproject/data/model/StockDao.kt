package com.example.entranceproject.data.model

import androidx.room.*
import com.example.entranceproject.ui.pager.Tab
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {

    fun getStocks(tab: Tab): Flow<List<Stock>> =
        when (tab) {
            Tab.STOCKS -> getAllStocks()
            Tab.FAVORITE -> getFavoriteStocks()
        }

    @Query("select * from stocks_table")
    fun getAllStocks(): Flow<List<Stock>>

    @Query("select * from stocks_table where isFavorite = 1")
    fun getFavoriteStocks(): Flow<List<Stock>>

    @Query("select * from stocks_table where ticker like :query or companyName like :query")
//    @Query("select * from stocks_table where (ticker or companyName) like :query") // Is it work?
    fun searchStocks(query: String): Flow<List<Stock>>

    @Query("select * from stocks_table where ticker = :ticker")
    suspend fun getStock(ticker: String): Stock

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<Stock>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stock: Stock)

    @Delete
    suspend fun delete(stock: Stock)

    @Update
    suspend fun update(stock: Stock)

    @Update
    suspend fun updateStocks(stocks: List<Stock>)

}