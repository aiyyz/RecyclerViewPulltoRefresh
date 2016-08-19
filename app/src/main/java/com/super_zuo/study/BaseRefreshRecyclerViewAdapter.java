package com.super_zuo.study;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

/**
 * Created by super-zuo on 16-7-1.
 */
public abstract class BaseRefreshRecyclerViewAdapter extends RecyclerView.Adapter {
    private final int VIEW_TYPE_REFRESH_HEADER = 0;
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_REFRESH_FOOTER = 2;
    private TextView tv_footer;
    private boolean footerClickRefresh = false;
    private Context context;
    private int failedFooterState;

    public List getData() {
        return data;
    }

    public List data;
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
        context = parent.getContext();
        switch (viewType) {
            case VIEW_TYPE_REFRESH_HEADER:
                View headerView = View
                        .inflate(context, R.layout.view_refresh_header, null);
                this.headerView = headerView;
                viewHolder = new RefreshHeaderViewHolder(headerView);
                break;
            case VIEW_TYPE_ITEM:
                viewHolder = onCreateItemViewHolder(parent);
                break;
            case VIEW_TYPE_REFRESH_FOOTER:
                View footerView = LayoutInflater.from(context)
                        .inflate(R.layout.view_refresh_footer, parent, false);
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
        if (failedFooterState != 0) {
            setFooterState(failedFooterState);
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
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
            if (holder instanceof RefreshFooterViewHolder) {
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

    public synchronized void setFooterVisible(boolean b) {
        if (b) {
            loadMore = true;
            notifyItemInserted(getItemCount());
        } else {
            loadMore = false;
            notifyDataSetChanged();
        }
    }
    public boolean isFooterVisible(){
        if (data != null) {
            return getItemCount() == data.size() + 2;
        }
        return false;
    }
    public void setFooterRefreshFailState() {
        if (tv_footer != null) {
            tv_footer.setText(context.getResources().getString(R.string.click_retry));
        }
        footerClickRefresh = true;
    }

    public void setHeaderState(int i) {
        switch (i) {
            case 0:
                pb.clearAnimation();
                ((AnimationDrawable) pb.getBackground()).stop();
                tv_loading.setText(context.getResources().getString(R.string.xlistview_header_hint_normal));

                break;
            case 1:
                pb.clearAnimation();
                ((AnimationDrawable) pb.getBackground()).stop();
                tv_loading.setText(context.getResources().getString(R.string.xlistview_header_hint_ready));
                break;
            case 2:
                pb.clearAnimation();
                ((AnimationDrawable) pb.getBackground()).start();
                tv_loading.setText(context.getResources().getString(R.string.xlistview_header_hint_loading));
                break;
        }
    }

    /**
     * if footer has been GC or hadn't been inflater, this method will no use,because tv_footer is null,so I set the footer text in
     * {@link #onAttachedToRecyclerView(RecyclerView)}
     *
     * @param i 0:loading 1:load fail 2:no more data
     */
    public void setFooterState(int i) {
        if (tv_footer == null) {
            failedFooterState = i;
            return;
        }
        switch (i) {
            case 0:// loading
                tv_footer.setText(context.getResources().getString(R.string.xlistview_header_hint_loading));
                break;
            case 1://load fail
                setFooterRefreshFailState();
                break;
            case 2://no more data
                tv_footer.setText(context.getResources().getString(R.string.xlistview_header_hint_have_bottom_line));
                break;
            default:
                tv_footer.setText(context.getResources().getString(R.string.xlistview_header_hint_loading));
                break;
        }
    }

    public int getHeaderPaddingTop() {
        return headerView.getPaddingTop();
    }


    private class RefreshHeaderViewHolder extends RecyclerView.ViewHolder {

        public RefreshHeaderViewHolder(final View headerView) {
            super(headerView);
            pb = (ImageView) headerView.findViewById(R.id.pb);
            tv_loading = (TextView) headerView.findViewById(R.id.tv_loading);
            headerView.post(new Runnable() {
                @Override
                public void run() {
                    headerViewMeasuredHeight = headerView.getMeasuredHeight();
                    setHeaderPadding();
                }
            });
        }
    }

    private class RefreshFooterViewHolder extends RecyclerView.ViewHolder {
        public RefreshFooterViewHolder(View footerView) {
            super(footerView);
            tv_footer = (TextView) footerView.findViewById(R.id.tv_footer);
            footerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (footerClickRefresh && footerClickListener != null) {
                        footerClickListener.onFooterClick();
                    }
                }
            });
        }
    }

    public interface FooterClickListener {
        void onFooterClick();
    }

    public void setFooterClickListener(FooterClickListener footerClickListener) {
        this.footerClickListener = footerClickListener;
    }

    private FooterClickListener footerClickListener;


}
