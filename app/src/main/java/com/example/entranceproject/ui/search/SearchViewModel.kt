package com.example.entranceproject.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.network.websocket.WebSocketHandler
import com.example.entranceproject.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: Repository,
    private val webSocketHandler: WebSocketHandler
) : ViewModel() {

    val query = MutableStateFlow("")

    @ExperimentalCoroutinesApi
    val stocks = combine(query) {}
        .flatMapLatest { repository.searchStocks(query.value).flowOn(Dispatchers.IO) }

    fun updateFavorite(stock: Stock) =
        viewModelScope.launch(Dispatchers.IO) { repository.updateFavorite(stock) }

    fun refreshData() =
        viewModelScope.launch(Dispatchers.IO) { repository.refreshData() }

    override fun onCleared() {
        webSocketHandler.closeSocket()
    }

    companion object {
        private val TAG = "${SearchViewModel::class.java.simpleName}_TAG"
    }
}