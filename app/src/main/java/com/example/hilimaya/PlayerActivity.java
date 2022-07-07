package com.example.hilimaya;

import androidx.viewpager.widget.ViewPager;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.hilimaya.adapters.PlayerTrackPageAdapter;
import com.example.hilimaya.base.BaseActivity;
import com.example.hilimaya.interfaces.IPlayerViewCallBack;
import com.example.hilimaya.presenters.PlayerPresenter;
import com.example.hilimaya.utils.LogUtil;
import com.example.hilimaya.views.SobPopWindow;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;

public class PlayerActivity extends BaseActivity implements IPlayerViewCallBack, ViewPager.OnPageChangeListener {

    private ImageView mPlayControl;
    private PlayerPresenter mPlayerPresenter;
    private SimpleDateFormat mMinuteChangeTool = new SimpleDateFormat("mm:ss");
    private SimpleDateFormat mHourChangeTool = new SimpleDateFormat("hh:mm:ss");
    private TextView mCurrentPosition;
    private TextView mTrackDuration;
    private SeekBar mPlayProgress;
    private String TAG = "PlayerActivity";
    private ImageView mPlayPreTrack;
    private ImageView mPlayNextTrack;
    private TextView mPlayTitle;
    private ViewPager mTrackPageView;
    private PlayerTrackPageAdapter mAdapter;
    private boolean mIsUserTouchPageView = false;
    private ImageView mPlayModeChangeIv;
    private XmPlayListControl.PlayMode mCurrentMode = PLAY_MODEL_LIST;
    public final int BG_ANIMATION_DURATION = 500;

    //创建一个hashmap用于保存状态的切换
    private static Map<XmPlayListControl.PlayMode, XmPlayListControl.PlayMode> sPlayModeRule = new HashMap<>();

    /**
     * 设置播放器模式，mode取值为PlayMode中的下列之一：
     * PLAY_MODEL_SINGLE单曲播放
     * PLAY_MODEL_SINGLE_LOOP 单曲循环播放
     * PLAY_MODEL_LIST列表播放
     * PLAY_MODEL_LIST_LOOP列表循环
     * PLAY_MODEL_RANDOM 随机播放
     */
    static {
        sPlayModeRule.put(PLAY_MODEL_LIST, PLAY_MODEL_LIST_LOOP);
        sPlayModeRule.put(PLAY_MODEL_LIST_LOOP, PLAY_MODEL_RANDOM);
        sPlayModeRule.put(PLAY_MODEL_RANDOM, PLAY_MODEL_SINGLE);
        sPlayModeRule.put(PLAY_MODEL_SINGLE, PLAY_MODEL_SINGLE_LOOP);
        sPlayModeRule.put(PLAY_MODEL_SINGLE_LOOP, PLAY_MODEL_LIST);
    }

    private ImageView mPlayListIv;
    private SobPopWindow mSobPopWindow;
    private ValueAnimator mEnterAnimation;
    private ValueAnimator mOutAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initView();
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        mPlayerPresenter.registerViewCallBack(this);
        mPlayerPresenter.getPlayList();
        initEvent();
        initAnimation();
    }

    private void initAnimation() {
        mEnterAnimation = ValueAnimator.ofFloat(1.0f, 0.7f);
        mEnterAnimation.setDuration(BG_ANIMATION_DURATION);
        mEnterAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //会获得在整个更新过程中的说有animation
                float value = (float) animation.getAnimatedValue();
                updateWindowAlpha(value);
            }
        });

        mOutAnimation = ValueAnimator.ofFloat(0.7f, 1.0f);
        mOutAnimation.setDuration(BG_ANIMATION_DURATION);
        mOutAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //这是退出的动画
                float value = (float) animation.getAnimatedValue();
                updateWindowAlpha(value);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放资源
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unregisterViewCallBack(this);
            mPlayerPresenter = null;
        }
    }


    /**
     * 给控件添加监听事件
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {
        mPlayControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果正在播放中 就暂停
                //如果现在的状态是非播放 就让播放器进行播放
                if (mPlayerPresenter.isPlay()) {
                    mPlayerPresenter.pause();
                } else {
                    mPlayerPresenter.play();
                }
            }
        });
        mPlayProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mPlayerPresenter.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //开始触摸的时候
                LogUtil.d(TAG, "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //结束触摸的时候
                LogUtil.d(TAG, "onStopTrackingTouch");
            }
        });

        mPlayPreTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //播放上一首
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.playPre();
                }
            }
        });
        mPlayNextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //播放下一首
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.playNext();
                }
            }
        });

        mTrackPageView.addOnPageChangeListener(this);

        mTrackPageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mIsUserTouchPageView = true;
                        break;
                }
                return false;
            }
        });

        mPlayModeChangeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchPlayMode();
            }
        });

        mPlayListIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出菜单
                mSobPopWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                //处理一下背景 有点透明度
                //修改背景的时候有一个透明度的变化
                //封装在onCreate方法中
                mEnterAnimation.start();
            }
        });
        mSobPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //当pop窗体消失的时候调用该方法
                mOutAnimation.start();
            }
        });
        mSobPopWindow.SetPlayListItemClickListener(new SobPopWindow.PlayListItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //切换到对应的歌曲
                mPlayerPresenter.playByIndex(position);
            }
        });
        mSobPopWindow.setPlayActionClickListener(new SobPopWindow.PlayActionClickListener() {
            @Override
            public void onPlayModeClick() {
                //切换播放模式
                switchPlayMode();
            }

            @Override
            public void onOrderClick() {
                //点击了切换顺序
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.reversePlayList();
                }
            }
        });

    }

    private void switchPlayMode() {
        //处理切换模式的点击事件
        XmPlayListControl.PlayMode playMode = sPlayModeRule.get(mCurrentMode);
        if (mPlayerPresenter != null) {
            mPlayerPresenter.switchPlayMode(playMode);
        }
    }

    private void updateWindowAlpha(float alpha) {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.alpha = alpha;
        window.setAttributes(attributes);
    }

    private void updateModeIcon() {
        //对应不同的情况 有不同的图标 没找图标 就不显示了
        switch (mCurrentMode) {
            case PLAY_MODEL_LIST:
                break;
            case PLAY_MODEL_SINGLE:
                break;
            case PLAY_MODEL_RANDOM:
                break;
            case PLAY_MODEL_LIST_LOOP:
                break;
        }
    }

    /**
     * 找到各个控件
     */
    private void initView() {
        mPlayPreTrack = this.findViewById(R.id.previous_iv);
        mPlayNextTrack = this.findViewById(R.id.next_iv);
        mCurrentPosition = this.findViewById(R.id.current_position);
        mPlayControl = this.findViewById(R.id.play_control_iv);
        mTrackDuration = this.findViewById(R.id.track_duration);
        mPlayProgress = this.findViewById(R.id.progress_track);
        mPlayTitle = this.findViewById(R.id.track_title);
        mTrackPageView = this.findViewById(R.id.track_page_view);
        //创建适配器
        mAdapter = new PlayerTrackPageAdapter();
        //配置适配器
        mTrackPageView.setAdapter(mAdapter);
        //找见切换播放模式的imageView
        mPlayModeChangeIv = this.findViewById(R.id.play_mode_switch_iv);
        //找见可以弹出菜单的imageview
        mPlayListIv = this.findViewById(R.id.play_list_iv);
        mSobPopWindow = new SobPopWindow();
        if (mPlayTitle != null && mSobPopWindow != null) {
            //用于第一次无法完成标题的加载 还有弹出列表的加载
            mPlayTitle.setText(this.getIntent().getStringExtra("Title"));
            mSobPopWindow.setCurrentPlayPosition(this.getIntent().getIntExtra("Position", 0));
        }
    }

    @Override
    public void onPlayStart() {
        //开始播放 将UI变为暂停的image
        if (mPlayControl != null) {
            mPlayControl.setImageResource(R.mipmap.play_stop_icon);
        }
    }

    @Override
    public void onPlayPause() {
        //暂停播放 UI变为开始播放的UI
        if (mPlayControl != null) {
            mPlayControl.setImageResource(R.mipmap.play_continue_icon);
        }
    }

    @Override
    public void onPlayStop() {
        if (mPlayControl != null) {
            mPlayControl.setImageResource(R.mipmap.play_continue_icon);
        }
    }

    @Override
    public void onPlayError() {

    }

    @Override
    public void onPrePlay(Track track) {

    }

    @Override
    public void onNextPlay(Track track) {

    }

    @Override
    public void onListLoaded(List<Track> list) {
        //将数据设置到适配器中
        if (mAdapter != null) {
            mAdapter.setData(list);
        }
        //数据回来之后 也需要给节目列表一份
        if (mSobPopWindow != null) {
            mSobPopWindow.setListData(list);
        }
    }

    @Override
    public void onPlayModeChanged(XmPlayListControl.PlayMode mode) {
        //更新播放模式 并且修改UI
        mCurrentMode = mode;
        //更新pop里的播放模式
        mSobPopWindow.updatePlayMode(mCurrentMode);
        updateModeIcon();
    }

    @Override
    public void onProgressChange(int currentProgress, int total) {
        //当状态发生改变的时候
        String totalDuration;
        String currentTime;
        if (currentProgress > 1000 * 60 * 60) {
            totalDuration = mHourChangeTool.format(total);
            currentTime = mHourChangeTool.format(currentProgress);
        } else {
            totalDuration = mMinuteChangeTool.format(total);
            currentTime = mMinuteChangeTool.format(currentProgress);
        }
        if (mCurrentPosition != null) {
            mCurrentPosition.setText(currentTime);
        }
        if (mTrackDuration != null) {
            mTrackDuration.setText(totalDuration);
        }

        //更新进度条
        if (mPlayProgress != null) {
            mPlayProgress.setMax(total);
            mPlayProgress.setProgress(currentProgress);
        }
    }

    @Override
    public void onTrackUpdate(Track track, int position) {
        if (mPlayTitle != null) {
            mPlayTitle.setText(track.getTrackTitle());
        }

        //同时更新界面上图片的显示
        if (mTrackPageView != null) {
            mTrackPageView.setCurrentItem(position, true);
        }

        //更新弹出列表中播放的index
        if (mSobPopWindow != null) {
            mSobPopWindow.setCurrentPlayPosition(position);
        }
    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void updateListOrder(boolean isReverse) {
        mSobPopWindow.updateOrderIcon(!isReverse);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        // 当页面被选中的时候 就切换页面播放的内容
        if (mPlayerPresenter != null && mIsUserTouchPageView) {
            mPlayerPresenter.playByIndex(position);
        }
        mIsUserTouchPageView = false;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}