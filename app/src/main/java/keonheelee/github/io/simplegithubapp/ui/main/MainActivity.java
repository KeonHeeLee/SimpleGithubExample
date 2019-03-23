package keonheelee.github.io.simplegithubapp.ui.main;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import keonheelee.github.io.simplegithubapp.R;
import keonheelee.github.io.simplegithubapp.ui.search.SearchActivity;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSearch = findViewById(R.id.btnActivityMainSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 저장소 검색 액티비티 호출
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });
    }
}
