package com.example.entranceproject.network.model

data class SearchResultDto(
    val count: Int,
    val result: List<SearchItemDto>
) {
    data class SearchItemDto(
        val description: String,
        val displaySymbol: String,
        val symbol: String,
        val type: String
    )
}