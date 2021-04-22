package com.example.entranceproject.ui.stocks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.entranceproject.R
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.databinding.FragmentStocksBinding
import com.example.entranceproject.repository.Resource
import com.example.entranceproject.ui.main.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

@AndroidEntryPoint
class StocksFragment : Fragment() {

    private var _binding: FragmentStocksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    private val onStarClickListener: (stock: Stock) -> Unit = { stock ->
        viewModel.updateFavorite(stock.copy(isFavorite = !stock.isFavorite))
    }

    private val onVisibleTickerChange: (
        oldTicker: CharSequence, newTicker: String
    ) -> Unit = { old: CharSequence, new: String ->
//        val tickers = viewModel.visibleTickers.value.toMutableSet()
//        tickers.remove(old)
//        tickers.add(new)
//        viewModel.visibleTickers.value = tickers
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
        _binding = FragmentStocksBinding.inflate(inflater, container, false)

//        Log.d(TAG, "onCreateView: Subscribed to latest prices")
//        viewModel.subscribeToPricesUpdate(viewModel.visibleTickers)
        val stockAdapter = StockAdapter(onStarClickListener, onVisibleTickerChange)
        binding.apply {
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.refreshData()
                showSnackBar("I do nothing. He he he")
                swipeRefreshLayout.isRefreshing = false
            }

            recyclerViewStocks.apply {
                adapter = stockAdapter
                val alterBackground = ResourcesCompat
                    .getDrawable(resources, R.drawable.bg_light, context.theme)!!
                val mainBackground = ResourcesCompat
                    .getDrawable(resources, R.drawable.bg_dark, context.theme)!!
                addItemDecoration(StockItemDecoration(alterBackground, mainBackground))

                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        Log.i(TAG, "onScrollStateChanged: -------------------- $newState")
                        if (newState == RecyclerView.SCROLL_STATE_IDLE)
                            viewModel.visibleTickers.value =
                                stockAdapter.getCurrentVisibleItems().map(Stock::ticker)
                        super.onScrollStateChanged(recyclerView, newState)
                    }

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        Log.i(TAG, "onScrolled: -------------------- $dy")
                        if (viewModel.visibleTickers.value.isEmpty())
                            viewModel.visibleTickers.value =
                                stockAdapter.getCurrentVisibleItems().map(Stock::ticker)
                        super.onScrolled(recyclerView, dx, dy)
                    }
                })
                hasFixedSize()
            }

            viewModel.stocks.observe(viewLifecycleOwner) { result ->
                if (result.data?.isEmpty() == true)
                    textViewNoStocks.visibility = View.VISIBLE
                else
                    textViewNoStocks.visibility = View.GONE
                when (result.status) {
                    Resource.Status.SUCCESS -> {
                        swipeRefreshLayout.isRefreshing = false
                        textViewError.visibility = View.GONE
                        progressBar.visibility = View.GONE
                        stockAdapter.submitList(result.data)
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
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        fun newInstance(sectionNumber: Int): StocksFragment {
            return StocksFragment().apply {
                arguments = Bundle().apply { putInt(ARG_SECTION_NUMBER, sectionNumber) }
            }
        }
    }
}