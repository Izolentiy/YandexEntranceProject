package com.example.entranceproject.repository

import android.util.Log
import com.example.entranceproject.data.StockDatabase
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.network.FinnhubService
import com.example.entranceproject.network.TRENDING_TICKERS
import com.example.entranceproject.network.websocket.SocketUpdate
import com.example.entranceproject.network.websocket.WebSocketHandler
import com.example.entranceproject.ui.pager.Tab
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class Repository @Inject constructor(
    private val service: FinnhubService,
    private val database: StockDatabase,
    private val webSocketHandler: WebSocketHandler
) {
    private val stockDao = database.stockDao()

    fun getStocks(tab: Tab, relevantTickers: List<String>) = networkBoundResource(
        loadFromDb = { stockDao.getStocks(tab) },

        // Fetch stocks info
        shouldFetchStocks = { stocks ->
            if (tab == Tab.FAVORITE) false
            else stocks.isEmpty()
        },
        fetchStocks = {
            withContext(Dispatchers.IO) {
                // Get trending tickers from Mboum.com
                /*val tickers = service.getMostWatchedTickers().tickers*/
                val tickers = TRENDING_TICKERS
//                Log.e(TAG, "getStocks: $tickers")
                tickers.map { async { getStockData(it) } }.awaitAll()
            }
        },
        saveFetchResult = { stocks -> stockDao.insertStocks(stocks) },

        // Fetch only prices
        shouldFetchPrices = {
            tab != Tab.FAVORITE && relevantTickers.isNotEmpty()
        },
        fetchPrices = {
            withContext(Dispatchers.IO) {
                relevantTickers.map { async { updateStockPrice(it) } }.awaitAll()
            }
        },
        updatePrices = { stocks -> stockDao.updateStocks(stocks) },
    )

    fun openSocket(): StateFlow<SocketUpdate> = webSocketHandler.openSocket()

    fun closeSocket() { webSocketHandler.closeSocket() }

    suspend fun searchStocks(query: String) = flow {
        Log.e(TAG, "searchStocks: --------------------------------------START")
        try {
            emit(Resource.loading(emptyList<Stock>()))
            Log.d(TAG, "searchStocks: --------------------------------------LOADING")
            val response = service.search(query)
            Log.d(TAG, "searchStocks: ${response.count}")

            val stocks = response.result.map {
                Stock(ticker=it.symbol, companyName=it.description)
//                if (it == response.result.first()) getStockData(it.symbol)
//                else Stock(ticker=it.symbol, companyName=it.description)
            }
            emit(Resource.success(stocks))
            Log.e(TAG, "searchStocks: --------------------------------------YES")
        } catch (exception: Throwable) {
            Log.e(TAG, "searchStocks: $exception")
            emit(Resource.error(emptyList<Stock>(), exception))
            Log.e(TAG, "searchStocks: --------------------------------------NO")
        }
    }

    suspend fun refreshData() {

    }

    suspend fun updateFavorite(stock: Stock) = stockDao.update(stock)

    private suspend fun updateStockPrice(ticker: String) = coroutineScope {
        val quoteData = async { service.getQuoteData(ticker) }
        val stock = stockDao.getStock(ticker)
        with(quoteData.await()) {
            return@coroutineScope stock.copy(currentPrice = c, openPrice = o)
        }
    }

    private suspend fun getStockData(ticker: String) = coroutineScope {
        val quoteData = async { service.getQuoteData(ticker) }
        val companyData = async { service.getCompanyProfile(ticker) }

        companyData.await().let { company ->
            quoteData.await().let { quote ->
                return@coroutineScope Stock(
                    ticker = ticker, companyName = company.name, companyLogo = company.logo,
                    webUrl = company.weburl, country = company.country, currency = company.currency,
                    currentPrice = quote.c, openPrice = quote.o, isFavorite = false
                )
            }
        }
    }

    companion object {
        private val TAG = "${Repository::class.java.simpleName}_TAG"
    }

}

