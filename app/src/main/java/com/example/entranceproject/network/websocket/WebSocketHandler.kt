package com.example.entranceproject.network.websocket

import android.util.Log
import com.example.entranceproject.data.StockDatabase
import com.example.entranceproject.network.FinnhubService.Companion.FINNHUB_KEY
import com.example.entranceproject.network.FinnhubService.Companion.WEB_SOCKET_URL
import com.example.entranceproject.network.model.WebSocketMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.*
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class WebSocketHandler @Inject constructor(
    private val socketClient: OkHttpClient,
    private val database: StockDatabase,
    private val gson: Gson
) : WebSocketListener() {
    private val stockDao = database.stockDao()

    // To manage update coroutine lifecycle
    private var updateJob: Job? = null
    private var updateScope: CoroutineScope? = null

    private val listeningTickers = mutableSetOf<String>()
    private var webSocket: WebSocket? = null

    private var _webSocketIsOpened = false
    val webSocketIsOpened get() = _webSocketIsOpened

    /*private val _webSocketMessages = MutableSharedFlow<WebSocketMessage>()
    val webSocketMessages get() = webSocketMessages.asSharedFlow()*/

    fun openSocket() {
        val request = Request.Builder().url("$WEB_SOCKET_URL?token=$FINNHUB_KEY").build()
        webSocket = socketClient.newWebSocket(request, this)
        socketClient.dispatcher.executorService.shutdown()
    }

    fun closeSocket() {
        try {
            webSocket?.close(NORMAL_CLOSURE_STATUS, null)
            webSocket = null
        } catch (exception: Exception) {
            Log.e(TAG, "stopSocket: $exception")
        }
    }

    suspend fun setSubscription(tickersToListen: StateFlow<List<String>>) {
        updateJob = Job()
        updateScope = CoroutineScope(coroutineContext + updateJob!!)
        Log.d(TAG, "setSubscription: updateScope = $updateScope")
        updateScope!!.launch {  }
        tickersToListen.onStart {
            while (!_webSocketIsOpened)
                delay(200)
        }.onEach { tickers ->
            Log.d(TAG, "setSubscription: subscriptions: $listeningTickers")
            listeningTickers.filter { !tickers.contains(it) }.forEach { unsubscribeFrom(it) }
            tickers.filter { !listeningTickers.contains(it) }.forEach { subscribeTo(it) }
            Log.d(TAG, "setSubscription: subscriptions: $listeningTickers")
        }.onCompletion {
            Log.d(TAG, "setSubscription: onCompletion")
            // reversed() used to avoid ConcurrentModificationException
            listeningTickers.reversed().forEach { unsubscribeFrom(it) }
        }.launchIn(updateScope!!)
    }

    suspend fun cancelSubscription() { updateJob?.cancel() }

    private fun subscribeTo(ticker: String) {
        listeningTickers.add(ticker)
        val request = """{"type":"subscribe","symbol":"$ticker"}"""
        webSocket?.send(request)
        Log.d(TAG, "subscribeTo: $ticker, webSocket is null: ${webSocket == null}")
    }

    private fun unsubscribeFrom(ticker: String) {
        listeningTickers.remove(ticker)
        val request = """{"type":"unsubscribe","symbol":"$ticker"}"""
        webSocket?.send(request)
        Log.d(TAG, "unsubscribeFrom: $ticker, webSocket is null: ${webSocket == null}")
    }

    // Socket callbacks
    override fun onOpen(webSocket: WebSocket, response: Response) {
        _webSocketIsOpened = true
        Log.d(TAG, "onOpen: WEB SOCKET OPENED")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val socketUpdate = gson.fromJson(text, WebSocketMessage::class.java)
        updateScope?.launch {
            Log.d(TAG, "onMessage: $text")
            socketUpdate.listOfPrices?.forEach {
                val stock = stockDao.getStock(it.symbol)
                stockDao.update(stock.copy(
                    ticker = it.symbol,
                    currentPrice = it.price,
                    priceLastUpdated = it.timestamp
                ))
            }
        }
        /*updateScope?.launch {
            _sharedFlow.emit(socketUpdate)
            Log.d(TAG, "onMessage: emitted: $socketUpdate")
        }*/
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "onFailure: response = $response, error = $t")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        updateJob?.cancel()
        webSocket.close(NORMAL_CLOSURE_STATUS, reason)
        _webSocketIsOpened = false
        Log.d(TAG, "onClosing: reason = $reason, code = $code")
    }

    companion object {
        private val TAG = "${WebSocketListener::class.java.simpleName}_TAG"
        private const val NORMAL_CLOSURE_STATUS = 1000
    }
}

