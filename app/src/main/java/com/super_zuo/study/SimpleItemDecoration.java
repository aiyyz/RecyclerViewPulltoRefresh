package com.super_zuo.study;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;

/**
 * Created by super-zuo on 16-7-12.
 */
public class SimpleItemDecoration extends RecyclerView.ItemDecoration {
    private int space;
    private int span;

    public SimpleItemDecoration(int space, int span) {
        this.space = space;
        this.span = span;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        // set header and footer space zero
        outRect.bottom = space;
        if (parent.getChildLayoutPosition(view) == 0
                || parent.getChildLayoutPosition(view) == parent.getAdapter().getItemCount()) {
            outRect.top = 0;
            outRect.left = 0;
            outRect.right = 0;

        } else if (parent.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            int spanIndex = lp.getSpanIndex();
            Log.e("dadadse",parent.getChildLayoutPosition(view)+"+++++++++"+spanIndex);
            if (spanIndex == span-1) {
                outRect.left = space;
                outRect.right = space;

            } else {
                outRect.left = space;
                outRect.right = 0;
            }
        } else if (parent.getLayoutManager() instanceof GridLayoutManager){
            if (parent.getChildLayoutPosition(view) % span == 0) {
                outRect.left = space;
                outRect.right = space;
            } else {
                outRect.left = space;
                outRect.right = 0;
            }
        }

    }
}
