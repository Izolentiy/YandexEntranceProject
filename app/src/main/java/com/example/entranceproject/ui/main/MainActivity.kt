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
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PagerFragment.newInstance()).commit()
    }

    companion object {
        private val TAG = "${MainActivity::class.java.simpleName}_TAG"
    }

}