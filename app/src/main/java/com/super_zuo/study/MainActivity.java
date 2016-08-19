package com.super_zuo.study;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BaseRefreshRecyclerView rcv_test;
    private TestRecyclerViewAdapter madapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rcv_test = (BaseRefreshRecyclerView) findViewById(R.id.rcv_test);
        madapter = new TestRecyclerViewAdapter();
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        rcv_test.setLayoutManager(staggeredGridLayoutManager);
        rcv_test.addItemDecoration(new SimpleItemDecoration(20,3));
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        ArrayList list = new ArrayList();
        for (int i = 0; i < 15; i++) {
            list.add(i);
        }
        madapter.setData(list);
        rcv_test.setAdapter(madapter);
        rcv_test.setOnRefreshAndLoadMoreListener(new BaseRefreshRecyclerView.OnRefreshAndLoadMoreListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(MainActivity.this, "Refreshing", Toast.LENGTH_SHORT).show();
                rcv_test.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rcv_test.completeRefresh();
                    }
                }, 3000);
            }

            @Override
            public void onLoadMore() {
                rcv_test.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        List data = madapter.getData();
                        for (int i = 0; i < 10; i++) {
                            data.add(i * 1000);
                        }
                        madapter.setData(data);
                        madapter.notifyDataSetChanged();
                        rcv_test.completeLoadMore();
                    }
                }, 3000);
            }
        });

    }

    /**
     * 模拟数据回调，刷新和加载更多共用一个回调。completeRefresh()与completeLoadMore()
     * 需要分刷新和加载更多不同情况分别调用
     */
    public class dataCallBack {
        private boolean isLoadMore;

        void onSuccess(){
            rcv_test.completeLoading();//必须调用，重置状态。
            List data = new ArrayList();//解析后的数据。
                if (data != null && data.size() > 0) {
                    if (isLoadMore) {
                        List products = rcv_test.getData();
                        if (products != null) {
                            int size = products.size();
                            products.addAll(data);
                            madapter.notifyItemInserted(size + 1);
                        } else {
                            madapter.setData(data);
                            madapter.notifyDataSetChanged();
                        }
                        rcv_test.completeLoadMore();
                    } else {
                        madapter.setData(data);
                        madapter.notifyDataSetChanged();
                        rcv_test.completeRefresh();
                    }


                } else {
                    rcv_test.noMoreData();
                }
        }
        void onFailed(){
            rcv_test.completeLoading();//必须调用
            madapter.setFooterRefreshFailState();
        }
    }
}
