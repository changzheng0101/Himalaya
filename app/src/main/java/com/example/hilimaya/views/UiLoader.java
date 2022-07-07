package com.example.hilimaya.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.hilimaya.R;
import com.example.hilimaya.base.BaseApplication;

public abstract class UiLoader extends FrameLayout {

    private View mLoadingView;
    private View mNetWorkErrorView;
    private View mEmptyView;
    private View mSuccessView;
    private OnRetryClickListener mOnRetryClickListener;


    public enum UIStatus {
        //用于记录状态
        LOADING, SUCCESS, NETWORK_ERROR, EMPTY, NONE
    }

    public UIStatus mCurrentStatus = UIStatus.NONE;

    public void updateStatus(UIStatus status) {
        mCurrentStatus = status;
        //更新UI一定要在主线程上 所以采用handler
        BaseApplication.getsHandler().post(new Runnable() {
            @Override
            public void run() {
                switchUIByCurrentStatus();
            }
        });
    }

    public UiLoader(@NonNull Context context) {
        //保证多次可以引用到同一个对象
        this(context, null);
    }

    public UiLoader(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UiLoader(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化UI
     */
    private void init() {
        switchUIByCurrentStatus();
    }

    private void switchUIByCurrentStatus() {
        //加载中
        if (mLoadingView == null) {
            mLoadingView = getLoadingView();
            addView(mLoadingView);
        }
        //根据状态设置是否可见
        mLoadingView.setVisibility(mCurrentStatus == UIStatus.LOADING ? VISIBLE : GONE);

        //网络错误
        if (mNetWorkErrorView == null) {
            mNetWorkErrorView = getNetWorkErrorView();
            addView(mNetWorkErrorView);
        }
        //根据状态设置是否可见
        mNetWorkErrorView.setVisibility(mCurrentStatus == UIStatus.NETWORK_ERROR ? VISIBLE : GONE);

        //列表为空
        if (mEmptyView == null) {
            mEmptyView = getEmptyView();
            addView(mEmptyView);
        }
        //根据状态设置是否可见
        mEmptyView.setVisibility(mCurrentStatus == UIStatus.EMPTY ? VISIBLE : GONE);

        //加载中
        if (mSuccessView == null) {
            mSuccessView = getSuccessView(this);
            addView(mSuccessView);
        }
        //根据状态设置是否可见
        mSuccessView.setVisibility(mCurrentStatus == UIStatus.SUCCESS ? VISIBLE : GONE);
    }

    protected abstract View getSuccessView(ViewGroup container); //将这个变为抽象的 用其孩子进行实现

    private View getEmptyView() {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view, this, false);
    }

    private View getNetWorkErrorView() {
        View NetworkView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_networkerror_view, this, false);
        NetworkView.findViewById(R.id.network_error_icon).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //重新获取数据
                if (mOnRetryClickListener != null) {
                    mOnRetryClickListener.onRetryClick();
                }
            }
        });
        return NetworkView;
    }

    private View getLoadingView() {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_loading_view, this, false);
    }

    public void setOnRetryClickListener(OnRetryClickListener listener) {
        this.mOnRetryClickListener = listener;
    }

    public interface OnRetryClickListener {
        void onRetryClick();
    }
}
