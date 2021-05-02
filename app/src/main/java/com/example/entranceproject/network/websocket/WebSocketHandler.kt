package com.example.entranceproject.network.websocket

import android.util.Log
import com.example.entranceproject.network.FinnhubService.Companion.FINNHUB_KEY
import com.example.entranceproject.network.FinnhubService.Companion.WEB_SOCKET_URL
import com.example.entranceproject.network.model.TickerPriceDto
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import javax.inject.Inject

class WebSocketHandler @Inject constructor(
    private val socketClient: OkHttpClient,
    private val gson: Gson
) : WebSocketListener() {

    // To manage update coroutine lifecycle
    private val updateJob = Job()

    /*private val tickers = mutableListOf(
        "GME",
        "AMC",
        "PLTR",
        "FB",
        "NIO",
        "TSLA",
        "MARA"
    )*/

    private val subscriptions = mutableListOf<String>()
//    private var listeningTickers: StateFlow<List<String>>? = null
    private val _events = MutableStateFlow(SocketUpdate())
    private var webSocket: WebSocket? = null

    val events get() = _events.asStateFlow()
    /*val events
        get() = _events.map {
            Log.d(TAG, "new message: ----")
            gson.fromJson(it.text, TickerPriceDto::class.java)
        }*/

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
//        listeningTickers = tickersToListen
//        listeningTickers?.collect { tickers ->
        tickersToListen.collect { tickers ->
            Log.d(TAG, "setSubscription: subscriptions: $subscriptions")
            subscriptions.filter { !tickers.contains(it) }.forEach { unsubscribeFrom(it) }
            tickers.filter { !subscriptions.contains(it) }.forEach { subscribeTo(it) }
            Log.d(TAG, "setSubscription: subscriptions: $subscriptions")
        }
    }

    private fun subscribeTo(ticker: String) {
        subscriptions.add(ticker)
        val request = """{"type":"subscribe","symbol":"$ticker"}"""
        webSocket!!.send(request)
        Log.d(TAG, "subscribeTo: $ticker")
    }

    private fun unsubscribeFrom(ticker: String) {
        subscriptions.remove(ticker)
        val request = """{"type":"unsubscribe","symbol":"$ticker"}"""
        webSocket!!.send(request)
        Log.d(TAG, "unsubscribeFrom: $ticker")
    }

    // Socket callbacks
    override fun onOpen(webSocket: WebSocket, response: Response) {
//        tickers.onEach { ticker ->
//            webSocket.send("""{"type":"subscribe","symbol":"$ticker"}""")
//        }
        val ticker = "APPL"
        webSocket.send("""{"type":"subscribe","symbol":"$ticker"}""")
        webSocket.send("""{"type":"subscribe","symbol":"AMZN"}""")
        Log.d(TAG, "onOpen: subscribedTo: $ticker, AMZN")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        _events.value = SocketUpdate(text)
        Log.d(TAG, "onMessage: WebSocket message received ")
        Log.d(TAG, "onMessage: message value = ${_events.value}")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.d(TAG, "onFailure: ${t.message}")
        _events.value = SocketUpdate(error = t)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "onClosing: $reason")
        _events.value = SocketUpdate(error = SocketAbortedException())
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

