package com.example.entranceproject.ui.stocks

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.entranceproject.R
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.databinding.ItemStockBinding
import java.text.NumberFormat
import java.util.*

class StockAdapter(
    private val onStarClickListener: (Stock) -> Unit
) : ListAdapter<Stock, StockAdapter.StockViewHolder>(StockComparator()) {
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): StockViewHolder = StockViewHolder(
        ItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        Log.e(TAG, "bind: green=${R.color.green}")
        Log.e(TAG, "bind: red=${R.color.red}")
        layoutManager = recyclerView.layoutManager as LinearLayoutManager
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null)
            holder.bind(currentItem)
    }

    fun getCurrentVisibleItems(): List<Stock> {
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val lastVisible = layoutManager.findLastVisibleItemPosition()
        val firstCompletelyVisible = layoutManager.findFirstCompletelyVisibleItemPosition()
        val lastCompletelyVisible = layoutManager.findLastCompletelyVisibleItemPosition()

        Log.e(TAG, "getCurrentVisibleItems: firstVisible = $firstVisible")
        Log.e(TAG, "getCurrentVisibleItems: lastVisible  = $lastVisible")
//        Log.d(TAG, "getCurrentVisibleItems: $firstCompletelyVisible")
//        Log.d(TAG, "getCurrentVisibleItems: $lastCompletelyVisible")
        return try {
            val visibleItems = currentList.subList(firstVisible, lastVisible + 1)
            val completelyVisible =
                currentList.subList(firstCompletelyVisible, lastCompletelyVisible + 1)
            val visibleTickers = visibleItems.map(Stock::ticker)
            Log.d(TAG, "getCurrentVisibleItems: visibleItems = $visibleTickers")
//            Log.d(TAG, "getCurrentVisibleItems: ${completelyVisible.map(Stock::ticker)}")
            visibleItems
        } catch (error: Exception) {
            Log.e(TAG, "getCurrentVisibleItems: $error")
            emptyList()
        }
    }

    companion object {
        private val TAG = "${StockAdapter::class.java.simpleName}_TAG"
    }

    inner class StockViewHolder(private val binding: ItemStockBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.imageViewStar.setOnClickListener {
                onStarClickListener(getItem(bindingAdapterPosition))
            }
        }

        // Bind stock properties to corresponding view in view holder
        fun bind(stock: Stock) {
            Log.d(TAG, "bind: ${stock.ticker}")
            binding.apply {
                // Company logo
                Glide.with(itemView)
                    .load(stock.companyLogo)
                    .error(R.drawable.ic_logo_placeholder)
                    .fitCenter()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageLogo)

                // Star icon
                val starIcon = if (stock.isFavorite) R.drawable.ic_star_colored else R.drawable.ic_star
                imageViewStar.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        itemView.resources, starIcon, itemView.context.theme
                    )
                )

                // Company name and stock ticker
                textViewCompanyName.text = stock.companyName
                textViewTicker.text = stock.ticker

                // For a while only dollars
                val format = NumberFormat.getCurrencyInstance(Locale.US)
                format.currency = Currency.getInstance("USD")

                textViewCurrentPrice.text = format.format(stock.currentPrice)
                textViewDayDelta.text = format.format(stock.dailyDelta)

                // Daily delta color
                val deltaColor = if (stock.dailyDelta >= 0) R.color.green else R.color.red
                Log.e(TAG, "bind: final=$deltaColor")
                textViewDayDelta.setTextColor(ResourcesCompat.getColor(
                    itemView.resources, deltaColor, itemView.context.theme
                ))
            }
        }
    }

    class StockComparator : DiffUtil.ItemCallback<Stock>() {
        override fun areItemsTheSame(oldItem: Stock, newItem: Stock): Boolean =
            oldItem.ticker == newItem.ticker

        override fun areContentsTheSame(oldItem: Stock, newItem: Stock): Boolean =
            oldItem == newItem
    }
}