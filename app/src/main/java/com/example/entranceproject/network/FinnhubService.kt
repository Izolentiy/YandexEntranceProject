package com.example.entranceproject.network

import com.example.entranceproject.BuildConfig
import com.example.entranceproject.network.model.*
import retrofit2.Response
import retrofit2.http.*

interface FinnhubService {
    companion object {
        const val WEB_SOCKET_URL = "wss://ws.finnhub.io"
        const val BASE_URL = "https://finnhub.io/api/v1/"
        const val LOGOS_URL = "https://logo.clearbit.com/"
        const val TOKEN_PARAMETER = "X-Finnhub-Token"
        const val FINNHUB_KEY = BuildConfig.FINNHUB_KEY
        const val MBOUM_KEY = BuildConfig.MBOUM_KEY
    }

    @Headers("$TOKEN_PARAMETER:$FINNHUB_KEY")
    @GET("search")
    suspend fun search(
        @Query("q") query: String
    ): Response<SearchResultDto>

    @Headers("$TOKEN_PARAMETER:$FINNHUB_KEY")
    @GET("stock/profile2")
    suspend fun getCompanyProfile(
        @Query("symbol") symbol: String
    ): Response<CompanyProfileDto>

    @Headers("$TOKEN_PARAMETER:$FINNHUB_KEY")
    @GET("quote")
    suspend fun getQuoteData(
        @Query("symbol") symbol: String
    ): Response<QuoteDto>

    @GET
    suspend fun getMostWatchedTickers(
        @Url url: String = "https://mboum.com/api/v1/tr/trending?apikey=$MBOUM_KEY"
    ): Response<TickersDto>

}
