package com.example.entranceproject.repository

import kotlinx.coroutines.flow.*

fun <ResultType, RequestType> networkBoundResource(
    loadFromDb: () -> Flow<ResultType>,
    shouldFetch: (ResultType) -> Boolean = { true },
    fetch: suspend () -> RequestType,
    saveFetchResult: suspend (RequestType) -> Unit
) = flow {
    val data = loadFromDb().first()

    val flow = if (shouldFetch(data)) {
        emit(Resource.loading(data))
        try {
            saveFetchResult(fetch())
            loadFromDb().map { Resource.success(it) }
        } catch (exception: Throwable) {
            loadFromDb().map { Resource.error(it, exception) }
        }
    } else {
        loadFromDb().map { Resource.success(it) }
    }

    emitAll(flow)
}