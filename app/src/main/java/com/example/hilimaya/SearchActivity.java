package com.example.hilimaya;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hilimaya.adapters.AlbumListAdapter;
import com.example.hilimaya.adapters.SearchRecommendAdapter;
import com.example.hilimaya.base.BaseActivity;
import com.example.hilimaya.interfaces.ISearchViewCallBack;
import com.example.hilimaya.presenters.SearchPresenter;
import com.example.hilimaya.utils.LogUtil;
import com.example.hilimaya.views.FlowTextLayout;
import com.example.hilimaya.views.UiLoader;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


//crtl+shift+回车  直接跳到下一行
public class SearchActivity extends BaseActivity implements ISearchViewCallBack {

    private static final String TAG = "SearchActivity";
    private View mBackBtn;
    private EditText mInputText;
    private View mSearchBtn;
    private FrameLayout mContainer;
    private SearchPresenter mSearchPresenter;

    private UiLoader mUiLoader;
    private RecyclerView mResultListView;
    private AlbumListAdapter mAlbumListAdapter;
    private FlowTextLayout mFlowTextLayout;
    private InputMethodManager mImm;
    private View mDelBtn;
    public static final int TIME_SHOW_IMM = 500;
    private RecyclerView mSearchRecommendList;
    private SearchRecommendAdapter mSearchRecommendAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initView();
        initEvent();
        initPresenter();
    }

    private void initPresenter() {
        mSearchPresenter = SearchPresenter.getSearchPresenter();
        mSearchPresenter.registerViewCallBack(this);
        //管理键盘的
        mImm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        //获得热词
        mSearchPresenter.getHotWord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSearchPresenter != null) {
            mSearchPresenter.unregisterViewCallBack(this);
            mSearchPresenter = null;
        }
    }

    private void initEvent() {
        if (mSearchRecommendAdapter != null) {
            mSearchRecommendAdapter.setItemClickListener(new SearchRecommendAdapter.ItemClickListener() {
                @Override
                public void onItemClick(String keyword) {
                    //TODO 当item被点击时候执行
                }
            });
        }
        mDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputText.setText("");
            }
        });
        mFlowTextLayout.setClickListener(new FlowTextLayout.ItemClickListener() {
            @Override
            public void onItemClick(String text) {
                //把热词填入搜索框
                mInputText.setText(text);
                mInputText.setSelection(text.length());
                //进行搜索
                if (mSearchPresenter != null) {
                    mSearchPresenter.doSearch(text);
                }
                //改变UI状态
                if (mUiLoader != null) {
                    mUiLoader.updateStatus(UiLoader.UIStatus.LOADING);
                }
            }
        });
        //出现错误时候的重新点击事件
        mUiLoader.setOnRetryClickListener(new UiLoader.OnRetryClickListener() {
            @Override
            public void onRetryClick() {
                if (mSearchPresenter != null) {
                    mSearchPresenter.reSearch();
                    mUiLoader.updateStatus(UiLoader.UIStatus.LOADING);
                }
            }
        });
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //这部分响应当输入框中的文字变化时候
                if (TextUtils.isEmpty(s)) {
                    mSearchPresenter.getHotWord();
                    mDelBtn.setVisibility(View.GONE);
                }else {
                    //获取联系的关键词
                    getSuggestWord(s.toString());
                    mDelBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //调用搜索的逻辑
                String keyWord = mInputText.getText().toString().trim();
                if (mSearchPresenter != null) {
                    mSearchPresenter.doSearch(keyWord);
                    mUiLoader.updateStatus(UiLoader.UIStatus.LOADING);
                }
            }
        });
    }

    /**
     * 获取联想的关键词
     * @param s
     */
    private void getSuggestWord(String s) {
        if (mSearchPresenter != null) {
            mSearchPresenter.getRecommendByKeyword(s);
        }
    }

    private void initView() {
        mBackBtn = this.findViewById(R.id.back_btn);
        mInputText = this.findViewById(R.id.input_text);
        mDelBtn = this.findViewById(R.id.search_input_delete);
        mDelBtn.setVisibility(View.GONE);
        mInputText.postDelayed(new Runnable() {
            @Override
            public void run() {
                //获取焦点
                mInputText.requestFocus();
                mImm.showSoftInput(mInputText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, TIME_SHOW_IMM);
        mSearchBtn = this.findViewById(R.id.search_btn);
        mContainer = this.findViewById(R.id.container);

        if (mUiLoader == null) {
            mUiLoader = new UiLoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView();
                }
            };
            if (mUiLoader.getParent() instanceof ViewGroup) {
                ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
            }
            mContainer.addView(mUiLoader);
        }
    }

    //创建数据成功的View
    private View createSuccessView() {
        View resultView = LayoutInflater.from(this).inflate(R.layout.search_result_layout, null);
        //显示热词的控件
        mFlowTextLayout = resultView.findViewById(R.id.recommend_hot_word_list);
        mResultListView = resultView.findViewById(R.id.result_list_view);
        //设置recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mResultListView.setLayoutManager(linearLayoutManager);
        mAlbumListAdapter = new AlbumListAdapter();
        mResultListView.setAdapter(mAlbumListAdapter);
        mResultListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                //工具类功能  dp--px转换
                outRect.top = UIUtil.dip2px(view.getContext(), 5);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 5);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });

        //推荐搜索
        mSearchRecommendList = resultView.findViewById(R.id.search_recommend_list);
        LinearLayoutManager RecommendLinearLayoutManager=new LinearLayoutManager(this);
        mSearchRecommendList.setLayoutManager(RecommendLinearLayoutManager);
        mSearchRecommendAdapter = new SearchRecommendAdapter();
        mSearchRecommendList.setAdapter(mSearchRecommendAdapter);
        mSearchRecommendList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                //工具类功能  dp--px转换
                outRect.top = UIUtil.dip2px(view.getContext(), 2);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 2);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });
        return resultView;
    }

    @Override
    public void onSearchResultLoaded(List<Album> result) {
        hideSuccessView();
        mResultListView.setVisibility(View.VISIBLE);
        //隐藏软键盘
        mImm.hideSoftInputFromWindow(mInputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        if (result != null) {
            if (result.size() == 0) {
                //数据为空
                if (mUiLoader != null) {
                    mUiLoader.updateStatus(UiLoader.UIStatus.EMPTY);
                }
            } else {
                //如果数据不为空 那么就设置数据
                mAlbumListAdapter.setData(result);
                mUiLoader.updateStatus(UiLoader.UIStatus.SUCCESS);
            }
        }
    }

    @Override
    public void onHotWordLoaded(List<HotWord> hotWordList) {
        hideSuccessView();
        mFlowTextLayout.setVisibility(View.VISIBLE);
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UiLoader.UIStatus.SUCCESS);
        }
        LogUtil.e(TAG, "hotWordList size-->" + hotWordList.size());
        List<String> hotWords = new ArrayList<>();
        hotWords.clear();
        for (HotWord hotWord : hotWordList) {
            String searchWord = hotWord.getSearchword();
            hotWords.add(searchWord);
        }
        Collections.sort(hotWords);
        //更新UI
        mFlowTextLayout.setTextContents(hotWords);
    }

    @Override
    public void onLoadMoreResult(List<Album> result, boolean isOkay) {

    }

    @Override
    public void onRecommendWordLoaded(List<QueryResult> keyWordList) {
        //联想相关的关键词
        if (mSearchRecommendAdapter != null) {
            mSearchRecommendAdapter.setData(keyWordList);
        }
        //控制UI的隐藏和显示
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UiLoader.UIStatus.SUCCESS);
        }
        hideSuccessView();
        mSearchRecommendList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UiLoader.UIStatus.NETWORK_ERROR);
        }
    }

    private void hideSuccessView(){
        mFlowTextLayout.setVisibility(View.GONE);
        mResultListView.setVisibility(View.GONE);
        mSearchRecommendList.setVisibility(View.GONE);
    }
}