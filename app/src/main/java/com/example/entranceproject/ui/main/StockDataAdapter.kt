package com.example.entranceproject.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.entranceproject.R
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.databinding.ItemStockBinding

// This will be used in later version for data loading in chunks
class StockDataAdapter (private val listener: OnItemClickListener) :
    PagingDataAdapter<Stock, StockDataAdapter.StockViewHolder>(STOCK_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val binding =
            ItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val currentItem = getItem(position)

        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }

    inner class StockViewHolder(private val binding: ItemStockBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item != null) {
                        listener.onItemClick(item)
                    }
                }
            }
        }

        fun bind(stock: Stock) {
            binding.apply {
                Glide.with(itemView)
                    .load(stock.companyLogo)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_placeholder)
                    .into(imageLogo)

                textViewTicker.text = stock.ticker
                textViewCompanyName.text = stock.companyName
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(photo: Stock)
    }

    companion object {
        private val STOCK_COMPARATOR = object : DiffUtil.ItemCallback<Stock>() {
            override fun areItemsTheSame(oldItem: Stock, newItem: Stock) =
                oldItem.ticker == newItem.ticker

            override fun areContentsTheSame(oldItem: Stock, newItem: Stock) =
                oldItem == newItem
        }
    }
}