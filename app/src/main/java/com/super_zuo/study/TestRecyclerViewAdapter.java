package com.super_zuo.study;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by super-zuo on 16-7-5.
 */
public class TestRecyclerViewAdapter extends BaseRefreshRecyclerViewAdapater {
    @Override
    protected RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test, parent,false);
        return new TestViewHolder(view);
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder holder, int i) {
        TestViewHolder testViewHolder = (TestViewHolder) holder;
        testViewHolder.tv.setText("" + i);
    }


    static class TestViewHolder extends RecyclerView.ViewHolder {

        TextView tv;

        public TestViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv);
        }
    }
}
