package com.example.entranceproject.ui.main

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
        const val NUM_PAGES = 2
    }

    override fun createFragment(position: Int): Fragment =
        StocksFragment.newInstance(position)

    override fun getItemCount(): Int = NUM_PAGES

}