package com.example.entranceproject.ui.main

import android.util.Log
import androidx.lifecycle.*
import com.example.entranceproject.BuildConfig
import com.example.entranceproject.R
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.network.FinnhubService
import com.example.entranceproject.repository.Repository
import com.example.entranceproject.ui.main.PagerAdapter.Companion.TAB_TITLES
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    repository: Repository
) : ViewModel() {

    private val _index = MutableLiveData<Int>()

    val stocks = repository.getStocks().asLiveData()

    fun searchStocks(query: String) =
        viewModelScope.launch {}

    fun setIndex(index: Int) {
        _index.value = index
    }
}