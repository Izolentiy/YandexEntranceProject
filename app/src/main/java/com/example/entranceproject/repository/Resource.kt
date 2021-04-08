package com.example.entranceproject.repository

data class Resource<out T>(val status: Status, val data: T?, val error: Throwable?) {
    companion object {
        fun <T> success(data: T?): Resource<T> =
            Resource(Status.SUCCESS, data, null)

        fun <T> loading(data: T?): Resource<T> =
            Resource(Status.LOADING, data, null)

        fun <T> error(data: T?, error: Throwable): Resource<T> =
            Resource(Status.ERROR, data, error)
    }

    enum class Status {
        SUCCESS,
        LOADING,
        ERROR
    }
}
