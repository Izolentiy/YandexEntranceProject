package com.example.entranceproject.repository

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.network.FinnhubService
import retrofit2.HttpException

class StockPagingSource(
    private val service: FinnhubService,
    private val tickers: List<String>
) : PagingSource<Int, Stock>() {
    override fun getRefreshKey(state: PagingState<Int, Stock>): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Stock> {
        TODO("Not yet implemented")
    }
}