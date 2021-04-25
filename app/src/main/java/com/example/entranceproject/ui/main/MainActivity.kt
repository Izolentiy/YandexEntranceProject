package com.example.entranceproject.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.entranceproject.R
import com.example.entranceproject.databinding.ActivityMainBinding
import com.example.entranceproject.ui.pager.PagerFragment
import com.example.entranceproject.ui.search.SearchFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private val pagerFragment: PagerFragment by lazy { PagerFragment.newInstance() }
    private val searchFragment: SearchFragment by lazy { SearchFragment.newInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        supportFragmentManager.beginTransaction().addToBackStack(null)
//            .replace(R.id.fragment_container, searchFragment).commit()
        supportFragmentManager.beginTransaction().addToBackStack(null)
            .replace(R.id.fragment_container, pagerFragment).commit()
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

    companion object {
        private val TAG = "${MainActivity::class.java.simpleName}_TAG"
    }

}