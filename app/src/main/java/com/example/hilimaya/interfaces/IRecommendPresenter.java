package com.example.hilimaya.interfaces;

import com.example.hilimaya.base.IBasePrensenter;

/**
 * 这部分是逻辑层 用于处理逻辑 有了接口还需要在逻辑包中写一个类来进行实现
 */
public interface IRecommendPresenter extends IBasePrensenter<IRecommendViewCallBack> {
    /**
     * 获取推荐内容
     */
    void getRecommendList();

}
