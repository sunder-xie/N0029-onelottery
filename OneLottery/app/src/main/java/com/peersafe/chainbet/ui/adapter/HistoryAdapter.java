package com.peersafe.chainbet.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.OneLotteryBet;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryBetDBHelper;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.ImageUtils;

/**
 * Created by sunhaitao on 17/1/3.
 */

public class HistoryAdapter extends ListBaseAdapter<OneLottery>
{
    private LayoutInflater mLayoutInflater;
    private Context context;

    public HistoryAdapter(Context context)
    {
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new HistoryAdapter.ViewHolder(mLayoutInflater.
                inflate(R.layout.recent_history_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        OneLottery lottery = mDataList.get(position);
        HistoryAdapter.ViewHolder viewHolder = (HistoryAdapter.ViewHolder) holder;

        if (lottery.getPublisherHash().equals(ConstantCode.CommonConstant
                .ONELOTTERY_DEFAULT_OFFICAL_HASH))
        {
            viewHolder.mTvLabel.setText(context.getString(R.string.lottery_offical_flag));
        } else
        {
            viewHolder.mTvLabel.setText(context.getString(R.string.lottery_personal_flag));
        }

        Glide.with(context).load(ImageUtils.getLotteryImageview(lottery.getPictureIndex())).centerCrop().into(viewHolder.mImgHead);

        viewHolder.mTvDescrbe.setText(lottery.getLotteryName());

        String prizeTxID = lottery.getPrizeTxID();
        OneLotteryBet lotteryBet = OneLotteryBetDBHelper.getInstance().getLotteryBetByTxID
                (prizeTxID);
        if (null != lotteryBet)
        {
            String reward = String.format(context.getString(R.string.lottery_detail_award),
                    lotteryBet.getAttendeeName());
            SpannableString rewardSpan = new SpannableString(reward);
            rewardSpan.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color
                    .lottery_prize_text_color)), reward.length() - lotteryBet.getAttendeeName().length(), reward.length(), Spanned
                    .SPAN_INCLUSIVE_EXCLUSIVE);
            viewHolder.mTvAward.setText(rewardSpan);
        }

        viewHolder.mTvTime.setText(DateFormat.format(ConstantCode.CommonConstant
                .SIMPLE_DATE_FORMAT, lottery.getLastCloseTime()));
    }

    @Override
    public int getItemCount()
    {
        return mDataList.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView mTvLabel;
        ImageView mImgHead;
        TextView mTvDescrbe;
        TextView mTvTime;
        TextView mTvAward;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mTvLabel = (TextView) itemView.findViewById(R.id.tv_history_label_text);
            mImgHead = (ImageView) itemView.findViewById(R.id.img_history_top_img);
            mTvDescrbe = (TextView) itemView.findViewById(R.id.tv_history_lottery_describe);
            mTvAward = (TextView) itemView.findViewById(R.id.tv_history_award);
            mTvTime = (TextView) itemView.findViewById(R.id.tv_history_end_time);
        }
    }
}
