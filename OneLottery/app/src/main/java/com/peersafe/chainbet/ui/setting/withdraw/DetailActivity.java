package com.peersafe.chainbet.ui.setting.withdraw;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.WithdrawRecord;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.WithdrawRecordDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.InputPwdDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class DetailActivity extends BasicActivity implements View.OnClickListener
{
    TextView mStatus, mTime, mAmount, mBankName, mCardNumber, mCardHolder, mBillNumber, mReason;

    Button mConfirm, mAppeal;

    RelativeLayout mRLReason;

    WithdrawRecord record;

    InputPwdDialog inputPwdDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        EventBus.getDefault().register(this);

        record = (WithdrawRecord) getIntent().getSerializableExtra(ConstantCode.CommonConstant.WITH_DRAW_RECORD);

        mStatus = (TextView) findViewById(R.id.tv_bill_status);
        mTime = (TextView) findViewById(R.id.tv_time);
        mAmount = (TextView) findViewById(R.id.tv_amount);
        mBankName = (TextView) findViewById(R.id.tv_bank_name);
        mCardNumber = (TextView) findViewById(R.id.tv_card_number);
        mCardHolder = (TextView) findViewById(R.id.tv_card_name);
        mBillNumber = (TextView) findViewById(R.id.tv_bill_number);
        mReason = (TextView) findViewById(R.id.tv_reason);

        mConfirm = (Button) findViewById(R.id.btn_confirm);
        mAppeal = (Button) findViewById(R.id.btn_appeal);
        mConfirm.setOnClickListener(this);
        mAppeal.setOnClickListener(this);
        mRLReason = (RelativeLayout) findViewById(R.id.rl_reason);

        setData();

        initToolBar();
    }

    private void setData()
    {
        mConfirm.setVisibility(View.GONE);
        mAppeal.setVisibility(View.GONE);
        mRLReason.setVisibility(View.GONE);

        record = WithdrawRecordDBHelper.getInstance().getRecordByKey(record.getTxId());

        //状态
        switch (record.getState())
        {
            case ConstantCode.WithdrawType.WITHDRAW_TYPE_APPLYING:
                mStatus.setText(getString(R.string.withdraw_status_applying));
                break;
            case ConstantCode.WithdrawType.WITHDRAW_TYPE_PAY:
                mStatus.setText(getString(R.string.withdraw_status_pay));
                mConfirm.setVisibility(View.VISIBLE);
                mAppeal.setVisibility(View.VISIBLE);
                break;
            case ConstantCode.WithdrawType.WITHDRAW_TYPE_CONFIRM:
                mStatus.setText(getString(R.string.withdraw_status_confirm));
                mConfirm.setText(getString(R.string.btn_confirm_already));
                if(!StringUtils.isEmpty(record.getRemark()))
                {
                    mRLReason.setVisibility(View.VISIBLE);
                    mReason.setText(record.getRemark());
                }
                break;
            case ConstantCode.WithdrawType.WITHDRAW_TYPE_CANCEL:
                mStatus.setText(getString(R.string.withdraw_status_cancel));
                break;
            case ConstantCode.WithdrawType.WITHDRAW_TYPE_FAIL:
                mStatus.setText(getString(R.string.withdraw_status_fail));
                mRLReason.setVisibility(View.VISIBLE);
                mReason.setText(record.getRemark());
                break;
            case ConstantCode.WithdrawType.WITHDRAW_TYPE_APPEAL:
                mStatus.setText(getString(R.string.withdraw_status_appealing));
                break;
        }

        //时间
        SimpleDateFormat format = new SimpleDateFormat(ConstantCode.CommonConstant.SIMPLE_DATE_FORMAT);
        mTime.setText(format.format(record.getCreateTime()));

        //提现金额
        double amount = (double)record.getAmount() / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        mAmount.setText(String.valueOf(decimalFormat.format(amount)));

        //银行名字
        mBankName.setText(record.getOpeningBankName());

        //银行卡号
        mCardNumber.setText(record.getAccountId());

        //拥有者
        mCardHolder.setText(record.getAccountName());

        //订单号
        mBillNumber.setText(record.getRemitOrderNumber() == null ? "" : record.getRemitOrderNumber());
    }

    public void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(getString(R.string.withdraw_list_text));

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(final OLMessageModel model)
    {
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_REMIT_SUCCES_NOTIFY:
            case OLMessageModel.STMSG_MODEL_WITH_DRAW_FAIL_NOTIFY:
            case OLMessageModel.STMSG_MODEL_APPEAL_DONE_NOTIFY:
            case OLMessageModel.STMSG_MODEL_WITH_DRAW_APPEAL_CALLBACK:
            case OLMessageModel.STMSG_MODEL_WITH_DRAW_CONFIRM_NOTIFY:
                setData();
                break;
            case OLMessageModel.STMSG_MODEL_WITH_DRAW_CONFIRM:
                setData();
                WithdrawRecord record = (WithdrawRecord) model.getEventObject();
                WithdrawDialog withdrawDialog = getWithdrawDialog();
                if(withdrawDialog != null && withdrawDialog.isShowing())
                {
                    if(record != null)
                    {
                        withdrawDialog.setWithdrawStatus(WithdrawDialog.SUCCESS);
                    }
                    else
                    {
                        withdrawDialog.setWithdrawStatus(WithdrawDialog.FAIL);
                    }
                }
                break;

            case OLMessageModel.STMSG_MODEL_DISMISS_WITHDRAW_DIALOG:
                dismissWithdrawDialog();
                break;
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_confirm:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if(!OneLotteryManager.isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                inputPwdDialog = new InputPwdDialog(DetailActivity.this,InputPwdDialog.WITH_DRAW_CONFIRM,record);
                inputPwdDialog.show();
                break;

            case R.id.btn_appeal:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if(!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }
                Intent intent = new Intent(DetailActivity.this, AppealActivity.class);
                intent.putExtra(ConstantCode.CommonConstant.WITH_DRAW_RECORD, record);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
