package com.example.hilimaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public interface IAlbumDetailViewCallBack {

    /**
     * 用于进行页面的加载 加载数据
     * @param tracks
     */
    void onDetailListLoaded(List<Track> tracks);

    /**
     * 用于加载UI
     * @param album
     */
    void onAlbumLoaded(Album album);

    /**
     * 用于处理网络错误的情况
     */
    void onNetworkError(int errorCode,String errorMsg);


    /**
     * 加载更多的结果处理
     * @param size>0 表示加载成功  否则为失败
     */
    void onLoadMoreFinished(int size);


    /**
     * 下拉加载更多的结果
     * @param size>0 表示加载成功  否则为失败
     */
    void  onRefreshFinished(int size);
}
