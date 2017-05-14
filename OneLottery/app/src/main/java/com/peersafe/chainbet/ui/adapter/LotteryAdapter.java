package com.peersafe.chainbet.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.peersafe.chainbet.MainActivity;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.OneLotteryBet;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.FriendDBHelper;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryBetDBHelper;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryDeleteRet;
import com.peersafe.chainbet.ui.lottery.CreateLotteryActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.ImageUtils;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.log.OLLogger;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.WaitingDialog;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * AUTHOR : sunhaitao.
 * DATA : 16/12/19
 * DESCRIPTION :
 */

public class LotteryAdapter extends ListBaseAdapter<OneLottery>
{
    private LayoutInflater mLayoutInflater;
    private MainActivity context;
    private WeakReference mHandler;

    private Map<String, CountDownTimer> timerMap = new HashMap<>();
    private Map<String, Long> timeMap = new HashMap<>();

    public LotteryAdapter(Context context, Handler handler)
    {
        this.context = (MainActivity) context;
        mLayoutInflater = LayoutInflater.from(context);
        this.mHandler = new WeakReference(handler);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new ViewHolder(mLayoutInflater.inflate(R.layout.lottery_item_card, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        final OneLottery lottery = mDataList.get(position);
        final ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.mProgress.setProgressDrawable(context.getResources().getDrawable(R.drawable
                .shape_progressbar_mini));
        if (lottery.getPublisherHash().equals(ConstantCode.CommonConstant
                .ONELOTTERY_DEFAULT_OFFICAL_HASH))
        {
            viewHolder.mFlag.setText(context.getString(R.string.lottery_offical_flag));
            viewHolder.mCreater.setVisibility(View.GONE);

            viewHolder.mLlMy.setVisibility(View.GONE);
            viewHolder.mOther.setVisibility(View.VISIBLE);
            viewHolder.mEndTime.setVisibility(View.GONE);
            viewHolder.mBtnReward.setVisibility(View.GONE);
            viewHolder.mBtnCounter.setVisibility(View.GONE);
            viewHolder.mBetLayout.setVisibility(View.VISIBLE);
        } else if (FriendDBHelper.getInstance().isMyFriend(lottery.getPublisherHash()))
        {
            viewHolder.mFlag.setText(context.getString(R.string.lottery_personal_flag));
            viewHolder.mCreater.setVisibility(View.VISIBLE);

            String creater = String.format(context.getString(R.string
                    .recent_lottery_creater_holder), lottery.getPublisherName2());
            SpannableString createrSpan = new SpannableString(creater);
            createrSpan.setSpan(new ForegroundColorSpan(context
                    .getResources().getColor(R.color.lottery_prize_text_color)), creater.length() - lottery.getPublisherName2().length(), creater
                    .length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            viewHolder.mCreater.setText(createrSpan);

            viewHolder.mLlMy.setVisibility(View.GONE);
            viewHolder.mOther.setVisibility(View.VISIBLE);
            viewHolder.mEndTime.setVisibility(View.GONE);
            viewHolder.mBtnReward.setVisibility(View.GONE);
            viewHolder.mBtnCounter.setVisibility(View.GONE);
            viewHolder.mBetLayout.setVisibility(View.VISIBLE);
        } else
        {
            viewHolder.mFlag.setText(context.getString(R.string.lottery_personal_flag));

            if (lottery.getPublisherHash().equals(UserInfoDBHelper.getInstance()
                    .getCurPrimaryAccount().getWalletAddr()))
            {
                viewHolder.mCreater.setVisibility(View.GONE);

                //区分我的活动不同种状态
                switch (lottery.getState())
                {
                    case ConstantCode.OneLotteryState.ONELOTTERY_STATE_NOT_STARTED:
                    case ConstantCode.OneLotteryState.ONELOTTERY_STATE_ON_GOING:
                        viewHolder.mLlMy.setVisibility(View.GONE);
                        viewHolder.mOther.setVisibility(View.VISIBLE);
                        viewHolder.mEndTime.setVisibility(View.GONE);
                        viewHolder.mBtnReward.setVisibility(View.GONE);
                        viewHolder.mBtnCounter.setVisibility(View.GONE);
                        viewHolder.mBetLayout.setVisibility(View.VISIBLE);
                        break;

                    case ConstantCode.OneLotteryState.ONELOTTERY_STATE_CAN_REWARD:
                        viewHolder.mLlMy.setVisibility(View.GONE);
                        viewHolder.mOther.setVisibility(View.VISIBLE);
                        viewHolder.mBtnReward.setVisibility(View.GONE);
                        viewHolder.mEndTime.setVisibility(View.GONE);
                        viewHolder.mBetLayout.setVisibility(View.GONE);

                        Date countDownTime = lottery.getRewardCountDownTime();
                        if (countDownTime == null)
                        {
                            lottery.setRewardCountDownTime(new Date(System.currentTimeMillis() +
                                    60 * 1000));
                            viewHolder.mBtnCounter.setVisibility(View.VISIBLE);
                            viewHolder.mBtnCounter.setEnabled(false);
                            setCounterDown(60 * 1000, viewHolder.mBtnCounter, viewHolder
                                    .mBtnReward, position);
                        } else if (countDownTime.getTime() > System.currentTimeMillis())
                        {
                            viewHolder.mBtnCounter.setVisibility(View.VISIBLE);
                            viewHolder.mBtnCounter.setEnabled(false);
                            setCounterDown(countDownTime.getTime() - System.currentTimeMillis(),
                                    viewHolder.mBtnCounter, viewHolder.mBtnReward, position);
                        } else
                        {
                            viewHolder.mBtnCounter.setVisibility(View.GONE);
                            viewHolder.mBtnReward.setVisibility(View.VISIBLE);
                            viewHolder.mBtnReward.setText(context.getString(R.string
                                    .recent_time_to_reward));
                            viewHolder.mBtnReward.setBackgroundResource(R.drawable
                                    .selector_register_btn);
                            viewHolder.mBtnReward.setEnabled(true);
                        }
                        break;

                    case ConstantCode.OneLotteryState.ONELOTTERY_STATE_REWARD_ING:
                        viewHolder.mLlMy.setVisibility(View.GONE);
                        viewHolder.mOther.setVisibility(View.VISIBLE);
                        viewHolder.mBtnReward.setVisibility(View.VISIBLE);
                        viewHolder.mBtnCounter.setVisibility(View.GONE);
                        viewHolder.mEndTime.setVisibility(View.GONE);
                        viewHolder.mBetLayout.setVisibility(View.GONE);

                        viewHolder.mBtnReward.setText(context.getString(R.string
                                .recent_reward_ing));
                        viewHolder.mBtnReward.setBackgroundResource(R.drawable
                                .selector_bet_gray_btn);
                        viewHolder.mBtnReward.setEnabled(false);
                        break;

                    case ConstantCode.OneLotteryState.ONELOTTERY_STATE_REWARD_ALREADY:
                        viewHolder.mLlMy.setVisibility(View.VISIBLE);
                        viewHolder.mOther.setVisibility(View.GONE);

                        String prizeTxID = lottery.getPrizeTxID();
                        OneLotteryBet lotteryBet = OneLotteryBetDBHelper.getInstance()
                                .getLotteryBetByTxID(prizeTxID);
                        if (null != lotteryBet)
                        {
                            String reward = String.format(context.getString(R
                                    .string.lottery_detail_award), lotteryBet.getAttendeeName());
                            SpannableString rewardSpan = new SpannableString(reward);
                            rewardSpan.setSpan(new ForegroundColorSpan(context.getResources()
                                    .getColor(R.color.lottery_prize_text_color)), reward.length() - lotteryBet.getAttendeeName().length(), reward
                                    .length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                            viewHolder.mAward.setText(rewardSpan);
                        }
                        viewHolder.mTime.setText(String.format(context.getString(R.string
                                .lottery_detail_end_time), DateFormat.format(ConstantCode
                                        .CommonConstant.SIMPLE_DATE_FORMAT1,
                                lottery.getLastCloseTime())));
                        break;

                    case ConstantCode.OneLotteryState.ONELOTTERY_STATE_REFUND_ING:
                    case ConstantCode.OneLotteryState.ONELOTTERY_STATE_CAN_REFUND:
                    case ConstantCode.OneLotteryState.ONELOTTERY_STATE_REFUND_ALREADY:
                    case ConstantCode.OneLotteryState.ONELOTTERY_STATE_FAIL:
                        viewHolder.mLlMy.setVisibility(View.GONE);
                        viewHolder.mOther.setVisibility(View.VISIBLE);
                        viewHolder.mBtnReward.setVisibility(View.GONE);
                        viewHolder.mBtnCounter.setVisibility(View.GONE);
                        viewHolder.mEndTime.setVisibility(View.VISIBLE);
                        viewHolder.mBetLayout.setVisibility(View.GONE);
                        viewHolder.mProgress.setProgressDrawable(context.getResources()
                                .getDrawable(R.drawable
                                        .shape_progressbar_gray_mini));

                        viewHolder.mEndTime.setText(String.format(context.getString(R.string
                                        .lottery_detail_fail_time),
                                DateFormat.format(ConstantCode.CommonConstant.SIMPLE_DATE_FORMAT1,
                                        lottery.getCloseTime())));
                        break;
                }
            } else
            {
                viewHolder.mLlMy.setVisibility(View.GONE);
                viewHolder.mOther.setVisibility(View.VISIBLE);
            }
        }

        //头部图片和活动描述

        Glide.with(context).load(ImageUtils.getLotteryImageview(lottery.getPictureIndex())).into
                (viewHolder.mImage);
        viewHolder.mDescrip.setText(lottery.getLotteryName());

        //活动百分比图片和文字的显示
        viewHolder.mTvProgress.setText(StringUtils.getStringPercent(lottery.getProgress() / 100f));
        viewHolder.mProgress.setProgress(lottery.getProgress());

        viewHolder.mCost.setText(String.valueOf(lottery.getOneBetCost() / ConstantCode
                .CommonConstant.ONELOTTERY_MONEY_MULTIPLE));

        //点击开奖按钮
        RewardBtnOnclicker rewardBtnOnclicker = new RewardBtnOnclicker();
        rewardBtnOnclicker.mReward = viewHolder.mBtnReward;
        rewardBtnOnclicker.mLottery = lottery;
        viewHolder.mBtnReward.setOnClickListener(rewardBtnOnclicker);

        //不同状态下的投注按钮
        if (lottery.getState() == ConstantCode.OneLotteryState.ONELOTTERY_STATE_ON_GOING)
        {
            viewHolder.mBet.setText(context.getString(R.string.bet));
            viewHolder.mBet.setBackgroundResource(R.drawable.selector_register_btn);
            viewHolder.mBet.setEnabled(true);
        } else
        {
            if (lottery.getState() == ConstantCode.OneLotteryState.ONELOTTERY_STATE_NOT_STARTED
                    && UserInfoDBHelper.getInstance().getCurPrimaryAccount() != null && lottery
                    .getPublisherHash().equals(UserInfoDBHelper.getInstance()
                            .getCurPrimaryAccount().getWalletAddr()))
            {
                viewHolder.mBet.setText(context.getString(R.string.lottery_detail_revise));
                viewHolder.mBet.setBackgroundResource(R.drawable.selector_register_btn);
                viewHolder.mBet.setEnabled(true);
            } else
            {
                viewHolder.mBet.setText(context.getString(R.string.bet));
                viewHolder.mBet.setBackgroundResource(R.drawable.selector_bet_gray_btn);
                viewHolder.mBet.setEnabled(false);
            }
        }

        //设置投注按钮
        viewHolder.mBet.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if (!NetworkUtil.isNetworkConnected())
                {
                    context.showToast(context.getString(R.string.check_network));
                    return;
                }

                if(!OneLotteryManager.isServiceConnect)
                {
                    context.showToast(context.getString(R.string.check_service));
                    return;
                }

                if (lottery.getState() == ConstantCode.OneLotteryState.ONELOTTERY_STATE_NOT_STARTED
                        && UserInfoDBHelper.getInstance().getCurPrimaryAccount() != null && lottery
                        .getPublisherHash().equals(UserInfoDBHelper.getInstance()
                                .getCurPrimaryAccount().getWalletAddr()))
                {
                    Intent intent = new Intent(context, CreateLotteryActivity.class);
                    intent.putExtra(ConstantCode.CommonConstant.TYPE, CreateLotteryActivity
                            .MODIFY_LOTTERY_TYPE);
                    intent.putExtra(ConstantCode.CommonConstant.LOTTERYID, lottery
                            .getLotteryId());
                    context.startActivity(intent);
                } else
                {
                    if (!OneLotteryApi.isVisitor(context))
                    {
                        context.showBetDialog(context, lottery);
                        if (mHandler.get() != null)
                        {
                            Message msg = ((Handler) mHandler.get()).obtainMessage();
                            msg.obj = lottery;
                            ((Handler) mHandler.get()).sendMessage(msg);
                        }
                    }
                }
            }
        });
    }

    private class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView mFlag;
        private ImageView mImage;
        private TextView mDescrip;
        private TextView mCreater;
        private ProgressBar mProgress;
        private TextView mTvProgress;
        private TextView mCost;
        private Button mBet;
        private LinearLayout mOther;

        private Button mBtnCounter;

        private TextView mAward;
        private TextView mTime;
        private Button mBtnReward;
        private LinearLayout mLlMy;
        private TextView mEndTime;
        private RelativeLayout mBetLayout;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mImage = (ImageView) itemView.findViewById(R.id.lottery_item_top_imagview);
            mDescrip = (TextView) itemView.findViewById(R.id.tv_describtion);
            mCreater = (TextView) itemView.findViewById(R.id.tv_creater);
            mProgress = (ProgressBar) itemView.findViewById(R.id
                    .pb_progress);
            mTvProgress = (TextView) itemView.findViewById(R.id.tv_percent);
            mCost = (TextView) itemView.findViewById(R.id.tv_per_bet);
            mFlag = (TextView) itemView.findViewById(R.id.lottery_item_flag);
            mBet = (Button) itemView.findViewById(R.id.lottery_item_bet);
            mOther = (LinearLayout) itemView.findViewById(R.id.ll_other_lottery);

            mAward = (TextView) itemView.findViewById(R.id.tv_my_lottery_award);
            mTime = (TextView) itemView.findViewById(R.id.tv_my_end_time);
            mBtnReward = (Button) itemView.findViewById(R.id.btn_my_reward);
            mLlMy = (LinearLayout) itemView.findViewById(R.id.ll_my_lottery);
            mEndTime = (TextView) itemView.findViewById(R.id.tv_end_time);
            mBetLayout = (RelativeLayout) itemView.findViewById(R.id.rl_bet_layout);
            mBtnCounter = (Button) itemView.findViewById(R.id.btn_my_counter);
        }
    }

    public class RewardBtnOnclicker implements View.OnClickListener
    {
        Button mReward;
        OneLottery mLottery;

        @Override
        public void onClick(View view)
        {
            mReward.setText(context.getString(R.string.recent_reward_ing));
            mReward.setBackgroundResource(R.drawable.selector_bet_gray_btn);

            mLottery.setState(ConstantCode.OneLotteryState.ONELOTTERY_STATE_REWARD_ING);
            OneLotteryDBHelper.getInstance().insertOneLottery(mLottery);

            context.showWaitingDialog(context, WaitingDialog.WAITING_DIALOG_REWARD, null);

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    OneLotteryManager.getInstance().openReward(mLottery.getLotteryId());
                }
            }).start();
        }
    }

    public void setCounterDown(final long time, final Button counter, final Button reward, final
    int pos)
    {
        final OneLottery lottery = mDataList.get(pos);
        if (lottery != null && !StringUtils.isEmpty(lottery.getLotteryId()))
        {
            resetTimer(lottery.getLotteryId());
            final CountDownTimer timer = new CountDownTimer(time, 1000)
            {
                @Override
                public void onTick(long l)
                {
                    counter.setText(String.format(context.getString(R.string.second), l / 1000));
                }

                @Override
                public void onFinish()
                {
                    counter.setVisibility(View.GONE);
                    reward.setVisibility(View.VISIBLE);
                    reward.setText(context.getString(R.string.recent_time_to_reward));
                    reward.setBackgroundResource(R.drawable.selector_register_btn);
                    reward.setEnabled(true);
                    resetTimer(lottery.getLotteryId());
                    notifyItemChanged(pos);
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
            }
            timerMap.remove(lotteryId);
        }
    }
}
