package com.example.hilimaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.hilimaya.R;
import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

public class PlayerTrackPageAdapter extends PagerAdapter {
    private List<Track> mTracks=new ArrayList<>();

    @Override
    public int getCount() {
        return mTracks.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        //在创建的时候进行调用
        View itemView = LayoutInflater.from(container.getContext()).inflate(R.layout.item_track_page, container, false);
        container.addView(itemView);
        //设置数据
        //找到控件
        ImageView item=itemView.findViewById(R.id.track_photo);
        //设置图片
        Track track=mTracks.get(position);
        String coverUrlLarge = track.getCoverUrlLarge();
        Picasso.with(container.getContext()).load(coverUrlLarge).into(item);
        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        //程序结束的时候进行调用
        container.removeView((View) object);

    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    public void setData(List<Track> list) {
        mTracks.clear();
        mTracks.addAll(list);
        //通知UI数据改变
        notifyDataSetChanged();
    }


}
