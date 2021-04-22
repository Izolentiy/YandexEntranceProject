package com.example.entranceproject.network

import com.example.entranceproject.network.FinnhubService.Companion.BASE_URL
import com.example.entranceproject.network.FinnhubService.Companion.FINNHUB_KEY
import com.example.entranceproject.network.FinnhubService.Companion.TOKEN_PARAMETER
import okhttp3.Interceptor
import okhttp3.Response

class AuthTokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val inRequest = chain.request()
        val outRequest = inRequest.newBuilder()
            .addHeader(TOKEN_PARAMETER, FINNHUB_KEY)
            .build()
        return chain.proceed(outRequest)
    }
}
