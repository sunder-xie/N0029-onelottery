package com.peersafe.chainbet.ui.friend;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.peersafe.chainbet.MainActivity;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.Friend;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.FriendDBHelper;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryDeleteRet;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicFragment;
import com.peersafe.chainbet.ui.adapter.FriendAdapter;
import com.peersafe.chainbet.utils.log.OLLogger;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.OLRefreshHeader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.ui.friend
 * @description:
 * @date 14/12/16 PM5:54
 */
public class FriendFragment extends BasicFragment
{
    private LRecyclerView mRecyclerView = null;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;

    private FriendAdapter mDataAdapter = null;

    private LinearLayout mLyNoData;
    private ImageView mIvNoData;
    private TextView mTvNoData;
    private List<Friend> list = new ArrayList<>();
    private boolean isRefresh = false;
    private boolean isView = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_friend, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        EventBus.getDefault().register(this);

        mRecyclerView = (LRecyclerView) getView().findViewById(R.id.friend_list);
        mLyNoData = (LinearLayout) getView().findViewById(R.id.ly_empty);
        mTvNoData = (TextView) getView().findViewById(R.id.tv_empty);
        mIvNoData = (ImageView) getView().findViewById(R.id.iv_empty);

        //setLayoutManager must before setAdapter
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(manager);

        mRecyclerView.setPullRefreshEnabled(true);
        mRecyclerView.setRefreshHeader(new OLRefreshHeader(getActivity()));

        //禁用自动加载更多功能
        mRecyclerView.setLoadMoreEnabled(false);

        mDataAdapter = new FriendAdapter(getActivity(), this);

        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);
        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        //初始化toolbar
        initToolBar();

        onRefreshing();

        mDataAdapter.setOnDelListener(new FriendAdapter.onSwipeListener()
        {
            @Override
            public void onDel(int pos)
            {
                Friend friend = mDataAdapter.getDataList().get(pos);
                FriendDBHelper.getInstance().deleteFriend(friend);
                List<OneLottery> lotterys = OneLotteryDBHelper.getInstance()
                        .getLotteryByPublishHash(friend.getFriendHash());
                if (null != lotterys)
                {
                    for (int i = 0; i < lotterys.size(); i++)
                    {
                        OneLottery lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId(lotterys.get(i).getLotteryId());
                        lottery.setVersion(-1);
                        OneLotteryDBHelper.getInstance().insertOneLottery(lottery);
                    }
                }
                mDataAdapter.getDataList().remove(friend);
                onEmpty(mDataAdapter.getDataList().isEmpty());
                mDataAdapter.notifyItemRemoved(pos);

                if (pos != (mDataAdapter.getDataList().size()))
                { // 如果移除的是最后一个，忽略
                    mDataAdapter.notifyItemRangeChanged(pos, mDataAdapter.getDataList().size() -
                            pos);
                }
                // 取消关注后刷新好友活动界面
                OneLotteryManager.getInstance().SendEventBus(true, OLMessageModel
                        .STMSG_MODEL_ONE_LOTTERY_MODIFY_NOTIFY);
            }
        });

        mRecyclerView.setOnRefreshListener(new OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                if (!NetworkUtil.isNetworkConnected())
                {
                    mLyNoData.setVisibility(View.VISIBLE);
                    mIvNoData.setImageResource(R.drawable.no_network);
                    mDataAdapter.setDataList(new ArrayList<Friend>());
                    mTvNoData.setText(R.string.no_network);
                    mRecyclerView.refreshComplete();
                    return;
                }

                if(!OneLotteryManager.isServiceConnect)
                {
                    mLyNoData.setVisibility(View.VISIBLE);
                    mIvNoData.setImageResource(R.drawable.no_service);
                    mDataAdapter.setDataList(new ArrayList<Friend>());
                    mTvNoData.setText(R.string.no_service);
                    mRecyclerView.refreshComplete();
                    return;
                }

                isRefresh = true;
                onRefreshing();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(final OLMessageModel model)
    {
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_GET_LOTTERIES_OVER:
            case OLMessageModel.STMSG_MODEL_REFRESH_FRIEND_FRAGMENT:
                if(!isView)
                {
                    onRefreshing();
                }
                break;

            case OLMessageModel.STMSG_MODEL_NET_WORK_CONNECT:
                mRecyclerView.forceToRefresh();
                break;

            case OLMessageModel.STMSG_MODEL_NET_WORK_DISCONNECT:
                if (isRefresh)
                {
                    isRefresh = false;
                    mRecyclerView.refreshComplete();
                }
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);
        if (!hidden)
        {
            isView = true;
            onRefreshing();
        }else
        {
            isView = false;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        if(activity.getCurrentFragment() == 2)
        {
            isView = true;
            onRefreshing();
        }
    }

    public void onRefreshing()
    {
        super.onRefresh();

        if (isRefresh)
        {
            isRefresh = false;
            mRecyclerView.refreshComplete();
        }

        if (!NetworkUtil.isNetworkConnected())
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_network);
            mDataAdapter.setDataList(new ArrayList<Friend>());
            mTvNoData.setText(R.string.no_network);
            return;
        }

        if (!OneLotteryManager.isServiceConnect)
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_service);
            mDataAdapter.setDataList(new ArrayList<Friend>());
            mTvNoData.setText(R.string.no_service);
            return;
        }

        list = FriendDBHelper.getInstance().getAllFriendList();
        if (list != null && !list.isEmpty())
        {
            mLyNoData.setVisibility(View.GONE);
            mDataAdapter.setDataList(list);
        } else
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_data);
            mTvNoData.setText(R.string.no_friend);
            mDataAdapter.setDataList(new ArrayList<Friend>());
        }
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) getView().findViewById(R.id.recent_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.setTitle("");

        TextView title = (TextView) getView().findViewById(R.id.tv_toolbar_title);
        title.setText(getString(R.string.friend_all_my_friend));
    }

    public void onEmpty(boolean empty)
    {
        if (empty)
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_data);
            mTvNoData.setText(R.string.no_friend);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
