package com.super_zuo.study;

import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by super-zuo on 16-7-1.
 */
public abstract class BaseRefreshRecyclerViewAdapater extends RecyclerView.Adapter {
    private final int VIEW_TYPE_REFRESH_HEADER = 0;
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_REFRESH_FOOTER = 2;

    public List getData() {
        return data;
    }

    private List data;
    private int headerViewMeasuredHeight;
    private View headerView;
    public ImageView pb;
    public TextView tv_loading;

    private boolean loadMore = false;

    public void setData(List data) {
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_REFRESH_HEADER:
                View headerView = View
                        .inflate(parent.getContext(), R.layout.view_refresh_header, null);
                this.headerView = headerView;
                viewHolder = new RefreshHeaderViewHolder(headerView);
                break;
            case VIEW_TYPE_ITEM:
                viewHolder = onCreateItemViewHolder(parent);
                break;
            case VIEW_TYPE_REFRESH_FOOTER:
                View footerView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_refresh_header, parent, false);
                viewHolder = new RefreshFooterViewHolder(footerView);
                break;
        }
        return viewHolder;
    }

    protected abstract RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent);

    protected abstract void onBindItemViewHolder(RecyclerView.ViewHolder holder, int i);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        switch (itemViewType) {
            case VIEW_TYPE_ITEM:
                onBindItemViewHolder(holder, position - 1);
                break;
            case VIEW_TYPE_REFRESH_HEADER:
                prepareHeaderView(holder);
                break;
            case VIEW_TYPE_REFRESH_FOOTER:
                prepareFooterView(holder);
                break;
        }
    }

    private void prepareFooterView(RecyclerView.ViewHolder holder) {

    }

    private void prepareHeaderView(RecyclerView.ViewHolder holder) {
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_REFRESH_HEADER;
        } else if (position == 1 + data.size()) {
            return VIEW_TYPE_REFRESH_FOOTER;
        }
        return VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        if (data == null) {
            return 1;
        }
        if (loadMore) {
            return data.size() + 2;
        } else {
            return data.size() + 1;
        }
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager != null && layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return getItemViewType(position) == VIEW_TYPE_REFRESH_HEADER || getItemViewType(position) == VIEW_TYPE_REFRESH_FOOTER
                            ? gridLayoutManager.getSpanCount() : 1;
                }
            });
        }
        if (layoutManager != null && layoutManager instanceof LinearLayoutManager) {

        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        View itemView = holder.itemView;
        ViewGroup.LayoutParams lp = itemView.getLayoutParams();
        if (lp == null) {
            return;
        }
        if (holder instanceof RefreshHeaderViewHolder || holder instanceof RefreshFooterViewHolder) {

            if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                int position = holder.getLayoutPosition();
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
                if (position == 0 || position == getItemCount() - 1) {

                }
            }
        }
    }

    public int getHeaderRefreshHeight() {
        return headerViewMeasuredHeight;
    }

    public void setHeaderPadding() {
        setHeaderPadding(-headerViewMeasuredHeight);
    }

    public void setHeaderPadding(int measuredHeight) {
        if (headerView != null) {
            headerView.setPadding(0, measuredHeight, 0, 0);
        }
    }

    public void setFooterVisible(boolean b) {
        if (b) {
            loadMore = true;
            notifyItemInserted(getItemCount());
        } else {
            loadMore = false;
            notifyItemRemoved(getItemCount());
        }
    }

    public void setHeaderState(int i) {
        switch (i) {
            case 0:
                pb.clearAnimation();
                ((AnimationDrawable) pb.getBackground()).stop();
                break;
            case 1:
                pb.clearAnimation();
                ((AnimationDrawable) pb.getBackground()).stop();
                break;
            case 2:
                pb.clearAnimation();
                ((AnimationDrawable) pb.getBackground()).start();
                break;
        }
    }


    private class RefreshHeaderViewHolder extends RecyclerView.ViewHolder {

        public RefreshHeaderViewHolder(final View headerView) {
            super(headerView);
            pb = (ImageView) headerView.findViewById(R.id.pb);
            tv_loading = (TextView) headerView.findViewById(R.id.tv_loading);
            headerView.measure(0, 0);
            headerViewMeasuredHeight = headerView.getMeasuredHeight();
            setHeaderPadding();
        }
    }

    private class RefreshFooterViewHolder extends RecyclerView.ViewHolder {
        public RefreshFooterViewHolder(View footerView) {
            super(footerView);
            ImageView pb = (ImageView) footerView.findViewById(R.id.pb);
            TextView tv_loading = (TextView) footerView.findViewById(R.id.tv_loading);
        }
    }
}
