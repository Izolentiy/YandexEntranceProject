package com.example.entranceproject.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stocks_table")
data class Stock(
    @PrimaryKey
    val ticker: String,
    val companyLogo: String? = "",
    val companyName: String = "",
    val currency: String = "",
    var currentPrice: Double = 0.0,
    var dailyDelta: Double = 0.0,
    var isFavorite: Boolean = false
)