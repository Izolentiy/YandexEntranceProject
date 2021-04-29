package com.example.entranceproject.ui.pager

import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Transformations.switchMap
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.repository.Repository
import com.example.entranceproject.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PagerViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val tab = MutableStateFlow(Tab.values().first())
    /*val visibleTickers = MutableStateFlow(listOf<String>())*/

//    @ExperimentalCoroutinesApi
//    val stocks = switchMap(visibleTickers.asLiveData()) {
//        Log.d(TAG, "visibleTickers: ${visibleTickers.value}")
//        Log.d(TAG, "tab: ${tab.value}")
//        repository.getStocks(tab.value).asLiveData() }
    /*@ExperimentalCoroutinesApi
    val stocks = combine(visibleTickers) {
        Log.d(TAG, "visibleTickers: ${visibleTickers.value}")
        Log.d(TAG, "tab: ${tab.value}") }
    .flatMapLatest { repository.getStocks(tab.value) }*/

    // It work incorrectly because it is invoked before new tab value assigned
    /*val stocks = repository.getStocks(getTab())*/

    @ExperimentalCoroutinesApi
    val stocks = combine(tab) {}.flatMapLatest { repository.getStocks(tab.value) }

    fun updateFavorite(stock: Stock) =
        viewModelScope.launch(Dispatchers.IO) { repository.updateFavorite(stock) }

    fun refreshData() =
        viewModelScope.launch(Dispatchers.IO) { repository.refreshData() }

    fun setTab(index: Int) {
        tab.value = Tab.values()[index]
    }

    override fun onCleared() {
        repository.closeSocket()
    }

    companion object {
        private val TAG = "${PagerViewModel::class.java.simpleName}_TAG"
    }

}