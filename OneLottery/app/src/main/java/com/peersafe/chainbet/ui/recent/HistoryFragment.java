package com.peersafe.chainbet.ui.recent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.adapter.HistoryAdapter;
import com.peersafe.chainbet.ui.lottery.LotteryDetailActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.OLRefreshHeader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * AUTHOR : sunhaitao.
 * DATA : 16/12/30
 * DESCRIPTION :
 */
public class HistoryFragment extends LazyFragment implements OnItemClickListener
{
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;
    private HistoryAdapter mDataAdapter = null;

    private LRecyclerView mRecyclerView = null;

    private LinearLayout mLyNoData;
    private ImageView mIvNoData;
    private TextView mTvNoData;

    //数据集合
    private List<OneLottery> historyLotteres = null;
    private boolean isPrepared;
    private boolean isRefresh = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState)
    {
        EventBus.getDefault().register(this);
        return inflater.inflate(R.layout.history_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        initViews();
    }

    private void initViews()
    {
        mRecyclerView = (LRecyclerView) getView().findViewById(R.id.history_list);

        mLyNoData = (LinearLayout) getView().findViewById(R.id.ly_empty);
        mTvNoData = (TextView) getView().findViewById(R.id.tv_empty);
        mIvNoData = (ImageView) getView().findViewById(R.id.iv_empty);

        //setLayoutManager must before setAdapter
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);

        mRecyclerView.setLayoutManager(manager);

        mRecyclerView.setPullRefreshEnabled(true);
        mRecyclerView.setRefreshHeader(new OLRefreshHeader(getActivity()));

        //禁用自动加载更多功能
        mRecyclerView.setLoadMoreEnabled(false);

        mDataAdapter = new HistoryAdapter(getActivity());

        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);
        mRecyclerView.setAdapter(mLRecyclerViewAdapter);


        mLRecyclerViewAdapter.setOnItemClickListener(this);

        isPrepared = true;
        setDataList();

        mRecyclerView.setOnRefreshListener(new OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                if (!NetworkUtil.isNetworkConnected())
                {
                    mLyNoData.setVisibility(View.VISIBLE);
                    mIvNoData.setImageResource(R.drawable.no_network);
                    mTvNoData.setText(R.string.no_network);
                    mDataAdapter.setDataList(new ArrayList<OneLottery>());
                    mRecyclerView.refreshComplete();
                    return;
                }

                if(!OneLotteryManager.isServiceConnect)
                {
                    mLyNoData.setVisibility(View.VISIBLE);
                    mIvNoData.setImageResource(R.drawable.no_service);
                    mTvNoData.setText(R.string.no_service);
                    mDataAdapter.setDataList(new ArrayList<OneLottery>());
                    mRecyclerView.refreshComplete();
                    return;
                }

                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        isRefresh = true;
                        OneLotteryManager.getInstance().getLotteries(true, true);
                    }
                }).start();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(OLMessageModel model)
    {
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_GET_LOTTERIES_OVER:
                setDataList();
                if (isRefresh)
                {
                    isRefresh = false;
                    mRecyclerView.refreshComplete();
                }
                break;
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_OVER_NOTIFY:
            case OLMessageModel.STMSG_MODEL_OPEN_REWARD_CALLBACK:
            case OLMessageModel.STMSG_MODEL_OPEN_REWARD_NOTIFY:
                setDataList();
                break;
            case OLMessageModel.STMSG_MODEL_NET_WORK_CONNECT:
                mRecyclerView.forceToRefresh();
                setDataList();
                break;
            case OLMessageModel.STMSG_MODEL_NET_WORK_DISCONNECT:
                if (isRefresh)
                {
                    isRefresh = false;
                    mRecyclerView.refreshComplete();
                }
                setDataList();
                break;
        }
    }

    private void setDataList()
    {
        if (!NetworkUtil.isNetworkConnected())
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_network);
            mDataAdapter.setDataList(new ArrayList<OneLottery>());
            mTvNoData.setText(R.string.no_network);
            return;
        }

        if (!OneLotteryManager.isServiceConnect)
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_service);
            mDataAdapter.setDataList(new ArrayList<OneLottery>());
            mTvNoData.setText(R.string.no_service);
            return;
        }

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                historyLotteres = OneLotteryDBHelper.getInstance().getHistoryLotteres();

                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (null != historyLotteres && !historyLotteres.isEmpty())
                        {
                            mLyNoData.setVisibility(View.GONE);
                            mDataAdapter.setDataList(historyLotteres);
                        } else
                        {
                            mLyNoData.setVisibility(View.VISIBLE);
                            mIvNoData.setImageResource(R.drawable.no_data);
                            mTvNoData.setText(R.string.no_lottery);
                            mDataAdapter.setDataList(new ArrayList<OneLottery>());
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onItemClick(View view, int position)
    {
        if (!NetworkUtil.isNetworkConnected())
        {
            Toast.makeText(OneLotteryApplication.getAppContext(), getString(R.string
                    .check_network), Toast.LENGTH_SHORT).show();
            return;
        }

        if(!OneLotteryManager.getInstance().isServiceConnect)
        {
            Toast.makeText(OneLotteryApplication.getAppContext(),getString(R.string.check_service),Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), LotteryDetailActivity.class);
        intent.putExtra(ConstantCode.CommonConstant.LOTTERYID, historyLotteres.get(position)
                .getLotteryId());
        startActivity(intent);
    }

    @Override
    public void onDestroyView()
    {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    protected void lazyLoad()
    {
        if (!isPrepared || !isVisible)
        {
            return;
        }

        if (this.isRefresh)
        {
            this.isRefresh = false;
            this.mRecyclerView.refreshComplete();
        }

        setDataList();
    }
}
