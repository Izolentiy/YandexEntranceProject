package com.example.entranceproject.ui.pager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.repository.Repository
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

    private val _tab = MutableStateFlow(Tab.values().first())
    val tab get() = _tab.asStateFlow()
    private val _visibleTickers = MutableStateFlow(listOf<String>())
    val visibleTickers get() = _visibleTickers.asStateFlow()

    @ExperimentalCoroutinesApi
    val stocks = combine(_tab) {}
        .flatMapLatest { repository.getStocks(_tab.value).flowOn(Dispatchers.IO) }

    fun subscribeToPriceUpdates() =
        viewModelScope.launch(Dispatchers.IO) {
            repository.openSocket()
            repository.setSubscription(visibleTickers).collect()
        }


    fun updateFavorite(stock: Stock) =
        viewModelScope.launch(Dispatchers.IO) { repository.updateFavorite(stock) }

    fun refreshData() =
        viewModelScope.launch(Dispatchers.IO) { repository.refreshData() }

    fun setVisibleTickers(tickers: List<String>) { _visibleTickers.value = tickers }

    fun setTab(index: Int) { _tab.value = Tab.values()[index] }

    override fun onCleared() {
        repository.closeSocket()
    }

    companion object {
        private val TAG = "${PagerViewModel::class.java.simpleName}_TAG"
    }

}