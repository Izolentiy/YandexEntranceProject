package com.example.entranceproject.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "stocks_table",
    foreignKeys = [ForeignKey(
        entity = Ticker::class,
        parentColumns = ["symbol"],
        childColumns = ["ticker"]
    )],
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
    val isFavorite: Boolean = false
) {
    var dailyDelta = currentPrice - openPrice
}