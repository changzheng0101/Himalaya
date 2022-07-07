package com.example.hilimaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hilimaya.R;
import com.example.hilimaya.utils.LogUtil;
import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.ArrayList;
import java.util.List;

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.innerHolder> {

    private List<Album> mData=new ArrayList<>();
    private static final String TAG="RecommendListAdapter";
    private onItemClickListener mItemClickListener;


    @NonNull
    @Override
    public innerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //这里是找到view
        View itemView= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommend,parent,false);
        return new innerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull innerHolder holder, int position) {
        //进行数据的封装
        holder.itemView.setTag(position);  //任何view的子类都会有setTag这个方法
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    int position=(int) v.getTag();
                    mItemClickListener.onItemClick(position,mData.get(position));
                }
                LogUtil.d(TAG,"click item-->"+v.getTag());
            }
        });
        holder.setData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        //返回要显示的个数
        if (mData != null) {
            return mData.size();
            //快捷键 fori 可以生成有i的循环
        }
        return 0;
    }

    public void setData(List<Album> albumList) {
        if (mData != null) {
            mData.clear();
            mData.addAll(albumList);
        }
        //更新UI
        notifyDataSetChanged();
    }


    //这个玩意用于保存内部的view？
    public class innerHolder extends RecyclerView.ViewHolder {
        public innerHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setData(Album album) {
            //找到控件  设置数据
            //专辑封面
            ImageView albumCoverIv=itemView.findViewById(R.id.album_cover);
            //title
            TextView albumTitleTv=itemView.findViewById(R.id.album_title_tv);
            //描述
            TextView albumDesrcTv=itemView.findViewById(R.id.album_description_tv);
            //播放量
            TextView albumPlayCountTv=itemView.findViewById(R.id.album_play_count);
            //专辑内容数量
            TextView albumContentCountTv=itemView.findViewById(R.id.album_content_size);


            //通过查阅文档可以获得内容
            albumTitleTv.setText(album.getAlbumTitle());
            albumDesrcTv.setText(album.getAlbumIntro());
            albumPlayCountTv.setText(album.getPlayCount()+"");
            albumContentCountTv.setText(album.getIncludeTrackCount()+"");


            Picasso.with(itemView.getContext()).load(album.getCoverUrlLarge()).into(albumCoverIv);
        }
    }

    public void setOnItemClickListener(onItemClickListener listener){
        this.mItemClickListener=listener;
    }

    public interface onItemClickListener{
        void onItemClick(int position, Album album);
    }
}
