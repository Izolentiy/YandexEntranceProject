package com.example.entranceproject.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickers_table")
data class Ticker(
    @PrimaryKey
    val symbol: String,
    val exchange: String
)