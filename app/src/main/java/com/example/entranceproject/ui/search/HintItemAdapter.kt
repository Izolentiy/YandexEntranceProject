package com.example.entranceproject.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.entranceproject.databinding.ItemSuggestionBinding

class HintItemAdapter(
    private val onSuggestionClickListener: (CharSequence) -> Unit
) : ListAdapter<String, HintItemAdapter.HintItemViewHolder>(HintComparator) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): HintItemViewHolder = HintItemViewHolder(
        ItemSuggestionBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: HintItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class HintItemViewHolder(private val binding: ItemSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.textViewSuggestion.apply {
                setOnClickListener { onSuggestionClickListener(text) }
            }
        }

        fun bind(suggestion: String) {
            binding.textViewSuggestion.text = suggestion
        }

    }

    object HintComparator : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}