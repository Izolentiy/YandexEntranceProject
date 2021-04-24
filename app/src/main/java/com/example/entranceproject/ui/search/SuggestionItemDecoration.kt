package com.example.entranceproject.ui.search

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SuggestionItemDecoration(
    private val endMargin: Int,
    private val defaultMargin: Int
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
            .let { if (it == RecyclerView.NO_POSITION) return else it }
        val lastPosition = parent.adapter?.itemCount?.minus(1)

        // Setting up margin top 16dp for the first element and margin bottom for the last one
        // I add marginStart to the first suggestion items
        // In future should be added RTL languages support
        outRect.left = when (position) {
            0 -> endMargin
            1 -> endMargin
            else -> defaultMargin
        }
        outRect.right = when (position) {
            lastPosition -> endMargin
            (lastPosition?.minus(1)) -> endMargin
            else -> defaultMargin
        }
    }
}