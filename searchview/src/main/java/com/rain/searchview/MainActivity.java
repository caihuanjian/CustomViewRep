package com.rain.searchview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.rain.searchview.views.SearchView;

public class MainActivity extends AppCompatActivity {

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchView = (SearchView) findViewById(R.id.searchview);
    }

    public void onClick(View view) {
        searchView.start();
    }

    public void stop(View view) {
        searchView.stop();
    }
}
