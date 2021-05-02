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
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis

class Repository @Inject constructor(
    private val service: FinnhubService,
    private val database: StockDatabase,
    private val webSocketHandler: WebSocketHandler
) {
    private val stockDao = database.stockDao()

    // Fetch stocks info
    fun getStocks(tab: Tab) = networkBoundResource(
        loadFromDb = { stockDao.getStocks(tab) },
        shouldFetch = { stocks ->
            if (tab == Tab.FAVORITE) false
            else stocks.isEmpty()
        },
        fetchData = {
            val scope = CoroutineScope(coroutineContext)
            var stocks = listOf<Stock>()
            val time = measureTimeMillis {
                stocks = TRENDING_TICKERS.asFlow()
                    .map { scope.async { getStockData(it) } }
                    .flowOn(Dispatchers.IO)
                    .buffer()
                    .map { it.await() }
                    .flowOn(Dispatchers.Default)
                    .toList()
            }
            Log.d(TAG, log("getStocks: STOCK DATA FETCHING TOOK $time ms"))
            stocks
        },
        saveFetchResult = { stocks -> stockDao.insertStocks(stocks) }
    )

    // Update stock prices
    fun updatePrices(tickers: List<String>) = networkBoundResource(
        loadFromDb = { stockDao.getStocksByTicker(tickers) },
        shouldFetch = { loadedTickers -> loadedTickers.isNotEmpty() || tickers.isNotEmpty() },
        fetchData = { stocks ->
            Log.d(TAG, log("UPDATE_STOCKS: ${stocks.map { stock -> stock.ticker }}"))
            withContext(Dispatchers.IO) {
                /*tickers.map { async { updateStockPrice(it) } }.awaitAll()*/
                stocks.map { async { getStockPrice(it) } }.awaitAll()
            }
        },
        saveFetchResult = { stocks -> stockDao.updateStocks(stocks) }
    )

    // Search for stocks
    fun searchStocks(query: String) = networkBoundResource(
        loadFromDb = { stockDao.searchStocks("%$query%") },
        shouldFetch = { query.isNotEmpty() },
        fetchData = { stocks ->
            val scope = CoroutineScope(coroutineContext)
            val tickersInLocal = stocks.map { it.ticker }
            var foundStocks = setOf<Stock>()

            val response = service.search(query).result
            response.forEach { Log.d(TAG, "searchStocks: $it") }

            val time = measureTimeMillis {
                // Get stock data
                foundStocks = response.asFlow()
                    .filter { it.symbol != "" }
                    .filter { !tickersInLocal.contains(it.symbol) }
                    .flowOn(Dispatchers.Default)
                    .map { scope.async { getStockData(it.symbol) } }
                    .flowOn(Dispatchers.IO)
                    .buffer()
                    .map { it.await() }
                    .flowOn(Dispatchers.Default)
                    .catch { emit(Stock("")) }
                    .toSet()

                // Get stock prices
                foundStocks = foundStocks.asFlow()
                    .filter { it.ticker != "" }
                    .filter { !tickersInLocal.contains(it.ticker) }
                    .flowOn(Dispatchers.Default)
                    .map { scope.async { getStockPrice(it) } }
                    .flowOn(Dispatchers.IO)
                    .buffer()
                    .map { it.await() }
                    .filter { it.currentPrice != 0.0 || it.dailyDelta != 0.0 }
                    .flowOn(Dispatchers.Default)
                    .catch { emit(Stock("")) }
                    .toSet()
            }
            Log.d(TAG, log("searchStocks: QUERY=$query, SEARCH TOOK: $time ms"))
            foundStocks
        },
        saveFetchResult = { stocks ->
            val tickers = stocks.map { it.ticker }
            val inLocal = stockDao.getStocksByTicker(tickers).first().map { it.ticker }
            val fromNet = tickers.filter { ticker -> ticker !in inLocal }

            Log.d(TAG, "searchStocks: SAVE_FETCH_RESULT --- ALL_FOUND   $tickers")
            Log.d(TAG, "searchStocks: SAVE_FETCH_RESULT --- FOUND_IN_DB $inLocal")
            Log.d(TAG, "searchStocks: SAVE_FETCH_RESULT --- NEW         $fromNet")

            stockDao.insertStocks(stocks.filter { it.ticker in fromNet })
            stockDao.updateStocks(stocks.filter { it.ticker in inLocal })
        }
    )

    // Methods to manage WebSocket
    fun openSocket() { webSocketHandler.openSocket() }

    fun closeSocket() { webSocketHandler.closeSocket() }

    suspend fun getSubscription(tickers: StateFlow<List<String>>): StateFlow<SocketUpdate> {
        webSocketHandler.setSubscription(tickers)
        return webSocketHandler.events
    }

    // ---
    suspend fun refreshData() { /*TODO("Implement data refreshing")*/}

    suspend fun updateFavorite(stock: Stock) = stockDao.update(stock)

    // Helper methods
    private suspend fun getStockPrice(stock: Stock): Stock {
        val quoteData = service.getQuoteData(stock.ticker)
        with(quoteData) {
            Log.d(TAG, log("GET_STOCK_PRICE: ${stock.ticker}"))
            return stock.copy(currentPrice = c ?: 0.0, openPrice = o ?: 0.0)
        }
    }

    private suspend fun getStockData(ticker: String): Stock {
        val companyData = service.getCompanyProfile(ticker)
        with(companyData) {
            Log.d(TAG, log("GET_COMPANY_DATA: $ticker"))
            return Stock(
                ticker = this.ticker ?: "",
                companyName = name,
                companyLogo = logo,
                webUrl = weburl,
                country = country,
                currency = currency,
            )
        }
    }

    companion object {
        private val TAG = "${Repository::class.java.simpleName}_TAG"
    }

    fun log(msg: String) = "[${Thread.currentThread().name}] $TAG --- $msg"

}

