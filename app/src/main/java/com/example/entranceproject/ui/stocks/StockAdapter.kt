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
    private val onStarClickListener: (Stock) -> Unit,
    private val onVisibleTickerChange: (CharSequence, String) -> Unit
) : ListAdapter<Stock, StockAdapter.StockViewHolder>(StockComparator()) {
    private lateinit var layoutManager: LinearLayoutManager
    private var viewHolderCount: Int = 0
    private val visibleStocks = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        viewHolderCount++
        visibleStocks
        return StockViewHolder(
            ItemStockBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        layoutManager = recyclerView.layoutManager as LinearLayoutManager
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null)
            holder.bind(currentItem)
    }

//    override fun getItemCount(): Int {
//        TODO("Not yet implemented")
//    }

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
            val visibleItems = currentList.subList(firstVisible, lastVisible+1)
            val completelyVisible =
                currentList.subList(firstCompletelyVisible, lastCompletelyVisible+1)
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
//            Log.d(TAG, "StockViewHolder: $viewHolderCount")
            binding.imageViewStar.setOnClickListener {
                onStarClickListener(getItem(bindingAdapterPosition))
            }
        }

        // Bind stock properties to corresponding view in view holder
        fun bind(stock: Stock) {
//            visibleStocks.add(stock.ticker)
//            Log.d(TAG, "bind: $visibleStocks")

//            Log.e(TAG, "bind: ${stock.ticker}, ${stock.country}")
            // The version below is not acceptable, as it was error-prone
            /*if (binding.textViewTicker.text != stock.ticker) {
                onVisibleTickerChange(binding.textViewTicker.text, stock.ticker)
            }*/

            binding.apply {
                Glide.with(itemView)
                    .load(stock.companyLogo)
                    .error(R.drawable.ic_logo_placeholder)
                    .fitCenter()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageLogo)

                when (stock.isFavorite) {
                    true -> imageViewStar.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            itemView.resources, R.drawable.ic_star_colored, itemView.context.theme
                        )
                    )
                    false -> imageViewStar.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            itemView.resources, R.drawable.ic_star, itemView.context.theme
                        )
                    )
                }

                textViewCompanyName.text = stock.companyName
                textViewTicker.text = stock.ticker

                // For a while only dollars
                val format = NumberFormat.getCurrencyInstance(Locale.US)
                format.currency = Currency.getInstance("USD")

                textViewCurrentPrice.text = format.format(stock.currentPrice)
                textViewDayDelta.text = format.format(stock.dailyDelta)

                if (stock.dailyDelta >= 0) textViewDayDelta.setTextColor(
                    ResourcesCompat.getColor(
                        itemView.resources, R.color.green, itemView.context.theme
                    )
                )
                else textViewDayDelta.setTextColor(
                    ResourcesCompat.getColor(
                        itemView.resources, R.color.red, itemView.context.theme
                    )
                )
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