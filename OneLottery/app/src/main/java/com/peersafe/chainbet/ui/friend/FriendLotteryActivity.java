package com.peersafe.chainbet.ui.friend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.adapter.FriendLotteryAdapter;
import com.peersafe.chainbet.ui.lottery.LotteryDetailActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/1/9
 * DESCRIPTION : 好友列表点击后的好友活动列表界面
 */

public class FriendLotteryActivity extends BasicActivity implements OnItemClickListener
{
    private LRecyclerView mRecyclerView = null;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;

    private FriendLotteryAdapter mDataAdapter = null;

    private LinearLayout mLyNoData;
    private ImageView mIvNoData;
    private TextView mTvNoData;

    private List<OneLottery> list;

    private String friendHash;
    private String friendId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lottery);
        
        friendHash = getIntent().getStringExtra("FriendHash");
        friendId = getIntent().getStringExtra("FriendId");

        mRecyclerView = (LRecyclerView) findViewById(R.id.duration_list);
        mLyNoData = (LinearLayout) findViewById(R.id.ly_empty);
        mTvNoData = (TextView) findViewById(R.id.tv_empty);
        mIvNoData = (ImageView) findViewById(R.id.iv_empty);

        //setLayoutManager must before setAdapter
        LinearLayoutManager manager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(manager);

        mRecyclerView.setPullRefreshEnabled(false);
//        mRecyclerView.setRefreshHeader(new OLRefreshHeader(this));

        //禁用自动加载更多功能
        mRecyclerView.setLoadMoreEnabled(false);

        mDataAdapter = new FriendLotteryAdapter(this);

        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);
        mRecyclerView.setAdapter(mLRecyclerViewAdapter);
        mLRecyclerViewAdapter.setOnItemClickListener(this);

        initToolBar();

        EventBus.getDefault().register(this);

        setDataList();
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(friendId);

        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    @Override
    public void onItemClick(View view, int position)
    {
        if (!NetworkUtil.isNetworkConnected())
        {
            showToast(getString(R.string.check_network));
            return;
        }

        if(!OneLotteryManager.isServiceConnect)
        {
            showToast(getString(R.string.check_service));
            return;
        }

        if (list != null)
        {
            Intent intent = new Intent(FriendLotteryActivity.this, LotteryDetailActivity.class);
            intent.putExtra(ConstantCode.CommonConstant.LOTTERYID, list.get(position)
                    .getLotteryId());
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(final OLMessageModel model)
    {
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_NOTIFY:
                if (list != null && !list.isEmpty())
                {
                    OneLottery mol = (OneLottery) model.getEventObject();
                    for (OneLottery ol : list)
                    {
                        if (ol != null && mol != null && ol.getLotteryId().equals(mol
                                .getLotteryId()))
                        {
                            setDataList();
                            break;
                        }
                    }
                }
                break;
        }
    }

    private void setDataList()
    {
        if (!NetworkUtil.isNetworkConnected())
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_network);
            mTvNoData.setText(R.string.no_network);
            mDataAdapter.setDataList(new ArrayList<OneLottery>());
            return;
        }

        list = OneLotteryDBHelper.getInstance().getLotteryByPublishHash(friendHash);
        if (null != list && !list.isEmpty())
        {
            mLyNoData.setVisibility(View.GONE);
            mDataAdapter.setDataList(list);
        } else
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_data);
            mTvNoData.setText(R.string.no_lottery);
            mDataAdapter.setDataList(new ArrayList<OneLottery>());
        }
    }
}
