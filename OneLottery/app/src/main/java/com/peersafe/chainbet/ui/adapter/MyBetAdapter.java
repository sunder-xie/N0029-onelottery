package com.peersafe.chainbet.ui.adapter;

import android.content.Context;
import android.media.tv.TvInputService;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.OneLotteryBet;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.ui.lottery.LotteryBetDialog;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.ImageUtils;
import com.peersafe.chainbet.utils.common.StringUtils;

import java.text.SimpleDateFormat;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/2/8
 * DESCRIPTION :
 */

public class MyBetAdapter extends ListBaseAdapter
{
    private LayoutInflater mLayoutInflater;

    public MyBetAdapter(Context context)
    {
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
    }

    private int mCurIndex;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new MyBetAdapter.ViewHolder(mLayoutInflater.
                inflate(R.layout.my_bet_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        OneLotteryBet bet = (OneLotteryBet) mDataList.get(position);
        final OneLottery oneLottery = bet.getOneLottery();
        if (oneLottery == null)
        {
            return;
        }
        MyBetAdapter.ViewHolder viewHolder = (MyBetAdapter.ViewHolder) holder;

        viewHolder.mName.setText(oneLottery.getLotteryName());

        Glide.with(mContext).load(ImageUtils.getLotterySquare(oneLottery.getPictureIndex())).into
                (viewHolder.mIcon);

        switch (mCurIndex)
        {
            case 0:
            case 2:
                viewHolder.mRelType.setVisibility(View.VISIBLE);
                viewHolder.mRLMyDur.setVisibility(View.GONE);
                viewHolder.mPro.setVisibility(View.GONE);
                viewHolder.mTime.setVisibility(View.VISIBLE);
                viewHolder.mState.setVisibility(View.VISIBLE);

                if (oneLottery.getState() == ConstantCode.OneLotteryState
                        .ONELOTTERY_STATE_REWARD_ALREADY &&
                        oneLottery.getPrizeTxID().equals(bet.getTicketId()))
                {
                    String betCount = String.format(mContext.getString(R.string.setting_bet_query_luckey_number),
                            oneLottery.getRewardNumbers());
                    SpannableString betCountSpan = new SpannableString(betCount);
                    betCountSpan.setSpan(new ForegroundColorSpan(mContext.getResources().getColor
                            (R.color.app_primary_color)), betCount.length() - oneLottery
                            .getRewardNumbers().length(), betCount.length(), Spanned
                            .SPAN_EXCLUSIVE_EXCLUSIVE);
                    viewHolder.mBetCount.setText(betCountSpan);
                } else
                {
                    String betCount = String.format(mContext.getString(R.string.setting_bet_query_bet),
                            bet.getBetCount());
                    int index = betCount.indexOf("" + bet.getBetCount());
                    SpannableString betCountSpan = new SpannableString(betCount);
                    betCountSpan.setSpan(new ForegroundColorSpan(mContext.getResources().getColor
                            (R.color.app_primary_color)), index, index + String.valueOf(bet.getBetCount()).length(), Spanned
                            .SPAN_EXCLUSIVE_EXCLUSIVE);
                    viewHolder.mBetCount.setText(betCountSpan);
                }

                if (mCurIndex == 0)
                {
                    String status = "";
                    String tvStatus = "";
                    switch (oneLottery.getState())
                    {
                        case 1:
                            tvStatus = mContext.getString(R.string.setting_bet_query_dur);
                            status = String.format(mContext.getString(R.string
                                    .setting_bet_query_status),tvStatus);
                            break;
                        case 2:
                        case 3:
                            tvStatus = mContext.getString(R.string.recent_reward_ing);
                            status = String.format(mContext.getString(R.string
                                    .setting_bet_query_status), tvStatus);
                            break;
                        case 4:
                            tvStatus = mContext.getString(R.string.setting_bet_query_reward_already);
                            status = String.format(mContext.getString(R.string
                                    .setting_bet_query_status),tvStatus);
                            break;
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                            tvStatus = mContext.getString(R.string.setting_bet_query_fail);
                            status = String.format(mContext.getString(R.string
                                    .setting_bet_query_status), tvStatus);
                            break;
                    }
                    if (!StringUtils.isEmpty(status))
                    {
                        SpannableString statusSpan = new SpannableString(status);
                        statusSpan.setSpan(new ForegroundColorSpan(mContext.getResources()
                                .getColor(R.color.lottery_prize_text_color)), status.length() - tvStatus.length(), status.length(),
                                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

                        viewHolder.mState.setText(statusSpan);
                    }
                } else
                {
                    viewHolder.mState.setVisibility(View.GONE);
                }


                SimpleDateFormat dateFormat = new SimpleDateFormat(ConstantCode.CommonConstant
                        .SIMPLE_DATE_FORMAT);
                viewHolder.mTime.setText(dateFormat.format(bet.getCreateTime()));
                break;
            case 1:
                viewHolder.mRelType.setVisibility(View.GONE);
                viewHolder.mRLMyDur.setVisibility(View.VISIBLE);
                viewHolder.mPro.setVisibility(View.VISIBLE);
                viewHolder.mTime.setVisibility(View.GONE);

                viewHolder.mPro.setMax(oneLottery.getMaxBetCount());
                viewHolder.mPro.setProgress(oneLottery.getCurBetCount());

                String durBet = String.format(mContext.getString(R.string.setting_bet_query_bet),
                        bet.getBetCount());
                int index = durBet.indexOf("" + bet.getBetCount());
                SpannableString durBetSpan = new SpannableString(durBet);
                durBetSpan.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color
                        .app_primary_color)), index, index + String.valueOf(bet.getBetCount()).length(), Spanned
                        .SPAN_EXCLUSIVE_EXCLUSIVE);
                viewHolder.mDurBet.setText(durBetSpan);

                viewHolder.mBet.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (!OneLotteryApi.isVisitor(mContext))
                        {
                            LotteryBetDialog lotteryBetDialog = new LotteryBetDialog(mContext,
                                    oneLottery);
                            lotteryBetDialog.show();
                        }
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount()
    {
        return mDataList.size();
    }

    public void setCurIndex(int mCurIndex)
    {
        this.mCurIndex = mCurIndex;
    }

    private class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView mIcon;
        TextView mName;
        TextView mBetCount;
        TextView mState;
        TextView mTime;

        RelativeLayout mRelType;
        RelativeLayout mRLMyDur;

        ProgressBar mPro;
        TextView mDurBet;
        Button mBet;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.img_bet_icon);
            mName = (TextView) itemView.findViewById(R.id.tv_name);
            mBetCount = (TextView) itemView.findViewById(R.id.tv_bet_count);
            mState = (TextView) itemView.findViewById(R.id.tv_status);
            mTime = (TextView) itemView.findViewById(R.id.tv_time);

            mRelType = (RelativeLayout) itemView.findViewById(R.id.rl_all_tv_type);
            mRLMyDur = (RelativeLayout) itemView.findViewById(R.id.rl_dur_my_bet);

            mPro = (ProgressBar) itemView.findViewById(R.id.pb_bet_progress);
            mDurBet = (TextView) itemView.findViewById(R.id.tv_dur_my_bet);
            mBet = (Button) itemView.findViewById(R.id.btn_dur_add_bet);
        }
    }
}
