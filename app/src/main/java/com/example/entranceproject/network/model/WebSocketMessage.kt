package com.example.entranceproject.network.model

// Socket message holder
data class WebSocketMessage(
    val listOfPrices: List<TickerPriceDto>?
) {
    data class TickerPriceDto(
        val type: String,
        val symbol: String,
        val price: Double,
        val volume: Double,
        val timestamp: Long
    )
}
