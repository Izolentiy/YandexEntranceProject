package com.example.entranceproject.ui.search

import androidx.lifecycle.*
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.repository.Repository
import com.example.entranceproject.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val query = MutableStateFlow("")
//    private val showSuggestions = MutableStateFlow(true)

    // Encapsulation...
    /*private val _stocks = MutableLiveData<Resource<List<Stock>>>()
    val stocks: LiveData<Resource<List<Stock>>> get() = _stocks*/

    private val _stocks = MutableStateFlow<Resource<List<Stock>>?>(null)
    val stocks: StateFlow<Resource<List<Stock>>?> get() = _stocks

    fun searchStocks() =
        viewModelScope.launch(Dispatchers.IO) {
            repository.searchStocks(query.value).collect { _stocks.value = it }
        }

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