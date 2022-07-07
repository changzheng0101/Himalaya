package com.example.hilimaya.interfaces;

import android.os.Trace;

import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

public interface IPlayerViewCallBack {

    /**
     * 开始播放
     */
    void onPlayStart();

    /**
     * 播放暂停
     */
    void onPlayPause();

    /**
     * 播放停止
     */
    void onPlayStop();

    /**
     * 播放错误
     */
    void onPlayError();

    /**
     * 播放前一首
     */
    void onPrePlay(Track track);

    /**
     * 播放下一首
     */
    void onNextPlay(Track track);


    /**
     * 当列表加载好后
     *
     * @param list
     */
    void onListLoaded(List<Track> list);

    /**
     * 当播放状态改变的时候
     *
     * @param mode
     */
    void onPlayModeChanged(XmPlayListControl.PlayMode mode);

    /**
     * 当播放进度改变时候
     *
     * @param currentProgress
     * @param total
     */
    void onProgressChange(int currentProgress, int total);

    /**
     * 更新数据 整个传入
     *
     * @param track
     */
    void onTrackUpdate(Track track,int position);

    //================之下是与广告有关的部分

    /**
     * 广告加载中
     */
    void onAdLoading();

    /**
     * 广告加载完成
     */
    void onAdFinished();


    /**
     * 更新正序还是逆序的UI
     */
    void updateListOrder(boolean isReverse);

}
