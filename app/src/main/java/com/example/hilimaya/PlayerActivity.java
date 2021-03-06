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

    //????????????hashmap???????????????????????????
    private static Map<XmPlayListControl.PlayMode, XmPlayListControl.PlayMode> sPlayModeRule = new HashMap<>();

    /**
     * ????????????????????????mode?????????PlayMode?????????????????????
     * PLAY_MODEL_SINGLE????????????
     * PLAY_MODEL_SINGLE_LOOP ??????????????????
     * PLAY_MODEL_LIST????????????
     * PLAY_MODEL_LIST_LOOP????????????
     * PLAY_MODEL_RANDOM ????????????
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
                //??????????????????????????????????????????animation
                float value = (float) animation.getAnimatedValue();
                updateWindowAlpha(value);
            }
        });

        mOutAnimation = ValueAnimator.ofFloat(0.7f, 1.0f);
        mOutAnimation.setDuration(BG_ANIMATION_DURATION);
        mOutAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //?????????????????????
                float value = (float) animation.getAnimatedValue();
                updateWindowAlpha(value);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //????????????
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unregisterViewCallBack(this);
            mPlayerPresenter = null;
        }
    }


    /**
     * ???????????????????????????
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {
        mPlayControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //????????????????????? ?????????
                //????????????????????????????????? ???????????????????????????
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
                //?????????????????????
                LogUtil.d(TAG, "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //?????????????????????
                LogUtil.d(TAG, "onStopTrackingTouch");
            }
        });

        mPlayPreTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //???????????????
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.playPre();
                }
            }
        });
        mPlayNextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //???????????????
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
                //????????????
                mSobPopWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                //?????????????????? ???????????????
                //????????????????????????????????????????????????
                //?????????onCreate?????????
                mEnterAnimation.start();
            }
        });
        mSobPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //???pop????????????????????????????????????
                mOutAnimation.start();
            }
        });
        mSobPopWindow.SetPlayListItemClickListener(new SobPopWindow.PlayListItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //????????????????????????
                mPlayerPresenter.playByIndex(position);
            }
        });
        mSobPopWindow.setPlayActionClickListener(new SobPopWindow.PlayActionClickListener() {
            @Override
            public void onPlayModeClick() {
                //??????????????????
                switchPlayMode();
            }

            @Override
            public void onOrderClick() {
                //?????????????????????
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.reversePlayList();
                }
            }
        });

    }

    private void switchPlayMode() {
        //?????????????????????????????????
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
        //????????????????????? ?????????????????? ???????????? ???????????????
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
     * ??????????????????
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
        //???????????????
        mAdapter = new PlayerTrackPageAdapter();
        //???????????????
        mTrackPageView.setAdapter(mAdapter);
        //???????????????????????????imageView
        mPlayModeChangeIv = this.findViewById(R.id.play_mode_switch_iv);
        //???????????????????????????imageview
        mPlayListIv = this.findViewById(R.id.play_list_iv);
        mSobPopWindow = new SobPopWindow();
        if (mPlayTitle != null && mSobPopWindow != null) {
            //?????????????????????????????????????????? ???????????????????????????
            mPlayTitle.setText(this.getIntent().getStringExtra("Title"));
            mSobPopWindow.setCurrentPlayPosition(this.getIntent().getIntExtra("Position", 0));
        }
    }

    @Override
    public void onPlayStart() {
        //???????????? ???UI???????????????image
        if (mPlayControl != null) {
            mPlayControl.setImageResource(R.mipmap.play_stop_icon);
        }
    }

    @Override
    public void onPlayPause() {
        //???????????? UI?????????????????????UI
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
        //??????????????????????????????
        if (mAdapter != null) {
            mAdapter.setData(list);
        }
        //?????????????????? ??????????????????????????????
        if (mSobPopWindow != null) {
            mSobPopWindow.setListData(list);
        }
    }

    @Override
    public void onPlayModeChanged(XmPlayListControl.PlayMode mode) {
        //?????????????????? ????????????UI
        mCurrentMode = mode;
        //??????pop??????????????????
        mSobPopWindow.updatePlayMode(mCurrentMode);
        updateModeIcon();
    }

    @Override
    public void onProgressChange(int currentProgress, int total) {
        //??????????????????????????????
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

        //???????????????
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

        //????????????????????????????????????
        if (mTrackPageView != null) {
            mTrackPageView.setCurrentItem(position, true);
        }

        //??????????????????????????????index
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
        // ??????????????????????????? ??????????????????????????????
        if (mPlayerPresenter != null && mIsUserTouchPageView) {
            mPlayerPresenter.playByIndex(position);
        }
        mIsUserTouchPageView = false;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}