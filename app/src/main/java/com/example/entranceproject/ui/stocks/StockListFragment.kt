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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.entranceproject.R
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.databinding.FragmentStocksBinding
import com.example.entranceproject.repository.Resource
import com.example.entranceproject.ui.main.MainViewModel
import com.google.android.material.snackbar.Snackbar

abstract class StockListFragment : Fragment() {

    private var _binding: FragmentStocksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    private val onStarClickListener: (stock: Stock) -> Unit = { stock ->
        viewModel.updateFavorite(stock.copy(isFavorite = !stock.isFavorite))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStocksBinding.inflate(inflater, container, false)

//        Log.d(TAG, "onCreateView: Subscribed to latest prices")
//        viewModel.subscribeToPricesUpdate(viewModel.visibleTickers)
        val stockAdapter = StockAdapter(onStarClickListener)
//        binding.apply {
        binding.layoutStockList.apply {
            configureSwipeRefreshLayout(swipeRefreshLayout)
            configureRecyclerView(recyclerViewStocks, stockAdapter)
            viewModel.stocks.observe(viewLifecycleOwner) { result ->
                displayStocks(result, stockAdapter)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected open fun configureRecyclerView(recyclerView: RecyclerView, adapter: StockAdapter) {
        with(recyclerView) {
            setAdapter(adapter)
            val endMargin = resources
                .getDimensionPixelSize(R.dimen.activity_horizontal_margin)
            val defaultMargin = resources
                .getDimensionPixelSize(R.dimen.stock_item_vertical_margin)
            val alterBackground = ResourcesCompat
                .getDrawable(resources, R.drawable.bg_light_shape, context.theme)!!
            val mainBackground = ResourcesCompat
                .getDrawable(resources, R.drawable.bg_dark_shape, context.theme)!!
            addItemDecoration(StockItemDecoration(
                endMargin, defaultMargin, alterBackground, mainBackground
            ))
            hasFixedSize()
        }
    }

    protected open fun displayStocks(result: Resource<List<Stock>>, adapter: StockAdapter) {
        with(binding.layoutStockList) {
            if (result.data?.isEmpty() == true)
                textViewNoStocks.visibility = View.VISIBLE
            else
                textViewNoStocks.visibility = View.GONE
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    textViewError.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    adapter.submitList(result.data)
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

    protected open fun configureSwipeRefreshLayout(swipeRefreshLayout: SwipeRefreshLayout) {
        swipeRefreshLayout.setOnRefreshListener {
            showSnackBar("I do nothing. He he he")
            swipeRefreshLayout.isRefreshing = false
        }
    }

    protected open fun showSnackBar(message: String) {
        Snackbar.make(
            activity?.findViewById(android.R.id.content)!!,
            message, Snackbar.LENGTH_LONG
        ).show()
    }

    companion object {
        private val TAG = "${StockListFragment::class.java.simpleName}_TAG"
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int) = StocksFragment().apply {
            arguments = Bundle().apply { putInt(ARG_SECTION_NUMBER, sectionNumber) }
        }
    }
}