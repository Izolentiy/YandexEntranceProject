package com.example.entranceproject.repository

import android.util.Log
import com.example.entranceproject.data.StockDatabase
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.network.FinnhubService
import com.example.entranceproject.network.trendingTickers
import com.example.entranceproject.ui.main.PagerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Repository @Inject constructor(
    private val service: FinnhubService,
    private val database: StockDatabase
) {
    private val stockDao = database.stockDao()

    fun getStocks() = networkBoundResource(
        loadFromDb = {
            stockDao.getStocks()
        },
        fetch = {
//            // Some issues with my plan, could not fetch data
//            val tickers = service.getMostWatchedTickers().tickers
            val tickers = trendingTickers
            tickers.map { getData(it) }
        },
        saveFetchResult = { stocks ->
            stockDao.insertStocks(stocks)
        }
    )

    private suspend fun getData(ticker: String) = withContext(Dispatchers.IO) {
        val quoteData = async { getQuote(ticker) }
        val companyData = async { getCompanyProfile(ticker) }

        companyData.await().let { company ->
            quoteData.await().let { quote ->
                return@withContext Stock(
                    ticker, company.name, company.logo,
                    company.country, company.currency, quote.c, quote.o, false
                )
            }
        }
    }

    private suspend fun getQuote(ticker: String) =
        service.getQuoteData(ticker)

    private suspend fun getCompanyProfile(ticker: String) =
        service.getCompanyProfile(ticker)

}

