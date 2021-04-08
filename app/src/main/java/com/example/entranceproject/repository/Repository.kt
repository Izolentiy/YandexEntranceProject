package com.example.entranceproject.repository

import com.example.entranceproject.data.StockDatabase
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.network.FinnhubService
import com.example.entranceproject.network.trendingTickers
import com.example.entranceproject.network.websocket.SocketUpdate
import com.example.entranceproject.network.websocket.WebServiceProvider
import com.example.entranceproject.ui.main.Tab
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject

class Repository @Inject constructor(
    private val service: FinnhubService,
    private val database: StockDatabase,
    private val webServiceProvider: WebServiceProvider
) {
    private val stockDao = database.stockDao()

    fun getStocks(tab: Tab) = networkBoundResource(
        loadFromDb = { stockDao.getStocks(tab) },
        shouldFetch = { stocks ->
            if (tab == Tab.FAVORITE) false
            else stocks.isEmpty()
        },
        fetch = {
            withContext(Dispatchers.IO) {
                /*// Some issues with my plan, could not fetch data
                val tickers = service.getMostWatchedTickers().tickers*/
                val tickers = trendingTickers
                tickers.map { async { getTickerData(it) } }.awaitAll()
            }
        },
        saveFetchResult = { stocks -> stockDao.insertStocks(stocks) }
    )

    fun closeSocket() { webServiceProvider.stopSocket() }

    fun openSocket(): Channel<SocketUpdate> = webServiceProvider.startSocket()

    suspend fun updateFavorite(stock: Stock) = stockDao.update(stock)

    private suspend fun getTickerData(ticker: String) = coroutineScope {
        val quoteData = async { service.getQuoteData(ticker) }
        val companyData = async { service.getCompanyProfile(ticker) }

        companyData.await().let { company ->
            quoteData.await().let { quote ->
                return@coroutineScope Stock(
                    ticker, company.name, company.logo,
                    company.country, company.currency, quote.c, quote.o, false
                )
            }
        }
    }

}

