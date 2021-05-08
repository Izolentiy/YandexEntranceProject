package com.example.entranceproject.repository

import android.util.Log
import com.example.entranceproject.data.StockDatabase
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.network.FinnhubService
import com.example.entranceproject.network.TRENDING_TICKERS
import com.example.entranceproject.ui.pager.Tab
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis

class Repository @Inject constructor(
    private val service: FinnhubService,
    private val database: StockDatabase
) {
    private val stockDao = database.stockDao()

    // Core methods
    fun getStocks(tab: Tab) = networkBoundResource(
        loadFromDb = { stockDao.getStocks(tab) },
        shouldFetch = { stocks -> stocks.isEmpty()
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

    fun searchStocks(query: String) = networkBoundResource(
        loadFromDb = { stockDao.searchStocks("%$query%") },
        shouldFetch = { query.isNotEmpty() },
        fetchData = { stocks ->
            val scope = CoroutineScope(coroutineContext)
            val tickersInLocal = stocks.map { it.ticker }
            var foundStocks = setOf<Stock>()

            val request = service.search(query)

            if (request.isSuccessful) {
                val startTime = System.currentTimeMillis()
                val response = request.body()?.result!!
                response.forEach { Log.d(TAG, "searchStocks: $it") }

                // Get stock data
                foundStocks = response.asFlow()
                    .filter { it.symbol != "" }
                    /*.filter { it.type == "Common Stock" || it.type == "GBR" }*/
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

                val time = startTime - System.currentTimeMillis()
                Log.d(TAG, log("searchStocks: QUERY=$query, SEARCH TOOK: $time ms"))
            }
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

    suspend fun receiveMinuteUpdates(tickers: StateFlow<List<String>>) {
        val scope = CoroutineScope(coroutineContext)
        val minuteSinceLastUpdate: (Long) -> Boolean = { lastUpdated ->
            val currTime = System.currentTimeMillis()
            with(TimeUnit.MILLISECONDS) { toMinutes(currTime) - toMinutes(lastUpdated) >= 1 }
        }
        tickers.filter { it.isNotEmpty() }.collect { tickersToListen ->
            val stocks = stockDao.getStocksByTicker(tickersToListen).first()
            val time = measureTimeMillis {
                stockDao.updateStocks(
                    stocks.asFlow()
                        .filter {
                            minuteSinceLastUpdate(it.priceLastUpdated) || it.currentPrice == 0.0
                        }
                        .map { scope.async { getStockPrice(it) } }
                        .flowOn(Dispatchers.IO)
                        .buffer()
                        .map { it.await() }
                        .flowOn(Dispatchers.Default)
                        .catch { emit(Stock("")) }
                        .filter { it.ticker != "" }
                        .toList()
                )
            }
            Log.d(TAG, log("subscribeTo: ${tickers.value}, UPDATE TOOK: $time ms"))
        }
    }

    // Helper methods
    suspend fun refreshData() { /*TODO("Implement data refreshing")*/}

    suspend fun updateFavorite(stock: Stock) = stockDao.update(stock)

    private suspend fun getStockPrice(stock: Stock): Stock {
        val quoteData = service.getQuoteData(stock.ticker)
        return if (quoteData.isSuccessful) with(quoteData.body()!!) {
            Log.d(TAG, log("GET_STOCK_PRICE: ${stock.ticker}"))
            stock.copy(
                currentPrice = c ?: 0.0,
                openPrice = o ?: 0.0,
                priceLastUpdated = System.currentTimeMillis()
            )
        } else Stock(ticker = "")
    }

    private suspend fun getStockData(ticker: String): Stock {
        val companyData = service.getCompanyProfile(ticker)
        return if (companyData.isSuccessful) with(companyData.body()!!) {
            Log.d(TAG, log("GET_COMPANY_DATA: $ticker"))
            Stock(
                ticker = this.ticker ?: "",
                companyName = name,
                companyLogo = logo,
                webUrl = weburl,
                country = country,
                currency = currency,
            )
        } else Stock(ticker = "")
    }

    companion object {
        private val TAG = "${Repository::class.java.simpleName}_TAG"
    }

    fun log(msg: String) = "[${Thread.currentThread().name}] $TAG --- $msg"

}

