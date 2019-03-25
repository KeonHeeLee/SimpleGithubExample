package keonheelee.github.io.simplegithubapp.ui.main

import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import keonheelee.github.io.simplegithubapp.R
import keonheelee.github.io.simplegithubapp.ui.search.SearchActivity

class MainActivity : AppCompatActivity() {

    lateinit internal var btnSearch: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSearch = findViewById(R.id.btnActivityMainSearch)
        btnSearch.setOnClickListener {
            // 저장소 검색 액티비티 호출
            startActivity(Intent(this@MainActivity, SearchActivity::class.java))
        }
    }
}
