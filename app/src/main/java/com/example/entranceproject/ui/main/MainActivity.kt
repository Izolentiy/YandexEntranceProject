package com.example.entranceproject.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.get
import com.example.entranceproject.databinding.ActivityMainBinding
import com.example.entranceproject.ui.pager.PagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            // SearchView setting
            searchView.setOnQueryTextListener(this@MainActivity)
            searchView.setOnQueryTextFocusChangeListener { view, hasFocus ->
                Log.e(TAG, "onCreate: ${searchView[0]}")
                Log.e(TAG, "onCreate: $view, $hasFocus" )
            }

//            Log.d("WEB_SOCKET_TAG", "onCreate: Subscribed to web socket events")
//            viewModel.subscribeToSocketEvents(listOf("AAPL", "YNDX"))

            // TabLayout setting
            viewPager.adapter = PagerAdapter(this@MainActivity)
            TabLayoutMediator(tabsLayout, viewPager) { tab, position ->
                tab.text = resources.getString(PagerAdapter.TAB_TITLES[position])
            }.attach()
        }
    }

    // Search query change listener methods
    override fun onQueryTextSubmit(query: String?): Boolean {
//        viewModel.searchStocks(query.orEmpty())
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }

    companion object {
        private val TAG = "${MainActivity::class.java.simpleName}_TAG"
    }

}