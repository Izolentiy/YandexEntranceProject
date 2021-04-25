package com.example.entranceproject.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.entranceproject.R
import com.example.entranceproject.data.RECENT_REQUESTS
import com.example.entranceproject.data.SUGGESTIONS
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.databinding.FragmentSearchBinding
import com.example.entranceproject.repository.Resource
import com.example.entranceproject.ui.main.MainViewModel
import com.example.entranceproject.ui.stocks.StockAdapter
import com.example.entranceproject.ui.stocks.StockItemDecoration
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private var showSuggestions = true

    private val onStarClickListener: (stock: Stock) -> Unit = { stock ->
        viewModel.updateFavorite(stock.copy(isFavorite = !stock.isFavorite))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        /*val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            TODO("Do something")
        }*/

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.searchBar.apply {
            setNavigationOnClickListener {
                showSuggestions = !showSuggestions
                updateVisibility()
            }
        }

        // Configure completion hints
        val suggestionItemAdapter = StringSuggestionItemAdapter()
        val recentItemAdapter = StringSuggestionItemAdapter()
        binding.layoutSearchSuggestions.apply {
            // Suggestion list
            decorateSuggestionList(recyclerViewPopular)
            recyclerViewPopular.adapter = suggestionItemAdapter
            suggestionItemAdapter.submitList(SUGGESTIONS)

            // Recent search list
            decorateSuggestionList(recyclerViewRecent)
            recyclerViewRecent.adapter = recentItemAdapter
            recentItemAdapter.submitList(RECENT_REQUESTS)
        }

        // Configure stock list
        val stockAdapter = StockAdapter(onStarClickListener)
        binding.layoutSearchResult.apply {
            swipeRefreshLayout.setOnRefreshListener {
//                    viewModel.refreshData()
                showSnackBar("Woo roo roo")
                swipeRefreshLayout.isRefreshing = false
            }

            decorateStockList(recyclerViewStocks)
            recyclerViewStocks.adapter = stockAdapter
            recyclerViewStocks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    /*if (newState == RecyclerView.SCROLL_STATE_IDLE)
                        viewModel.visibleTickers.value =
                            stockAdapter.getCurrentVisibleItems().map(Stock::ticker)*/
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    /*if (viewModel.visibleTickers.value.isEmpty())
                        viewModel.visibleTickers.value =
                            stockAdapter.getCurrentVisibleItems().map(Stock::ticker)*/
                }
            })

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

    private fun updateVisibility() {
        binding.layoutSearchSuggestions.root
            .visibility = if (showSuggestions) View.VISIBLE else View.GONE

        binding.appbarSearchResult.root
            .visibility = if (showSuggestions) View.GONE else View.VISIBLE
        binding.layoutSearchResult.root
            .visibility = if (showSuggestions) View.GONE else View.VISIBLE
    }

    private fun decorateStockList(
        recyclerView: RecyclerView
    ) {
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

    private fun decorateSuggestionList(
        recyclerView: RecyclerView
    ) {
        recyclerView.apply {
            layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)

            val endMargin = resources
                .getDimensionPixelSize(R.dimen.activity_horizontal_margin)
            val defaultMargin = resources
                .getDimensionPixelSize(R.dimen.suggestion_item_margin)
            addItemDecoration(SuggestionItemDecoration(endMargin, defaultMargin))
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
        private val TAG = "${SearchFragment::class.java}_TAG"

        @JvmStatic
        fun newInstance() = SearchFragment().apply {
            arguments = Bundle().apply { }
        }
    }
}