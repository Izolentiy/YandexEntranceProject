package com.example.entranceproject.ui.search

import android.util.Log
import androidx.lifecycle.*
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.repository.Repository
import com.example.entranceproject.repository.Resource
import com.example.entranceproject.ui.main.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val query = MutableStateFlow("")

    // Encapsulation...
    private val _stocks = MutableLiveData<Resource<List<Stock>>>()
    val stocks: LiveData<Resource<List<Stock>>> get() = _stocks

    fun searchStocks(query: String) =
        viewModelScope.launch(Dispatchers.IO) { repository.searchStocks(query) }

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