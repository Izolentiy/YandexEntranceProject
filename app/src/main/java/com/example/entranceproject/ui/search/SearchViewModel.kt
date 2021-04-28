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
//    private val showSuggestions = MutableStateFlow(true)

    // Encapsulation...
    /*private val _stocks = MutableLiveData<Resource<List<Stock>>>()
    val stocks: LiveData<Resource<List<Stock>>> get() = _stocks*/

    private val _stocks = MutableStateFlow<Resource<List<Stock>>?>(null)
    val stocks: StateFlow<Resource<List<Stock>>?> get() = _stocks
//    val stocks = switchMap(query.asLiveData()) {
//        Log.d(TAG, "visibleTickers: ${query.value}")
//        repository.searchStocks(query.value).asLiveData() }

    /*@ExperimentalCoroutinesApi
    val stocks = combine(query) { query -> query.first() }
        .flatMapLatest { query -> repository.searchStocks(query) }*/

    fun searchStocks(query: String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.searchStocks(query).collect { _stocks.value = it }
        }

//    fun searchStocks() {}

    fun updateQuery(newQuery: String) { query.value = newQuery }

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