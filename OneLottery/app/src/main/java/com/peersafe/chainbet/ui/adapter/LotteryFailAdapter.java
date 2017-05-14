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
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.ImageUtils;

/**
 * Created by sunhaitao on 17/1/3.
 */

public class LotteryFailAdapter extends ListBaseAdapter<OneLottery>
{
    private LayoutInflater mLayoutInflater;
    private Context context;

    public LotteryFailAdapter(Context context)
    {
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new LotteryFailAdapter.ViewHolder(mLayoutInflater.
                inflate(R.layout.recent_fail_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        OneLottery lottery = mDataList.get(position);
        LotteryFailAdapter.ViewHolder viewHolder = (LotteryFailAdapter.ViewHolder) holder;

        if (lottery.getPublisherHash().equals(ConstantCode.CommonConstant.ONELOTTERY_DEFAULT_OFFICAL_HASH))
        {
            viewHolder.mTvLabel.setText(context.getString(R.string.lottery_offical_flag));
        } else
        {
            viewHolder.mTvLabel.setText(context.getString(R.string.lottery_personal_flag));
        }

        Glide.with(context).load(ImageUtils.getLotteryImageview(lottery.getPictureIndex())).into(viewHolder.mImgHead);

        viewHolder.mTvTime.setText(DateFormat.format(ConstantCode.CommonConstant.SIMPLE_DATE_FORMAT, lottery.getCloseTime()));

        viewHolder.mTvDescrbe.setText(lottery.getLotteryName());

        String creater = String.format(context.getString(R.string.recent_lottery_creater_holder), lottery.getPublisherName2());
        SpannableString spannableString = new SpannableString(creater);
        spannableString.setSpan(new ForegroundColorSpan(context.getResources().
                getColor(R.color.app_primary_color)), creater.length() - lottery.getPublisherName2().length(), creater.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewHolder.mTvCreate.setText(spannableString);
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
        TextView mTvCreate;
        TextView mTvTime;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mTvLabel = (TextView) itemView.findViewById(R.id.tv_fail_label_text);
            mImgHead = (ImageView) itemView.findViewById(R.id.img_fail_top_img);
            mTvDescrbe = (TextView) itemView.findViewById(R.id.tv_fail_lottery_describe);
            mTvCreate = (TextView) itemView.findViewById(R.id.tv_fail_lottery_holder);
            mTvTime = (TextView) itemView.findViewById(R.id.tv_fail_time);
        }
    }
}
