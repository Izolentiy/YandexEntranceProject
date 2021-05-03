package com.example.entranceproject.network.websocket

import android.util.Log
import com.example.entranceproject.data.StockDatabase
import com.example.entranceproject.network.FinnhubService.Companion.FINNHUB_KEY
import com.example.entranceproject.network.FinnhubService.Companion.WEB_SOCKET_URL
import com.example.entranceproject.network.model.WebSocketMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

@Singleton
class WebSocketHandler @Inject constructor(
    private val socketClient: OkHttpClient,
    private val database: StockDatabase,
    private val gson: Gson
) : WebSocketListener() {
    private val stockDao = database.stockDao()

    // To manage update coroutine lifecycle
    private val updateJob = Job()
    private var updateScope: CoroutineScope? = null

    private val listeningTickers = mutableListOf<String>()
    private var webSocket: WebSocket? = null

    // There I tried to use shared flows but it didn't work.
    /*private val _sharedFlow = MutableSharedFlow<SocketUpdate>()
    val sharedFlow
        get() = _sharedFlow.asSharedFlow()
            .filter { it.error == null }
            .map { gson.fromJson(it.text, WebSocketMessage::class.java) }

    private val _events = MutableStateFlow(SocketUpdate())
    val events get() = _events.asStateFlow()*/

    fun openSocket() {
        val request = Request.Builder().url("$WEB_SOCKET_URL?token=$FINNHUB_KEY").build()
        webSocket = socketClient.newWebSocket(request, this)
        socketClient.dispatcher().executorService().shutdown()
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
        updateScope = CoroutineScope(coroutineContext + updateJob)
        tickersToListen.collect { tickers ->
            Log.d(TAG, "setSubscription: subscriptions: $listeningTickers")
            listeningTickers.filter { !tickers.contains(it) }.forEach { unsubscribeFrom(it) }
            tickers.filter { !listeningTickers.contains(it) }.forEach { subscribeTo(it) }
            Log.d(TAG, "setSubscription: subscriptions: $listeningTickers")
        }
    }

    suspend fun cancelSubscription() { updateJob.cancel() }

    private fun subscribeTo(ticker: String) {
        listeningTickers.add(ticker)
        val request = """{"type":"subscribe","symbol":"$ticker"}"""
        webSocket!!.send(request)
        Log.d(TAG, "subscribeTo: $ticker")
    }

    private fun unsubscribeFrom(ticker: String) {
        listeningTickers.remove(ticker)
        val request = """{"type":"unsubscribe","symbol":"$ticker"}"""
        webSocket!!.send(request)
        Log.d(TAG, "unsubscribeFrom: $ticker")
    }

    // Socket callbacks
    override fun onOpen(webSocket: WebSocket, response: Response) {
        listeningTickers.forEach { ticker ->
            webSocket.send("""{"type":"subscribe","symbol":"$ticker"}""")
        }
//        updateScope?.launch { testSharedFlow.emit("---- opened") }
        Log.d(TAG, "onOpen: WEB SOCKET OPENED")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "onMessage: $text")
        val socketUpdate = gson.fromJson(text, WebSocketMessage::class.java)
        updateScope?.launch {
            socketUpdate.listOfPrices?.forEach {
                val stock = stockDao.getStock(it.symbol)
                stockDao.update(stock.copy(
                    ticker = it.symbol,
                    currentPrice = it.price,
                    priceLastUpdated = it.timestamp
                ))
                Log.d(TAG, "onMessage: price updated")
            }
        }

//        _events.value = SocketUpdate(text)
//        updateScope?.launch { _sharedFlow.emit(SocketUpdate(text)) }
//        _sharedFlow.tryEmit(SocketUpdate(text))
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.d(TAG, "onFailure: ${t.message}")

//        _events.value = SocketUpdate(error = t)
//        updateScope?.launch { _sharedFlow.emit(SocketUpdate(error = t)) }
//        _sharedFlow.tryEmit(SocketUpdate(error = t))
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "onClosing: $reason")

//        _events.value = SocketUpdate(error = SocketAbortedException())
//        updateScope?.launch { _sharedFlow.emit(SocketUpdate(error = SocketAbortedException())) }
//        _sharedFlow.tryEmit(SocketUpdate(error = SocketAbortedException()))
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
    }

    companion object {
        private val TAG = "${WebSocketListener::class.java.simpleName}_TAG"
        private const val NORMAL_CLOSURE_STATUS = 1000
    }
}

