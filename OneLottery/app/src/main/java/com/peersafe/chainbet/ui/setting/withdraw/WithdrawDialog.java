package com.peersafe.chainbet.ui.setting.withdraw;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
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
 * AUTHOR : sunhaitao.
 * DATA : 17/4/7
 * DESCRIPTION :
 */

public class WithdrawDialog extends Dialog implements View.OnClickListener
{
    public static final int FAIL = 1;

    public static final int SUCCESS = 2;

    public static final int PROCESS = 3;

    TextView mTitle;

    ProgressBar mPbBar;

    ImageView mImg;

    Button mConfrim;

    int mCurrentType;

    public WithdrawDialog(Context context)
    {
        this(context, R.style.MyDialogStyle);
    }

    public WithdrawDialog(Context context, int themeResId)
    {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.with_draw_dialog);

        // 预先设置Dialog的一些属性
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        p.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(p);

        setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失

        mTitle = (TextView) findViewById(R.id.tv_title);
        mPbBar = (ProgressBar) findViewById(R.id.pb_waiting);
        mImg = (ImageView) findViewById(R.id.img_success_or_fail);
        mConfrim = (Button) findViewById(R.id.btn_confirm);

        mImg.setVisibility(View.GONE);
        mPbBar.setVisibility(View.VISIBLE);
        mTitle.setText(getContext().getString(R.string.withdraw_progress));
        mConfrim.setText(getContext().getString(R.string.common_see_other));
        mCurrentType = PROCESS;

        mConfrim.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_confirm:
                dismiss();
                OneLotteryManager.getInstance().SendEventBus(mCurrentType, OLMessageModel.STMSG_MODEL_DISMISS_WITHDRAW_DIALOG);
                break;
        }
    }

    public void setWithdrawStatus(int type)
    {
        switch (type)
        {
            case 1:
                mTitle.setText(getContext().getString(R.string.withdraw_fail));
                mCurrentType = FAIL;
                mImg.setImageResource(R.drawable.with_draw_fail);
                mImg.setVisibility(View.VISIBLE);
                mPbBar.setVisibility(View.GONE);
                mConfrim.setText(getContext().getString(R.string.btn_confirm));
                break;
            case 2:
                mTitle.setText(getContext().getString(R.string.withdraw_success));
                mCurrentType = SUCCESS;
                mImg.setImageResource(R.drawable.with_draw_success);
                mImg.setVisibility(View.VISIBLE);
                mPbBar.setVisibility(View.GONE);
                mConfrim.setText(getContext().getString(R.string.btn_confirm));
                break;
            case 3:
                mTitle.setText(getContext().getString(R.string.withdraw_progress));
                mCurrentType = PROCESS;
                mConfrim.setText(getContext().getString(R.string.common_see_other));
                mPbBar.setVisibility(View.VISIBLE);
                mImg.setVisibility(View.GONE);
                break;
        }
    }
}
