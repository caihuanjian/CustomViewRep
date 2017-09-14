package com.rain.pullview;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final PullViewImpl pullToRefresh = (PullViewImpl) findViewById(R.id.pull_to_refresh);
        pullToRefresh.addOnStateChageListener(new PullViewBase.OnStateChangeListener() {
            @Override
            public void onStateChange(PullViewBase.PullState oldState, PullViewBase.PullState newState) {
                Log.d("onStateChange", "old:" + oldState + ",new:" + newState);
            }
        });
        pullToRefresh.setOnRefreshListener(new PullViewBase.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(MainActivity.this, "做刷新的逻辑...", Toast.LENGTH_SHORT).show();
                pullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        pullToRefresh.refreshComplete();
                    }
                }, 1500);
            }
        });
    }
}
