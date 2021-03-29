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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        val gson = GsonBuilder()
            .registerTypeAdapter(TickersDto::class.java, TickerDeserializer())
            .setLenient()
            .create()
        return Retrofit.Builder()
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
    fun provideDatabase(
        app: Application,
        callback: StockDatabase.StockCallback
    ) = Room.databaseBuilder(app, StockDatabase::class.java, "stocks_table")
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()

    @Provides
    fun provideDao(db: StockDatabase) = db.stockDao()

    @Provides
    @ApplicationScope
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

}