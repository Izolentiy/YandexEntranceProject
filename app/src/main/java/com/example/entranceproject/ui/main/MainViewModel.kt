package com.example.entranceproject.ui.main

import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Transformations.switchMap
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.repository.Repository
import com.example.entranceproject.ui.pager.Tab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val tab = MutableStateFlow(Tab.values().first())
    val visibleTickers = MutableStateFlow(listOf<String>())

    @ExperimentalCoroutinesApi
    val stocks = switchMap(visibleTickers.asLiveData()) {
        Log.d(TAG, "visibleTickers: ${visibleTickers.value}")
        Log.d(TAG, "tab: ${tab.value}")
        repository.getStocks(tab.value, visibleTickers.value).asLiveData() }

    fun updateFavorite(stock: Stock) =
        viewModelScope.launch(Dispatchers.IO) { repository.updateFavorite(stock) }

    fun searchStocks(query: String) =
        viewModelScope.launch(Dispatchers.IO) {}

    fun subscribeToSocketEvents(tickers: List<String>) =
        viewModelScope.launch(Dispatchers.IO) {}

    fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) { repository.refreshData() }
    }

    fun setTab(index: Int) {
        tab.value = Tab.values()[index]
    }

    override fun onCleared() {
        repository.closeSocket()
    }

    companion object {
        private val TAG = "${MainViewModel::class.java.simpleName}_TAG"
    }

}