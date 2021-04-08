package com.example.entranceproject.ui.stocks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.databinding.FragmentStocksBinding
import com.example.entranceproject.repository.Resource
import com.example.entranceproject.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StocksFragment : Fragment() {

    private var _binding: FragmentStocksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    private val onStarClickListener: (stock: Stock) -> Unit = { stock ->
        viewModel.updateFavorite(stock.copy(isFavorite = !stock.isFavorite))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setTab(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStocksBinding.inflate(inflater, container, false)

        val stockAdapter = StockAdapter(onStarClickListener)
        binding.apply {
            recyclerViewStocks.adapter = stockAdapter
            recyclerViewStocks.hasFixedSize()

            viewModel.stocks.observe(viewLifecycleOwner) { result ->
                if (result.data?.isEmpty() == true)
                    textViewNoStocks.visibility = View.VISIBLE
                else
                    textViewNoStocks.visibility = View.GONE
                when (result.status) {
                    Resource.Status.SUCCESS -> {
                        textViewError.visibility = View.GONE
                        progressLoading.visibility = View.GONE
                        stockAdapter.submitList(result.data)
                    }
                    Resource.Status.LOADING -> {
                        textViewError.visibility = View.GONE
                        progressLoading.visibility = View.VISIBLE
                    }
                    Resource.Status.ERROR -> {
                        Log.d("STOCKS_LOADING_TAG", "onCreateView: ${result.error?.message}")
                        textViewError.visibility = View.VISIBLE
                        textViewNoStocks.visibility = View.GONE
                        progressLoading.visibility = View.GONE
                    }
                }
            }
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
                arguments = Bundle().apply { putInt(ARG_SECTION_NUMBER, sectionNumber) }
            }
        }
    }
}