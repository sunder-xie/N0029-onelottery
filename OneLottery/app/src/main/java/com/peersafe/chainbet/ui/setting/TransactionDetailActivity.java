package com.peersafe.chainbet.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
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
import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.TransactionDetail;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.TransactionDetailDBHelper;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.adapter.TransactionDetailAdapter;
import com.peersafe.chainbet.ui.lottery.LotteryDetailActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.config.OLPreferenceUtil;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.OLRefreshHeader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author caozhongzheng
 * @Description 明细查询页面
 * @date 2017/2/8 16:28
 */
public class TransactionDetailActivity extends BasicActivity implements OnItemClickListener
{

    private static final String TAG = TransactionDetailActivity.class.getSimpleName();

    //无数据或网络
    private LinearLayout mLyNoData;
    private ImageView mIvNoData;
    private TextView mTvNoData;

    //明细列表
    private LRecyclerView mRecyclerView = null;

    //适配器
    private TransactionDetailAdapter mAdapter = null;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;

    //数据库一共多少条数据
    private int TOTAL_COUNTER = 0;

    //已经获取到多少条数据了
    private int mCurrentCounter = 0;

    //明细
    private List<TransactionDetail> list = new ArrayList<>();

    // 登录用户
    private UserInfo curUser = UserInfoDBHelper.getInstance().getCurPrimaryAccount();

    private boolean isRefresh = false;
    private boolean isHistoryFetchOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        EventBus.getDefault().register(this);

        initToolBar();

        initViews();
    }

    private void initToolBar()
    {
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(getString(R.string.detail_query));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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


    private void initViews()
    {
        mLyNoData = (LinearLayout) findViewById(R.id.ly_empty);
        mTvNoData = (TextView) findViewById(R.id.tv_empty);
        mIvNoData = (ImageView) findViewById(R.id.iv_empty);

        mRecyclerView = (LRecyclerView) findViewById(R.id.detail_query_list);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.setLoadMoreEnabled(true);

        mRecyclerView.setPullRefreshEnabled(true);
        mRecyclerView.setRefreshHeader(new OLRefreshHeader(this));

        mAdapter = new TransactionDetailAdapter(this);
        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mAdapter);

        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        //下拉刷新
        mRecyclerView.setOnRefreshListener(new OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                if (!checkNetwork() && !checkService())
                {
                    mRecyclerView.refreshComplete();
                    return;
                }

                new Handler().postDelayed(new Runnable()
                {
                    public void run()
                    {
                        mRecyclerView.refreshComplete();
                    }

                }, 2000);
            }
        });

        //加载更多
        mRecyclerView.setOnLoadMoreListener(new OnLoadMoreListener()
        {
            @Override
            public void onLoadMore()
            {
                if (!checkNetwork() && !checkService())
                {
                    return;
                }
                LoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState
                        (mRecyclerView);
                if (state == LoadingFooter.State.Loading)
                {
                    return;
                }

                // loading more
                RecyclerViewStateUtils.setFooterViewState(TransactionDetailActivity.this,
                        mRecyclerView, ConstantCode.PAGE_SIZE, LoadingFooter.State.Loading, null);
                requestData();
            }
        });

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if(this.isRefresh)
        {
            this.isRefresh = false;
            this.mRecyclerView.refreshComplete();
        }

        if (!checkNetwork() && !checkService())
        {
            return;
        }

        //OneLotteryApi.getCurUserId()
        if (curUser != null)
        {
            list = TransactionDetailDBHelper.getInstance().getTransactionDetailByUserInfo(
                    curUser.getWalletAddr(), curUser.getUserId());
        }
        if (list != null && !list.isEmpty())
        {
            TOTAL_COUNTER = list.size();
            mLyNoData.setVisibility(View.GONE);
            mAdapter.setDataList(list.size() > ConstantCode.PAGE_SIZE ? list.subList(0, ConstantCode.PAGE_SIZE) :
                    list);
            mCurrentCounter = mAdapter.getItemCount();
        } else
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_data);
            mAdapter.setDataList(new ArrayList<TransactionDetail>());
            mTvNoData.setText(R.string.no_data);
        }
    }

    private boolean checkNetwork()
    {
        if (!NetworkUtil.isNetworkConnected())
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_network);
            mTvNoData.setText(R.string.no_network);
            mAdapter.setDataList(new ArrayList<TransactionDetail>());
            RecyclerViewStateUtils.setFooterViewState(mRecyclerView, LoadingFooter.State.Normal);

            return false;
        }
        return true;
    }

    private boolean checkService()
    {
        if(!OneLotteryManager.isServiceConnect)
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_service);
            mTvNoData.setText(R.string.no_service);
            mAdapter.setDataList(new ArrayList<TransactionDetail>());
            RecyclerViewStateUtils.setFooterViewState(mRecyclerView, LoadingFooter.State.Normal);

            return false;
        }

        return true;
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
                // 更新界面
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(NetworkUtil.isNetworkConnected() && OneLotteryManager.isServiceConnect)
                        {
                            if (TransactionDetailActivity.this.isRefresh)
                            {
                                mAdapter.clear();
                                mCurrentCounter = 0;
                            }

                            int currentSize = mAdapter.getItemCount();

                            boolean isDBFetchAll = false;
                            ArrayList<TransactionDetail> newList = new ArrayList<>();
                            for (int i = 0; i < ConstantCode.PAGE_SIZE; i++)
                            {
                                if (newList.size() + currentSize >= TOTAL_COUNTER)
                                {
                                    isDBFetchAll = true;
                                    break;
                                }
                                newList.add(list.get(currentSize + i));
                            }

                            // 如果数据库数据都列出来了，应该去联网获取一下是不是还有老旧的数据，一次来个至少20条，然后刷新页面
                            if (isDBFetchAll || newList.size() < ConstantCode.PAGE_SIZE)
                            {
                                if (curUser == null || !OLPreferenceUtil.getInstance(TransactionDetailActivity.this)
                                        .getGetLastOver(curUser.getUserId()))
                                {
                                    // 根据userinfo的lastGetBlockHeight往回倒数据，知道perHeight和curHeight一致，就可以停止了，并更新lastGetHeight
                                    List<TransactionDetail> ts = OneLotteryManager.getInstance().getHistoryTransacetions(ConstantCode.PAGE_SIZE);
                                    if (ts != null && !ts.isEmpty())
                                    {
                                        // 有数据就把数据加进去
                                        list = TransactionDetailDBHelper.getInstance().getTransactionDetailByUserInfo(
                                                curUser.getWalletAddr(), curUser.getUserId());
                                        TOTAL_COUNTER = list != null ? list.size() : 0;
                                        isHistoryFetchOver = ts.size() < ConstantCode.PAGE_SIZE;
                                    } else {
                                        isHistoryFetchOver = true;
                                    }
                                } else {
                                    isHistoryFetchOver = true;
                                }

                            }

                            if (TransactionDetailActivity.this.isRefresh)
                            {
                                TransactionDetailActivity.this.isRefresh = false;
                                TransactionDetailActivity.this.mRecyclerView.refreshComplete();
                            }

                            addItems(newList);

                            mAdapter.notifyDataSetChanged();
                            RecyclerViewStateUtils.setFooterViewState(mRecyclerView,
                                    isHistoryFetchOver ? LoadingFooter.State.TheEnd : LoadingFooter.State.Normal);
                        }
                        else
                        {
                            if (TransactionDetailActivity.this.isRefresh)
                            {
                                TransactionDetailActivity.this.isRefresh = false;
                                TransactionDetailActivity.this.mRecyclerView.refreshComplete();
                            }

                            mAdapter.notifyDataSetChanged();
                            RecyclerViewStateUtils.setFooterViewState(TransactionDetailActivity.this,
                                    mRecyclerView, ConstantCode.PAGE_SIZE, LoadingFooter.State.NetWorkError, mFooterClick);
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
            RecyclerViewStateUtils.setFooterViewState(TransactionDetailActivity.this, mRecyclerView, ConstantCode.PAGE_SIZE, LoadingFooter.State.Loading, null);
            requestData();
        }
    };

    private void addItems(ArrayList<TransactionDetail> newList)
    {
        mAdapter.addAll(newList);

        mCurrentCounter += newList.size();
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

        TransactionDetail tra = mAdapter.getDataList().get(position);
        switch (tra.getType())
        {
            case ConstantCode.TransactionType.TRANSACTION_TYPE_TRANSFOR:
                break;

            case ConstantCode.TransactionType.TRANSACTION_TYPE_TRANSFOR_FROM_OTHER:
                break;

            case ConstantCode.TransactionType.TRANSACTION_TYPE_TRANSFOR_FROM_ADMIN:
                break;

            case ConstantCode.TransactionType.TRANSACTION_TYPE_CREATE_LOTTERY:
            case ConstantCode.TransactionType.TRANSACTION_TYPE_MODIFY_LOTTERY:
            case ConstantCode.TransactionType.TRANSACTION_TYPE_BET:
            case ConstantCode.TransactionType.TRANSACTION_TYPE_REFUND:
            case ConstantCode.TransactionType.TRANSACTION_TYPE_PRIZE:
            case ConstantCode.TransactionType.TRANSACTION_TYPE_PERCENTAGE:
                if (StringUtils.isEmpty(tra.getTxId()))
                {
                    Intent intent_ld = new Intent(TransactionDetailActivity.this,
                            LotteryDetailActivity.class);
                    intent_ld.putExtra(ConstantCode.CommonConstant.LOTTERYID, tra.getTxId());
                    startActivity(intent_ld);
                }

                break;

            default:
                break;
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
        //参考message在insert的时候也发出一个消息。此处只处理一个消息，就是将数据添加到即可。
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_TRANSFER_DETAIL:
                TransactionDetail detail = (TransactionDetail) model.getEventObject();
                if (detail != null)
                {
//                  list.add(0, detail);
                    mAdapter.getDataList().add(0, detail);
                    mAdapter.setDataList(mAdapter.getDataList());
                }
            case OLMessageModel.STMSG_MODEL_NET_WORK_CONNECT:
                onResume();
                break;
            case OLMessageModel.STMSG_MODEL_NET_WORK_DISCONNECT:
                mRecyclerView.refreshComplete();
                break;
        }
    }
}
