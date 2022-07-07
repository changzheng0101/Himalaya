package com.example.hilimaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hilimaya.R;
import com.example.hilimaya.base.BaseApplication;
import com.example.hilimaya.views.SobPopWindow;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.InnerHolder> {
    private List<Track> mData = new ArrayList<>();
    private int playingIndex = 0;
    private SobPopWindow.PlayListItemClickListener mOnItemClickListener = null;

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_play_list, parent, false);
        return new InnerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder holder, int position) {
        //设置点击事件 这里的itemview是list中的其中一条
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });
        //绑定数据
        Track track = mData.get(position);
        TextView TrackTitleTv = holder.itemView.findViewById(R.id.track_title_tv);
        TrackTitleTv.setText(track.getTrackTitle());
        TrackTitleTv.setTextColor(BaseApplication.getContext().getResources().
                getColor(position == playingIndex ? R.color.main_color : R.color.play_list_color));
        //让imageView控制显示与否
        ImageView PlayingIconIv = holder.itemView.findViewById(R.id.playing_icon_iv);
        PlayingIconIv.setVisibility(position == playingIndex ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<Track> data) {
        mData.clear();
        mData.addAll(data);
        //通知UI发生改变
        notifyDataSetChanged();
    }

    public void setCurrentPlayPosition(int position) {
        playingIndex = position;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(SobPopWindow.PlayListItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


}
