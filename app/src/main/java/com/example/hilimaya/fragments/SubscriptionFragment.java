package com.example.hilimaya.fragments;

import android.view.LayoutInflater;
import android.view.View;

import com.example.hilimaya.R;
import com.example.hilimaya.base.BaseFragment;

public class SubscriptionFragment extends BaseFragment {
    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater) {
        View rootView=layoutInflater.inflate(R.layout.fragment_subscription,null);
        return rootView;
    }
}
