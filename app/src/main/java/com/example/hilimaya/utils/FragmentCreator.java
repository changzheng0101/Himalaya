package com.example.hilimaya.utils;

import com.example.hilimaya.base.BaseFragment;
import com.example.hilimaya.fragments.HistoryFragment;
import com.example.hilimaya.fragments.RecommendFragment;
import com.example.hilimaya.fragments.SubscriptionFragment;

import java.util.HashMap;
import java.util.Map;

public class FragmentCreator {
    public final static int INDEX_RECOMMEND=0;
    public final static int INDEX_SUBSCRIPTION=1;
    public final static int INDEX_HISTORY=2;

    public final static int FRAGMENT_COUNT=3;

    private static Map<Integer, BaseFragment> sCache=new HashMap<>();

    //用于获取Fragment
    public static BaseFragment getFragment(int index){
        BaseFragment baseFragment=sCache.get(index);
        if (baseFragment!=null){
            return baseFragment;
        }

        switch (index){
            case INDEX_RECOMMEND:
                baseFragment=new RecommendFragment();
                break;
            case INDEX_SUBSCRIPTION:
                baseFragment=new SubscriptionFragment();
                break;
            case INDEX_HISTORY:
                baseFragment=new HistoryFragment();
                break;
        }
        sCache.put(index,baseFragment);
        return baseFragment;
    }
}
