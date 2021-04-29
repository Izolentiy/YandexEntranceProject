package com.example.entranceproject.repository

import android.util.Log
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline loadFromDb: () -> Flow<ResultType>,
    crossinline shouldFetch: (ResultType) -> Boolean = { true },
    crossinline fetchData: suspend (ResultType) -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit
) = flow {
    val data = loadFromDb().first()

    val flow = if (shouldFetch(data)) {
        emit(Resource.loading(data))
        try {
            saveFetchResult(fetchData(data))
            loadFromDb().map { Resource.success(it) }
        } catch (exception: Throwable) {
            loadFromDb().map { Resource.error(it, exception) }
        }
    } else {
        Log.d(TAG, "networkBoundResource: shouldNotFetchAnything")
        loadFromDb().map { Resource.success(it) }
    }

    emitAll(flow)
}

const val TAG = "NetworkBound_TAG"