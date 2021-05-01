package com.example.entranceproject.ui.pager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.entranceproject.data.model.Stock
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
class PagerViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val tab = MutableStateFlow(Tab.values().first())
    /*val visibleTickers = MutableStateFlow(listOf<String>())*/

    @ExperimentalCoroutinesApi
    val stocks = combine(tab) {}
        .flatMapLatest { repository.getStocks(tab.value).flowOn(Dispatchers.IO) }

    fun updateFavorite(stock: Stock) =
        viewModelScope.launch(Dispatchers.IO) { repository.updateFavorite(stock) }

    fun refreshData() =
        viewModelScope.launch(Dispatchers.IO) { repository.refreshData() }

    fun setTab(index: Int) {
        tab.value = Tab.values()[index]
    }

    fun getTab() = tab.value

    override fun onCleared() {
        repository.closeSocket()
    }

    companion object {
        private val TAG = "${PagerViewModel::class.java.simpleName}_TAG"
    }

}