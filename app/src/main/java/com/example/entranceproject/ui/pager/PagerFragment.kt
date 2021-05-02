package com.example.entranceproject.ui.pager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.entranceproject.R
import com.example.entranceproject.databinding.FragmentPagerBinding
import com.example.entranceproject.ui.search.SearchFragment
import com.google.android.material.tabs.TabLayoutMediator

class PagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPagerBinding.inflate(inflater)

        binding.apply {
            // Setting Toolbar
            searchBar.setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container, SearchFragment.newInstance())
                    .commit()
            }

            // Setting ViewPager2
            viewPager.adapter = PagerAdapter(this@PagerFragment)
            TabLayoutMediator(tabsLayout, viewPager) { tab, position ->
                tab.text = resources.getString(PagerAdapter.TAB_TITLES[position])
            }.attach()
        }

        return binding.root
    }

    // Lifecycle callbacks to see lifecycle events on log
    override fun onResume() {
        Log.e(TAG, "onResume: PAGER_FRAGMENT ")
        super.onResume()
    }

    override fun onStart() {
        Log.e(TAG, "onStart: PAGER_FRAGMENT ")
        super.onStart()
    }

    override fun onStop() {
        Log.e(TAG, "onStop: PAGER_FRAGMENT ")
        super.onStop()
    }

    override fun onPause() {
        Log.e(TAG, "onPause: PAGER_FRAGMENT ")
        super.onPause()
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy: PAGER_FRAGMENT ")
        super.onDestroy()
    }

    companion object {
        private val TAG = "${PagerFragment::class.java.simpleName}_TAG"

        @JvmStatic
        fun newInstance() = PagerFragment()
    }
}