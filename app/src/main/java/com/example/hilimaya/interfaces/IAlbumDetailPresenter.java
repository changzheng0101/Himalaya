package com.example.hilimaya.interfaces;

import com.example.hilimaya.base.IBasePrensenter;

public interface IAlbumDetailPresenter extends IBasePrensenter<IAlbumDetailViewCallBack> {
    /**
     * 下拉刷新
     */
    void pull2RefreshMore();

    /**
     * 上拉加载更多
     */
    void loadMore();

    /**
     * 获取详情
     * @param albumId
     * @param page
     */
    void getAlbumDetail(int albumId,int page);

}
