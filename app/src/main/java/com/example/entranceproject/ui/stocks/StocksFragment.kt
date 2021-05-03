package com.example.entranceproject.ui.stocks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.entranceproject.R
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.databinding.LayoutStockListBinding
import com.example.entranceproject.repository.Resource
import com.example.entranceproject.ui.pager.PagerViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class StocksFragment : Fragment() {

    private var _binding: LayoutStockListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PagerViewModel by viewModels()

    private val onStarClickListener: (stock: Stock) -> Unit = { stock ->
        viewModel.updateFavorite(stock.copy(isFavorite = !stock.isFavorite))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setTab(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
    }

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutStockListBinding.inflate(inflater, container, false)

        val stockAdapter = StockAdapter(onStarClickListener)
        binding.apply {
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.refreshData()
                showSnackBar("I do nothing. He he he")
                swipeRefreshLayout.isRefreshing = false
            }

            decorateStockList(recyclerViewStocks)
            recyclerViewStocks.adapter = stockAdapter
            recyclerViewStocks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val tickers = stockAdapter.getCurrentVisibleItems().map(Stock::ticker)
                        Log.d(TAG, "onScrollStateChanged: $tickers")
                        viewModel.setVisibleTickers(tickers)
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (viewModel.visibleTickers.value.isEmpty()) {
                        val tickers = stockAdapter.getCurrentVisibleItems().map(Stock::ticker)
                        Log.d(TAG, "onScrolled: $tickers")
                        viewModel.setVisibleTickers(tickers)
                        viewModel.subscribeToPriceUpdates()
                    }
                }
            })


//            viewModel.stocks.observe(viewLifecycleOwner) { result ->
//                handleResult(result, stockAdapter)
//            }
            lifecycleScope.launchWhenStarted {
                viewModel.stocks.collect { result -> handleResult(result, stockAdapter) }
            }
        }
        return binding.root
    }

    // Lifecycle callbacks to see lifecycle events on log
    override fun onResume() {
        Log.e(TAG, "onResume: STOCKS_FRAGMENT ${viewModel.tab.value}")
        super.onResume()
    }

    override fun onStart() {
        Log.e(TAG, "onStart: STOCKS_FRAGMENT ${viewModel.tab.value}")
        super.onStart()
    }

    override fun onStop() {
        Log.e(TAG, "onStop: STOCKS_FRAGMENT ${viewModel.tab.value}")
        super.onStop()
    }

    override fun onPause() {
        Log.e(TAG, "onPause: STOCKS_FRAGMENT ${viewModel.tab.value}")
        super.onPause()
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy: STOCKS_FRAGMENT ${viewModel.tab.value}")
        super.onDestroy()
    }

    override fun onDestroyView() {
        Log.e(TAG, "onDestroyView: STOCKS_FRAGMENT ${viewModel.visibleTickers}")
        super.onDestroyView()
        _binding = null
    }

    // Result processing
    private fun handleResult(result: Resource<List<Stock>>, stockAdapter: StockAdapter) {
        binding.apply {
            Log.d(TAG, "handleResult: --- --- --- ---")
            if (result.data?.isEmpty() == true)
                textViewNoStocks.visibility = View.VISIBLE
            else
                textViewNoStocks.visibility = View.GONE
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    textViewError.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
                Resource.Status.LOADING -> {
                    textViewError.visibility = View.GONE
//                        if (!swipeRefreshLayout.isRefreshing)
                    progressBar.visibility = View.VISIBLE
                }
                Resource.Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                    Log.e(TAG, "onCreateView: ${result.error}")
                    showSnackBar(result.error?.message!!)
                    textViewError.visibility = View.VISIBLE
                    textViewNoStocks.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
            }
            stockAdapter.submitList(result.data)
        }
    }

    // Helper methods
    private fun decorateStockList(recyclerView: RecyclerView) {
        recyclerView.apply {
            val endMargin = resources
                .getDimensionPixelSize(R.dimen.activity_horizontal_margin)
            val defaultMargin = resources
                .getDimensionPixelSize(R.dimen.stock_item_vertical_margin)
            val alterBackground = ResourcesCompat
                .getDrawable(resources, R.drawable.bg_light_shape, context.theme)!!
            val mainBackground = ResourcesCompat
                .getDrawable(resources, R.drawable.bg_dark_shape, context.theme)!!

            val decoration =
                StockItemDecoration(endMargin, defaultMargin, alterBackground, mainBackground)
            addItemDecoration(decoration)
            hasFixedSize()
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(
            activity?.findViewById(android.R.id.content)!!,
            message, Snackbar.LENGTH_LONG
        ).show()
    }

    companion object {
        private val TAG = "${StocksFragment::class.java.simpleName}_TAG"
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int) = StocksFragment().apply {
            arguments = Bundle().apply { putInt(ARG_SECTION_NUMBER, sectionNumber) }
        }
    }

}