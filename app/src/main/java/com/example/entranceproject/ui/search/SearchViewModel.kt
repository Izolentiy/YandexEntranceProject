package com.example.entranceproject.ui.search

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
class SearchViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val query = MutableStateFlow("")

    @ExperimentalCoroutinesApi
    val stocks = combine(query) { query -> query.first() }
        .flatMapLatest { query -> repository.searchStocks(query) }

    fun updateFavorite(stock: Stock) =
        viewModelScope.launch(Dispatchers.IO) { repository.updateFavorite(stock) }

    fun refreshData() =
        viewModelScope.launch(Dispatchers.IO) { repository.refreshData() }

    override fun onCleared() {
        repository.closeSocket()
    }

    companion object {
        private val TAG = "${SearchViewModel::class.java.simpleName}_TAG"
    }
}