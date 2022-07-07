package com.example.hilimaya.base;

public interface IBasePrensenter<T> {

    /**
     * 注册Ui回调
     * @param t
     */
    void registerViewCallBack(T t);

    /**
     * 取消注册
     * @param t
     */
    void unregisterViewCallBack(T t);
}
