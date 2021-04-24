package com.example.entranceproject.repository

import android.util.Log
import kotlinx.coroutines.flow.*

fun <ResultType, RequestType> networkBoundResource(
    loadFromDb: () -> Flow<ResultType>,
    shouldFetchStocks: (ResultType) -> Boolean = { true },
    shouldFetchPrices: () -> Boolean = { false },
    fetchPrices: suspend () -> RequestType,
    updatePrices: suspend (RequestType) -> Unit,
    fetchStocks: suspend () -> RequestType,
    saveFetchResult: suspend (RequestType) -> Unit
) = flow {
    val data = loadFromDb().first()

    Log.d(TAG, "networkBoundResource: $data")
    val flow = when {
        shouldFetchStocks(data) -> {
            emit(Resource.loading(data))
            Log.d(TAG, "networkBoundResource: shouldFetchAll")
            try {
                saveFetchResult(fetchStocks())
                loadFromDb().map { Resource.success(it) }
            } catch (exception: Throwable) {
                loadFromDb().map { Resource.error(it, exception) }
            }
        }
        shouldFetchPrices() -> {
            Log.d(TAG, "networkBoundResource: shouldFetchPrices")
            try {
                emit(Resource.loading(data))  // For the test purpose
                updatePrices(fetchPrices())
                loadFromDb().map { Resource.success(it) }
            } catch (exception: Throwable) {
                loadFromDb().map { Resource.error(it, exception) }
            }
        }
        else -> {
            Log.d(TAG, "networkBoundResource: shouldNotFetchAnything")
            loadFromDb().map { Resource.success(it) }
        }
    }

    emitAll(flow)
}

private const val TAG = "NetworkBound_TAG"