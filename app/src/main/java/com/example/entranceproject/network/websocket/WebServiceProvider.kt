package com.example.entranceproject.network.websocket

import android.util.Log
import com.example.entranceproject.network.FinnhubService.Companion.FINNHUB_KEY
import com.example.entranceproject.network.FinnhubService.Companion.WEB_SOCKET_URL
import kotlinx.coroutines.channels.Channel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import javax.inject.Inject

class WebServiceProvider @Inject constructor(
    private val socketClient: OkHttpClient
) {
    private var webSocket: WebSocket? = null
    private var webSocketListener: WebSocketListener? = null

    fun startSocket(): Channel<SocketUpdate> =
        with(WebSocketListener()) {
            setupSocket(this)
            socketEventChannel
        }

    fun stopSocket() {
        try {
            webSocket?.close(NORMAL_CLOSURE_STATUS, null)
            webSocket = null
            webSocketListener?.socketEventChannel?.close()
            webSocketListener = null
        } catch (exception: Exception) {
            Log.d("SOCKET_TAG", "stopSocket: ${exception.message}")
        }
    }

    private fun setupSocket(listener: WebSocketListener) {
        webSocketListener = listener
        webSocket = socketClient.newWebSocket(
            Request.Builder().url("$WEB_SOCKET_URL?token=$FINNHUB_KEY").build(),
            webSocketListener!!
//            webSocketListener
        )
//        socketClient.dispatcher.executorService.shutdown()
        socketClient.dispatcher().executorService().shutdown()
    }

    companion object {
        const val NORMAL_CLOSURE_STATUS = 1000
    }
}