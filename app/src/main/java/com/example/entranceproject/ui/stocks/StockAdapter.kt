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
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.entranceproject.R
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.databinding.ItemStockBinding
import com.example.entranceproject.network.FinnhubService.Companion.LOGOS_URL
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

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null)
            holder.bind(currentItem)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        layoutManager = recyclerView.layoutManager as LinearLayoutManager
    }

    fun getCurrentVisibleItems(): List<Stock> {
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val lastVisible = layoutManager.findLastVisibleItemPosition()
        val firstCompletelyVisible = layoutManager.findFirstCompletelyVisibleItemPosition()
        val lastCompletelyVisible = layoutManager.findLastCompletelyVisibleItemPosition()

        return try {
            val visibleItems = currentList.subList(firstVisible, lastVisible + 1)
            val completelyVisible =
                currentList.subList(firstCompletelyVisible, lastCompletelyVisible + 1)
            val visibleTickers = visibleItems.map(Stock::ticker)
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

        fun bind(stock: Stock) {
            binding.apply {
                // Company logo
                val logoImage =
                    if (stock.webUrl != null) { prepareLogoUrl(stock.webUrl) }
                    else stock.companyLogo ?: ""

                val radius = itemView.resources
                    .getDimensionPixelSize(R.dimen.stock_item_logo_corner_radius)

                Glide.with(itemView)
                    .load(logoImage)
                    .error(R.drawable.bg_logo_dark_shape)
                    .transform(FitCenter(), RoundedCorners(radius))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageLogo)

                // Star icon
                val starIcon =
                    if (stock.isFavorite) R.drawable.ic_star_colored else R.drawable.ic_star
                imageViewStar.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        itemView.resources, starIcon, itemView.context.theme
                    )
                )

                // Company name and stock ticker
                textViewCompanyName.text = stock.companyName
                textViewTicker.text = stock.ticker

                // Get currency symbol
                val currency = try {
                    stock.country?.let { country ->
                        Currency.getInstance(Locale(country, country)).symbol.last()
                    } ?: "$"
                } catch (error: IllegalArgumentException) {
                    ""
                }

                // Price and day delta
                textViewCurrentPrice.text = itemView.resources
                    .getString(R.string.price, currency, stock.currentPrice)
                textViewDayDelta.text = itemView.resources
                    .getString(R.string.price, currency, stock.dailyDelta)

                // Daily delta color
                val deltaColor = if (stock.dailyDelta >= 0) R.color.green else R.color.red
                textViewDayDelta.setTextColor(
                    ResourcesCompat.getColor(
                        itemView.resources, deltaColor, itemView.context.theme
                    )
                )
            }
        }

        private fun prepareLogoUrl(url: String): String {
            // Replace "/us/en" and "en-us" parts
            // to make urls recognizable for clearbit.com
            // "https://squareup.com/us/en"
            // "https://www.microsoft.com/en-us"
            val comOut = url.replaceAfter(".com", "")
            val ruOut = "${comOut.replaceAfter(".ru", "")}?size=52"
            return "$LOGOS_URL$ruOut"
        }

    }

    class StockComparator : DiffUtil.ItemCallback<Stock>() {
        override fun areItemsTheSame(oldItem: Stock, newItem: Stock): Boolean =
            oldItem.ticker == newItem.ticker

        override fun areContentsTheSame(oldItem: Stock, newItem: Stock): Boolean =
            oldItem == newItem
    }
}