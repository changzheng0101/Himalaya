package com.example.hilimaya;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hilimaya.adapters.DetailListAdapter;
import com.example.hilimaya.base.BaseActivity;
import com.example.hilimaya.base.BaseApplication;
import com.example.hilimaya.interfaces.IAlbumDetailViewCallBack;
import com.example.hilimaya.interfaces.IPlayerViewCallBack;
import com.example.hilimaya.presenters.AlbumDetailPresenter;
import com.example.hilimaya.presenters.PlayerPresenter;
import com.example.hilimaya.utils.ImageBlur;
import com.example.hilimaya.utils.LogUtil;
import com.example.hilimaya.views.RoundRectImageView;
import com.example.hilimaya.views.UiLoader;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.header.bezierlayout.BezierLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class DetailActivity extends BaseActivity implements IAlbumDetailViewCallBack, UiLoader.OnRetryClickListener, DetailListAdapter.OnItemClickListener, IPlayerViewCallBack {

    private static final String TAG = "DetailActivity";
    private ImageView mLargeCover;
    private RoundRectImageView mSmallCover;
    private TextView mAlbumTitle;
    private TextView mAlbumAuthor;
    private AlbumDetailPresenter mAlbumDetailPresenter;
    private int mCurrentPage = 1;
    private RecyclerView mAlbumList;
    private LinearLayoutManager mLinearLayoutManager;
    private DetailListAdapter mDetailListAdapter;
    private FrameLayout mDetailListContainer;
    private UiLoader mUiLoader;
    private long mDetailId;
    private PlayerPresenter mPlayerPresenter;
    private ImageView mPlayControlIv;
    private TextView mPlayControlTv;
    private List<Track> mCurrentTracks = null;
    private final static int DEFAULT_PLAY_INDEX = 0;
    private TwinklingRefreshLayout mRefreshLayout;
    private boolean mIsLoadMore = false;
    private String mTrackTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //???????????????????????????
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        getWindow().setStatusBarColor(Color.TRANSPARENT);


        initView();
        mAlbumDetailPresenter = AlbumDetailPresenter.getInstance();
        mAlbumDetailPresenter.registerViewCallBack(this);

        //????????????????????????
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        mPlayerPresenter.registerViewCallBack(this);
        updatePlayState(mPlayerPresenter.isPlay());
        initEvent();
    }

    private void initEvent() {
        mPlayControlIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //????????????????????????????????????
                if (mPlayerPresenter != null) {
                    boolean has = mPlayerPresenter.hasPlayList();
                    if (has) {
                        handlePlayControl();
                    } else {
                        handleNoPlayList();
                    }
                }
            }

            private void handlePlayControl() {
                //???????????????????????????????????????
                if (mPlayerPresenter.isPlay()) {
                    mPlayerPresenter.pause();
                } else {
                    mPlayerPresenter.play();
                }
            }
        });
    }

    /**
     * ?????????????????????????????????????????????
     */
    private void handleNoPlayList() {
        mPlayerPresenter.setPlayerList(mCurrentTracks, DEFAULT_PLAY_INDEX);
    }

    private void initView() {
        mDetailListContainer = this.findViewById(R.id.detail_list_container);
        //??????UIloader
        if (mUiLoader == null) {
            mUiLoader = new UiLoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView(container);
                }
            };
            mDetailListContainer.removeAllViews(); //?????????????????????????????????
            mDetailListContainer.addView(mUiLoader); //??????????????????????????????
            mUiLoader.setOnRetryClickListener(DetailActivity.this);
        }
        mLargeCover = this.findViewById(R.id.iv_large_cover);
        mSmallCover = this.findViewById(R.id.iv_small_cover);
        mAlbumTitle = this.findViewById(R.id.tv_album_title);
        mAlbumAuthor = this.findViewById(R.id.tv_album_author);
        //???????????????????????????
        mPlayControlIv = this.findViewById(R.id.detail_play_control_iv);
        mPlayControlTv = this.findViewById(R.id.play_tips_tv);
        mPlayControlTv.setSelected(true);
    }

    private View createSuccessView(ViewGroup container) {
        View DetailListView = LayoutInflater.from(this).inflate(R.layout.item_detail_list, container, false);
        mRefreshLayout = DetailListView.findViewById(R.id.refreshLayout);
        mAlbumList = DetailListView.findViewById(R.id.detail_list_rv);
        //???RecyclerView????????????
        //1.?????????????????????
        mLinearLayoutManager = new LinearLayoutManager(this);
        mAlbumList.setLayoutManager(mLinearLayoutManager);
        //2.???????????????
        mDetailListAdapter = new DetailListAdapter();
        mAlbumList.setAdapter(mDetailListAdapter);
        //????????????
        mAlbumList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                //???????????????  dp--px??????
                outRect.top = UIUtil.dip2px(view.getContext(), 3);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 3);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });

        //???????????????????????????
        mDetailListAdapter.setOnItemClickListener(this);
        //????????????????????????
        BezierLayout headerView = new BezierLayout(this);
        mRefreshLayout.setHeaderView(headerView);
        mRefreshLayout.setMaxHeadHeight(140);

        //??????????????????????????????????????????
        mRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                super.onRefresh(refreshLayout);
                BaseApplication.getsHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DetailActivity.this, "????????????", Toast.LENGTH_SHORT).show();

                        mRefreshLayout.finishRefreshing();
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                super.onLoadMore(refreshLayout);
                mIsLoadMore = true;
                //??????????????????
                if (mAlbumDetailPresenter != null) {
                    mAlbumDetailPresenter.loadMore();
                }
//                BaseApplication.getsHandler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(DetailActivity.this,"??????????????????",Toast.LENGTH_SHORT).show();
//                        mRefreshLayout.finishLoadmore();
//                    }
//                },2000);
            }
        });
        return DetailListView;
    }

    @Override
    public void onDetailListLoaded(List<Track> tracks) {
        if (mIsLoadMore && mRefreshLayout != null) {
            mRefreshLayout.finishLoadmore();
            mIsLoadMore = false;
        }
        this.mCurrentTracks = tracks;

        //????????????????????? ???????????????????????????
        if (tracks == null || tracks.size() == 0) {
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UiLoader.UIStatus.EMPTY);
            }
        }
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UiLoader.UIStatus.SUCCESS);
        }

        //?????? ?????? UI?????????
        mDetailListAdapter.setData(tracks);
    }

    @Override
    public void onAlbumLoaded(Album album) {
        //???????????????????????????
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UiLoader.UIStatus.LOADING);
        }
        mDetailId = album.getId();
        //???????????????????????????
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.getAlbumDetail((int) mDetailId, mCurrentPage);
        }
        if (mAlbumTitle != null) {
            mAlbumTitle.setText(album.getAlbumTitle());
        }
        if (mAlbumAuthor != null) {
            mAlbumAuthor.setText(album.getAnnouncer().getNickname());
        }
        //??????????????????????????????
        if (mLargeCover != null) {
            Picasso.with(this).load(album.getCoverUrlLarge()).into(mLargeCover, new Callback() {
                @Override
                public void onSuccess() {
                    Drawable drawable = mLargeCover.getDrawable();
                    if (drawable != null) {
                        ImageBlur.makeBlur(mLargeCover, DetailActivity.this);
                    }
                }

                @Override
                public void onError() {
                    LogUtil.d(TAG, "onError");
                }
            });
            //??????????????????
//            Picasso.with(this).load(album.getCoverUrlLarge()).into(mLargeCover);
//            ImageBlur.makeBlur(mLargeCover,this);
        }
        if (mSmallCover != null) {
            Picasso.with(this).load(album.getCoverUrlLarge()).into(mSmallCover);
        }
    }

    @Override
    public void onNetworkError(int errorCode, String errorMsg) {
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UiLoader.UIStatus.NETWORK_ERROR);
        }
    }

    @Override
    public void onLoadMoreFinished(int size) {
        if (size>0) {
            Toast.makeText(this,"????????????"+size+"?????????",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this,"???????????????????????????",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRefreshFinished(int size) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAlbumDetailPresenter.unregisterViewCallBack(this);
    }

    @Override
    public void onRetryClick() {
        //????????????????????? ????????????  ?????????????????????
        //???????????????????????????
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UiLoader.UIStatus.LOADING);
        }
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.getAlbumDetail((int) mDetailId, mCurrentPage);
        }
    }

    @Override
    public void onItemClick(List<Track> detailTracks, int position) {
        //????????????????????????
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        mPlayerPresenter.setPlayerList(detailTracks, position);
        //????????????
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("Title", detailTracks.get(position).getTrackTitle());
        intent.putExtra("Position", position);
        startActivity(intent);
    }

    private void updatePlayState(boolean play) {
        if (mPlayControlIv != null || mPlayControlTv != null) {
            mPlayControlIv.setImageResource(play ? R.mipmap.play_stop_icon : R.mipmap.play_info_icon);
            if (!play) {
                mPlayControlTv.setText(R.string.click_play_tips_text);
            }else {
                mPlayControlTv.setText(mTrackTitle);
            }
        }
    }

    @Override
    public void onPlayStart() {
        updatePlayState(true);
    }

    @Override
    public void onPlayPause() {
        updatePlayState(false);
    }

    @Override
    public void onPlayStop() {
        updatePlayState(false);
    }

    @Override
    public void onPlayError() {

    }

    @Override
    public void onPrePlay(Track track) {

    }

    @Override
    public void onNextPlay(Track track) {

    }

    @Override
    public void onListLoaded(List<Track> list) {

    }

    @Override
    public void onPlayModeChanged(XmPlayListControl.PlayMode mode) {

    }

    @Override
    public void onProgressChange(int currentProgress, int total) {

    }

    @Override
    public void onTrackUpdate(Track track, int position) {
        if (track != null) {
            this.mTrackTitle = track.getTrackTitle();
            if (!TextUtils.isEmpty(mTrackTitle) && mPlayControlTv != null) {
                mPlayControlTv.setText(mTrackTitle);
            }
        }
    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void updateListOrder(boolean isReverse) {

    }
}