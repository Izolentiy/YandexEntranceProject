package com.example.entranceproject.di

import android.app.Application
import androidx.room.Room
import com.example.entranceproject.data.StockDatabase
import com.example.entranceproject.network.FinnhubService
import com.example.entranceproject.network.model.TickerDeserializer
import com.example.entranceproject.network.model.TickersDto
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
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(39, TimeUnit.SECONDS)
//            .hostnameVerifier { _,_ -> true }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val gson = GsonBuilder()
            .registerTypeAdapter(TickersDto::class.java, TickerDeserializer())
            .setLenient()
            .create()
        return Retrofit.Builder()
            .client(client)
            .baseUrl(FinnhubService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

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