package com.peersafe.chainbet.ui.recent;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.peersafe.chainbet.MainActivity;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.ui.BasicFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.ui.recent
 * @description:
 * @date 14/12/16 PM5:54
 */
public class RecentFragment extends BasicFragment
{
    private MainActivity myContext;

    //页卡标题集合
    private List<String> mTitleList = new ArrayList<String>();

    private ViewPager mViewPager;

    //页卡视图集合
    private List<android.support.v4.app.Fragment> mViewList = new ArrayList<>();

    private TabLayout mTabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_recent, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mViewPager = (ViewPager) getView().findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) getView().findViewById(R.id.tabs);

        //添加页卡视图
        mViewList.add(new RewardFragment());
        mViewList.add(new HistoryFragment());
        mViewList.add(new FailFragment());

        //添加页卡标题
        mTitleList.add(getString(R.string.recent_reward));
        mTitleList.add(getString(R.string.recent_history));
        mTitleList.add(getString(R.string.recent_fail));

        //给ViewPager设置适配器
        HeaderPagerAdapter mAdapter = new HeaderPagerAdapter(myContext.getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        //设置头部标题
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);

        //初始化toolbar
        initToolBar();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) getView().findViewById(R.id.recent_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.setTitle("");

        TextView title = (TextView) getView().findViewById(R.id.tv_toolbar_title);
        title.setText(getString(R.string.recent_announcement));
    }

    class HeaderPagerAdapter extends FragmentPagerAdapter
    {
        public HeaderPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position)
        {
            return mViewList.get(position);
        }

        @Override
        public int getCount()
        {
            return mViewList.size();
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return mTitleList.get(position);
        }
    }

    @Override
    public void onAttach(Activity activity)
    {
        myContext = (MainActivity) activity;
        super.onAttach(activity);
    }
}
