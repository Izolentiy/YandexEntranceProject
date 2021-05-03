package com.example.entranceproject.network.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

// Socket message deserializer
class WebSocketMessageDeserializer : JsonDeserializer<WebSocketMessage> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): WebSocketMessage {
        val result = json?.asJsonObject
        return WebSocketMessage(
            result?.get("data")?.asJsonArray?.map { data ->
                with(data.asJsonObject) {
                    WebSocketMessage.TickerPriceDto(
                        type = result.get("type").asString,
                        symbol = get("s").asString,
                        price = get("p").asDouble,
                        volume = get("v").asDouble,
                        timestamp = get("t").asLong
                    )
                }
            })
    }
}

// This is how json message from WebSocket looks like
/*
{
    "data": [
    {
        "p": 7296.89,
        "s": "BINANCE:BTCUSDT",
        "t": 1575526691134,
        "v": 0.011467
    }
    ],
    "type": "trade"
}
*/
