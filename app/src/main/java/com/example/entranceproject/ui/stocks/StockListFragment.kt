package com.example.entranceproject.ui.stocks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.entranceproject.R
import com.example.entranceproject.data.model.Stock
import com.example.entranceproject.databinding.LayoutStockListBinding
import com.example.entranceproject.repository.Resource
import com.example.entranceproject.ui.search.SearchFragment
import com.google.android.material.snackbar.Snackbar

/**
 * It's a draft only
 */
class StockListFragment : Fragment() {

    private var _binding: LayoutStockListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutStockListBinding.inflate(inflater, container, false)
        return binding.root
    }

    protected fun handleResult(result: Resource<List<Stock>>, stockAdapter: StockAdapter) {
        binding.apply {
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
                    progressBar.visibility = View.VISIBLE
                }
                Resource.Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                    textViewError.visibility = View.VISIBLE
                    textViewNoStocks.visibility = View.GONE
                    progressBar.visibility = View.GONE

                    showSnackBar(result.error?.message!!)
                    Log.e(TAG, "onCreateView: ${result.error}")
                }
            }
            stockAdapter.submitList(result.data)
        }
    }

    protected fun decorateStockList(recyclerView: RecyclerView) {
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

    protected fun showSnackBar(message: String) {
        Snackbar.make(
            activity?.findViewById(android.R.id.content)!!,
            message, Snackbar.LENGTH_LONG
        ).show()
    }

    private val TAG by lazy { "${StockListFragment::class.java.simpleName}_TAG" }

}