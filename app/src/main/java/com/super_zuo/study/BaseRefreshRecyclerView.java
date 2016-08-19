package com.super_zuo.study;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;


/**
 * Created by super-zuo on 16-7-1.
 */
public class BaseRefreshRecyclerView extends RecyclerView {

    private static final int STATE_LOADING_MORE = 3;
    private BaseRefreshRecyclerViewAdapter mAdapter;
    private int headerRefreshHeight;
    private float startY;
    private final int STATE_PULL_TO_REFRESH = 0;
    private final int STATE_LOADING = 1;
    private final int STATE_RELASE_TO_REFRESH = 2;
    private int currentState = STATE_PULL_TO_REFRESH;
    private int currentDist = 0;
    private ValueAnimator animator_hide_header;
    private int firstCompletelyVisibleItemPosition;
    private int lastVisibleItemPosition = 0;
    private OnRefreshAndLoadMoreListener onRefreshAndLoadMoreListener;
    boolean hasInit = false;
    private List data;

    public void setNoMoreData(boolean noMoreData) {
        this.noMoreData = noMoreData;
    }

    private boolean noMoreData;

    public void setOnRefreshAndLoadMoreListener(OnRefreshAndLoadMoreListener onRefreshAndLoadMoreListener) {
        this.onRefreshAndLoadMoreListener = onRefreshAndLoadMoreListener;
    }


    public void setRefreshAble(boolean refreshAble) {
        this.refreshAble = refreshAble;
    }

    private boolean refreshAble = true;

    public BaseRefreshRecyclerView(Context context) {
        this(context, null, 0);
    }

    public BaseRefreshRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseRefreshRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Adapter adapter = getAdapter();
        if (!(adapter instanceof BaseRefreshRecyclerViewAdapter)) {
            throw new IllegalArgumentException("the adapter must extents BaseRefreshRecyclerViewAdapter");
        }
        mAdapter = (BaseRefreshRecyclerViewAdapter) adapter;
        mAdapter.setFooterClickListener(new BaseRefreshRecyclerViewAdapter.FooterClickListener() {
            @Override
            public void onFooterClick() {
                if (currentState == STATE_LOADING || currentState == STATE_LOADING_MORE || noMoreData) {
                    return;
                }
                onLoadMore();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (!refreshAble) {
            return super.dispatchTouchEvent(e);
        }
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentDist = 0;
                startY = e.getY();
                headerRefreshHeight = mAdapter.getHeaderRefreshHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentState == STATE_LOADING || currentState == STATE_LOADING_MORE) {
                    break;
                }
                float tmpY = e.getY();
                float ranY = 1.5f;
                if (currentState == STATE_PULL_TO_REFRESH) {
                    if (tmpY < startY) {
                        if (lastVisibleItemPosition == mAdapter.getItemCount() - 1 && getScrollState() == SCROLL_STATE_IDLE) {
                            loadMore();
                        }
                        break;
                    }
                    if ((tmpY - startY) / ranY <= this.headerRefreshHeight) {
                        currentDist = (int) ((tmpY - startY) / ranY);
                        mAdapter.setHeaderPadding((int) ((tmpY - startY) / ranY - this.headerRefreshHeight));
                        initAnimationHideHeader();
                    } else if (firstCompletelyVisibleItemPosition >= 0 && firstCompletelyVisibleItemPosition <= 1) {
                        currentState = STATE_RELASE_TO_REFRESH;
                        changeWightState();
                    }
                }
                if (currentState == STATE_RELASE_TO_REFRESH) {
                    changeWightState();
                    currentDist = (int) ((tmpY - startY) / ranY - this.headerRefreshHeight);
                    mAdapter.setHeaderPadding(currentDist);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (currentState == STATE_LOADING) {
                    break;
                }
                if (currentState == STATE_LOADING_MORE) {
                    break;
                }
                if (currentState == STATE_PULL_TO_REFRESH && currentDist > 10) {
                    if (animator_hide_header == null) {
                        initAnimationHideHeader();
                    }
                    animator_hide_header.start();
                }
                if (currentState == STATE_RELASE_TO_REFRESH) {
                    currentState = STATE_LOADING;
                    changeWightState();
                    View view = getLayoutManager().getChildAt(0);
                    if (view.getTop() <= 5) {
                        onRefresh();
                        initAnimaionRelasetoRefresh();
                    } else {
                        currentDist = -view.getTop();
                        animator_hide_header.start();
                        currentState = STATE_PULL_TO_REFRESH;
                    }

                }
                currentDist = 0;
                break;
        }
        return super.dispatchTouchEvent(e);

    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            firstCompletelyVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        }
        if (layoutManager instanceof GridLayoutManager) {
            lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            firstCompletelyVisibleItemPosition = ((GridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] last = null;
            int[] first = null;
            if (!hasInit) {
                last = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                first = new int[last.length];
                hasInit = true;
            }
            int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(last);
            int[] firstCompletelyVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPositions(first);
            firstCompletelyVisibleItemPosition = firstCompletelyVisibleItemPositions[0];
            for (int i : lastVisibleItemPositions) {
                lastVisibleItemPosition = i > lastVisibleItemPosition ? i : lastVisibleItemPosition;
            }
        }

        if (dy > 0 && lastVisibleItemPosition == mAdapter.getItemCount() - 1) {
            loadMore();
        }
    }

    private void loadMore() {
        if (currentState == STATE_LOADING || currentState == STATE_LOADING_MORE || noMoreData) {
            return;
        }
        onLoadMore();
    }


    private void initAnimaionRelasetoRefresh() {
        ValueAnimator animator_relase_torefresh = ValueAnimator.ofInt(currentDist, 0);
        animator_relase_torefresh.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAdapter.setHeaderPadding((Integer) valueAnimator.getAnimatedValue());
            }
        });
        animator_relase_torefresh.setDuration(400);
        animator_relase_torefresh.start();
    }

    private void initAnimationRefreshOver() {
        if (headerRefreshHeight == 0 || mAdapter.getHeaderPaddingTop() < 0) {
            return;
        }
        ValueAnimator animator_refresh_over = ValueAnimator.ofInt(0, -headerRefreshHeight);
        animator_refresh_over.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAdapter.setHeaderPadding((Integer) valueAnimator.getAnimatedValue());
            }
        });
        animator_refresh_over.setDuration(200);
        animator_refresh_over.start();
    }

    private void initAnimationHideHeader() {
        animator_hide_header = ValueAnimator.ofInt(-currentDist, -headerRefreshHeight);
        animator_hide_header.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAdapter.setHeaderPadding((Integer) valueAnimator.getAnimatedValue());
            }
        });
        animator_hide_header.setDuration(100);
    }

    private void onRefresh() {
        setNoMoreData(false);
        if (onRefreshAndLoadMoreListener != null) {
            onRefreshAndLoadMoreListener.onRefresh();
        }
    }

    public void completeRefresh() {
        currentState = STATE_PULL_TO_REFRESH;
        changeWightState();
        initAnimationRefreshOver();
        if (mAdapter.isFooterVisible()) {
            mAdapter.setFooterVisible(false);
        }
//            mAdapter.setFooterState(0);
    }

    public void completeLoadMore() {
        if (mAdapter.isFooterVisible()) {
            mAdapter.setFooterVisible(false);
        }
        currentState = STATE_PULL_TO_REFRESH;
    }

    private void onLoadMore() {

        if (onRefreshAndLoadMoreListener != null) {
            currentState = STATE_LOADING_MORE;
            onRefreshAndLoadMoreListener.onLoadMore();
            mAdapter.setFooterState(0);
            mAdapter.setFooterVisible(true);
        }
    }

    private void changeWightState() {
        switch (currentState) {
            case STATE_PULL_TO_REFRESH:
                mAdapter.setHeaderState(0);
                break;
            case STATE_RELASE_TO_REFRESH:
                mAdapter.setHeaderState(1);
                break;
            case STATE_LOADING:
                mAdapter.setHeaderState(2);
                break;
        }
    }

    public void noMoreData() {
        mAdapter.setFooterState(2);
        mAdapter.setFooterVisible(true);
        setNoMoreData(true);
    }

    public void completeLoading() {
        currentState = STATE_PULL_TO_REFRESH;
    }

    public List getData() {
        return data;
    }

    public interface OnRefreshAndLoadMoreListener {
        void onRefresh();

        void onLoadMore();
    }

}
