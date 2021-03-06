package com.example.entranceproject.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.lang.System.currentTimeMillis

@Entity(
    tableName = "stocks_table",
    primaryKeys = ["ticker"]
)
data class Stock(
    val ticker: String,
    val companyName: String? = "",
    val companyLogo: String? = "",
    val webUrl: String? = "",
    val country: String? = "",
    val currency: String? = "",
    val currentPrice: Double = 0.0,
    val openPrice: Double = 0.0,
    val isFavorite: Boolean = false,
    val priceLastUpdated: Long = currentTimeMillis()
) {
    var dailyDelta = currentPrice - openPrice
}