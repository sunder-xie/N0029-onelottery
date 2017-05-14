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
import com.peersafe.chainbet.db.WithdrawBankCard;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.WithdrawBankCardDBHelper;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;

import java.util.Date;

public class AddBankCardActivity extends BasicActivity implements View.OnClickListener
{

    EditText mBankName,mCardNumber,mCardHolder;

    Button mConfirm;

    WithdrawBankCard card = null;

    TextView mAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bank_card);

        card = (WithdrawBankCard) getIntent().getSerializableExtra(ConstantCode.CommonConstant.BANK_CARD);

        mBankName = (EditText) findViewById(R.id.et_bank_name);
        mCardNumber = (EditText) findViewById(R.id.et_card_number);
        mCardHolder = (EditText) findViewById(R.id.et_card_holder);
        mConfirm = (Button) findViewById(R.id.btn_confrim);
        mAlert = (TextView) findViewById(R.id.tv_alert);

        mConfirm.setBackgroundResource(R.drawable.selector_bet_gray_btn);
        mConfirm.setEnabled(false);

        mBankName.addTextChangedListener(new CustomTextWatcher());
        mCardNumber.addTextChangedListener(new CustomTextWatcher());
        mCardHolder.addTextChangedListener(new CustomTextWatcher());

        mConfirm.setOnClickListener(this);

        if(card != null)
        {
            mBankName.setText(card.getOpeningBank());
            mCardNumber.setText(card.getBankCardId());
            mCardHolder.setText(card.getAccountName());
            mConfirm.setBackgroundResource(R.drawable.selector_transfer_btn);
            mConfirm.setEnabled(true);
        }

        initToolBar();
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) findViewById(R.id.tv_title);
        if(card != null)
        {
            title.setText(getString(R.string.withdraw_motify_bank_card));
            mAlert.setText(getString(R.string.withdraw_motify_bank_card_alert));
        }else
        {
            title.setText(getString(R.string.withdraw_add_bank_card));
            mAlert.setText(getString(R.string.withdraw_bind_bank_alert));
        }

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
        if (!NetworkUtil.isNetworkConnected())
        {
            showToast(getString(R.string.check_network));
            return;
        }

        if (!OneLotteryManager.getInstance().isServiceConnect)
        {
            showToast(getString(R.string.check_service));
            return;
        }

        String bankName = mBankName.getText().toString();
        String cardNumber = mCardNumber.getText().toString();
        String cardHolder = mCardHolder.getText().toString();

        WithdrawBankCard bankCard = new WithdrawBankCard();
        String userId = OneLotteryApi.getCurUserId();
        bankCard.setOpeningBank(bankName);
        bankCard.setBankCardId(cardNumber);
        bankCard.setAccountName(cardHolder);
        bankCard.setUserId(userId);
        if(card != null)
        {
            bankCard.setIsDefaultCard(card.getIsDefaultCard());
            bankCard.setCreateTime(card.getCreateTime());
            WithdrawBankCardDBHelper.getInstance().delete(card);
            WithdrawBankCardDBHelper.getInstance().insertWithdrawBankCard(bankCard);
        }
        else
        {
            bankCard.setCreateTime(new Date());
            bankCard.setIsDefaultCard(true);
            WithdrawBankCardDBHelper.getInstance().setPrimaryBankCard(bankCard);
            Intent intent = new Intent(AddBankCardActivity.this,WithdrawActivity.class);
            intent.putExtra(ConstantCode.CommonConstant.BANK_CARD,bankCard);
            startActivity(intent);
        }

        finish();
    }

    class CustomTextWatcher implements TextWatcher
    {
        public int editStart;
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
            editStart = mCardNumber.getSelectionStart();
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable)
        {
            boolean a = StringUtils.isEmpty(mBankName.getText().toString());
            boolean b = StringUtils.isEmpty(mCardNumber.getText().toString());
            boolean c = StringUtils.isEmpty(mCardHolder.getText().toString());

            if(!a && !b && !c && mCardNumber.length() > 4)
            {
                mConfirm.setBackgroundResource(R.drawable.selector_transfer_btn);
                mConfirm.setEnabled(true);
            }
            else
            {
                mConfirm.setBackgroundResource(R.drawable.selector_bet_gray_btn);
                mConfirm.setEnabled(false);
            }
        }
    }
}
