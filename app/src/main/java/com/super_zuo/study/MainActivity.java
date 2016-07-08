package com.super_zuo.study;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BaseRefreshRecyclerView rcv_test = (BaseRefreshRecyclerView) findViewById(R.id.rcv_test);
        TestRecyclerViewAdapter madapter = new TestRecyclerViewAdapter();
        rcv_test.setLayoutManager(new LinearLayoutManager(this));
        ArrayList list = new ArrayList();
        for (int i = 0; i < 250; i++) {
            list.add(i);
        }
        madapter.setData(list);
        rcv_test.setAdapter(madapter);

    }
}
