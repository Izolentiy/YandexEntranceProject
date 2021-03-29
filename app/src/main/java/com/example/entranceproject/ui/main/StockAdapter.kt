package com.example.entranceproject.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.entranceproject.R
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.databinding.ItemStockBinding

class StockAdapter : ListAdapter<Stock, StockAdapter.StockViewHolder>(StockComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        return StockViewHolder(ItemStockBinding
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


    class StockViewHolder(private val binding: ItemStockBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Bind stock properties to corresponding view in view holder
        fun bind(stock: Stock) {
            binding.apply {
                Glide.with(itemView)
                    .load(stock.companyLogo)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_placeholder)
                    .into(imageLogo)

                if (stock.isFavorite)
                    imageViewStar.setImageDrawable(
                        itemView.resources.getDrawable(R.drawable.ic_star_colored)
                    )  // Note!!!

                textViewCompanyName.text = stock.companyName
                textViewTicker.text = stock.ticker

                textViewCurrentPrice.text = stock.currentPrice.toString()
                textViewDayDelta.text = stock.dailyDelta.toString()
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