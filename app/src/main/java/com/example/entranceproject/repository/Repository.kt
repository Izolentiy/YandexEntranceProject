package com.example.entranceproject.repository

import android.util.Log
import com.example.entranceproject.data.StockDatabase
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.data.model.Ticker
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
    private val tickerDao = database.tickerDao()
    private lateinit var currTickers: List<Ticker>
    init {
        getTickers()
    }

    // Fetch tickers
    fun getTickers() = networkBoundResource(
        loadFromDb = { tickerDao.getTickers() },
        shouldFetch = { tickers -> tickers.isEmpty() },
        fetchData = {
            /*withContext(Dispatchers.IO) {
                service.getMostWatchedTickers()
            }*/
            TRENDING_TICKERS.map { Ticker(it, "") }
        },
        saveFetchResult = { tickers -> tickerDao.insertTickers(tickers) }
    )

    // Fetch stocks info
    fun getStocks(tab: Tab) = networkBoundResource(
        loadFromDb = { stockDao.getStocks(tab) },
        shouldFetch = { stocks ->
            if (tab == Tab.FAVORITE) false
            else stocks.isEmpty()
        },
        fetchData = {
            Log.d(TAG, "networkBoundResource: fetchStocks")
            withContext(Dispatchers.IO) {
                // Get trending tickers from Mboum.com
                /*val tickers = service.getMostWatchedTickers().tickers*/
                val tickers = TRENDING_TICKERS
//                Log.e(TAG, "getStocks: $tickers")
                tickers.map { async { getStockData(it) } }.awaitAll()
            }
        },
        saveFetchResult = { stocks -> stockDao.insertStocks(stocks) },

    )

    // Fetch only prices
    fun updatePrices(tab: Tab, relevantTickers: List<String>) = networkBoundResource(
        loadFromDb = { stockDao.getStocks(tab) },
        shouldFetch = {
            tab != Tab.FAVORITE && relevantTickers.isNotEmpty()
        },
        fetchData = {
            Log.d(TAG, "networkBoundResource: fetchPrices")
            withContext(Dispatchers.IO) {
                relevantTickers.map { async { updateStockPrice(it) } }.awaitAll()
            }
        },
        saveFetchResult = { stocks -> stockDao.updateStocks(stocks) }
    )

    fun openSocket(): StateFlow<SocketUpdate> = webSocketHandler.openSocket()

    fun closeSocket() { webSocketHandler.closeSocket() }

    suspend fun searchStocks(query: String) = flow {
        Log.e(TAG, "searchStocks: state = START")
        try {
            Log.d(TAG, "searchStocks: state = LOADING")
            emit(Resource.loading(emptyList<Stock>()))

            val response = service.search(query).result
            emit(Resource.success(emptyList<Stock>()))
            TODO("Distinct found 'tickers' properly")
            val found = response
                .filter { it.type == "Common Stock" || it.type == "GDR" }
                .distinctBy { it.description }
            Log.d(TAG, "searchStocks: found = $found")

            val stocks = found
                .map { getStockData(it.symbol) }
                .filter { it.currentPrice != 0.0 }
            stockDao.insertStocks(stocks)

            Log.d(TAG, "searchStocks: state = SUCCESS")
            emit(Resource.success(stocks))
        } catch (exception: Throwable) {
            Log.e(TAG, "searchStocks: $exception")
            Log.d(TAG, "searchStocks: state = FAILURE")
            emit(Resource.error(emptyList<Stock>(), exception))
        }
    }

    suspend fun refreshData() {

    }

    suspend fun updateFavorite(stock: Stock) = stockDao.update(stock)

    private suspend fun getMostWatchedTickers() = coroutineScope {
        // Get trending tickers from Mboum.com
        val tickers = service.getMostWatchedTickers().tickers
    }

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
                    ticker = company.ticker, companyName = company.name, companyLogo = company.logo,
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

