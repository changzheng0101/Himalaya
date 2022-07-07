package com.example.hilimaya.interfaces;

import com.example.hilimaya.base.IBasePrensenter;

// crtl+n 可以进行全局的搜索
public interface ISearchPresenter extends IBasePrensenter<ISearchViewCallBack> {

    /**
     * 进行搜索
     * @param keyword
     */
    void doSearch(String keyword);

    /**
     *  重新搜索
     */
    void reSearch();


    /**
     * 加载更多的搜素结果
     */
    void loadMore();

    /**
     * 获取热词
     */
    void getHotWord();

    /**
     * 获取相似的推荐
     */
    void getRecommendByKeyword(String keyword);

}
