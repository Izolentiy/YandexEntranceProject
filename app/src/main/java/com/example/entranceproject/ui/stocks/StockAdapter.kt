package com.example.entranceproject.ui.stocks

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        return StockViewHolder(
            ItemStockBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null)
            holder.bind(currentItem)
    }

//    override fun getItemCount(): Int {
//        TODO("Not yet implemented")
//    }


    inner class StockViewHolder(private val binding: ItemStockBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            Log.d("STAR_TAG", "onBindClickListener: $bindingAdapterPosition")
            binding.imageViewStar.setOnClickListener {
                onStarClickListener(getItem(bindingAdapterPosition))
            }
        }

        // Bind stock properties to corresponding view in view holder
        fun bind(stock: Stock) {
            binding.apply {
                Glide.with(itemView)
                    .load(stock.companyLogo)
                    .fitCenter()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_logo_bg)
                    .into(imageLogo)

                if (stock.isFavorite) imageViewStar.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        itemView.resources, R.drawable.ic_star_colored, itemView.context.theme
                    )
                )
                else imageViewStar.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        itemView.resources, R.drawable.ic_star, itemView.context.theme
                    )
                )

                textViewCompanyName.text = stock.companyName
                textViewTicker.text = stock.ticker

                // For a while only dollars
                val format = NumberFormat.getCurrencyInstance(Locale.US)
                format.currency = Currency.getInstance("USD")

                textViewCurrentPrice.text = format.format(stock.currentPrice)
                textViewDayDelta.text = format.format(stock.dailyDelta)

                if (stock.dailyDelta >= 0)
                    textViewDayDelta.setTextColor(
                        ResourcesCompat.getColor(
                            itemView.resources, R.color.green, itemView.context.theme
                        )
                    )
                else
                    textViewDayDelta.setTextColor(
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