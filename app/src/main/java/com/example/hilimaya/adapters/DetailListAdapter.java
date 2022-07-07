package com.example.hilimaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hilimaya.R;
import com.example.hilimaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

//crtl+- 折叠代码 crtl++打开代码 crtl+x 删除一行代码
public class DetailListAdapter extends RecyclerView.Adapter<DetailListAdapter.InnerHolder> {
    private static final String TAG = "DetailListAdapter";
    private List<Track> mDetailTracks = new ArrayList<>();

    //格式化时间
    private SimpleDateFormat mUpDataDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat mDurationFormat = new SimpleDateFormat("mm:ss");
    private OnItemClickListener mOnItemClickListener = null;

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_detail, parent, false);
        return new InnerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder holder, int position) {
        View itemView = holder.itemView;
        //找到控件，设置数据
        //顺序id
        TextView orderTv = itemView.findViewById(R.id.album_count_tv);
        //标题
        TextView titleTv = itemView.findViewById(R.id.detail_item_title);
        //播放次数
        TextView playCountTv = itemView.findViewById(R.id.detail_item_play_count);
        //时长
        TextView durationTv = itemView.findViewById(R.id.detail_item_play_time);
        //更新日期
        TextView updateDateTv = itemView.findViewById(R.id.detail_item_update_time);

        //设置数据
        Track track = mDetailTracks.get(position);
        orderTv.setText(position + 1 + "");
        titleTv.setText(track.getTrackTitle());
        playCountTv.setText(track.getPlayCount() + "");
        //getDuration 获得的时长单位为s  要乘以1000 转化为ms
        int durationMil = track.getDuration() * 1000;
        String duration = mDurationFormat.format(durationMil);
        durationTv.setText(duration);
        LogUtil.d(TAG, duration);
        updateDateTv.setText(mUpDataDateFormat.format(track.getUpdatedAt()));

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(mDetailTracks,position);
//                Toast.makeText(v.getContext(), "you click " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDetailTracks.size();
    }

    public void setData(List<Track> tracks) {
        //1.清空里面的数据
        mDetailTracks.clear();
        //2.将所有数据进行添加
        mDetailTracks.addAll(tracks);
        //3.通知数据改变
        notifyDataSetChanged();
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(List<Track> detailTracks, int position);
    }
}
