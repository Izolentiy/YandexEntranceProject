package com.example.entranceproject.ui.pager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.entranceproject.R
import com.example.entranceproject.ui.stocks.StocksFragment

class PagerAdapter(hostFragment: Fragment) :
    FragmentStateAdapter(hostFragment) {
    companion object {
        val TAB_TITLES = listOf(
            R.string.stocks,
            R.string.favorite
        )
        val NUM_PAGES = Tab.values().size
    }

    override fun createFragment(position: Int): Fragment =
        StocksFragment.newInstance(position)

    override fun getItemCount(): Int = NUM_PAGES

}