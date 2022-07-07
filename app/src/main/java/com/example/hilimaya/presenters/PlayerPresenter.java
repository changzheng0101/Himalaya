package com.example.hilimaya.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.hilimaya.api.XimalayaApi;
import com.example.hilimaya.base.BaseApplication;
import com.example.hilimaya.interfaces.IPlayerPresenter;
import com.example.hilimaya.interfaces.IPlayerViewCallBack;
import com.example.hilimaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.advertis.Advertis;
import com.ximalaya.ting.android.opensdk.model.advertis.AdvertisList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.advertis.IXmAdsStatusListener;
import com.ximalaya.ting.android.opensdk.player.constants.PlayerConstants;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;

public class PlayerPresenter implements IPlayerPresenter, IXmAdsStatusListener, IXmPlayerStatusListener {

    private static final String TAG = "PlayerPresenter";
    //存放多个回调接口
    private List<IPlayerViewCallBack> mIPlayerViewCallBacks = new ArrayList<>();
    private XmPlayerManager mPlayerManager;
    private int mCurrentIndex = DEFAULT_PLAY_INDEX;
    private final SharedPreferences mPlayModSp;
    private XmPlayListControl.PlayMode mCurrentMode = PLAY_MODEL_LIST;
    private final static int DEFAULT_PLAY_INDEX = 0;
    //是否翻转 未进行初始化处理
    private boolean mIsReverse = false;

    //繁体字切换 alt+shift+f
    //保存各种状态
//    PLAY_MODEL_SINGLE单曲播放
//    PLAY_MODEL_SINGLE_LOOP 单曲循环播放
//    PLAY_MODEL_LIST列表播放
//    PLAY_MODEL_LIST_LOOP列表循环
//    PLAY_MODEL_RANDOM 随机播放
    //按住alt可以快速竖向选择
    public static final int PLAY_MODEL_LIST_INT = 0;
    public static final int PLAY_MODEL_LIST_LOOP_INT = 1;
    public static final int PLAY_MODEL_RANDOM_INT = 2;
    public static final int PLAY_MODEL_SINGLE_INT = 3;
    public static final int PLAY_MODEL_SINGLE_LOOP_INT = 4;

    //sp's name and key
    public static final String PLAY_MODE_SP_NAME = "PlayMod";
    public static final String PLAY_MODE_SP_KEY = "CurrentMode";
    private Track mCurrentTrack;
    private int mProgressDuration=0;
    private int mCurrentProgressPosition=0;


    private PlayerPresenter() {
        //私有化构造方法
        //获得播放器
        mPlayerManager = XmPlayerManager.getInstance(BaseApplication.getContext());
        //添加这两个监听  之后会引入一堆方法
        mPlayerManager.addAdsStatusListener(this);
        mPlayerManager.addPlayerStatusListener(this);

        //数据持久化 保存当前的播放模式
        mPlayModSp = BaseApplication.getContext().getSharedPreferences(PLAY_MODE_SP_NAME, Context.MODE_PRIVATE);
    }

    private static PlayerPresenter sPlayerPresenter = null;

    public static PlayerPresenter getPlayerPresenter() {
        if (sPlayerPresenter == null) {
            synchronized (PlayerPresenter.class) {
                if (sPlayerPresenter == null) {
                    sPlayerPresenter = new PlayerPresenter();
                }
            }
        }
        return sPlayerPresenter;

    }

    private boolean isPlayListSet = false;

    /**
     * 设置播放列表
     *
     * @param tracks
     * @param index
     */
    public void setPlayerList(List<Track> tracks, int index) {
        if (mPlayerManager != null) {
            mPlayerManager.setPlayList(tracks, index);
            isPlayListSet = true;
            mCurrentTrack = tracks.get(index);
            mCurrentIndex = index;
        } else {
            LogUtil.d(TAG, "mPlayerManager is null");
        }
    }

    @Override
    public void play() {
        if (isPlayListSet) {
            mPlayerManager.play();
        }
    }

    @Override
    public void pause() {
        if (mPlayerManager != null) {
            mPlayerManager.pause();
        }
    }

    @Override
    public void stop() {
        if (mPlayerManager != null) {
            mPlayerManager.stop();
        }
    }

    @Override
    public void playPre() {
        if (mPlayerManager != null) {
            mPlayerManager.playPre();
        }
    }

    @Override
    public void playNext() {
        if (mPlayerManager != null) {
            mPlayerManager.playNext();
        }
    }

    @Override
    public void switchPlayMode(XmPlayListControl.PlayMode mode) {
        if (mPlayerManager != null) {
            mCurrentMode = mode;
            mPlayerManager.setPlayMode(mode);
            for (IPlayerViewCallBack iPlayerViewCallBack : mIPlayerViewCallBacks) {
                iPlayerViewCallBack.onPlayModeChanged(mode);
            }
        }
        //将数据保持到sp中去
        SharedPreferences.Editor editor = mPlayModSp.edit();
        editor.putInt(PLAY_MODE_SP_KEY, getIntByPlayMode(mode));
        editor.commit();
    }

    private int getIntByPlayMode(XmPlayListControl.PlayMode mode) {
        switch (mode) {
            case PLAY_MODEL_LIST_LOOP:
                return PLAY_MODEL_LIST_LOOP_INT;
            case PLAY_MODEL_RANDOM:
                return PLAY_MODEL_RANDOM_INT;
            case PLAY_MODEL_SINGLE:
                return PLAY_MODEL_SINGLE_INT;
            case PLAY_MODEL_LIST:
                return PLAY_MODEL_LIST_INT;
            case PLAY_MODEL_SINGLE_LOOP:
                return PLAY_MODEL_SINGLE_LOOP_INT;
        }
        return PLAY_MODEL_LIST_INT;
    }

    private XmPlayListControl.PlayMode getModeByPlayInt(int index) {
        switch (index) {
            case PLAY_MODEL_LIST_LOOP_INT:
                return PLAY_MODEL_LIST_LOOP;
            case PLAY_MODEL_RANDOM_INT:
                return PLAY_MODEL_RANDOM;
            case PLAY_MODEL_SINGLE_INT:
                return PLAY_MODEL_SINGLE;
            case PLAY_MODEL_LIST_INT:
                return PLAY_MODEL_LIST;
            case PLAY_MODEL_SINGLE_LOOP_INT:
                return PLAY_MODEL_SINGLE_LOOP;
        }
        return PLAY_MODEL_LIST;
    }

    /**
     * 判断是否有播放列表
     *
     * @return
     */
    public boolean hasPlayList() {
        return isPlayListSet;
    }

    @Override
    public void getPlayList() {
        if (mPlayerManager != null) {
            List<Track> playList = mPlayerManager.getPlayList();
            //通知UI
            for (IPlayerViewCallBack iPlayerViewCallBack : mIPlayerViewCallBacks) {
                iPlayerViewCallBack.onListLoaded(playList);
            }
        }
    }

    @Override
    public void playByIndex(int index) {
        //切换播放器到index的位置进行播放
        if (mPlayerManager != null) {
            mPlayerManager.play(index);
        }
    }

    @Override
    public void seekTo(int progress) {
        mPlayerManager.seekTo(progress);
    }

    @Override
    public boolean isPlay() {
        //获取播放状态
        return mPlayerManager.isPlaying();
    }

    @Override
    public void reversePlayList() {
        //把列表翻转
        List<Track> playList = mPlayerManager.getPlayList();
        Collections.reverse(playList);
        mIsReverse = !mIsReverse;
        //第一参数是播放列表 第二个是播放的下标 注意下标的变化
        mCurrentIndex = playList.size() - 1 - mCurrentIndex;
        mPlayerManager.setPlayList(playList, mCurrentIndex);
        //更新UI
        mCurrentTrack = (Track) mPlayerManager.getCurrSound();
        for (IPlayerViewCallBack iPlayerViewCallBack : mIPlayerViewCallBacks) {
            iPlayerViewCallBack.onListLoaded(playList);
            iPlayerViewCallBack.onTrackUpdate(mCurrentTrack, mCurrentIndex);
            iPlayerViewCallBack.updateListOrder(mIsReverse);
        }
    }

    @Override
    public void playByAlbumId(long albumId) {
        //获取专辑的列表内容
        XimalayaApi ximalayaApi = XimalayaApi.getInstance();
        ximalayaApi.getAlbumDetail(new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                //把专辑内容设置给播放器
                List<Track> tracks = trackList.getTracks();
                if (tracks != null && tracks.size() > 0) {
                    mPlayerManager.setPlayList(tracks, DEFAULT_PLAY_INDEX);
                    isPlayListSet = true;
                    mCurrentTrack = tracks.get(DEFAULT_PLAY_INDEX);
                    mCurrentIndex = DEFAULT_PLAY_INDEX;
                }
            }

            @Override
            public void onError(int i, String s) {
                Toast.makeText(BaseApplication.getContext(), "加载失败", Toast.LENGTH_SHORT).show();
            }
        }, (int) albumId, 1);

        //进行播放
    }


    @Override
    public void registerViewCallBack(IPlayerViewCallBack iPlayerViewCallBack) {
        handlePlayStatus(iPlayerViewCallBack);
        iPlayerViewCallBack.onProgressChange(mCurrentProgressPosition,mProgressDuration);
        //从sp中获得模式
        int anInt = mPlayModSp.getInt(PLAY_MODE_SP_KEY, PLAY_MODEL_LIST_INT);
        mCurrentMode = getModeByPlayInt(anInt);
        iPlayerViewCallBack.onPlayModeChanged(mCurrentMode);
        if (!mIPlayerViewCallBacks.contains(iPlayerViewCallBack)) {
            mIPlayerViewCallBacks.add(iPlayerViewCallBack);
        }
    }

    private void handlePlayStatus(IPlayerViewCallBack iPlayerViewCallBack) {
        int playerStatus = mPlayerManager.getPlayerStatus();
        //根据状态调用接口的方法
        if (PlayerConstants.STATE_STARTED == playerStatus) {
            iPlayerViewCallBack.onPlayStart();
        } else {
            iPlayerViewCallBack.onPlayPause();
        }
    }

    @Override
    public void unregisterViewCallBack(IPlayerViewCallBack iPlayerViewCallBack) {
        mIPlayerViewCallBacks.remove(iPlayerViewCallBack);
    }

    //这是关于广告的接口部分
    @Override
    public void onStartGetAdsInfo() {
        LogUtil.d(TAG, "onStartGetAdsInfo");
    }

    @Override
    public void onGetAdsInfo(AdvertisList advertisList) {
        LogUtil.d(TAG, "onGetAdsInfo");
    }

    @Override
    public void onAdsStartBuffering() {
        LogUtil.d(TAG, "onAdsStartBuffering");
    }

    @Override
    public void onAdsStopBuffering() {
        LogUtil.d(TAG, "onAdsStopBuffering");
    }

    @Override
    public void onStartPlayAds(Advertis advertis, int i) {
        LogUtil.d(TAG, "onStartPlayAds");
    }

    @Override
    public void onCompletePlayAds() {
        LogUtil.d(TAG, "onCompletePlayAds");
    }

    @Override
    public void onError(int i, int i1) {
        LogUtil.d(TAG, "onError");
    }

    //关于播放器的各个状态
    @Override
    public void onPlayStart() {
        LogUtil.d(TAG, "onPlayStart");
        //这里用于UI的更新
        for (IPlayerViewCallBack iPlayerViewCallBack : mIPlayerViewCallBacks) {
            iPlayerViewCallBack.onPlayStart();
        }
    }

    @Override
    public void onPlayPause() {
        LogUtil.d(TAG, "onPlayPause");
        for (IPlayerViewCallBack iPlayerViewCallBack : mIPlayerViewCallBacks) {
            iPlayerViewCallBack.onPlayPause();
        }
    }

    @Override
    public void onPlayStop() {
        LogUtil.d(TAG, "onPlayStop");
        for (IPlayerViewCallBack iPlayerViewCallBack : mIPlayerViewCallBacks) {
            iPlayerViewCallBack.onPlayStop();
        }
    }

    @Override
    public void onSoundPlayComplete() {
        LogUtil.d(TAG, "onSoundPlayComplete");
    }

    @Override
    public void onSoundPrepared() {
        mPlayerManager.setPlayMode(mCurrentMode);
        LogUtil.d(TAG, "onSoundPrepared");
        mPlayerManager.play();
    }

    @Override
    public void onSoundSwitch(PlayableModel LastModel, PlayableModel CurModel) {
        //当有歌曲发生切换时候调用这个方法
        // 第一个参数是上一个model 可能为空 第二个是现在播放的 有好几种类型
        //第一种写法 不推荐 有可能改了track这个名字
        //if ("track".equals(CurModel.getKind())){
        //    Track currentTrack=(Track) CurModel;
        //}
        //快捷键：alt+shift+insert 开启或关闭竖向选择模式
        mCurrentIndex = mPlayerManager.getCurrentIndex();
        //第二种写法
        if (CurModel instanceof Track) {
            //无法在第一次进入时候更新
            mCurrentTrack = (Track) CurModel;
//            LogUtil.d(TAG,"title==>"+currentTrack.getTrackTitle());
            for (IPlayerViewCallBack iPlayerViewCallBack : mIPlayerViewCallBacks) {
                iPlayerViewCallBack.onTrackUpdate(mCurrentTrack, mCurrentIndex);
            }
        }
//        LogUtil.d(TAG, "onSoundSwitch==>"+CurModel.getKind());
    }

    @Override
    public void onBufferingStart() {
        LogUtil.d(TAG, "onBufferingStart");
    }

    @Override
    public void onBufferingStop() {
        LogUtil.d(TAG, "onBufferingStop");
    }

    @Override
    public void onBufferProgress(int i) {
        LogUtil.d(TAG, "onBufferProgress" + i);
    }

    @Override
    public void onPlayProgress(int currentPos, int duration) {
        this.mCurrentProgressPosition = currentPos;
        this.mProgressDuration = duration;
        // currentPos 单位ms 播放了多长时间 duration
        // 这个操作是通知UI进行改变
        for (IPlayerViewCallBack iPlayerViewCallBack : mIPlayerViewCallBacks) {
            iPlayerViewCallBack.onProgressChange(currentPos, duration);
        }
        LogUtil.d(TAG, "onPlayProgress:" + "currentPos->" + currentPos + ":duration->" + duration);
    }

    @Override
    public boolean onError(XmPlayerException e) {
        LogUtil.d(TAG, "onError");
        return false;
    }

    public Track getCurrentTrack() {
        return mCurrentTrack;
    }
}
