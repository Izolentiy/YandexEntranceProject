package com.example.entranceproject.ui.search

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.entranceproject.R
import com.example.entranceproject.data.RECENT_REQUESTS
import com.example.entranceproject.data.SUGGESTIONS
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.databinding.FragmentSearchBinding
import com.example.entranceproject.repository.Resource
import com.example.entranceproject.ui.stocks.StockAdapter
import com.example.entranceproject.ui.stocks.StockItemDecoration
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SearchFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()
    private var showSuggestions = true

    private val onSuggestionClickListener: (suggestion: CharSequence) -> Unit = { suggestion ->
        binding.searchView.setQuery(suggestion, false)
    }

    private val onStarClickListener: (stock: Stock) -> Unit = { stock ->
        viewModel.updateFavorite(stock.copy(isFavorite = !stock.isFavorite))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        // Configure search view
        binding.apply {
            searchBar.setNavigationOnClickListener { requireActivity().onBackPressed() }

            searchView.requestFocus()
            searchView.setOnQueryTextListener(this@SearchFragment)
            searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    searchView.queryHint = ""
                    val inputMethodManager = requireActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                } else {
                    val inputMethodManager = requireActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(searchView.windowToken, 0)
                    searchView.queryHint = "Find company or ticker"
                }
            }
        }

        // Configure completion hints
        val suggestionItemAdapter = HintItemAdapter(onSuggestionClickListener)
        val recentItemAdapter = HintItemAdapter(onSuggestionClickListener)
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

            lifecycleScope.launchWhenStarted {
                viewModel.stocks
                    .collect { result -> result?.let { handleResult(result, stockAdapter) } }
            }
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        // Hide keyboard when the user collapses an app
        val inputMethodManager = requireActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Search query change listener methods
    override fun onQueryTextSubmit(query: String?): Boolean {
        viewModel.searchStocks()
        showSuggestions = false
        updateVisibility()
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.updateQuery(newText ?: "Error")
        if (newText?.isEmpty() == true)
            showSuggestions = true; updateVisibility()
        return true
    }

    // Result processing
    private fun handleResult(result: Resource<List<Stock>>, stockAdapter: StockAdapter) {
        val textViewLabel = binding.appbarSearchResult.textViewLabel
        binding.layoutSearchResult.apply {
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

                    textViewLabel.text = getString(R.string.stocks_found, result.data?.size)
                }
                Resource.Status.LOADING -> {
                    textViewError.visibility = View.GONE
//                        if (!swipeRefreshLayout.isRefreshing)
                    progressBar.visibility = View.VISIBLE
                    textViewLabel.text = getString(R.string.stocks)
                }
                Resource.Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                    Log.e(TAG, "onCreateView: ${result.error}")
                    showSnackBar(result.error?.message!!)
                    textViewError.visibility = View.VISIBLE
                    textViewNoStocks.visibility = View.GONE
                    progressBar.visibility = View.GONE

                    textViewLabel.text = getString(R.string.stocks)
                }
            }
        }
    }

    // Helper methods
    private fun updateVisibility() {
        binding.layoutSearchSuggestions.root
            .visibility = if (showSuggestions) View.VISIBLE else View.GONE

        binding.appbarSearchResult.root
            .visibility = if (showSuggestions) View.GONE else View.VISIBLE
        binding.layoutSearchResult.root
            .visibility = if (showSuggestions) View.GONE else View.VISIBLE
    }

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

    private fun decorateSuggestionList(recyclerView: RecyclerView) {
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