package com.example.entranceproject.network

import com.example.entranceproject.network.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FinnhubService {
    companion object {
        const val BASE_URL = "https://finnhub.io/api/v1/"
        const val TOKEN_PARAMETER = "X-Finnhub-Token"
    }

    @GET("stock/symbol")
    suspend fun getStockList(
        @Query("exchange") exchange: String,
        @Header(TOKEN_PARAMETER) apiKey: String
    ): Response<TickersDto>

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Header(TOKEN_PARAMETER) apiKey: String
    ): Response<SearchResultDto>

    @GET("stock/profile2")
    suspend fun getCompanyProfile(
        @Query("symbol") symbol: String,
        @Header(TOKEN_PARAMETER) apiKey: String
    ): Response<CompanyProfile2Dto>

    @GET("quote")
    suspend fun getQuoteData(
        @Query("symbol") symbol: String,
        @Header(TOKEN_PARAMETER) apiKey: String
    ): Response<QuoteDto>

}
