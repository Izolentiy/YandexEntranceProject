package com.example.entranceproject.network.model

// Socket message holder
data class TickerPriceDto(
    val type: String?,
    val symbol: String?,
    val price: Double?
)
