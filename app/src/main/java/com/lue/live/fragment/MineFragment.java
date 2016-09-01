package com.lue.live.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lue.live.R;

/**
 * Created by Lue on 2016/7/18.
 */
public class MineFragment extends Fragment
{
    private View view;

    private Activity mActivity;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();

        view = LayoutInflater.from(mActivity).inflate(R.layout.fragment_mine, null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return view;
    }
}
