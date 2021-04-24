package com.example.entranceproject.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.entranceproject.data.model.Suggestion
import com.example.entranceproject.databinding.ItemSuggestionBinding

class SuggestionItemAdapter :
    ListAdapter<Suggestion, SuggestionItemAdapter.SuggestionItemViewHolder>(SuggestionComparator) {

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

        fun bind(suggestion: Suggestion) {
            binding.textViewSuggestion.text = suggestion.text
        }

    }

    object SuggestionComparator : DiffUtil.ItemCallback<Suggestion>() {
        override fun areItemsTheSame(oldItem: Suggestion, newItem: Suggestion) =
            oldItem.text == newItem.text

        override fun areContentsTheSame(oldItem: Suggestion, newItem: Suggestion) =
            oldItem == newItem
    }
}