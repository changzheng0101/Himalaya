package com.example.hilimaya.presenters;

import com.example.hilimaya.api.XimalayaApi;
import com.example.hilimaya.interfaces.IRecommendPresenter;
import com.example.hilimaya.interfaces.IRecommendViewCallBack;
import com.example.hilimaya.utils.Constants;
import com.example.hilimaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RecommendPresenter implements IRecommendPresenter {
    private static final String TAG = "RecommendPresenter";

    //有可能对多个界面UI同时进行更新
    private List<IRecommendViewCallBack> mCallBacks = new ArrayList<>();
    private List<Album> mCurrentRecommendList=null;

    private RecommendPresenter() {
    }

    private static RecommendPresenter sInstance = null;

    /**
     * 获取单例对象
     */
    public static RecommendPresenter getInstance() {
        if (sInstance == null) {
            synchronized (RecommendPresenter.class) {
                if (sInstance == null) {
                    sInstance = new RecommendPresenter();
                }
            }
        }
        return sInstance;
    }


    /*
     * 获取推荐内容  其实就是猜你喜欢
     * 接口：3.10.6 获取猜你喜欢专辑
     * */
    @Override
    public void getRecommendList() {
        handleLoading();
        XimalayaApi ximalayaApi=XimalayaApi.getInstance();
        //获取推荐数据
        ximalayaApi.getRecommendList(new IDataCallBack<GussLikeAlbumList>() {
            @Override
            public void onSuccess(GussLikeAlbumList gussLikeAlbumList) {
                //数据获取成功
                if (gussLikeAlbumList != null) {
                    List<Album> albumList = gussLikeAlbumList.getAlbumList();
                    //获取数据之后，要进行UI的更新
                    //UpdataRecommendUI(albumList);
                    handleRecommendResult(albumList);
                }
            }
            @Override
            public void onError(int i, String s) {
                //数据获取失败 一般为网络问题
                LogUtil.d(TAG, "error code->" + i + "|error msg" + s);
                handleRecommendError();
            }
        });
    }


    /**
     * 获取推荐的专辑列表
     * @return 使用之前要判空
     */
    public List<Album> getCurrentRecommendList(){
        return mCurrentRecommendList;
    }

    private void handleLoading() {
        if (mCallBacks != null) {
            for (IRecommendViewCallBack mCallBack : mCallBacks) {
                mCallBack.onLoading(); //通知每个有这个接口的进行更新
            }
        }
    }

    private void handleRecommendError() {
        //通知UI进行更新
        if (mCallBacks != null) {
            for (IRecommendViewCallBack mCallBack : mCallBacks) {
                mCallBack.onNetWorkError(); //通知每个有这个接口的进行更新
            }
        }
    }


    private void handleRecommendResult(List<Album> albumList) {
        if (albumList!=null) {
            //通知UI进行更新
            if (mCallBacks != null) {
                //测试为空的情况
//                albumList.clear();
                if (albumList.size() == 0) {
                    for (IRecommendViewCallBack mCallBack : mCallBacks) {
                        mCallBack.onEmpty();
                    }
                } else {
                    for (IRecommendViewCallBack mCallBack : mCallBacks) {
                        mCallBack.onRecommendListLoaded(albumList); //通知每个有这个接口的进行更新
                    }
                    this.mCurrentRecommendList=albumList;
                }
            }
        }
    }


    @Override
    public void registerViewCallBack(IRecommendViewCallBack callBack) {
        if (mCallBacks != null && !mCallBacks.contains(callBack)) {
            mCallBacks.add(callBack);
        }
    }

    @Override
    public void unregisterViewCallBack(IRecommendViewCallBack callBack) {
        if (mCallBacks != null) {
            mCallBacks.remove(callBack);
        }
    }
}
