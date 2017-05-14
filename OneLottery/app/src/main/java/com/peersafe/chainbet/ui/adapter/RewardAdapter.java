package com.peersafe.chainbet.ui.adapter;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.peersafe.chainbet.MainActivity;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.ImageUtils;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.log.OLLogger;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.WaitingDialog;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sunhaitao on 17/1/3.
 */

public class RewardAdapter extends ListBaseAdapter<OneLottery>
{
    private LayoutInflater mLayoutInflater;
    private MainActivity context;

    private Map<String, CountDownTimer> timerMap = new HashMap<>();

    public RewardAdapter(Context context)
    {
        this.context = (MainActivity) context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new RewardAdapter.ViewHolder(mLayoutInflater.
                inflate(R.layout.recent_award_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        final OneLottery lottery = mDataList.get(position);
        final RewardAdapter.ViewHolder viewHolder = (RewardAdapter.ViewHolder) holder;

        if (lottery.getPublisherHash().equals(ConstantCode.CommonConstant
                .ONELOTTERY_DEFAULT_OFFICAL_HASH))
        {
            viewHolder.mTvLabel.setText(context.getString(R.string.lottery_offical_flag));
        } else
        {
            viewHolder.mTvLabel.setText(context.getString(R.string.lottery_personal_flag));
        }

        if (lottery.getState() == ConstantCode.OneLotteryState.ONELOTTERY_STATE_REWARD_ING)
        {
            viewHolder.mBtnReward.setVisibility(View.VISIBLE);
            viewHolder.mBtnReward.setText(context.getString(R.string.recent_reward_ing));
            viewHolder.mBtnReward.setBackgroundResource(R.drawable.selector_bet_gray_btn);
            viewHolder.mBtnReward.setEnabled(false);
        } else
        {
            Date countDownTime = lottery.getRewardCountDownTime();
            if (null == countDownTime)
            {
                lottery.setRewardCountDownTime(new Date(System.currentTimeMillis() + 60 * 1000));
                viewHolder.mBtnCounter.setVisibility(View.VISIBLE);
                viewHolder.mBtnReward.setVisibility(View.GONE);
                viewHolder.mBtnCounter.setEnabled(false);
                setCounterDown(60 * 1000, viewHolder.mBtnCounter, viewHolder.mBtnReward, position);
            } else if (countDownTime.getTime() > System.currentTimeMillis())
            {

                viewHolder.mBtnReward.setVisibility(View.GONE);
                viewHolder.mBtnCounter.setVisibility(View.VISIBLE);
                viewHolder.mBtnCounter.setEnabled(false);
                setCounterDown(countDownTime.getTime() - System.currentTimeMillis(),
                        viewHolder.mBtnCounter, viewHolder.mBtnReward, position);
            } else
            {
                viewHolder.mBtnCounter.setVisibility(View.GONE);
                viewHolder.mBtnReward.setVisibility(View.VISIBLE);
                viewHolder.mBtnReward.setText(context.getString(R.string.recent_time_to_reward));
                viewHolder.mBtnReward.setBackgroundResource(R.drawable.selector_register_btn);
                viewHolder.mBtnReward.setEnabled(true);
            }

            viewHolder.mBtnReward.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                    if (!NetworkUtil.isNetworkConnected())
                    {
                        Toast.makeText(context, context.getString(R.string.check_network),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(!OneLotteryManager.isServiceConnect)
                    {
                        Toast.makeText(context,context.getString(R.string.check_service),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!OneLotteryApi.isVisitor(context))
                    {
                        viewHolder.mBtnReward.setText(context.getString(R.string
                                .recent_reward_ing));
                        viewHolder.mBtnReward.setBackgroundResource(R.drawable
                                .selector_bet_gray_btn);

                        lottery.setState(ConstantCode.OneLotteryState.ONELOTTERY_STATE_REWARD_ING);
                        OneLotteryDBHelper.getInstance().insertOneLottery(lottery);
                        // TODO 如果是直接删除活动的话，那么就不需要显示waitingDialog了吧
                        context.showWaitingDialog(context, WaitingDialog.WAITING_DIALOG_REWARD,
                                null);

                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                OneLotteryManager.getInstance().openReward(lottery.getLotteryId());
                            }
                        }).start();
                    }
                }
            });
        }

        Glide.with(context).load(ImageUtils.getLotteryImageview(lottery.getPictureIndex())).into
                (viewHolder.mImgHead);
        viewHolder.mTvDescrbe.setText(lottery.getLotteryName());

        String creater = context.getString(R.string.recent_lottery_creater_holders) + " " +
                lottery.getPublisherName2();
        SpannableString spannableString = new SpannableString(creater);
        spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color
                .app_primary_color)), creater.length() - lottery.getPublisherName2().length(), creater.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        Button mBtnReward;
        Button mBtnCounter;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mTvLabel = (TextView) itemView.findViewById(R.id.tv_reward_label_text);
            mImgHead = (ImageView) itemView.findViewById(R.id.img_reward_top_img);
            mTvDescrbe = (TextView) itemView.findViewById(R.id.tv_reward_lottery_describe);
            mTvCreate = (TextView) itemView.findViewById(R.id.tv_reward_lottery_holder);
            mBtnReward = (Button) itemView.findViewById(R.id.btn_reward);
            mBtnCounter = (Button) itemView.findViewById(R.id.btn_counter);
        }
    }

    public void setCounterDown(final long time, final Button counter, final Button reward, final
    int pos)
    {
        final OneLottery lottery = mDataList.get(pos);
        if (lottery != null && !StringUtils.isEmpty(lottery.getLotteryId()))
        {
            resetTimer(lottery.getLotteryId());

            CountDownTimer timer = new CountDownTimer(time, 1000)
            {
                @Override
                public void onTick(long l)
                {
                    counter.setText(String.format(context.getString(R.string.second), l / 1000));
                }

                @Override
                public void onFinish()
                {
                    OLLogger.d("RewardAdapter", "onfinish position is == " + pos);
                    counter.setVisibility(View.GONE);
                    reward.setVisibility(View.VISIBLE);
                    reward.setText(context.getString(R.string.recent_time_to_reward));
                    reward.setBackgroundResource(R.drawable.selector_register_btn);
                    reward.setEnabled(true);
                    notifyItemChanged(pos);
                    resetTimer(lottery.getLotteryId());
                }
            }.start();
            timerMap.put(lottery.getLotteryId(), timer);
        }
    }

    private void resetTimer(String lotteryId)
    {
        if (null != timerMap.get(lotteryId))
        {
            if (timerMap.get(lotteryId) != null)
            {
                timerMap.get(lotteryId).cancel();
                OLLogger.d("RewardAdapter", "resetTimer");
            }
            timerMap.remove(lotteryId);
        }
    }
}
