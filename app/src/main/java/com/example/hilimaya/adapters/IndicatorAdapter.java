package com.example.hilimaya.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.example.hilimaya.MainActivity;
import com.example.hilimaya.R;
import com.example.hilimaya.utils.LogUtil;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;



//该适配器用于最上面的进行滑动的那部分 使用这些东西得更新build中的内容
public class IndicatorAdapter  extends CommonNavigatorAdapter {
    private  final  String TAG="IndicatorAdapter";

    private final String[] titles;
    private  OnIndicatorTabClickListener mOnTabClickListener;

    public IndicatorAdapter(Context context) {
        titles = context.getResources().getStringArray(R.array.indicator_name);
        context.getResources().getLayout(R.layout.activity_main);
    }

    @Override
    public int getCount() {
        if (titles != null) {
            return titles.length;
        }
        return 0;
    }

    @Override
    public IPagerTitleView getTitleView(Context context, int index) {
        SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
        simplePagerTitleView.setNormalColor(Color.GRAY);
        simplePagerTitleView.setSelectedColor(Color.WHITE);
        simplePagerTitleView.setText(titles[index]);
        simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                contentpager.setCurrentItem(index);
                //ToDo:
                if (mOnTabClickListener != null) {
                    mOnTabClickListener.onTabClick(index);
                }
            }
        });
        return simplePagerTitleView;
    }

    @Override
    public IPagerIndicator getIndicator(Context context) {
        LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
        linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
        linePagerIndicator.setColors(Color.WHITE);
        return linePagerIndicator;
    }

    //这部分用于实现当点击事件发生时候  可以进行切换 接口实现好像解决了无法访问其他地方的变量这一问题
    public void setOnIndicatorTabClickListener(OnIndicatorTabClickListener Listener){
        this.mOnTabClickListener=Listener;
    }
    public interface OnIndicatorTabClickListener{
        void onTabClick(int index);
    }
}
