package com.peersafe.chainbet.ui.setting;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.utils.common.BillUtils;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.WaitingDialog;

import java.util.HashMap;
import java.util.Map;

import cn.beecloud.BCPay;
import cn.beecloud.async.BCCallback;
import cn.beecloud.async.BCResult;
import cn.beecloud.entity.BCPayResult;

public class RechangeActivity extends BasicActivity implements View.OnClickListener
{
    private static final String walletAddr = "walletAddr";
    private static final String timesTamp = "timestamp";
    private static final int MAX_TOTAL_AMOUNT = 1000000;
    private String toastMsg;

    private Button mBtnRechange;

    private Button mBtn50, mBtn100, mBtn200, mBtn500, mBtn1000;

    private RelativeLayout mRlAli, mRlWeiXin;

    private ImageView mAli, mWeiXin;

    private EditText mEtEnter;

    private boolean isAli = false, isWeiXin = false;

    private Button[] buttons;

    private int position = -1;

    private boolean config = false;

    int rechange = 0;

    private  int totalAmount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rechange);

        mBtnRechange = (Button) findViewById(R.id.btn_setting_rechange);
        mBtnRechange.setEnabled(true);
        mBtn50 = (Button) findViewById(R.id.btn50);
        mBtn100 = (Button) findViewById(R.id.btn100);
        mBtn200 = (Button) findViewById(R.id.btn200);
        mBtn500 = (Button) findViewById(R.id.btn500);
        mBtn1000 = (Button) findViewById(R.id.btn1000);
        buttons = new Button[]{mBtn50, mBtn100, mBtn200, mBtn500, mBtn1000};

        mEtEnter = (EditText) findViewById(R.id.et_rechange_enter);

        mRlAli = (RelativeLayout) findViewById(R.id.rel_ali);
        mRlWeiXin = (RelativeLayout) findViewById(R.id.rel_wx);

        isAli = true;
        mAli = (ImageView) findViewById(R.id.btn_ali);
        mWeiXin = (ImageView) findViewById(R.id.btn_wx);
        mAli.setImageResource(R.drawable.seclect_icon);

        mBtnRechange.setOnClickListener(this);
        mBtn50.setOnClickListener(this);
        mBtn100.setOnClickListener(this);
        mBtn200.setOnClickListener(this);
        mBtn500.setOnClickListener(this);
        mBtn1000.setOnClickListener(this);

        mRlAli.setOnClickListener(this);
        mRlWeiXin.setOnClickListener(this);

        mEtEnter.addTextChangedListener(watcher);
        initToolBar();
    }

    private TextWatcher watcher = new TextWatcher()
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
            String val = editable.toString();
            if (!StringUtils.isEmpty(val))
            {
                try
                {
                    totalAmount = Integer.parseInt(val);
                    if (totalAmount > 0)
                    {
                        if (totalAmount > MAX_TOTAL_AMOUNT)
                        {
                            mEtEnter.setText("" + MAX_TOTAL_AMOUNT);
                            mEtEnter.setSelection(("" + MAX_TOTAL_AMOUNT).length());
                        }
                    } else
                    {
                        mEtEnter.setText("");
                    }
                }
                catch (NumberFormatException e)
                {
                    e.printStackTrace();
                }
            }

            if(StringUtils.isEmpty(editable.toString()))
            {
                config = false;
            }

            if(!StringUtils.isEmpty(editable.toString()) && !config)
            {
                choicePerMuch(-1);
                position = -1;
                config = true;
            }
        }
    };

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(getString(R.string.setting_rechange_center));

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

    //支付结果返回入口
    BCCallback bcCallback = new BCCallback()
    {
        @Override
        public void done(final BCResult bcResult)
        {
            final BCPayResult bcPayResult = (BCPayResult) bcResult;

            //根据你自己的需求处理支付结果
            String result = bcPayResult.getResult();

            //注意！
            //所有支付渠道建议以服务端的状态金额为准，此处返回的RESULT_SUCCESS仅仅代表手机端支付成功
            Message msg = mHandler.obtainMessage();

            //单纯的显示支付结果
            msg.what = 2;
            if (result.equals(BCPayResult.RESULT_SUCCESS))
            {
                msg.what = 3;
            } else if (result.equals(BCPayResult.RESULT_CANCEL))
            {
                toastMsg = getString(R.string.setting_rechange_pay_cancel);
            } else if (result.equals(BCPayResult.RESULT_FAIL))
            {
                toastMsg = getString(R.string.setting_rechange_pay_fail);

                if(bcPayResult.getErrCode() == BCPayResult.APP_INTERNAL_NETWORK_ERR_CODE
                        && bcPayResult.getErrMsg().equals(BCPayResult.FAIL_NETWORK_ISSUE))
                {
                    toastMsg = getString(R.string.check_network);
                }
                //失败的原因查看
//                        + bcPayResult.getErrCode() +
//                        " # " + bcPayResult.getErrMsg() +
//                        " # " + bcPayResult.getDetailInfo();

                //你发布的项目中不应该出现如下错误，此处由于支付宝政策原因，
                //不再提供支付宝支付的测试功能，所以给出提示说明
                if (bcPayResult.getErrMsg().equals("PAY_FACTOR_NOT_SET") &&
                        bcPayResult.getDetailInfo().startsWith("支付宝参数"))
                {
                    toastMsg = "支付失败：由于支付宝政策原因，故不再提供支付宝支付的测试功能，给您带来的不便，敬请谅解";
                }

            } else if (result.equals(BCPayResult.RESULT_UNKNOWN))
            {
                //可能出现在支付宝8000返回状态
                toastMsg = getString(R.string.setting_rechange_order_unkown);
            } else
            {
                toastMsg = "invalid return";
            }

            mHandler.sendMessage(msg);
        }
    };

    // 通过Handler.Callback()可消除内存泄漏警告
    private Handler mHandler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 2:
                    mBtnRechange.setEnabled(true);
                    showToast(toastMsg);
                    break;
                case 3:
                    mBtnRechange.setEnabled(true);
                    showWaitingDialog(RechangeActivity.this,WaitingDialog.WAITING_DIALOG_RECHANGE,String.valueOf(rechange));
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onResume()
    {
        super.onResume();
        mBtnRechange.setEnabled(true);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn50:
                choicePerMuch(0);
                break;
            case R.id.btn100:
                choicePerMuch(1);
                break;
            case R.id.btn200:
                choicePerMuch(2);
                break;
            case R.id.btn500:
                choicePerMuch(3);
                break;
            case R.id.btn1000:
                choicePerMuch(4);
                break;

            case R.id.rel_ali:
                isAli = true;
                isWeiXin = false;
                mAli.setImageResource(R.drawable.seclect_icon);
                mWeiXin.setImageResource(R.drawable.no_seclect_icon);
                break;

            case R.id.rel_wx:
                isWeiXin = true;
                isAli = false;
                mWeiXin.setImageResource(R.drawable.seclect_icon);
                mAli.setImageResource(R.drawable.no_seclect_icon);
                break;

            case R.id.btn_setting_rechange:

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

                switch (position)
                {
                    case -1:
                        if (StringUtils.isEmpty(mEtEnter.getText().toString()))
                        {
                            showToast(getString(R.string.setting_rechange_enter_money));
                            return;
                        }
                        rechange = Integer.parseInt(mEtEnter.getText().toString());
                        break;
                    case 0:
                        rechange = 100;
                        break;
                    case 1:
                        rechange = 200;
                        break;
                    case 2:
                        rechange = 500;
                        break;
                    case 3:
                        rechange = 1000;
                        break;
                    case 4:
                        rechange = 10000;
                        break;
                    default:
                        break;
                }

                mBtnRechange.setEnabled(false);

                long timetamp = System.currentTimeMillis();
                Map<String, String> mapOptional = new HashMap<String, String>();

                UserInfo userInfo = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
                if (userInfo != null)
                {
                    mapOptional.put(walletAddr, userInfo.getWalletAddr());
                    mapOptional.put(timesTamp, String.valueOf(timetamp));
                }

                if (isWeiXin)
                {
                    //对于微信支付, 手机内存太小会有OutOfResourcesException造成的卡顿, 以致无法完成支付
                    //这个是微信自身存在的问题
                    if (BCPay.isWXAppInstalledAndSupported() &&
                            BCPay.isWXPaySupported())
                    {
                        BCPay.getInstance(RechangeActivity.this).reqWXPaymentAsync(
                                String.format(getString(R.string.setting_rechange_order_title),rechange),      //订单标题
                                rechange,                                            //订单金额(分) <integer 最大数限制>
                                BillUtils.genBillNum(userInfo.getWalletAddr(),timetamp),   //订单流水号
                                mapOptional,                                               //扩展参数(可以null)
                                bcCallback);                                               //支付完成后回调入口
                    } else
                    {
                        showToast(getString(R.string.setting_rechange_no_install_weixin));
                        mBtnRechange.setEnabled(true);
                        dismissProgressDialog();
                    }
                    break;
                }

                if (isAli)
                {
                    BCPay.getInstance(RechangeActivity.this).reqAliPaymentAsync(
                            String.format(getString(R.string.setting_rechange_order_title),rechange),          //订单标题
                            rechange,                                                    //订单金额 <integer 最大数限制>
                            BillUtils.genBillNum(userInfo.getWalletAddr(),timetamp),     //订单流水号
                            mapOptional,                                                 //扩展参数(可以为null)
                            bcCallback);                                                 //支付完成回调入口

                    break;
                }
                break;
            default:
                break;
        }
    }

    private void choicePerMuch(int pos)
    {
        for (int i = 0; i < buttons.length; i++)
        {
            if (i == pos)
            {
                buttons[i].setBackgroundResource(R.drawable.shape_rechange_orange_bg);
                buttons[i].setTextColor(getResources().getColor(R.color.white));
                position = pos;
                mEtEnter.setText("");
            } else
            {
                buttons[i].setBackgroundResource(R.drawable.shape_rechange_block_bg);
                buttons[i].setTextColor(getResources().getColor(R.color.common_text_color));
            }
        }
    }

}
