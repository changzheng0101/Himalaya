package com.example.hilimaya;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hilimaya.adapters.IndicatorAdapter;
import com.example.hilimaya.adapters.MainContentAdapter;
import com.example.hilimaya.interfaces.IPlayerViewCallBack;
import com.example.hilimaya.presenters.PlayerPresenter;
import com.example.hilimaya.presenters.RecommendPresenter;
import com.example.hilimaya.utils.LogUtil;
import com.example.hilimaya.views.RoundRectImageView;
import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;

import java.util.List;


/*
 demo的GitHub:https://github.com/hackware1993/MagicIndicator
 */
public class MainActivity extends FragmentActivity implements IPlayerViewCallBack {
    private static final String TAG = "MainActivity";
    private MagicIndicator magicIndicator;
    private ViewPager contentpager;
    private IndicatorAdapter indicatorAdapter;
    private RoundRectImageView mRoundRectImageView;
    private TextView mHeaderTitle;
    private TextView mTrackAuthor;
    private ImageView mPlayControlIv;
    private PlayerPresenter mPlayerPresenter;
    private View mPlayControlItem;
    private View mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        initPresenter();
    }

    private void initPresenter() {
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        mPlayerPresenter.registerViewCallBack(this);
    }

    private void initEvent() {
        indicatorAdapter.setOnIndicatorTabClickListener(new IndicatorAdapter.OnIndicatorTabClickListener() {
            @Override
            public void onTabClick(int index) {
                LogUtil.d(TAG, "click index is-->" + index);
                if (contentpager != null) {
                    contentpager.setCurrentItem(index);
                }
            }
        });

        mPlayControlIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置暂停和继续的响应
                if (mPlayerPresenter != null) {
                    boolean hasPlayList = mPlayerPresenter.hasPlayList();
                    if (!hasPlayList) {
                        playFirstRecommend();
                    } else {
                        if (mPlayerPresenter.isPlay()) {
                            mPlayerPresenter.pause();
                        } else {
                            mPlayerPresenter.play();
                        }
                    }
                }
            }
        });

        mPlayControlItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasPlayList = mPlayerPresenter.hasPlayList();
                if (!hasPlayList) {
                    playFirstRecommend();
                }
                //跳转到播放器界面 没有的话先播放
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                intent.putExtra("Title", mPlayerPresenter.getCurrentTrack().getTrackTitle());
//                intent.putExtra("Position", position);
                startActivity(intent);
            }
        });
        mSearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 获取第一个推荐的内容
     */
    private void playFirstRecommend() {
        List<Album> currentRecommend= RecommendPresenter.getInstance().getCurrentRecommendList();
        if (currentRecommend != null&&currentRecommend.size()>0) {
            Album album=currentRecommend.get(0);
            long albumId = album.getId();
            mPlayerPresenter.playByAlbumId(albumId);
        }
    }

    private void initView() {
        // crtl+alt+f 可以快速变为成员变量 这部分是最上面的那个indicator 就是那个头上切换那个
        magicIndicator = findViewById(R.id.magic_indicator);
        magicIndicator.setBackgroundColor(this.getResources().getColor(R.color.main_color));
        //为indicator添加adapter
        indicatorAdapter = new IndicatorAdapter(this);
        CommonNavigator commonNavigator = new CommonNavigator(this); //这代码是根据demo拷贝过来的
        commonNavigator.setAdjustMode(true); //可以自动的进行平均分
        commonNavigator.setAdapter(indicatorAdapter);

        //Viewpager
        contentpager = this.findViewById(R.id.content_pager);

        //创建内容适配器
        FragmentManager supportFragmentAdapter = getSupportFragmentManager();
        MainContentAdapter mainContentAdapter = new MainContentAdapter(supportFragmentAdapter);
        contentpager.setAdapter(mainContentAdapter);

        //进行绑定
        magicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(magicIndicator, contentpager);

        //控制播放相关的UI
        mRoundRectImageView = this.findViewById(R.id.main_track_cover);
        mHeaderTitle = this.findViewById(R.id.main_track_title);
        mHeaderTitle.setSelected(true);//保证有跑马灯的效果
        mTrackAuthor = this.findViewById(R.id.main_author);
        mPlayControlIv = this.findViewById(R.id.main_play_control);
        mPlayControlItem = this.findViewById(R.id.main_play_control_item);

        //搜索
        mSearchView = this.findViewById(R.id.search_btn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unregisterViewCallBack(this);
        }
    }

    private void updatePlayControlIcon(boolean isPlay) {
        if (mPlayControlIv != null) {
            mPlayControlIv.setImageResource(!isPlay ? R.mipmap.play_continue_icon : R.mipmap.play_stop_icon);
        }
    }

    @Override
    public void onPlayStart() {
        updatePlayControlIcon(true);
    }

    @Override
    public void onPlayPause() {
        updatePlayControlIcon(false);
    }

    @Override
    public void onPlayStop() {
        updatePlayControlIcon(false);
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

    }

    @Override
    public void onPlayModeChanged(XmPlayListControl.PlayMode mode) {

    }

    @Override
    public void onProgressChange(int currentProgress, int total) {

    }

    @Override
    public void onTrackUpdate(Track track, int position) {
        if (track != null) {
            String trackTitle = track.getTrackTitle();
            String nickname = track.getAnnouncer().getNickname();
            String coverUrlMiddle = track.getCoverUrlMiddle();
            if (mHeaderTitle != null) {
                mHeaderTitle.setText(trackTitle);
            }
            if (mTrackAuthor != null) {
                mTrackAuthor.setText(nickname);
            }
            if (mRoundRectImageView != null) {
                Picasso.with(this).load(coverUrlMiddle).into(mRoundRectImageView);
            }
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

    }
}