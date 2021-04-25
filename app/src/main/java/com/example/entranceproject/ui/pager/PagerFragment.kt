package com.example.entranceproject.ui.pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.entranceproject.R
import com.example.entranceproject.databinding.FragmentPagerBinding
import com.example.entranceproject.ui.search.SearchFragment
import com.google.android.material.tabs.TabLayoutMediator

class PagerFragment : Fragment() {

    /*private var _binding: FragmentPagerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPagerBinding.inflate(inflater)

        binding.apply {
            searchBar.setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container, SearchFragment.newInstance())
                    .commit()
            }

            // Setting ViewPager2
            viewPager.adapter = PagerAdapter(requireContext())
            TabLayoutMediator(tabsLayout, viewPager) { tab, position ->
                tab.text = resources.getString(PagerAdapter.TAB_TITLES[position])
            }.attach()
        }

        return binding.root
    }


    companion object {
        private val TAG = "${PagerFragment::class.java.simpleName}_TAG"

        @JvmStatic
        fun newInstance() = PagerFragment()
    }
}