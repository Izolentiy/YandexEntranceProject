package com.example.entranceproject.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.entranceproject.databinding.FragmentStocksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StocksFragment : Fragment() {

    private var _binding: FragmentStocksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStocksBinding.inflate(inflater, container, false)

        val stockAdapter = StockAdapter()
        binding.apply {
            recyclerViewTasks.adapter = stockAdapter
        }
        viewModel.stocks.observe(viewLifecycleOwner) { result ->
            stockAdapter.submitList(result.data)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int): StocksFragment {
            return StocksFragment().apply {
                arguments = Bundle().apply {
                    Log.d("INDEX_TAG", "newInstance: $sectionNumber")
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}