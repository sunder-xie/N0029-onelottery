package com.peersafe.chainbet.ui.lottery;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryBetDBHelper;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.model.AttendBean;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.adapter.ListBaseAdapter;
import com.peersafe.chainbet.utils.common.ConstantCode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author moying
 * @Description 计算详情界面
 * @date 2017/1/9 16:12
 */
public class CalculateDetailActivity extends BasicActivity implements View.OnClickListener
{
    private LRecyclerView mRecyclerView = null;

    private CalculateAdapter mDataAdapter = null;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;

    private Button mBtnA;
    private Button mBtnB;
    private TextView mTvTime;
    private TextView mTvTotal;
    private TextView mTvNumber;
    private LinearLayout mllTimeClose;
    private TextView mTvTextDesc;

    private boolean isSlectA = false;
    private boolean isSlectB = false;

    private OneLottery lottery;

    List<AttendBean> mDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculation_detail);

        lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId(getIntent().getStringExtra
                (ConstantCode.CommonConstant.LOTTERYID));

        mBtnA = (Button) findViewById(R.id.btn_a);
        mBtnB = (Button) findViewById(R.id.btn_b);
        mTvTime = (TextView) findViewById(R.id.tv_close_time);
        mTvTotal = (TextView) findViewById(R.id.tv_number_total);
        mRecyclerView = (LRecyclerView) findViewById(R.id.list);
        mTvTextDesc = (TextView) findViewById(R.id.tv_close_time_desc);
        mllTimeClose = (LinearLayout) findViewById(R.id.ll_attend_time_user);
        mTvNumber = (TextView) findViewById(R.id.tv_prize_number);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);

        mRecyclerView.setPullRefreshEnabled(false);
        mRecyclerView.setLoadMoreEnabled(false);

        mDataAdapter = new CalculateAdapter(this);
        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);

        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        switch (lottery.getState())
        {
            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_NOT_STARTED:
            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_ON_GOING:
            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_CAN_REWARD:
            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_REWARD_ING:

                mTvNumber.setText(getString(R.string.calculation_waiting_open));
                mTvTime.setText(getString(R.string.calculation_waiting_open));
                mTvTotal.setText(getString(R.string.calculation_waiting_open));
                mBtnA.setVisibility(View.GONE);
                break;

            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_REWARD_ALREADY:

                mTvTime.setText(String.valueOf(lottery.getLastCloseTime().getTime()));
                mTvNumber.setText(lottery.getRewardNumbers());
                mBtnA.setEnabled(true);

                mDataList = OneLotteryBetDBHelper.getInstance().getOneHundredBetInfo(lottery
                        .getLotteryId());
                mDataAdapter.setDataList(mDataList);

                long total = 0;
                for (AttendBean bean : mDataList)
                {
                    long betTime = bean.getAttendTime().getTime();
                    total += betTime;
                }

                mTvTotal.setText(String.valueOf(total));
                break;

            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_REFUND_ALREADY:
            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_CAN_REFUND:
            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_REFUND_ING:
            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_FAIL:

                mTvNumber.setText(getString(R.string.calculation_open_fail));
                mTvTime.setText(getString(R.string.calculation_open_fail));
                mTvTotal.setText(getString(R.string.calculation_open_fail));
                mBtnA.setVisibility(View.GONE);
                break;
        }

        //解决scrollView和recycleView滑动冲突
        mRecyclerView.setNestedScrollingEnabled(false);

        mBtnA.setOnClickListener(this);
        mBtnB.setOnClickListener(this);

        initToolBar();
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(getString(R.string.calculation_detail));

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
            case R.id.btn_a:
                if (isSlectA)
                {
                    isSlectA = false;
                    mllTimeClose.setVisibility(View.GONE);
                    mBtnA.setText(getString(R.string.calculation_down));
                    Drawable drawable = getResources().getDrawable(R.drawable.arrow_down);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable
                            .getMinimumHeight());
                    mBtnA.setCompoundDrawables(null, null, drawable, null);
                } else
                {
                    isSlectA = true;
                    mllTimeClose.setVisibility(View.VISIBLE);
                    mBtnA.setText(getString(R.string.calculation_up));
                    Drawable drawable = getResources().getDrawable(R.drawable.arrow_up);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable
                            .getMinimumHeight());
                    mBtnA.setCompoundDrawables(null, null, drawable, null);
                }
                break;
            case R.id.btn_b:
                if (isSlectB)
                {
                    isSlectB = false;
                    mTvTextDesc.setVisibility(View.GONE);
                    mBtnB.setText(getString(R.string.calculation_down));
                    Drawable drawable = getResources().getDrawable(R.drawable.arrow_down);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable
                            .getMinimumHeight());
                    mBtnB.setCompoundDrawables(null, null, drawable, null);
                } else
                {
                    isSlectB = true;
                    mTvTextDesc.setVisibility(View.VISIBLE);
                    mBtnB.setText(getString(R.string.calculation_up));
                    Drawable drawable = getResources().getDrawable(R.drawable.arrow_up);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable
                            .getMinimumHeight());
                    mBtnB.setCompoundDrawables(null, null, drawable, null);
                }
                break;
            default:
                break;
        }
    }

    public class CalculateAdapter extends ListBaseAdapter
    {
        private LayoutInflater mLayoutInflater;

        public CalculateAdapter(Context context)
        {
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return new CalculateAdapter.ViewHolder(mLayoutInflater.
                    inflate(R.layout.calculate_detail_list, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
        {
            AttendBean bean = (AttendBean) getDataList().get(position);
            CalculateAdapter.ViewHolder viewHolder = (CalculateAdapter.ViewHolder) holder;

            SimpleDateFormat dateFormat = new SimpleDateFormat(ConstantCode.CommonConstant.SIMPLE_DATE_FORMAT2);
            String time = dateFormat.format(bean.getAttendTime());

            viewHolder.mTime.setText(time);
            viewHolder.mUser.setText(bean.getAttendName());

            viewHolder.mNumber.setText(String.valueOf(bean.getAttendTime().getTime()));
        }

        @Override
        public int getItemCount()
        {
            return mDataList.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView mTime;
            TextView mNumber;
            TextView mUser;

            public ViewHolder(View itemView)
            {
                super(itemView);
                mTime = (TextView) itemView.findViewById(R.id.calculate_time);
                mNumber = (TextView) itemView.findViewById(R.id.calculate_numbers);
                mUser = (TextView) itemView.findViewById(R.id.calculate_user);
            }
        }
    }
}
