package com.example.entranceproject.ui.main

import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Transformations.switchMap
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val tab = MutableLiveData<Tab>()
    val stocks = switchMap(tab) { repository.getStocks(it).asLiveData() }

    fun updateFavorite(stock: Stock) =
        viewModelScope.launch(Dispatchers.IO) { repository.updateFavorite(stock) }

    fun searchStocks(query: String) =
        viewModelScope.launch(Dispatchers.IO) {}

    fun subscribeToSocketEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.openSocket().consumeEach {
                    if (it.exception == null)
                        Log.d("WEB_SOCKET_TAG", "subscribeToSocketEvents: ${it.text}")
                    else
                        onSocketError(it.exception)
                }
            } catch (exception: Exception) {
                onSocketError(exception)
            }
        }
    }

    private fun onSocketError(error: Throwable) {
        Log.d("SOCKET_TAG", "onSocketError: ${error.message}")
    }

    override fun onCleared() {
        repository.closeSocket()
        super.onCleared()
    }

    fun setTab(index: Int) {
        tab.value = Tab.values()[index]
    }

}