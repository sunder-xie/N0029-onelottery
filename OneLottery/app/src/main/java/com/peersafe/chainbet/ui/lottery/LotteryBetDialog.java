package com.peersafe.chainbet.ui.lottery;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.InputPwdDialog;
import com.peersafe.chainbet.widget.KeyBackCancelEditText;

import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.ui.lottery
 * @description:
 * @date 28/12/16 AM10:03
 */
public class LotteryBetDialog extends Dialog implements View.OnClickListener
{
    private Context mContext;

    //此次投注对应的活动
    private OneLottery mLottery;

    //投入注数输入框
    private KeyBackCancelEditText mBetNumEditText;

    //显示每注投注金额的文本框
    private TextView mTvPerBetCost;

    //投注应付总额
    private TextView mTvBetAmount;

    //账号余额
    private TextView mTvBalance;

    private double mBalance;
    private long mBetAmount;

    //在构造方法里预加载我们的样式，这样就不用每次创建都指定样式了
    public LotteryBetDialog(Context context, OneLottery lottery)
    {
        this(context, R.style.StyleLotteryBetDialog);
        mLottery = lottery;
        mContext = context;
    }

    public LotteryBetDialog(Context context, int themeResId)
    {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.lottery_bet_pop);

        // 预先设置Dialog的一些属性
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        Display display = dialogWindow.getWindowManager().getDefaultDisplay();
        p.gravity = Gravity.BOTTOM;
        p.width = display.getWidth();
        dialogWindow.setAttributes(p);

        initViews();
    }

    private void initViews()
    {
        mBetNumEditText = (KeyBackCancelEditText) findViewById(R.id.et_bet_num);
        mTvPerBetCost = (TextView) findViewById(R.id.tv_one_bet_cost);
        mTvBetAmount = (TextView) findViewById(R.id.tv_bet_total);
        mTvBalance = (TextView) findViewById(R.id.tv_bet_balance);

        findViewById(R.id.btn_num_five).setOnClickListener(this);
        findViewById(R.id.btn_num_twenty).setOnClickListener(this);
        findViewById(R.id.btn_num_fifty).setOnClickListener(this);
        findViewById(R.id.btn_num_all).setOnClickListener(this);
        findViewById(R.id.btn_bet_immidiately).setOnClickListener(this);

        long l = mLottery.getOneBetCost() / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE;
        String perBetCost = String.format(mContext.getString(R.string.lottery_bet_per_cost),
                l);
        SpannableString obcSpan = new SpannableString(perBetCost);
        obcSpan.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.app_primary_color)),
                0,String.valueOf(l).length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        mTvPerBetCost.setText(obcSpan);


        long betAmount = 0;
        setBetAmountTextView(betAmount);

        String userId = OneLotteryApi.getCurUserId();
        String balance = "0.00";
        if (!StringUtils.isEmpty(userId))
        {
            UserInfo userInfo = UserInfoDBHelper.getInstance().getUserByUserId(OneLotteryApi
                    .getCurUserId());
            double s = ((double) userInfo.getBalance() / ConstantCode.CommonConstant
                    .ONELOTTERY_MONEY_MULTIPLE);
            DecimalFormat df = new DecimalFormat("0.00");
            balance = df.format(s);
            mBalance = s;
        }

        String balanceStr = String.format(mContext.getString(R.string.lottery_bet_balance),
                balance);
        int index = balanceStr.indexOf(balance);
        SpannableStringBuilder style = new SpannableStringBuilder(balanceStr);
        style.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color
                .app_primary_color)),
                index, index + balance.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        mTvBalance.setText(style);

        mBetNumEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                String numStr = s.toString().trim();
                if (!StringUtils.isEmpty(numStr))
                {
                    long num = Long.parseLong(numStr);
                    long perBetCost = mLottery.getOneBetCost() / ConstantCode.CommonConstant
                            .ONELOTTERY_MONEY_MULTIPLE;
                    setBetAmountTextView(num * perBetCost);
                } else
                {
                    setBetAmountTextView(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

        mBetNumEditText.setOnCancelDialogImp(new KeyBackCancelEditText.OnCancelDialogImp()
        {
            @Override
            public void onCancelDialog()
            {
                dismiss();
            }
        });
        mBetNumEditText.setText("1");
        mBetNumEditText.setSelection(1);
    }

    private void setBetAmountTextView(long betAmount)
    {
        String betAmountStr = String.format(mContext.getString(R.string.lottery_bet_total),
                betAmount);
        int indexAmount = betAmountStr.indexOf("" + betAmount);
        SpannableStringBuilder style = new SpannableStringBuilder(betAmountStr);
        style.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color
                .app_primary_color)),
                indexAmount, indexAmount + Long.toString(betAmount).length(), Spannable
                        .SPAN_EXCLUSIVE_INCLUSIVE);
        mTvBetAmount.setText(style);

        mBetAmount = betAmount;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_num_five:
                onClickBetNumBtn(5);
                break;
            case R.id.btn_num_twenty:
                onClickBetNumBtn(20);
                break;
            case R.id.btn_num_fifty:
                onClickBetNumBtn(50);
                break;
            case R.id.btn_num_all:
                onClickBetNumBtn(mLottery.getMaxBetCount() - mLottery.getCurBetCount());
                break;
            case R.id.btn_bet_immidiately:
                if(!NetworkUtil.isNetworkConnected())
                {
                    Toast.makeText(mContext,mContext.getString(R.string.check_network),Toast.LENGTH_SHORT).show();
                    break;
                }

                if(!OneLotteryManager.isServiceConnect)
                {
                    Toast.makeText(mContext,mContext.getString(R.string.check_service),Toast.LENGTH_SHORT).show();
                    break;
                }

                if (OneLotteryApi.isVisitor(mContext))
                {
                    dismiss();
                } else
                {
                    onClickBetImmediatelyBtn();
                }
                break;
            default:
                break;
        }
    }

    private void onClickBetNumBtn(long betCount)
    {
        long betAmount = 0;
        long perBetCost = mLottery.getOneBetCost() / ConstantCode.CommonConstant
                .ONELOTTERY_MONEY_MULTIPLE;
        long remainNum = mLottery.getMaxBetCount() - mLottery.getCurBetCount();
        if (remainNum >= betCount)
        {
            mBetNumEditText.setText("" + betCount);
            betAmount = betCount * perBetCost;
        } else
        {
            mBetNumEditText.setText("" + remainNum);
            betAmount = remainNum * perBetCost;
        }

        mBetNumEditText.setSelection(mBetNumEditText.getText().length());
        setBetAmountTextView(betAmount);
    }

    private void onClickBetImmediatelyBtn()
    {
        BasicActivity activity = (BasicActivity) mContext;

        if (mLottery.getStartTime().getTime() > System.currentTimeMillis()
                || mLottery.getState() < ConstantCode.OneLotteryState.ONELOTTERY_STATE_ON_GOING
//                || mLottery.getState() > ConstantCode.OneLotteryState.ONELOTTERY_STATE_CAN_REWARD
                )
        {
            activity.showToast(activity.getString(R.string.lottery_bet_not_start));
            return;
        }

        String betNumStr = mBetNumEditText.getText().toString().trim();
        if (StringUtils.isEmpty(betNumStr) || Integer.parseInt(betNumStr) <= 0)
        {
            activity.showToast(activity.getString(R.string.lottery_bet_num_empty));
            return;
        }

        long betNum = Long.parseLong(betNumStr);
        long remainNum = mLottery.getMaxBetCount() - mLottery.getCurBetCount();
        if (betNum > remainNum)
        {
            activity.showToast(activity.getString(R.string.lottery_bet_num_exceed));
            return;
        }

        if (mBetAmount > mBalance)
        {
            activity.showToast(activity.getString(R.string.lottery_bet_balance_not_enough));
            return;
        }

        // 投注的注数和消费金额,时间
        OneLottery ol = (OneLottery) mLottery.clone();
        if (ol != null)
        {
            ol.setCurBetCount(Integer.parseInt(betNumStr));
            ol.setCurBetAmount(mBetAmount);
            ol.setUpdateTime(Calendar.getInstance().getTime());
        }

        dismiss();
        InputPwdDialog inputPwdDialog = new InputPwdDialog(mContext, InputPwdDialog
                .OPER_TYPE_BET, ol);
        inputPwdDialog.show();
    }
}
