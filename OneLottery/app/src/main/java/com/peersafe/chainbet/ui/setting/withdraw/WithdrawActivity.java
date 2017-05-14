package com.peersafe.chainbet.ui.setting.withdraw;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.db.WithdrawBankCard;
import com.peersafe.chainbet.db.WithdrawRecord;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.CardAmountModel;
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

public class WithdrawActivity extends BasicActivity implements View.OnClickListener
{
    private String TAG = WithdrawActivity.class.getSimpleName();
    private static final int DECIMAL_DIGITS = 2;//小数的位数
    private TextView mBanlance, mBank;

    private EditText mAmount;

    private Button mWithdraw;
    public static WithdrawActivity instance = null;

    private WithdrawBankCard bankCard;

    //密码输入框
    private InputPwdDialog inputPwdDialog;
    private double banlanceFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);

        EventBus.getDefault().register(this);
        instance = this;
        bankCard = (WithdrawBankCard) getIntent().getSerializableExtra(ConstantCode
                .CommonConstant.BANK_CARD);

        mBanlance = (TextView) findViewById(R.id.tv_amount);
        mBank = (TextView) findViewById(R.id.tv_bank);
        mAmount = (EditText) findViewById(R.id.et_withdraw);
        mWithdraw = (Button) findViewById(R.id.btn_withdraw);
        mAmount.setCursorVisible(false);

        mWithdraw.setBackgroundResource(R.drawable.selector_bet_gray_btn);
        mWithdraw.setEnabled(false);

        mWithdraw.setOnClickListener(this);
        mBank.setOnClickListener(this);

        setData();

        initToolBar();

        mAmount.addTextChangedListener(textWatcher);
    }

    private TextWatcher textWatcher = new TextWatcher()
    {
        public int editStart;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
            editStart = mAmount.getSelectionStart();
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
            //处理不包含小数点整数部最大不能超过九位,小数点最大不能超过两位
            if(!charSequence.toString().contains(".") && charSequence.length() >= 10)
            {
                charSequence = charSequence.toString().subSequence(0,9);
                mAmount.setText(charSequence);
                mAmount.setSelection(charSequence.length());
            }

            if (charSequence.toString().contains("."))
            {
                if (charSequence.length() - 1 - charSequence.toString().indexOf(".") > DECIMAL_DIGITS)
                {
                    charSequence = charSequence.toString().subSequence(0,
                            charSequence.toString().indexOf(".") + DECIMAL_DIGITS + 1);
                    mAmount.setText(charSequence);
                    mAmount.setSelection(charSequence.length());
                }

                //含有小数点最大不能超过11位
                if(charSequence.length() >= 13)
                {
                    CharSequence beforeDian = charSequence.toString().subSequence(0, charSequence.toString().indexOf("."));
                    if(beforeDian.length() > 9)
                    {
                        beforeDian = charSequence.toString().substring(0,9);
                        CharSequence afterDian =  charSequence.toString().subSequence(charSequence.toString().indexOf(".") + 1,charSequence.length());
                        charSequence = beforeDian + "." + afterDian;
                        mAmount.setText(charSequence);
                        mAmount.setSelection(charSequence.length());
                    }
                }
            }

            if (charSequence.toString().trim().substring(0).equals("."))
            {
                charSequence = "0" + charSequence;
                mAmount.setText(charSequence);
                mAmount.setSelection(2);
            }

            if (charSequence.toString().startsWith("0")
                    && charSequence.toString().trim().length() > 1)
            {
                if (!charSequence.toString().substring(1, 2).equals("."))
                {
                    mAmount.setText(charSequence.subSequence(0, 1));
                    mAmount.setSelection(1);
                    return;
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable)
        {
            mAmount.setCursorVisible(true);

            if (StringUtils.isEmpty(editable.toString()) || Double.parseDouble(editable.toString()) <= 0)
            {
                mAmount.setCursorVisible(false);
                mWithdraw.setBackgroundResource(R.drawable.selector_bet_gray_btn);
                mWithdraw.setEnabled(false);
            } else
            {
                mWithdraw.setBackgroundResource(R.drawable.selector_transfer_btn);
                mWithdraw.setEnabled(true);
            }
        }
    };

    private void setData()
    {
        //设置剩余余额
        UserInfo info = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        Long balance = info.getBalance();
        banlanceFormat = ((double) balance / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE);
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        mBanlance.setText(decimalFormat.format(banlanceFormat));
        mAmount.setHint(String.format(getString(R.string.withdraw_can), decimalFormat.format
                (banlanceFormat)));

        //设置银行开户行和银行卡号
        String number = bankCard.getBankCardId();
        String newNumber = number.replaceAll(" ", "");
        number = newNumber.substring(newNumber.length() - 4, newNumber.length());
        mBank.setText(bankCard.getOpeningBank() + "(" + number + ")");
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(getString(R.string.setting_with_draw));

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

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_withdraw:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if (!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                if (Double.parseDouble(mAmount.getText().toString().trim()) > banlanceFormat)
                {
                    showToast(getString(R.string.lottery_bet_balance_not_enough));
                    break;
                }

                String amount = String.valueOf(mAmount.getText());
                CardAmountModel cardAmountModel = new CardAmountModel();
                cardAmountModel.setAmount(Double.parseDouble(amount.trim()) * ConstantCode
                        .CommonConstant.ONELOTTERY_MONEY_MULTIPLE);
                cardAmountModel.setBankCard(bankCard);

                inputPwdDialog = new InputPwdDialog(this, InputPwdDialog.WITH_DRAW,
                        cardAmountModel);
                inputPwdDialog.show();
                break;

            case R.id.tv_bank:
                Intent intent = new Intent(WithdrawActivity.this, BankcardActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(final OLMessageModel model)
    {
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_WITH_DRAW_CALLBACK:
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
            case OLMessageModel.STMSG_MODEL_REFRSH_SETTING_BALANCE:
                setData();
                break;

            case OLMessageModel.STMSG_MODEL_DISMISS_WITHDRAW_DIALOG:
                int type = (int) model.getEventObject();
                if (type == WithdrawDialog.FAIL)
                {
                    dismissWithdrawDialog();
                } else
                {
                    dismissWithdrawDialog();
                    BankcardActivity.isntance.finish();
                    finish();
                }
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
