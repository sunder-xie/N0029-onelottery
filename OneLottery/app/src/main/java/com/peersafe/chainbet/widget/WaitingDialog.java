package com.peersafe.chainbet.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.peersafe.chainbet.R;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.model.OLMessageModel;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.widget
 * @description:
 * @date 28/12/16 PM2:40
 */
public class WaitingDialog extends Dialog implements View.OnClickListener
{
    //当前等待对话框的类型
    private int mDialogType;
    public final static int WAITING_DIALOG_BET = 0;   //正在投注中

    public static final int WAITING_DIALOG_CREATE = 1; //正在创建活动中

    public static final int WAITING_DIALOG_REWARD = 2; //正在开奖中

    public static final int WAITING_DIALOG_MODIFY = 3; //修改中

    public static final int WAITING_DIALOG_DELETE = 4; //删除中

    public static final int WAITING_DIALOG_TRANSFER = 5; //转账

    public static final int WAITING_DIALOG_RECHANGE = 6; //充值中

    //等待框的头
    ImageView mIvWaitingHeader;

    //等待框的内容
    TextView mTvWaitingContent;

    //等待旋转框
    ProgressBar mPbWaitingAnimtion;

    //等待开奖提示语
    TextView mTvRewardText;

    //查看其它
    Button mSeeOther;

    //当前输入密码对应操作的相应对象
    public static Object mCurOperObject;

    private String mTxId;

    //在构造方法里预加载我们的样式，这样就不用每次创建都指定样式了
    public WaitingDialog(Context context, int dialogType, Object operObject)
    {
        this(context, R.style.MyDialogStyle, dialogType);
        mCurOperObject = operObject;
    }

    public WaitingDialog(Context context, int themeResId, int dialogType)
    {
        super(context, themeResId);
        mDialogType = dialogType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_waitting);


        // 预先设置Dialog的一些属性
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        p.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(p);

        setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失

        initViews();
    }

    private void initViews()
    {
        mIvWaitingHeader = (ImageView) findViewById(R.id.iv_wait_head);
        mTvWaitingContent = (TextView) findViewById(R.id.tv_waitting_content);
        mPbWaitingAnimtion = (ProgressBar) findViewById(R.id.pb_waiting);
        mTvRewardText = (TextView) findViewById(R.id.tv_reward_text);
        mSeeOther = (Button) findViewById(R.id.btn_see_other);

        mSeeOther.setOnClickListener(this);

        switch (mDialogType)
        {
            case WAITING_DIALOG_BET:
                mTvWaitingContent.setText(getContext().getString(R.string.lottery_bet_waiting));
                mIvWaitingHeader.setImageResource(R.drawable.waiting_bet_head);
                break;

            case WAITING_DIALOG_CREATE:
                mTvWaitingContent.setText(getContext().getString(R.string.create_lottery_waiting));
                mIvWaitingHeader.setImageResource(R.drawable.waiting_create_head);
                break;

            case WAITING_DIALOG_MODIFY:
                mTvWaitingContent.setText(getContext().getString(R.string.create_lottery_modify_waiting));
                mIvWaitingHeader.setImageResource(R.drawable.waiting_create_head);
                break;

            case WAITING_DIALOG_DELETE:
                mTvWaitingContent.setText(getContext().getString(R.string.create_lottery_delete_waiting));
                mIvWaitingHeader.setImageResource(R.drawable.waiting_create_head);
                break;

            case WAITING_DIALOG_TRANSFER:
                mTvWaitingContent.setText(getContext().getString(R.string
                        .transfer_transfering));
                mIvWaitingHeader.setImageResource(R.drawable.waiting_create_head);
                break;

            case WAITING_DIALOG_RECHANGE:
                String value = (String) mCurOperObject;
                SpannableString span = new SpannableString(value + getContext().getString(R
                        .string.setting_rechange_waiting));
                span.setSpan(new ForegroundColorSpan(getContext().getResources().
                        getColor(R.color.rechange_text_color)), 0, span.length() - getContext().
                        getString(R.string.setting_rechange_waiting).length(), Spanned
                        .SPAN_EXCLUSIVE_EXCLUSIVE);
                mTvWaitingContent.setText(span);
                mIvWaitingHeader.setImageResource(R.drawable.waiting_bet_head);
                mSeeOther.setText(getContext().getString(R.string.ok));
                mPbWaitingAnimtion.setVisibility(View.GONE);
                break;

            case WAITING_DIALOG_REWARD:
                mTvWaitingContent.setText(getContext().getString(R.string.recent_reward_lottery_waiting));
                mIvWaitingHeader.setImageResource(R.drawable.reward);
                mPbWaitingAnimtion.setVisibility(View.GONE);
                mTvRewardText.setVisibility(View.VISIBLE);
                mTvRewardText.setText(getContext().getString(R.string.recent_reward_waiting_text));
                mSeeOther.setText(getContext().getString(R.string.confirm));
                break;

            default:
                break;
        }
    }

    public void setBtnText(String content)
    {
        mSeeOther.setText(getContext().getString(R.string.confirm));
        mPbWaitingAnimtion.setVisibility(View.GONE);
        mTvWaitingContent.setText(content);
    }

    public void setTxId(String txId)
    {
        mTxId = txId;
    }

    public String getTxId()
    {
        return mTxId;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_see_other:
                if (mDialogType == WAITING_DIALOG_CREATE)
                {
                    dismiss();
                    OneLotteryManager.getInstance().SendEventBus(null, OLMessageModel
                            .STMSG_MODEL_CREATE_LOTTERY_FINISH);
                } else if (mDialogType == WAITING_DIALOG_BET)
                {
                    dismiss();
                } else if (mDialogType == WAITING_DIALOG_REWARD)
                {
                    dismiss();
                } else if (mDialogType == WAITING_DIALOG_MODIFY)
                {
                    dismiss();
                    OneLotteryManager.getInstance().SendEventBus(null, OLMessageModel
                            .STMSG_MODEL_CREATE_LOTTERY_FINISH);
                } else if (mDialogType == WAITING_DIALOG_DELETE)
                {
                    dismiss();
                    OneLotteryManager.getInstance().SendEventBus(null, OLMessageModel
                            .STMSG_MODEL_CREATE_LOTTERY_FINISH);
                } else if (mDialogType == WAITING_DIALOG_RECHANGE)
                {
                    dismiss();
                } else if (mDialogType == WAITING_DIALOG_TRANSFER)
                {
                    dismiss();
                    OneLotteryManager.getInstance().SendEventBus(null, OLMessageModel
                            .STMSG_MODEL_TRANSFER_FINISH);
                }
                break;
        }
    }
}
