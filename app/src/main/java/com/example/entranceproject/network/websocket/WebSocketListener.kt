package com.example.entranceproject.network.websocket

import android.util.Log
import com.example.entranceproject.network.websocket.WebServiceProvider.Companion.NORMAL_CLOSURE_STATUS
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketListener : WebSocketListener() {
    val socketEventChannel: Channel<SocketUpdate> = Channel(10)

    override fun onOpen(webSocket: WebSocket, response: Response) {
        webSocket.send("""{"type":"subscribe","symbol":"AAPL"}""")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        GlobalScope.launch { socketEventChannel.send(SocketUpdate(text)) }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        GlobalScope.launch { socketEventChannel.send(SocketUpdate(exception = t)) }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        GlobalScope.launch {
            socketEventChannel.send(SocketUpdate(exception = SocketAbortedException()))
        }
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
        socketEventChannel.close()
    }
}

