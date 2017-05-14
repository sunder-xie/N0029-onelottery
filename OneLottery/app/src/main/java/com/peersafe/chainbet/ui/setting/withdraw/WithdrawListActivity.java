package com.peersafe.chainbet.ui.setting.withdraw;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.Friend;
import com.peersafe.chainbet.db.WithdrawRecord;
import com.peersafe.chainbet.logic.WithdrawLogic;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.WithdrawRecordDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.adapter.ListBaseAdapter;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.OLRefreshHeader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class WithdrawListActivity extends BasicActivity implements OnItemClickListener
{

    private LRecyclerView mRecyclerView;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;
    private WithdrawListAdapter mDataAdapter = null;

    private List mDatalist = new ArrayList();

    public static WithdrawListActivity isntance = null;

    private LinearLayout mLyNoData;
    private TextView mTvNoData;
    private ImageView mIvNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_list);

        EventBus.getDefault().register(this);

        isntance = this;

        mRecyclerView = (LRecyclerView) findViewById(R.id.list);
        mLyNoData = (LinearLayout) findViewById(R.id.ly_empty);
        mTvNoData = (TextView) findViewById(R.id.tv_empty);
        mIvNoData = (ImageView) findViewById(R.id.iv_empty);

        LinearLayoutManager manager = new LinearLayoutManager(WithdrawListActivity.this);
        mRecyclerView.setLayoutManager(manager);

        mRecyclerView.setPullRefreshEnabled(true);
        mRecyclerView.setLoadMoreEnabled(false);
        mRecyclerView.setRefreshHeader(new OLRefreshHeader(WithdrawListActivity.this));

        mDataAdapter = new WithdrawListAdapter(WithdrawListActivity.this);

        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);
        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        mLRecyclerViewAdapter.setOnItemClickListener(this);

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

                if(!OneLotteryManager.getInstance().isServiceConnect)
                {
                    mLyNoData.setVisibility(View.VISIBLE);
                    mIvNoData.setImageResource(R.drawable.no_service);
                    mDataAdapter.setDataList(new ArrayList<Friend>());
                    mTvNoData.setText(getString(R.string.no_service));
                    mRecyclerView.refreshComplete();
                    return;
                }

                getRefresh();
            }
        });

        getRefresh();

        setData();

        initToolBar();
    }

    /**
     * 比对数据库和网络数据是否一样
     */
    private void getRefresh()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                final boolean result = WithdrawLogic.queryWithdrawList();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mRecyclerView.refreshComplete();
                        if(result)
                        {
                            setData();
                        }
                    }
                });
            }
        }).start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(final OLMessageModel model)
    {
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_REMIT_SUCCES_NOTIFY:
            case OLMessageModel.STMSG_MODEL_WITH_DRAW_FAIL_NOTIFY:
            case OLMessageModel.STMSG_MODEL_APPEAL_DONE_NOTIFY:
            case OLMessageModel.STMSG_MODEL_WITH_DRAW_APPEAL_CALLBACK:
            case OLMessageModel.STMSG_MODEL_WITH_DRAW_CONFIRM:
            case OLMessageModel.STMSG_MODEL_WITH_DRAW_NOTIFY:
                setData();
                break;
        }
    }

    public void setData()
    {
        mDatalist = WithdrawRecordDBHelper.getInstance().getAllRecordList();
        if (mDatalist != null && !mDatalist.isEmpty())
        {
            mLyNoData.setVisibility(View.GONE);
            mDataAdapter.setDataList(mDatalist);
        } else
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_data);
            mTvNoData.setText(R.string.no_record);
            mDataAdapter.setDataList(new ArrayList<Friend>());
        }
    }

    @Override
    public void onItemClick(View view, int position)
    {
        WithdrawRecord record = (WithdrawRecord) mDatalist.get(position);
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(ConstantCode.CommonConstant.WITH_DRAW_RECORD, record);
        startActivity(intent);
    }

    class WithdrawListAdapter extends ListBaseAdapter
    {
        LayoutInflater mLayoutInflater = null;
        Context context;

        public WithdrawListAdapter(Context context)
        {
            this.context = context;
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return new WithdrawListAdapter.ViewHolder(mLayoutInflater.
                    inflate(R.layout.whthdraw_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int pos)
        {
            WithdrawRecord record = (WithdrawRecord) mDataList.get(pos);
            final WithdrawListAdapter.ViewHolder viewHolder = (WithdrawListAdapter.ViewHolder)
                    holder;

            double amount = (double)record.getAmount() / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE;
            DecimalFormat df = new DecimalFormat("0.00");
            if(record.getState() == ConstantCode.WithdrawType.WITHDRAW_TYPE_FAIL)
            {
                viewHolder.mCount.setText("+" + df.format(amount));
            }
            else
            {
                viewHolder.mCount.setText("-" + df.format(amount));
            }

            switch (record.getState())
            {
                case ConstantCode.WithdrawType.WITHDRAW_TYPE_APPLYING:
                    viewHolder.mStatus.setText(getString(R.string.withdraw_status_applying));
                    break;
                case ConstantCode.WithdrawType.WITHDRAW_TYPE_PAY:
                    viewHolder.mStatus.setText(getString(R.string.withdraw_status_pay));
                    break;
                case ConstantCode.WithdrawType.WITHDRAW_TYPE_CONFIRM:
                    viewHolder.mStatus.setText(getString(R.string.withdraw_status_confirm));
                    break;
                case ConstantCode.WithdrawType.WITHDRAW_TYPE_CANCEL:
                    viewHolder.mStatus.setText(getString(R.string.withdraw_status_cancel));
                    break;
                case ConstantCode.WithdrawType.WITHDRAW_TYPE_FAIL:
                    viewHolder.mStatus.setText(getString(R.string.withdraw_status_fail));
                    break;
                case ConstantCode.WithdrawType.WITHDRAW_TYPE_APPEAL:
                    viewHolder.mStatus.setText(getString(R.string.withdraw_status_appealing));
                    break;
            }

            SimpleDateFormat format = new SimpleDateFormat(ConstantCode.CommonConstant.SIMPLE_DATE_FORMAT);
            viewHolder.mTime.setText(format.format(record.getCreateTime()));
        }

        private class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView mCount;
            TextView mTime;
            TextView mStatus;

            public ViewHolder(View itemView)
            {
                super(itemView);
                mCount = (TextView) itemView.findViewById(R.id.tv_count);
                mTime = (TextView) itemView.findViewById(R.id.tv_time);
                mStatus = (TextView) itemView.findViewById(R.id.tv_status);
            }
        }
    }

    public void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(getString(R.string.withdraw_list_text));

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

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
    protected void onDestroy()
    {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }
}
