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
            Log.d(TAG, "networkBoundResource: fetchStocks")
            withContext(Dispatchers.IO) {
                val tickers = TRENDING_TICKERS
                Log.e(TAG, "getStocks: $tickers")
                tickers.map { async { getStockData(it) } }.awaitAll()
            }
        },
        saveFetchResult = { stocks -> stockDao.insertStocks(stocks) },

    )

    // Update stock prices
    fun updatePrices(tickers: List<String>) = networkBoundResource(
        loadFromDb = { stockDao.getStocksByTickers(tickers) },
        shouldFetch = { loadedTickers -> loadedTickers.isNotEmpty() || tickers.isNotEmpty() },
        fetchData = { stocks ->
            Log.d(TAG, "networkBoundResource: fetchPrices")
            withContext(Dispatchers.IO) {
                /*tickers.map { async { updateStockPrice(it) } }.awaitAll()*/
                stocks.map { async { updateStockPrice(it) } }.awaitAll()
            }
        },
        saveFetchResult = { stocks -> stockDao.updateStocks(stocks) }
    )

    // Search for stocks
    fun searchStocks(query: String) = networkBoundResource(
        loadFromDb = { stockDao.searchStocks("$query%") },
        shouldFetch = { query.isNotEmpty() },
        fetchData = {
            val supervisorJob = SupervisorJob()
            with(CoroutineScope(coroutineContext + supervisorJob)) {
                Log.d(TAG, "searchStocks: $query")
                val response = service.search(query).result
                val found = response
                    .filter { it.type == "Common Stock" || it.type == "GDR" }
                    .distinctBy { it.displaySymbol }
                    .distinctBy { it.description }
                Log.d(TAG, "searchStocks: found = ${found.map { "\n$it" }}")

                val stocks = mutableListOf<Stock>()
                found.forEach { stocks.add(getStockData(it.symbol)) }
                Log.d(TAG, "searchStocks: $stocks")
                stocks
            }
        },
        saveFetchResult = { stocks -> stockDao.insertStocks(stocks) }
    )

    fun openSocket(): StateFlow<SocketUpdate> = webSocketHandler.openSocket()

    fun closeSocket() { webSocketHandler.closeSocket() }

    suspend fun refreshData() {

    }

    suspend fun updateFavorite(stock: Stock) = stockDao.update(stock)

    private suspend fun updateStockPrice(stock: Stock) = coroutineScope {
        val quoteData = async { service.getQuoteData(stock.ticker) }
        with(quoteData.await()) {
            return@coroutineScope stock.copy(currentPrice = c, openPrice = o)
        }
    }

    private suspend fun getStockData(ticker: String) = coroutineScope {
        val quoteData = async { service.getQuoteData(ticker) }
        val companyData = async { service.getCompanyProfile(ticker) }

        return@coroutineScope try {
            companyData.await().let { company ->
                quoteData.await().let { quote ->
                    Log.d(TAG, "getStockData: TICKER = $ticker")
                    return@coroutineScope Stock(
                        ticker = company.ticker,
                        companyName = company.name,
                        companyLogo = company.logo,
                        webUrl = company.weburl,
                        country = company.country,
                        currency = company.currency,
                        currentPrice = quote.c,
                        openPrice = quote.o,
                        isFavorite = false
                    )
                }
            }
        } catch (exception: Exception) {
            Log.e(TAG, "getStockData: ticker = $ticker, error = $exception")
            Stock(ticker)
        }
    }

    companion object {
        private val TAG = "${Repository::class.java.simpleName}_TAG"
    }

}

