package com.example.entranceproject.network.model

// To use CompanyProfile request you need to have premium
data class CompanyProfileDto(
    val address: String,
    val city: String,
    val country: String,
    val currency: String,
    val cusip: String,
    val description: String,
    val employeeTotal: String,
    val exchange: String,
    val finnhubIndustry: String,
    val ggroup: String,
    val gind: String,
    val gsector: String,
    val gsubind: String,
    val ipo: String,
    val isin: String,
    val logo: String,
    val marketCapitalization: Double,
    val naics: String,
    val naicsNationalIndustry: String,
    val naicsSector: String,
    val naicsSubsector: String,
    val name: String,
    val phone: String,
    val sedol: String,
    val shareOutstanding: Double,
    val state: String,
    val ticker: String,
    val weburl: String
)