package com.example.hilimaya.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hilimaya.DetailActivity;
import com.example.hilimaya.R;
import com.example.hilimaya.adapters.AlbumListAdapter;
import com.example.hilimaya.base.BaseFragment;
import com.example.hilimaya.interfaces.IRecommendViewCallBack;
import com.example.hilimaya.presenters.AlbumDetailPresenter;
import com.example.hilimaya.presenters.RecommendPresenter;
import com.example.hilimaya.utils.LogUtil;
import com.example.hilimaya.views.UiLoader;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class RecommendFragment extends BaseFragment implements IRecommendViewCallBack, AlbumListAdapter.onItemClickListener {
    private static final String TAG = "RecommendFragment";
    private RecyclerView mRecommendView;
    private AlbumListAdapter recommendListAdapter;
    private RecommendPresenter mRecommendPresenter;
    private UiLoader mUiLoader;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater) {
        mUiLoader = new UiLoader(getContext()) {
            @Override
            protected View getSuccessView(ViewGroup container) {
                return createSuccessView(layoutInflater);
            }
        };

        //获取到逻辑层的对象
        mRecommendPresenter = RecommendPresenter.getInstance();
        //先要设置通知接口的注册 进而对Ui层进行操作
        mRecommendPresenter.registerViewCallBack(this);
        //获取推荐列表
        mRecommendPresenter.getRecommendList();

        mUiLoader.setOnRetryClickListener(new UiLoader.OnRetryClickListener() {
            @Override
            public void onRetryClick() {
                //网络不好的时候 重新获取数据
                if (mRecommendPresenter != null) {
                    mRecommendPresenter.getRecommendList();
                }
            }
        });

//        //返回前与父类解绑 否则会导致崩溃  测试了下删除好像没崩溃
//        if (mUiLoader.getParent() instanceof ViewGroup) {
//            ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
//        }
        //将view进行返回
        return mUiLoader;
    }

    private View createSuccessView(LayoutInflater layoutInflater) {
        //加载view完成
        View rootView = layoutInflater.inflate(R.layout.fragment_recommend, null);
        //recyclerview控件的使用
//        1.拿到对应的控件
        mRecommendView = rootView.findViewById(R.id.recommend_list);
        TwinklingRefreshLayout twinklingRefreshLayout=rootView.findViewById(R.id.over_scroll_view);
        twinklingRefreshLayout.setPureScrollModeOn();
//        2.设置布局管理器  crtl+h 可以查看继承的关系
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecommendView.setLayoutManager(linearLayoutManager);
        //用于添加分割线
        mRecommendView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                //工具类功能  dp--px转换
                outRect.top = UIUtil.dip2px(view.getContext(), 5);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 5);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });
//        3.设置适配器
        recommendListAdapter = new AlbumListAdapter();
        mRecommendView.setAdapter(recommendListAdapter);

        //配置单项单击监听器
        recommendListAdapter.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onRecommendListLoaded(List<Album> result) {
        //当获取到推荐内容的时候 这个方法就会被调用--成功  更新UI
        //把数据设置给适配器，并且进行UI的更新
        recommendListAdapter.setData(result);
        mUiLoader.updateStatus(UiLoader.UIStatus.SUCCESS);
    }

    @Override
    public void onNetWorkError() {
        LogUtil.d(TAG,"onNetWorkError");
        mUiLoader.updateStatus(UiLoader.UIStatus.NETWORK_ERROR);
    }

    @Override
    public void onEmpty() {
        LogUtil.d(TAG,"onEmpty");
        mUiLoader.updateStatus(UiLoader.UIStatus.EMPTY);
    }

    @Override
    public void onLoading() {
        LogUtil.d(TAG,"onLoading");
        mUiLoader.updateStatus(UiLoader.UIStatus.LOADING);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //取消接口的注册 以免内存泄漏
        if (mRecommendPresenter != null) {
            mRecommendPresenter.unregisterViewCallBack(this);
        }
    }

    @Override
    public void onItemClick(int position, Album album) {
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);
        //用于实现当某一项被点击时
        Intent intent=new Intent(getContext(), DetailActivity.class);
        startActivity(intent);
    }
}
