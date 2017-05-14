package com.peersafe.chainbet.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.interfaces.OnLoadMoreListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.github.jdsjlzx.util.RecyclerViewStateUtils;
import com.github.jdsjlzx.view.LoadingFooter;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLotteryBet;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryBetDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.adapter.MyBetAdapter;
import com.peersafe.chainbet.ui.lottery.LotteryDetailActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.log.OLLogger;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.OLRefreshHeader;
import com.peersafe.chainbet.widget.WaitingDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MyBetAcitivity extends BasicActivity implements View.OnClickListener,
        OnItemClickListener
{
    private LRecyclerView mRecyclerView = null;

    private MyBetAdapter mDataAdapter = null;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;

    View mLotteryIndicatorView;

    //全部
    Button mBtnAll;

    //进行中
    Button mBtnDur;

    //幸运查询
    Button mBtnPrize;

    Button[] mIndex;

    int mCurIndex;

    //已经获取到多少条数据了
    private static int mCurrentCounter = 0;

    //数据库一共多少条数据
    private int TOTAL_COUNTER = 0;

    //每一页展示多少条数据
    private int REQUEST_COUNT = 20;

    LinearLayout mLyNoData;
    TextView mTvNoData;
    ImageView mIvNoData;

    private boolean isRefresh = false;

    private List[] mDataList = new List[3];

    private List<OneLotteryBet> mAll = new ArrayList<>();
    private List<OneLotteryBet> mDur = new ArrayList<>();
    private List<OneLotteryBet> mPrize = new ArrayList<>();

    private boolean isShowProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bet_acitivity);
        EventBus.getDefault().register(this);

        mRecyclerView = (LRecyclerView) findViewById(R.id.list);
        LinearLayoutManager manager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(manager);

        mRecyclerView.setPullRefreshEnabled(true);
        mRecyclerView.setRefreshHeader(new OLRefreshHeader(this));

        mDataAdapter = new MyBetAdapter(this);
        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);

        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        addIndexHeadView();

        mRecyclerView.setOnRefreshListener(new OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                if (!NetworkUtil.isNetworkConnected())
                {
                    mDataAdapter.setDataList(new ArrayList());
                    mLyNoData.setVisibility(View.VISIBLE);
                    mIvNoData.setImageResource(R.drawable.no_network);
                    mTvNoData.setText(R.string.no_network);
                    mRecyclerView.refreshComplete();
                    return;
                }

                if (!OneLotteryManager.isServiceConnect)
                {
                    mDataAdapter.setDataList(new ArrayList());
                    mLyNoData.setVisibility(View.VISIBLE);
                    mIvNoData.setImageResource(R.drawable.no_service);
                    mTvNoData.setText(R.string.no_service);
                    mRecyclerView.refreshComplete();
                    return;
                }

                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        isRefresh = true;
                        OneLotteryManager.getInstance().getLotteries(false, true);
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                getCurData(mCurIndex,false);
                            }
                        });
                    }
                }).start();
            }
        });

        setOnLoadMoreListen();

        initToolBar();

        mLRecyclerViewAdapter.setOnItemClickListener(this);
    }

    private void setOnLoadMoreListen()
    {
        mRecyclerView.setOnLoadMoreListener(new OnLoadMoreListener()
        {
            @Override
            public void onLoadMore()
            {
                LoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState
                        (mRecyclerView);
                if (state == LoadingFooter.State.Loading)
                {
                    return;
                }

                if (mCurrentCounter < TOTAL_COUNTER)
                {
                    // loading more
                    RecyclerViewStateUtils.setFooterViewState(MyBetAcitivity.this,
                            mRecyclerView, REQUEST_COUNT, LoadingFooter.State.Loading, null);
                    requestData();
                } else
                {
                    //the end
                    RecyclerViewStateUtils.setFooterViewState(MyBetAcitivity.this,
                            mRecyclerView, REQUEST_COUNT, LoadingFooter.State.TheEnd, null);

                }
            }
        });
    }

    /**
     * 请求底部参与列表数据
     */
    private void requestData()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                super.run();
                try
                {
                    Thread.sleep(80);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                // 更新界面
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (NetworkUtil.isNetworkConnected() && OneLotteryManager.isServiceConnect)
                        {
                            if (MyBetAcitivity.this.isRefresh)
                            {
                                mDataAdapter.clear();
                                mCurrentCounter = 0;
                            }

                            int currentSize = mDataAdapter.getItemCount();

                            ArrayList<OneLotteryBet> newList = new ArrayList<>();
                            for (int i = 0; i < REQUEST_COUNT; i++)
                            {
                                if (newList.size() + currentSize >= TOTAL_COUNTER)
                                {
                                    break;
                                }
                                newList.add((OneLotteryBet) mDataList[mCurIndex].get(currentSize + i));
                            }

                            if (MyBetAcitivity.this.isRefresh)
                            {
                                MyBetAcitivity.this.isRefresh = false;
                                MyBetAcitivity.this.mRecyclerView.refreshComplete();
                            }

                            addItems(newList);

                            mDataAdapter.notifyDataSetChanged();
                            RecyclerViewStateUtils.setFooterViewState(mRecyclerView, LoadingFooter.State.Normal);
                        } else
                        {
                            if (MyBetAcitivity.this.isRefresh)
                            {
                                MyBetAcitivity.this.isRefresh = false;
                                MyBetAcitivity.this.mRecyclerView.refreshComplete();
                            }

                            mDataAdapter.notifyDataSetChanged();
                            RecyclerViewStateUtils.setFooterViewState(MyBetAcitivity.this, mRecyclerView,
                                    REQUEST_COUNT, LoadingFooter.State.NetWorkError, mFooterClick);
                        }
                    }
                });
            }
        }.start();
    }

    /**
     * 点击底部加载更多
     */
    private View.OnClickListener mFooterClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            RecyclerViewStateUtils.setFooterViewState(MyBetAcitivity.this, mRecyclerView,
                    REQUEST_COUNT, LoadingFooter.State.Loading, null);
            requestData();
        }
    };

    private void addItems(ArrayList<OneLotteryBet> list)
    {
        mDataAdapter.addAll(list);
        mCurrentCounter += list.size();
    }

    /**
     * 添加
     */
    private void addIndexHeadView()
    {
        mLotteryIndicatorView = findViewById(R.id.layout_indicator);
        mBtnAll = (Button) findViewById(R.id.btn_bet_all);
        mBtnDur = (Button) findViewById(R.id.btn_bet_dur);
        mBtnPrize = (Button) findViewById(R.id.btn_bet_prize);

        mIndex = new Button[]{mBtnAll, mBtnDur, mBtnPrize};
        mCurIndex = 0;
        mIndex[0].setSelected(true);
        mLotteryIndicatorView.setTranslationX(getResources().getDimension(R.dimen.x240) *
                mCurIndex + getResources().getDimension(R.dimen.x32));

        mBtnAll.setOnClickListener(this);
        mBtnDur.setOnClickListener(this);
        mBtnPrize.setOnClickListener(this);

        mLyNoData = (LinearLayout) findViewById(R.id.ly_empty);
        mTvNoData = (TextView) findViewById(R.id.tv_empty);
        mIvNoData = (ImageView) findViewById(R.id.iv_empty);

        getCurData(3,true);
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(getString(R.string.setting_my_bet));

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
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_bet_all:
                setCurIndex(0);
                setCurAdapter(0);
                break;
            case R.id.btn_bet_dur:
                setCurIndex(1);
                setCurAdapter(1);
                break;
            case R.id.btn_bet_prize:
                setCurIndex(2);
                setCurAdapter(2);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(final OLMessageModel model)
    {
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_NOTIFY:
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_OVER_NOTIFY:
            case OLMessageModel.STMSG_MODEL_OPEN_REWARD_CALLBACK:
            case OLMessageModel.STMSG_MODEL_OPEN_REWARD_NOTIFY:
                getCurData(3,false);
                break;
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_CALLBACK:
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_CONSENSUS_OVER_NOTIFY:
            {
                boolean result = (boolean) model.getEventObject();

                if (result)
                {
                    getCurData(3,false);
                }

                WaitingDialog waitingDialog = getWaitingDialog();
                if (waitingDialog != null && waitingDialog.isShowing())
                {
                    if (result)
                    {
                        getWaitingDialog().setBtnText(getString(R.string
                                .message_bet_success));
                    } else
                    {
                        getWaitingDialog().setBtnText(getString(R.string
                                .message_bet_fail));
                    }
                }

                // 获取用户余额，刷新UI
                OneLotteryManager.getInstance().getUserBalance();
            }
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
            case OLMessageModel.STMSG_MODEL_REFRESH_BET_LIST:
                OLLogger.e("MyBet","refresh bet list");
                setCurAdapter(mCurIndex);
                break;
        }
    }

    private void setCurAdapter(final int mCurLotteryIndex)
    {
        if(isShowProgress)
        {
            isShowProgress = false;
            dismissProgressDialog();
        }

        if (this.isRefresh)
        {
            this.isRefresh = false;
            this.mRecyclerView.refreshComplete();
        }

        if (!NetworkUtil.isNetworkConnected())
        {
            mDataAdapter.setDataList(new ArrayList());
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_network);
            mTvNoData.setText(R.string.no_network);
            return;
        }

        if (!OneLotteryManager.isServiceConnect)
        {
            mDataAdapter.setDataList(new ArrayList());
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_service);
            mTvNoData.setText(R.string.no_service);
            return;
        }

        if (null != mDataList && !mDataList[mCurLotteryIndex].isEmpty())
        {
            TOTAL_COUNTER = mDataList[mCurLotteryIndex].size();
            mLyNoData.setVisibility(View.GONE);
            mDataAdapter.setCurIndex(mCurLotteryIndex);
            mDataAdapter.setDataList(mDataList[mCurLotteryIndex].size() > REQUEST_COUNT ? mDataList[mCurLotteryIndex].subList(0, REQUEST_COUNT) : mDataList[mCurLotteryIndex]);
            mCurrentCounter = mDataAdapter.getItemCount();
        } else
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_data);
            mTvNoData.setText(R.string.no_data);
            mDataAdapter.setDataList(new ArrayList());
        }
    }

    public void getCurData(final int mCurLotteryIndex,boolean isShow)
    {
        if(isShow)
        {
            isShowProgress = true;
            showProgressDialog(getString(R.string.loading),false);
        }

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                switch (mCurLotteryIndex)
                {
                    case 0:
                        mAll = OneLotteryBetDBHelper.getInstance().getMyAllBet();
                        break;
                    case 1:
                        mDur = OneLotteryBetDBHelper.getInstance().getMyAllDurBet();
                        break;
                    case 2:
                        mPrize = OneLotteryBetDBHelper.getInstance().getMyFinishBet();
                        break;
                    case 3:
                        mAll = OneLotteryBetDBHelper.getInstance().getMyAllBet();
                        mDur = OneLotteryBetDBHelper.getInstance().getMyAllDurBet();
                        mPrize = OneLotteryBetDBHelper.getInstance().getMyFinishBet();
                        OLLogger.e("MyBet",mAll.size() + " " + mDur.size() + " " + mPrize.size());
                        break;
                }

                mDataList[0] = mAll;
                mDataList[1] = mDur;
                mDataList[2] = mPrize;

                OneLotteryManager.getInstance().SendEventBus(null,OLMessageModel.STMSG_MODEL_REFRESH_BET_LIST);
            }
        }).start();
    }

    private void setCurIndex(int curIndex)
    {
        if (mCurIndex == curIndex)
        {
            return;
        }
        mIndex[mCurIndex].setSelected(false);
        mIndex[curIndex].setSelected(true);

        mCurIndex = curIndex;
        mLotteryIndicatorView.setTranslationX(getResources().getDimension(R.dimen.x240) *
                mCurIndex + getResources().getDimension(R.dimen.x32));
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

        OneLotteryBet oneLotteryBet = (OneLotteryBet) mDataList[mCurIndex].get(position);
        String lotteryId = oneLotteryBet.getLotteryId();

        Intent intent = new Intent(this, LotteryDetailActivity.class);
        intent.putExtra(ConstantCode.CommonConstant.LOTTERYID, lotteryId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
