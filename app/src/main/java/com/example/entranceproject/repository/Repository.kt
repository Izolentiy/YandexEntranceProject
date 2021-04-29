package com.example.entranceproject.repository

import android.util.Log
import com.example.entranceproject.data.StockDatabase
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.network.FinnhubService
import com.example.entranceproject.network.TRENDING_TICKERS
import com.example.entranceproject.network.model.SearchResultDto
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
            Log.d(TAG, log("GET_STOCKS: ${it.map { stock -> stock.ticker }}"))
            withContext(Dispatchers.IO) {
                val tickers = TRENDING_TICKERS
                tickers.map { async { getStockData(it) } }.awaitAll()
            }
        },
        saveFetchResult = { stocks -> stockDao.insertStocks(stocks) },

        )

    // Update stock prices
    fun updatePrices(tickers: List<String>) = networkBoundResource(
        loadFromDb = { stockDao.getStocksByTicker(tickers) },
        shouldFetch = { loadedTickers -> loadedTickers.isNotEmpty() || tickers.isNotEmpty() },
        fetchData = { stocks ->
            Log.d(TAG, log("UPDATE_STOCKS: ${stocks.map { stock -> stock.ticker }}"))
            withContext(Dispatchers.IO) {
                /*tickers.map { async { updateStockPrice(it) } }.awaitAll()*/
                stocks.map { async { updateStockPrice(it) } }.awaitAll()
            }
        },
        saveFetchResult = { stocks -> stockDao.updateStocks(stocks) }
    )

    // Search for stocks
    fun searchStocks(query: String) = networkBoundResource(
        loadFromDb = { stockDao.searchStocks("%$query%") },
        shouldFetch = { query.isNotEmpty() },
        fetchData = { stocks ->
            Log.d(TAG, log("searchStocks: QUERY=$query"))
            val foundStocks = mutableListOf<Stock>()
            val invalidTickers = mutableListOf<String>()

            val response = testSearch(query).result
                .asFlow()
                .filter { !invalidTickers.contains(it.symbol) }
                .filter { !foundStocks.map { stock -> stock.ticker }.contains(it.symbol) }
                .onEach {
                    Log.d(TAG, log("searchStocks: TICKER=${it.symbol}"))
                    invalidTickers.add(it.symbol)
                    foundStocks.add(getStockData(it.symbol))
                    invalidTickers.remove(it.symbol)
                }
                .retryWhen { cause, attempt ->
                    if (cause is retrofit2.HttpException) {
                        Log.d(TAG, "searchStocks: RETRY. ATTEMPT=$attempt")
                        return@retryWhen true
                    } else {
                        false
                    }
                }
                .catch { error ->
                    if (error is retrofit2.HttpException) println("Woo roo roo")
                }
                .toList()
            response.forEach { Log.d(TAG, "searchStocks: $it") }
            foundStocks.forEach { Log.d(TAG, "searchStocks: $it") }
            foundStocks.filter { it.currentPrice != 0.0 && it.dailyDelta != 0.0}
        },
        saveFetchResult = { stocks ->
            stockDao.insertStocks(stocks)
        }
    )

    fun openSocket(): StateFlow<SocketUpdate> = webSocketHandler.openSocket()

    fun closeSocket() {
        webSocketHandler.closeSocket()
    }

    suspend fun testSearch(query: String):  SearchResultDto {
        Log.d(TAG, "testSearch: ------ SEARCH")
        return service.search(query)
    }

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
                    Log.d(TAG, log("GET_STOCK_DATA: $ticker"))
//                    return@coroutineScope Stock(
                    Stock(
                        ticker = company.ticker,
                        companyName = company.name,
                        companyLogo = company.logo,
                        webUrl = company.weburl,
                        country = company.country,
                        currency = company.currency,
                        currentPrice = quote.c,
                        openPrice = quote.o
                    )
                }
            }
        } catch (exception: Exception) {
            log("GET_STOCK_DATA: $ticker, $exception")
//            Log.e(TAG, "getStockData: ticker = $ticker, error = $exception")
            Stock(ticker)
        }
    }

    companion object {
        private val TAG = "${Repository::class.java.simpleName}_TAG"
    }

    fun log(msg: String) = "[${Thread.currentThread().name}] $TAG --- $msg"

}

