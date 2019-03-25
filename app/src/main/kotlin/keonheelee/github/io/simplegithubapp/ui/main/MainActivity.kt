package keonheelee.github.io.simplegithubapp.ui.main

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import keonheelee.github.io.simplegithubapp.R
import keonheelee.github.io.simplegithubapp.ui.search.SearchActivity

import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnActivityMainSearch.setOnClickListener {
            // 저장소 검색 액티비티 호출
            startActivity<SearchActivity>()
        }
    }
}
