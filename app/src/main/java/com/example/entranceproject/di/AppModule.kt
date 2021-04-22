package com.example.entranceproject.di

import android.app.Application
import androidx.room.Room
import com.example.entranceproject.data.StockDatabase
import com.example.entranceproject.network.FinnhubService
import com.example.entranceproject.network.model.TickerPriceDeserializer
import com.example.entranceproject.network.model.TickersDeserializer
import com.example.entranceproject.network.model.TickerPriceDto
import com.example.entranceproject.network.model.TickersDto
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(39, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(TickersDto::class.java, TickersDeserializer())
        .registerTypeAdapter(TickerPriceDto::class.java, TickerPriceDeserializer())
        .setLenient()
        .create()

    @Provides
    @Singleton
    fun provideRetrofit(
        gson: Gson,
        client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(FinnhubService.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideService(retrofit: Retrofit): FinnhubService =
        retrofit.create(FinnhubService::class.java)

    @Provides
    @Singleton
    fun provideDatabase(app: Application) =
        Room.databaseBuilder(app, StockDatabase::class.java, "stocks_table")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @ApplicationScope
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

}