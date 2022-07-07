package com.example.hilimaya.views;

import android.app.Application;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hilimaya.R;
import com.example.hilimaya.adapters.PlayListAdapter;
import com.example.hilimaya.base.BaseApplication;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE;

public class SobPopWindow extends PopupWindow {

    private final View mPopView;
    private TextView mPlayListClose;
    private RecyclerView mPlayList;
    private PlayListAdapter mAdapter;
    private TextView mPlayModeTv;
    private ImageView mPlayModeIv;
    private View mPlayModeContainer;
    private PlayActionClickListener mPlayModeListener = null;
    private View mOrderContainer;
    private ImageView mOrderIv;
    private TextView mOrderTv;

    public SobPopWindow() {
        //设置他的宽度和高度
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //有的时候 只设置setOutsideTouchable无法完成关闭动作 还需要设置第一个
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setOutsideTouchable(true);
        //将view进行载入
        mPopView = LayoutInflater.from(BaseApplication.getContext()).inflate(R.layout.pop_play_list, null);
        //设置内容
        setContentView(mPopView);
        //设置窗口进入和退出的动画
        setAnimationStyle(R.style.pop_animation);
        initView();
        initEvent();
    }

    private void initView() {
        mPlayListClose = mPopView.findViewById(R.id.play_list_close_tv);
        mPlayList = mPopView.findViewById(R.id.play_list_rv);
        //为recyclerView设置布局管理器和adapter
        mPlayList.setLayoutManager(new LinearLayoutManager(BaseApplication.getContext()));
        mAdapter = new PlayListAdapter();
        mPlayList.setAdapter(mAdapter);
        //播放模式相关的UI
        mPlayModeTv = mPopView.findViewById(R.id.play_list_play_mode_tv);
        mPlayModeIv = mPopView.findViewById(R.id.play_list_play_mode_iv);
        mPlayModeContainer = mPopView.findViewById(R.id.play_list_play_mode_container);
        //正序和逆序的UI
        mOrderIv = mPopView.findViewById(R.id.play_list_play_order_iv);
        mOrderTv = mPopView.findViewById(R.id.play_list_play_order_tv);
        mOrderContainer = mPopView.findViewById(R.id.play_list_play_order_container);
    }

    private void initEvent() {
        mPlayListClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击的时候 关闭list
                dismiss();
            }
        });
        mPlayModeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换播放模式
                if (mPlayModeListener != null) {
                    mPlayModeListener.onPlayModeClick();
                }
            }
        });
        mOrderContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换正序和逆序
                mPlayModeListener.onOrderClick();
            }
        });
    }

    /**
     * 给适配器更新数据
     *
     * @param data
     */
    public void setListData(List<Track> data) {
        mAdapter.setData(data);
    }

    public void setCurrentPlayPosition(int position) {
        if (mAdapter != null) {
            mAdapter.setCurrentPlayPosition(position);
            //保证每次播放的歌曲都在视野中
            mPlayList.scrollToPosition(position);
        }
    }

    public void SetPlayListItemClickListener(PlayListItemClickListener listener) {
        mAdapter.setOnItemClickListener(listener);
    }

    public void updatePlayMode(XmPlayListControl.PlayMode currentMode) {
        updateModeIcon(currentMode);
    }

    public void updateOrderIcon(boolean isOrdering) {
        //用于更新UI接口 当顺序和逆序改变的时候
        mOrderIv.setImageResource(isOrdering?R.mipmap.ascending:R.mipmap.descending);
        mOrderTv.setText(BaseApplication.getContext().getResources().getString(isOrdering?R.string.order_text:R.string.reverse_text));
    }
    //更新弹出列表的那个播放模式
    private void updateModeIcon(XmPlayListControl.PlayMode playMode) {
        //对应不同的情况 有不同的图标 没找图标 就不显示了 还有文字的显示
        int textId=R.string.play_model_list_text;
        switch (playMode) {
            case PLAY_MODEL_LIST:
                textId=R.string.play_model_list_text;
                break;
            case PLAY_MODEL_SINGLE:
                textId=R.string.play_model_single_text;
                break;
            case PLAY_MODEL_RANDOM:
                textId=R.string.play_model_random_text;
                break;
            case PLAY_MODEL_LIST_LOOP:
                textId=R.string.play_model_list_loop_text;
                break;
        }
        mPlayModeTv.setText(textId);
    }

    public interface PlayListItemClickListener {
        void onItemClick(int position);
    }

    public void setPlayActionClickListener(PlayActionClickListener listener) {
        this.mPlayModeListener = listener;
    }

    public interface PlayActionClickListener {
        //播放模式被点击了
        void onPlayModeClick();
        //播放顺序和逆序切换按钮
        void onOrderClick();
    }
}
