package com.example.entranceproject.ui.pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.entranceproject.databinding.ContainerViewPagerBinding
import com.google.android.material.tabs.TabLayoutMediator

class PagerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ContainerViewPagerBinding.inflate(inflater)

        binding.viewPager.adapter = PagerAdapter(requireActivity())
        TabLayoutMediator(tabsLayout, binding.viewPager) { tab, position ->
            tab.text = resources.getString(PagerAdapter.TAB_TITLES[position])
        }
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}