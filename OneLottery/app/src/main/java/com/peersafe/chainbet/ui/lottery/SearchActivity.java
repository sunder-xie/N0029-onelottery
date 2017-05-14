package com.peersafe.chainbet.ui.lottery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryDeleteRet;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.adapter.FriendLotteryAdapter;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.log.OLLogger;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/2/6
 * DESCRIPTION :
 */
public class SearchActivity extends BasicActivity implements View.OnClickListener, View
        .OnKeyListener, OnItemClickListener
{
    private static final String TAG = SearchActivity.class.getSimpleName();
    private LRecyclerView mRecyclerView = null;

    private FriendLotteryAdapter mDataAdapter = null;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;

    //搜索
    private EditText mSearch;

    //取消
    private TextView mCancel;

    //数据源
    private List<OneLottery> mDataList = new ArrayList<>();

    private LinearLayout mLyNoData;
    private TextView mTvNoData;
    private ImageView mIvNoData;
    private boolean isFirst = false;

    private boolean isEnter = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lottery_search_activity);
        EventBus.getDefault().register(this);

        mCancel = (TextView) findViewById(R.id.search_cancel);
        mSearch = (EditText) findViewById(R.id.search);

        mLyNoData = (LinearLayout) findViewById(R.id.ly_empty);
        mTvNoData = (TextView) findViewById(R.id.tv_empty);
        mIvNoData = (ImageView) findViewById(R.id.iv_empty);

        mRecyclerView = (LRecyclerView) findViewById(R.id.search_list);

        //setLayoutManager must before setAdapter
        LinearLayoutManager manager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setPullRefreshEnabled(false);

        mDataAdapter = new FriendLotteryAdapter(this);

        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);
        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        mLRecyclerViewAdapter.setOnItemClickListener(this);
        mCancel.setOnClickListener(this);
        mSearch.setOnKeyListener(this);

        mSearch.addTextChangedListener(watcher);

        isFirst = true;

        mSearch.setFilters(new InputFilter[]{StringUtils.getFilter()});
    }

    private TextWatcher watcher = new TextWatcher()
    {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            // TODO Auto-generated method stub
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after)
        {
            // TODO Auto-generated method stub
        }

        @Override
        public void afterTextChanged(Editable s)
        {
            isEnter = false;
            // TODO Auto-generated method stub
            if (StringUtils.isEmpty(s.toString()))
            {
                isEnter = true;
                mDataAdapter.setDataList(new ArrayList<OneLottery>());
                mLyNoData.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.search_cancel:
                finish();
                break;
        }
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent)
    {
        if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP)
        {
            //先隐藏键盘
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getCurrentFocus()
                            .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            if (!NetworkUtil.isNetworkConnected())
            {
                mLyNoData.setVisibility(View.VISIBLE);
                mIvNoData.setImageResource(R.drawable.no_network);
                mDataAdapter.setDataList(new ArrayList<OneLottery>());
                mTvNoData.setText(R.string.no_network);
                return false;
            }

            if (!OneLotteryManager.isServiceConnect)
            {
                mLyNoData.setVisibility(View.VISIBLE);
                mIvNoData.setImageResource(R.drawable.no_service);
                mDataAdapter.setDataList(new ArrayList<OneLottery>());
                mTvNoData.setText(R.string.no_service);
                return false;
            }

            search();

        }
        return false;
    }

    private void search()
    {
        mDataList.clear();

        //进行搜索逻辑
        final String string = mSearch.getText().toString().trim();

        if (StringUtils.isEmpty(string))
        {
            setDataList();
            return;
        }

        mDataList = OneLotteryDBHelper.getInstance().getLocalLottery(string, true);

        if (mDataList.size() >= 10)
        {
            mDataList = mDataList.subList(0, 10);
            setDataList();
            return;
        }

        if (mDataList.size() != 0)
        {
            setDataList();
        }

        if (mDataList.size() < 10)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    List<OneLottery> list = OneLotteryDBHelper.getInstance().getLocalLottery
                            (string, false);
                    for (OneLottery lottery : list)
                    {
                        boolean resulet = OneLotteryManager.getInstance().getLotteryDetail
                                (lottery.getLotteryId());
                        if (!resulet)
                        {
                            resulet = OneLotteryManager.getInstance().getLotteryHistoryDetail
                                    (lottery.getLotteryId());
                        }

                        if (resulet)
                        {
                            OneLottery oneLottery = OneLotteryDBHelper.getInstance()
                                    .getLotteryByLotterId(lottery.getLotteryId());
                            mDataList.add(oneLottery);
                        }
                        if (mDataList.size() >= 10)
                        {
                            break;
                        }
                    }

                    Collections.sort(mDataList, new Comparator<OneLottery>()
                    {
                        @Override
                        public int compare(OneLottery oneLottery, OneLottery t1)
                        {
                            if (oneLottery.getCreateTime().getTime() > t1.getCreateTime().getTime())
                            {
                                return -1;
                            }
                            return 0;
                        }
                    });

                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (!isEnter)
                            {
                                setDataList();
                            }
                        }
                    });
                }
            }).start();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(final OLMessageModel model)
    {
        if (model == null)
        {
            return;
        }
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_NOTIFY:
                boolean refresh = false;
                for (int i = 0; i < mDataList.size(); i++)
                {
                    String lotteryId = (String) model.getEventObject();
                    if (lotteryId.equals(mDataList.get(i)))
                    {
                        refresh = true;
                    }
                }

                if (refresh)
                {
                    search();
                }
            case OLMessageModel.STMSG_MODEL_NET_WORK_CONNECT:

                break;
            case OLMessageModel.STMSG_MODEL_NET_WORK_DISCONNECT:
                onResume();
                break;
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (!isFirst)
        {
            setDataList();
        }
    }

    public void setDataList()
    {
        if (mDataList != null && !mDataList.isEmpty())
        {
            mLyNoData.setVisibility(View.GONE);
            mDataAdapter.setDataList(mDataList);
        } else
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_data);
            mTvNoData.setText(R.string.no_search_lottery);
            mDataAdapter.setDataList(new ArrayList<OneLottery>());
        }
    }

    @Override
    public void onItemClick(View view, int position)
    {
        OneLottery oneLottery = mDataList.get(position);
        String lotteryId = oneLottery.getLotteryId();
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
