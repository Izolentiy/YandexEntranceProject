package com.example.entranceproject.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.get
import com.example.entranceproject.R
import com.example.entranceproject.databinding.ActivityMainBinding
import com.example.entranceproject.ui.pager.PagerAdapter
import com.example.entranceproject.ui.search.SearchFragment
import com.example.entranceproject.ui.stocks.StocksFragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private val searchFragment: SearchFragment by lazy { SearchFragment.newInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            // SearchView setting
//            searchView.setOnQueryTextListener(this@MainActivity)
//            searchView.setOnQueryTextFocusChangeListener { view, hasFocus ->
//                Log.e(TAG, "onCreate: ${searchView[0]}")
//                Log.e(TAG, "onCreate: $view, $hasFocus" )
//            }
            toolbar.setOnClickListener {
                tabsLayout.visibility = View.GONE
                viewPager.visibility = View.GONE
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, searchFragment)
                    .commit()
                val appBar = findViewById<AppBarLayout>(R.id.app_bar)
            }

//            Log.d("WEB_SOCKET_TAG", "onCreate: Subscribed to web socket events")
//            viewModel.subscribeToSocketEvents(listOf("AAPL", "YNDX"))

            container.visibility = View.GONE
//            tabsLayout.visibility = View.GONE
//            viewPager.visibility = View.GONE

            // SearchFragment setting
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.container, SearchFragment.newInstance())
//                .commit()

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

    override fun onBackPressed() {
        super.onBackPressed()
    }

    companion object {
        private val TAG = "${MainActivity::class.java.simpleName}_TAG"
    }

}