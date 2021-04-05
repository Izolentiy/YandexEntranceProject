package com.example.entranceproject.network.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class TickerDeserializer : JsonDeserializer<TickersDto> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): TickersDto {
        val result = json?.asJsonArray?.flatMap {
            val tickers = it.asJsonObject.get("quotes").asJsonArray
            tickers.map { item -> item.asString }
        } ?: emptyList()
        return TickersDto(result)
    }
}