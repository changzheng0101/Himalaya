package com.example.hilimaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

/**
 * 用于当逻辑层发生变化的时候  通知UI发生改变
 */
public interface IRecommendViewCallBack {
    /**
     * 获取推荐内容的结果
     * @param result
     */
    void onRecommendListLoaded(List<Album> result);

    /**
     * 网络错误
     */
    void onNetWorkError();

    /**
     * 数据为空
     */
    void onEmpty();

    /**
     * 正在加载中
     */
    void onLoading();
}
