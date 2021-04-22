package com.example.entranceproject.network.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

// Socket message deserializer
class TickerPriceDeserializer : JsonDeserializer<TickerPriceDto> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): TickerPriceDto {
        val result = json?.asJsonObject
        val data = result?.get("data")?.asJsonArray?.get(0)?.asJsonObject

        val type = result?.get("type")?.asString
        val symbol = data?.get("s")?.asString
        val price =  data?.get("p")?.asDouble
        return TickerPriceDto(type, symbol, price)
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
