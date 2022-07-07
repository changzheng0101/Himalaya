package com.example.hilimaya.presenters;

import com.example.hilimaya.api.XimalayaApi;
import com.example.hilimaya.interfaces.ISearchPresenter;
import com.example.hilimaya.interfaces.ISearchViewCallBack;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.ArrayList;
import java.util.List;

public class SearchPresenter implements ISearchPresenter {

    private List<ISearchViewCallBack> mCallBacks = new ArrayList<>();
    private String mCurrentKeyWord = null;
    private XimalayaApi mXimalayaApi;
    private static final int DEFAULT_PAGE = 1;
    private int mCurrentPage = DEFAULT_PAGE;

    private SearchPresenter() {
        mXimalayaApi = XimalayaApi.getInstance();
    }

    private static SearchPresenter sSearchPresenter = new SearchPresenter();

    public static SearchPresenter getSearchPresenter() {
        if (sSearchPresenter == null) {
            synchronized (SearchPresenter.class) {
                if (sSearchPresenter == null) {
                    sSearchPresenter = new SearchPresenter();
                }
            }
        }
        return sSearchPresenter;
    }


    @Override
    public void doSearch(String keyword) {
        //用于当网络不好的时候 进行重新搜索
        this.mCurrentKeyWord = keyword;
        search(keyword);
    }

    private void search(String keyword) {
        mXimalayaApi.searchByKeyWord(keyword, mCurrentPage, new IDataCallBack<SearchAlbumList>() {
            @Override
            public void onSuccess(SearchAlbumList searchAlbumList) {
                List<Album> albums = searchAlbumList.getAlbums();
                if (albums != null) {
                    for (ISearchViewCallBack callBack : mCallBacks) {
                        callBack.onSearchResultLoaded(albums);
                    }
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                for (ISearchViewCallBack callBack : mCallBacks) {
                    callBack.onError(errorCode,errorMsg);
                }
            }
        });
    }

    @Override
    public void reSearch() {
        search(mCurrentKeyWord);
    }

    @Override
    public void loadMore() {

    }

    @Override
    public void getHotWord() {
        mXimalayaApi.getHowWords(new IDataCallBack<HotWordList>() {
            @Override
            public void onSuccess(HotWordList hotWordList) {
                if (hotWordList != null) {
                    List<HotWord> hotWords = hotWordList.getHotWordList();
                    for (ISearchViewCallBack callBack : mCallBacks) {
                        callBack.onHotWordLoaded(hotWords);
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                //处理错误情况
            }
        });
    }

    @Override
    public void getRecommendByKeyword(String keyword) {
        mXimalayaApi.getSuggestWord(keyword, new IDataCallBack<SuggestWords>() {
            @Override
            public void onSuccess(SuggestWords suggestWords) {
                if (suggestWords != null) {
                    List<QueryResult> keyWordList = suggestWords.getKeyWordList();
                    for (ISearchViewCallBack callBack : mCallBacks) {
                        callBack.onRecommendWordLoaded(keyWordList);
                    }
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });

    }

    @Override
    public void registerViewCallBack(ISearchViewCallBack iSearchViewCallBack) {
        if (!mCallBacks.contains(iSearchViewCallBack)) {
            mCallBacks.add(iSearchViewCallBack);
        }
    }

    @Override
    public void unregisterViewCallBack(ISearchViewCallBack iSearchViewCallBack) {
        mCallBacks.remove(iSearchViewCallBack);
    }
}
