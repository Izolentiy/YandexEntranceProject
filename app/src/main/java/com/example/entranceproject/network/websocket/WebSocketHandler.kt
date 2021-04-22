package com.example.entranceproject.network.websocket

import android.util.Log
import com.example.entranceproject.network.FinnhubService.Companion.FINNHUB_KEY
import com.example.entranceproject.network.FinnhubService.Companion.WEB_SOCKET_URL
import kotlinx.coroutines.flow.*
import okhttp3.*
import okhttp3.WebSocketListener
import javax.inject.Inject

class WebSocketHandler @Inject constructor(
    private val socketClient: OkHttpClient
) : WebSocketListener() {

    // later should be replaced with reactive list
    private val _listeningTickers = mutableListOf(
        "ETSY",
        "RBLX",
        "ZOM",
        "SQ",
        "WMT",
        "RIOT",
        "VIAC"
    )
    private val _events = MutableStateFlow(SocketUpdate())
    private var webSocket: WebSocket? = null

    fun openSocket(): StateFlow<SocketUpdate> {
        webSocket = socketClient.newWebSocket(
            Request.Builder().url("$WEB_SOCKET_URL?token=$FINNHUB_KEY").build(), this
        )
        socketClient.dispatcher().executorService().shutdown()
        return _events.asStateFlow()
    }

    fun closeSocket() {
        try {
            webSocket?.close(NORMAL_CLOSURE_STATUS, null)
            webSocket = null
        } catch (exception: Exception) {
            Log.e(TAG, "stopSocket: $exception")
        }
    }

    private fun subscribeTo(ticker: String) {
        val request = """{"type":"subscribe","symbol":"$ticker"}"""
        webSocket!!.send(request)
        Log.d(TAG, "subscribeTo: $ticker")
    }

    private fun unsubscribeFrom(ticker: String) {
        val request = "{'type':'unsubscribe', 'symbol':$ticker}"
        webSocket!!.send(request)
        Log.d(TAG, "unsubscribeFrom: $ticker")
    }

    // Socket callbacks
    override fun onOpen(webSocket: WebSocket, response: Response) {
        _listeningTickers.forEach { ticker ->
            subscribeTo(ticker)
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        _events.value = SocketUpdate(text)
        Log.d(TAG, "onMessage: WebSocket message received ")
        Log.d(TAG, "onMessage: message value = ${_events.value}")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        _events.value = SocketUpdate(error = t)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
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

