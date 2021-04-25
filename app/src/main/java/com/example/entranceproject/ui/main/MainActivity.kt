package com.example.entranceproject.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import com.example.entranceproject.R
import com.example.entranceproject.databinding.ActivityMainBinding
import com.example.entranceproject.databinding.AppBarTabLayoutBinding
import com.example.entranceproject.databinding.ContainerViewPagerBinding
import com.example.entranceproject.ui.pager.PagerAdapter
import com.example.entranceproject.ui.search.SearchFragment
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
        setSupportActionBar(binding.searchBar)

        binding.apply {

            // Search bar configuration
            // when user clicks on search bar, container will be replaced with SearchFragment
            configureSearchBar()

            // Fragment configuration (fragment depends on state of the searchBar)
            configureTabLayout()

            // SearchView setting
//            searchView.setOnQueryTextListener(this@MainActivity)
//            searchView.setOnQueryTextFocusChangeListener { view, hasFocus ->
//                Log.e(TAG, "onCreate: ${searchView[0]}")
//                Log.e(TAG, "onCreate: $view, $hasFocus" )
//            }
//            searchBar.setOnClickListener {
//                tabsLayout.visibility = View.GONE
//                viewPager.visibility = View.GONE
//                supportFragmentManager.beginTransaction()
//                    .replace(R.id.container, searchFragment)
//                    .commit()
//                val appBar = findViewById<AppBarLayout>(R.id.app_bar_layout)
//            }

//            Log.d("WEB_SOCKET_TAG", "onCreate: Subscribed to web socket events")
//            viewModel.subscribeToSocketEvents(listOf("AAPL", "YNDX"))

//            container.visibility = View.GONE
//            tabsLayout.visibility = View.GONE
//            viewPager.visibility = View.GONE

            // SearchFragment setting
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.container, SearchFragment.newInstance())
//                .commit()

            // TabLayout setting
//            viewPager.adapter = PagerAdapter(this@MainActivity)
//            TabLayoutMediator(tabsLayout, viewPager) { tab, position ->
//                tab.text = resources.getString(PagerAdapter.TAB_TITLES[position])
//            }.attach()
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
        supportFragmentManager.getBackStackEntryAt(0)
    }


    // Layout configuration methods
    private fun configureSearchBar() {
        binding.apply {
            searchBar.setOnClickListener {
                with(supportFragmentManager) {
                    beginTransaction()
                        .replace(fragmentContainer.id, searchFragment)
                        .addToBackStack(null)
                        .commit()
                    val backIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_back, theme)
                    if (backStackEntryCount == 0) searchBar.navigationIcon = backIcon
//                    searchBar.navigationIcon = if (backStackEntryCount == 0) backIcon else null
                }
            }
        }
    }

    private fun configureTabLayout() {
        binding.apply {
            // App bar configuration
            val tabsLayout = AppBarTabLayoutBinding.inflate(layoutInflater).tabsLayout
            val appbarContainerIndex = appBarLayout.indexOfChild(appBarContainer)
            Log.d(TAG, "configureTabLayout: $appbarContainerIndex")

            appBarLayout.removeViewAt(appbarContainerIndex)
            appBarLayout.addView(tabsLayout, appbarContainerIndex)

            // Container configuration
            val viewPager = ContainerViewPagerBinding.inflate(layoutInflater).viewPager
            val fragmentContainerIndex = coordinatorLayout.indexOfChild(fragmentContainer)
            Log.d(TAG, "configureTabLayout: $fragmentContainerIndex")

//            coordinatorLayout.removeViewAt(fragmentContainerIndex)
            coordinatorLayout.addView(viewPager, fragmentContainerIndex)
            viewPager.adapter = PagerAdapter(this@MainActivity)
            TabLayoutMediator(tabsLayout, viewPager) { tab, position ->
                tab.text = resources.getString(PagerAdapter.TAB_TITLES[position])
            }.attach()
        }
    }


    companion object {
        private val TAG = "${MainActivity::class.java.simpleName}_TAG"
    }

}