package com.example.hilimaya.interfaces;

import com.example.hilimaya.base.IBasePrensenter;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

//crtl+h 查看继承关系
public interface IPlayerPresenter extends IBasePrensenter<IPlayerViewCallBack> {
    /**
     * 播放
     */
    void play();

    /**
     * 暂停
     */
    void pause();

    /**
     * 停止
     */
    void stop();

    /**
     * 播放上一首
     */
    void playPre();

    /**
     * 播放下一首
     */
    void playNext();

    /**
     * 切换播放模式
     *
     * @param mode
     */
    void switchPlayMode(XmPlayListControl.PlayMode mode);

    /**
     * 获取播放列表
     */
    void getPlayList();

    /**
     * 播放列表中的某一个
     *
     * @param index
     */
    void playByIndex(int index);

    /**
     * 切换播放进度
     *
     * @param progress
     */
    void seekTo(int progress);

    /**
     * 是否正在播放
     * @return
     */
    boolean isPlay();

    /**
     * 将播放列表翻转
     */
    void reversePlayList();


    /**
     * 根据专辑的id进行播放
     * @param albumId
     */
    void playByAlbumId(long albumId);
}
