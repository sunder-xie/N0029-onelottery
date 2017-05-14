package com.peersafe.chainbet.ui.setting.withdraw;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class AppealActivity extends BasicActivity implements View.OnClickListener
{
    Button mBtnConfirm;

    EditText mEtReason;

    public static AppealActivity instance = null;

    InputPwdDialog inputPwdDialog;

    WithdrawRecord record;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appeal);

        instance = this;

        EventBus.getDefault().register(this);

        record = (WithdrawRecord) getIntent().getSerializableExtra(ConstantCode.CommonConstant.WITH_DRAW_RECORD);

        mBtnConfirm = (Button) findViewById(R.id.btn_appeal_confirm);
        mEtReason = (EditText) findViewById(R.id.et_appeal_reason);

        mBtnConfirm.setOnClickListener(this);
        mEtReason.addTextChangedListener(textWatcher);

        initToolBar();
    }

    public TextWatcher textWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {

        }

        @Override
        public void afterTextChanged(Editable editable)
        {
            if(StringUtils.isEmpty(editable.toString()))
            {
                mBtnConfirm.setBackgroundResource(R.drawable.selector_bet_gray_btn);
                mBtnConfirm.setEnabled(false);
            }
            else
            {
                mBtnConfirm.setBackgroundResource(R.drawable.selector_transfer_btn);
                mBtnConfirm.setEnabled(true);
            }
        }
    };

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(getString(R.string.withdraw_appeal));

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
            case OLMessageModel.STMSG_MODEL_DISMISS_WITHDRAW_DIALOG:
                int type = (int) model.getEventObject();
                if(type == 1)
                {
                    dismissWithdrawDialog();
                } else
                {
                    dismissWithdrawDialog();
                    finish();
                }
                break;
            case OLMessageModel.STMSG_MODEL_WITH_DRAW_APPEAL_CALLBACK:
                WithdrawRecord record = (WithdrawRecord) model.getEventObject();
                WithdrawDialog withdrawDialog = getWithdrawDialog();

                if (withdrawDialog != null && withdrawDialog.isShowing())
                {
                    if (record != null)
                    {
                        withdrawDialog.setWithdrawStatus(WithdrawDialog.SUCCESS);
                    } else
                    {
                        withdrawDialog.setWithdrawStatus(WithdrawDialog.FAIL);
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_appeal_confirm:

                if(!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if(!OneLotteryManager.isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                record.setRemark(mEtReason.getText().toString());
                WithdrawRecordDBHelper.getInstance().insert(record);

                inputPwdDialog = new InputPwdDialog(AppealActivity.this,InputPwdDialog.WITH_DRAW_APPEAL,record);
                inputPwdDialog.show();
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
