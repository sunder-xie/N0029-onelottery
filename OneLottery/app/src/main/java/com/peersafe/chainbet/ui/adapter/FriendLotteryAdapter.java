package com.peersafe.chainbet.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.utils.common.ImageUtils;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/1/9
 * DESCRIPTION : 好友列表点击后的好友活动列表界面Adapter
 */

public class FriendLotteryAdapter extends ListBaseAdapter<OneLottery>
{
    private LayoutInflater mLayoutInflater;
    private Context context;

    public FriendLotteryAdapter(Context context)
    {
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new FriendLotteryAdapter.ViewHolder(mLayoutInflater.
                inflate(R.layout.friend_lottery_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        final OneLottery lottery = mDataList.get(position);
        FriendLotteryAdapter.ViewHolder viewHolder = (FriendLotteryAdapter.ViewHolder) holder;

        Glide.with(context).load(ImageUtils.getLotteryImageview(lottery.getPictureIndex())).into(viewHolder.mImgIcon);
        viewHolder.mTvName.setText(lottery.getLotteryName());

        viewHolder.mPbProgress.setMax(lottery.getMaxBetCount());
        viewHolder.mPbProgress.setProgress(lottery.getCurBetCount());

        String totalBet = String.format(context.getString(R.string.friend_lottery_total_bet),lottery.getMaxBetCount());
        int index = totalBet.indexOf("" + lottery.getMaxBetCount());
        SpannableString countSpan = new SpannableString(totalBet);
        countSpan.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.app_primary_color)),index,
                index + String.valueOf(lottery.getMaxBetCount()).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewHolder.mTvCount.setText(countSpan);

        String bet = String.format(context.getString(R.string.friend_lottery_current_bet),lottery.getMaxBetCount() - lottery.getCurBetCount());
        int index1 = bet.indexOf("" + (lottery.getMaxBetCount() - lottery.getCurBetCount()));
        SpannableString betSpan = new SpannableString(bet);
        betSpan.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.app_primary_color)),
                index1,index1 + String.valueOf(lottery.getMaxBetCount() - lottery.getCurBetCount()).length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewHolder.mTvBet.setText(betSpan);
    }

    @Override
    public int getItemCount()
    {
        return mDataList.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView mImgIcon;
        TextView mTvName;
        ProgressBar mPbProgress;
        TextView mTvCount;
        TextView mTvBet;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mImgIcon = (ImageView) itemView.findViewById(R.id.img_friend_lottery_icon);
            mTvName = (TextView) itemView.findViewById(R.id.tv_friend_lottery_item_name);
            mPbProgress = (ProgressBar) itemView.findViewById(R.id.pb_friend_lottery_progress);
            mTvCount = (TextView) itemView.findViewById(R.id.tv_friend_lottery_item_count);
            mTvBet = (TextView) itemView.findViewById(R.id.tv_friend_lottery_item_bet);
        }
    }
}
