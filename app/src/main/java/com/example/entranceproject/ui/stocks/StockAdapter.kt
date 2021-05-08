package com.example.entranceproject.ui.stocks

import android.os.Bundle
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
) : ListAdapter<Stock, StockAdapter.StockViewHolder>(StockComparator) {
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder = StockViewHolder(
        ItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: StockViewHolder, position: Int, payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else payloads.forEach { it as Bundle
            if (it.containsKey(IS_FAVORITE)) {
                val isFavorite = it.getBoolean(IS_FAVORITE)
                holder.updateFavorite(isFavorite)
            }
            if (it.containsKey(CURRENT_PRICE) && it.containsKey(DAILY_DELTA)) {
                val currentPrice = it.getDouble(CURRENT_PRICE)
                val dailyDelta = it.getDouble(DAILY_DELTA)
                holder.updatePrice(currentPrice, dailyDelta)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        layoutManager = recyclerView.layoutManager as LinearLayoutManager
    }

    fun getCurrentVisibleItems(): List<Stock> {
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val lastVisible = layoutManager.findLastVisibleItemPosition()

        return try {
            // lastVisible + 1 need to make range last inclusive
            currentList.subList(firstVisible, lastVisible + 1)
        } catch (error: Exception) {
            Log.e(TAG, "getCurrentVisibleItems: $error")
            emptyList()
        }
    }

    companion object {
        private val TAG = "${StockAdapter::class.java.simpleName}_TAG"

        const val CURRENT_PRICE = "current_price"
        const val DAILY_DELTA = "daily_delta"
        const val IS_FAVORITE = "is_favorite"
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
                    if (stock.webUrl?.isNotEmpty() == true) prepareLogoUrl(stock)
                    else stock.companyLogo ?: ""
                val logoCornerRadius = itemView.resources
                    .getDimensionPixelSize(R.dimen.stock_item_logo_corner_radius)

                Glide.with(itemView)
                    .load(logoImage)
                    .error(R.drawable.bg_logo_dark_shape)
                    .transform(FitCenter(), RoundedCorners(logoCornerRadius))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageLogo)

                // Company name and stock ticker
                textViewCompanyName.text = stock.companyName
                textViewTicker.text = stock.ticker

                // Star icon
                updateFavorite(stock.isFavorite)

                // Price and daily delta, color text
                updatePrice(stock.currentPrice, stock.dailyDelta)
            }
        }

        fun updatePrice(currentPrice: Double, dailyDelta: Double) {
            // Get currency symbol, Finnhub returns all prices in USD
            val currency = Currency.getInstance("USD").symbol

            binding.apply {
                textViewCurrentPrice.text = itemView.resources
                    .getString(R.string.price, currency, currentPrice)
                textViewDayDelta.text = itemView.resources
                    .getString(R.string.price, currency, dailyDelta)

                val deltaColor = if (dailyDelta >= 0) R.color.green else R.color.red
                val deltaTextColor = ResourcesCompat
                    .getColor(itemView.resources, deltaColor, itemView.context.theme)
                textViewDayDelta.setTextColor(deltaTextColor)
            }


        }

        fun updateFavorite(isFavorite: Boolean) {
            val starIcon = if (isFavorite) R.drawable.ic_star_colored else R.drawable.ic_star
            val starImageDrawable = ResourcesCompat
                .getDrawable(itemView.resources, starIcon, itemView.context.theme)
            binding.imageViewStar.setImageDrawable(starImageDrawable)
        }

        private fun prepareLogoUrl(stock: Stock): String {
            /* Replace "/us/en" and "en-us" parts to make urls recognizable for clearbit.com
               "https://squareup.com/us/en"
               "https://www.microsoft.com/en-us" */
            val ruUrl = "${stock.webUrl?.replaceAfter(".ru", "/")}"
            var comUrl = ruUrl.replaceAfter(".com", "/")

            /* Special treatment for Yandex because it has https://yandex.com/ domain
               and it clearbit doesn't have icon for yandex.com
               the same situation for Alibaba */
            if (stock.ticker == "YNDX") comUrl = "https://yandex.ru/"
            if (stock.ticker == "BABA") comUrl = "https://alibaba.com/"
            return "$LOGOS_URL$comUrl"
        }

    }

    object StockComparator : DiffUtil.ItemCallback<Stock>() {
        override fun getChangePayload(oldItem: Stock, newItem: Stock) = Bundle().apply {
            if (oldItem.currentPrice != newItem.currentPrice) {
                putDouble(CURRENT_PRICE, newItem.currentPrice)
                putDouble(DAILY_DELTA, newItem.dailyDelta)
            }
            if (oldItem.isFavorite != newItem.isFavorite)
                putBoolean(IS_FAVORITE, newItem.isFavorite)
        }


        override fun areItemsTheSame(oldItem: Stock, newItem: Stock): Boolean =
            oldItem.ticker == newItem.ticker

        override fun areContentsTheSame(oldItem: Stock, newItem: Stock): Boolean =
            oldItem == newItem
    }
}