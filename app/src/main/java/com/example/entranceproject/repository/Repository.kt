package com.example.entranceproject.repository

import android.util.Log
import com.example.entranceproject.BuildConfig
import com.example.entranceproject.data.StockDatabase
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.network.FinnhubService
import kotlinx.coroutines.channels.ticker
import javax.inject.Inject

class Repository @Inject constructor(
    private val service: FinnhubService,
    private val database: StockDatabase
){
    var currentList = mutableListOf<Stock>()

    suspend fun getStocksList(exchange: String): List<Stock> {
        if (currentList.size == 0) {
            val response = service.getStockList(exchange, BuildConfig.API_KEY)
            val tickers = response.body()?.tickers

            val stocks = mutableListOf<Stock>()
            tickers?.subList(0, 20)?.map {ticker ->
                val quotes = getQuote(ticker)
                val companyInfo = getCompanyProfile(ticker)

                if (companyInfo != null && quotes != null) {
                    val delta = quotes.c - quotes.o
                    stocks.add(Stock(
                        ticker,
                        companyInfo.logo,
                        companyInfo.name,
                        companyInfo.currency,
                        quotes.c,
                        delta,
                        false))
                }
            }
            Log.d("REQUEST_URL", "getStocksList: ${response.raw().request()}")
            Log.d("TICKERS_LIST", "${tickers?.size}: $tickers")
            Log.d("TICKERS_SET", "${setOf(tickers).size}")
            currentList = stocks
        }
        return currentList
    }

    private suspend fun getQuote(ticker: String) =
        service.getQuoteData(ticker, BuildConfig.API_KEY).body()

    private suspend fun getCompanyProfile(ticker: String) =
        service.getCompanyProfile(ticker, BuildConfig.API_KEY).body()


    // For later
//    suspend fun searchStocks(query: String): List<Stock> {
//        return listOf(Stock("mock"))
//    }
//
//    suspend fun getAllCachedStocks(): List<Stock> {
//        return listOf(Stock("mock"))
//    }
}