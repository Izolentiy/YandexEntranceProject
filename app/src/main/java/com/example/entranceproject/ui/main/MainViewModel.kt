package com.example.entranceproject.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.entranceproject.BuildConfig
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.network.FinnhubService
import com.example.entranceproject.network.model.CompanyProfileDto
import com.example.entranceproject.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val finnhubService: FinnhubService,
    val repository: Repository
): ViewModel() {

    private val _index = MutableLiveData<Int>()
    private var stocksLiveData = MutableLiveData<List<Stock>>()
    val stocks: LiveData<List<Stock>> = stocksLiveData

    fun getStocks() =
        viewModelScope.launch {
             stocksLiveData.value = repository.getStocksList("US")
        }

    fun searchStocks(query: String) =
        viewModelScope.launch {
            repository.getStocksList("US")
            val response = finnhubService.getCompanyProfile(query, BuildConfig.API_KEY)
            Log.d("SEARCH_QUERY", query)
            Log.d("REQUEST_URL", response.raw().request().url().toString())
            Log.d("RESPONSE", response.body().toString())
        }

    fun setIndex(index: Int) {
        _index.value = index
    }
}