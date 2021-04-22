package com.example.entranceproject.ui.pager

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.entranceproject.R
import com.example.entranceproject.ui.stocks.StocksFragment

class PagerAdapter(context: Context) :
    FragmentStateAdapter(context as FragmentActivity) {
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