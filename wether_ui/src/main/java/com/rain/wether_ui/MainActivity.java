package com.rain.wether_ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.rain.wether_ui.views.SunView;

public class MainActivity extends AppCompatActivity {


    private SunView sunView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sunView = (SunView) findViewById(R.id.sunview);
    }

    public void onClick(View view) {
        sunView.setCurTimeWithAnim(900);
    }
}
