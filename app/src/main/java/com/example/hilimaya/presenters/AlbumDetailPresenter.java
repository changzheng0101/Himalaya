package com.example.hilimaya.presenters;

import com.example.hilimaya.api.XimalayaApi;
import com.example.hilimaya.interfaces.IAlbumDetailPresenter;
import com.example.hilimaya.interfaces.IAlbumDetailViewCallBack;
import com.example.hilimaya.utils.Constants;
import com.example.hilimaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlbumDetailPresenter implements IAlbumDetailPresenter {
    private static final String TAG = "AlbumDetailPresenter";
    private Album mTargetAlbum;
    private List<Track> mTracks = new ArrayList<>();

    private List<IAlbumDetailViewCallBack> mCallBacks = new ArrayList<>();
    private int mCurrentAlbumId = -1;
    private int mCurrentPageIndex = 0;

    //先声明为单例类 以防被多次引用
    //1.构造方法私有化
    private AlbumDetailPresenter() {
    }

    private static AlbumDetailPresenter sInstance = null;

    public static AlbumDetailPresenter getInstance() {
        if (sInstance == null) {
            synchronized (AlbumDetailPresenter.class) {  //锁防止多线程
                if (sInstance == null) {
                    sInstance = new AlbumDetailPresenter();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void pull2RefreshMore() {

    }

    @Override
    public void loadMore() {
        //加载更多内容
        mCurrentPageIndex++;
        doLoaded(true);
    }

    private void doLoaded(boolean isLoaderMore) {
        XimalayaApi ximalayaApi = XimalayaApi.getInstance();
        //通过调用api获取数据
        ximalayaApi.getAlbumDetail(new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                //确保在主线程中 可以进行UI的更新
                LogUtil.d(TAG, "current thread-->" + Thread.currentThread().getName());
                if (trackList != null) {
                    List<Track> tracks = trackList.getTracks();
                    LogUtil.d(TAG, "size -->" + tracks.size() + "");
                    if (isLoaderMore) {
                        //上拉加载 将结果放到后面去
                        mTracks.addAll(tracks);
                        handleLoadMoreResult(tracks.size());
                    } else {
                        //这个是下拉加载 加载到前面去
                        mTracks.addAll(0, tracks);
                    }
                    handlerAlbumDetailResult(mTracks);
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if (isLoaderMore) {
                    mCurrentPageIndex--;
                }
                LogUtil.d(TAG, "errorCode-->" + errorCode);
                LogUtil.d(TAG, "errorMsg-->" + errorMsg);
                handleNetworkError(errorCode, errorMsg);
            }
        },mCurrentAlbumId,mCurrentPageIndex);
    }

    /**
     * 处理加载更多的结果
     * @param size
     */
    private void handleLoadMoreResult(int size) {
        for (IAlbumDetailViewCallBack callBack : mCallBacks) {
            callBack.onLoadMoreFinished(size);
        }
    }

    @Override
    public void getAlbumDetail(int albumId, int page) {
        mTracks.clear();
        this.mCurrentAlbumId = albumId;
        this.mCurrentPageIndex = page;
        doLoaded(false);
    }

    private void handleNetworkError(int errorCode, String errorMsg) {
        //处理获得的结果  进行更新UI  在主线程中 可以直接进行UI更新
        for (IAlbumDetailViewCallBack callBack : mCallBacks) {
            callBack.onNetworkError(errorCode, errorMsg);
        }
    }

    private void handlerAlbumDetailResult(List<Track> tracks) {
        //处理获得的结果  进行更新UI  在主线程中 可以直接进行UI更新
        for (IAlbumDetailViewCallBack callBack : mCallBacks) {
            callBack.onDetailListLoaded(tracks);
        }
    }

    @Override
    public void registerViewCallBack(IAlbumDetailViewCallBack viewCallBack) {
        if (!mCallBacks.contains(viewCallBack)) {
            mCallBacks.add(viewCallBack);
            if (mTargetAlbum != null) {
                viewCallBack.onAlbumLoaded(mTargetAlbum);
            }
        }
    }

    @Override
    public void unregisterViewCallBack(IAlbumDetailViewCallBack viewCallBack) {
        mCallBacks.remove(viewCallBack);
    }

    public void setTargetAlbum(Album targetAlbum) {
        this.mTargetAlbum = targetAlbum;
    }
}
