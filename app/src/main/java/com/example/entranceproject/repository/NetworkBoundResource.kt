package com.example.entranceproject.repository

import android.util.Log
import kotlinx.coroutines.flow.*

fun <ResultType, RequestType> networkBoundResource(
    loadFromDb: () -> Flow<ResultType>,
    shouldFetch: (ResultType) -> Boolean = { true },
    fetchData: suspend () -> RequestType,
    saveFetchResult: suspend (RequestType) -> Unit
) = flow {
    val data = loadFromDb().first()

    val flow = if (shouldFetch(data)) {
        emit(Resource.loading(data))
        try {
            saveFetchResult(fetchData())
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

private const val TAG = "NetworkBound_TAG"