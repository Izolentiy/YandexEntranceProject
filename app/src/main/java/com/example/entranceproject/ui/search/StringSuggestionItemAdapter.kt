package com.example.entranceproject.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.entranceproject.databinding.ItemSuggestionBinding

class StringSuggestionItemAdapter :
    ListAdapter<String, StringSuggestionItemAdapter.SuggestionItemViewHolder>(SuggestionComparator) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): SuggestionItemViewHolder = SuggestionItemViewHolder(
        ItemSuggestionBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: SuggestionItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class SuggestionItemViewHolder(private val binding: ItemSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(suggestion: String) {
            binding.textViewSuggestion.text = suggestion
        }

    }

    object SuggestionComparator : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem

        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}