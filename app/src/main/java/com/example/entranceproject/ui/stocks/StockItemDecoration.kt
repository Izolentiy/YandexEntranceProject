package com.example.entranceproject.ui.stocks

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView

class StockItemDecoration(
    private val endMargin: Int,
    private val defaultMargin: Int,
    private val alterBackground: Drawable,
    private val mainBackground: Drawable
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
            .let { if (it == RecyclerView.NO_POSITION) return else it }
        view.background = if (position % 2 == 1) alterBackground else mainBackground

        val lastPosition = parent.adapter?.itemCount?.minus(1)
        // Setting up margin top 16dp for the first element and margin bottom for the last one
        outRect.top = if (position == 0) endMargin else defaultMargin
        outRect.bottom = if (position == lastPosition) endMargin else defaultMargin
    }

}