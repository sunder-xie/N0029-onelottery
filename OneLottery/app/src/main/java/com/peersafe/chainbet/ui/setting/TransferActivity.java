package com.peersafe.chainbet.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peersafe.chainbet.LoginActivity;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.LotteryJsonBean.ZXUserInfoRet;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.model.Transfer;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.log.OLLogger;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.AlertDialog;
import com.peersafe.chainbet.widget.InputPwdDialog;
import com.peersafe.chainbet.widget.KeyboardChangeListener;
import com.peersafe.chainbet.widget.WaitingDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;

/**
 * @author caozhongzheng
 * @Description 转账
 * @date 2017/2/7 10:22
 */
public class TransferActivity extends BasicActivity implements View.OnClickListener
{
    private static final String TAG = TransferActivity.class.getSimpleName();
    private static final int DECIMAL_DIGITS = 2; //小数点位数
    private static final int TRANSFER_ACCOUNT_INACTIVATED = 0x02;
    private LinearLayout mRoot;
    private LinearLayout mLyInputAccount;
    private EditText mEtNickOrAccount;
    private Button mTransferNext;
    private LinearLayout mLyTransDetail;
    private TextView mUserName;
    private TextView mUserAccount;
    private EditText mTransNum;
    private TextView mBalance;
    private TextView mRecharge;
    private Button mTransfer;

    private int STEP = 1; // 1：输入账号 2：转账
    private UserInfo userInfo;
    private UserInfo me = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
    private InputPwdDialog inputPwdDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transfer);

        initToolBar();

        initView();

        setKeyboardChangeLister();

        EventBus.getDefault().register(this);
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(getString(R.string.setting_transfer));
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // 根据当前状态来设置是返回输入账号界面，还是在转账详情界面
                if (STEP == 1)
                {
                    finish();
                } else
                {
                    showDetail(false);

                    mLyInputAccount.setVisibility(View.VISIBLE);
                    mLyTransDetail.setVisibility(View.GONE);
                }
            }

        });

    }

    private void initView()
    {
        mRoot = (LinearLayout) findViewById(R.id.activity_transfer);
        mLyInputAccount = (LinearLayout) findViewById(R.id.rl_input_account);
        mEtNickOrAccount = (EditText) findViewById(R.id.et_nick_or_account);
        mTransferNext = (Button) findViewById(R.id.btn_transfer_next);
        mLyTransDetail = (LinearLayout) findViewById(R.id.ly_transfer_detail);
        mUserName = (TextView) findViewById(R.id.tv_name_to);
        mUserAccount = (TextView) findViewById(R.id.tv_addr_to);
        mTransNum = (EditText) findViewById(R.id.et_sum_of_money);
        mBalance = (TextView) findViewById(R.id.tv_balance);
        mRecharge = (TextView) findViewById(R.id.tv_recharge);
        mTransfer = (Button) findViewById(R.id.btn_confirm_transfer);

        mTransferNext.setOnClickListener(this);
        mRecharge.setOnClickListener(this);
        mTransfer.setOnClickListener(this);
        mEtNickOrAccount.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent)
            {
                if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP)
                {
                    //先隐藏键盘
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);

                    if (!NetworkUtil.isNetworkConnected())
                    {
                        showToast(getString(R.string.check_network));
                        return false;
                    }

                    if(!OneLotteryManager.getInstance().isServiceConnect)
                    {
                        showToast(getString(R.string.check_service));
                        return false;
                    }

                    onSearchAccount();
                }
                return false;
            }
        });
        mEtNickOrAccount.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (StringUtils.isEmpty(s.toString()))
                {
                    mTransferNext.setBackgroundResource(R.drawable.selector_bet_gray_btn);
                    mTransferNext.setEnabled(false);
                } else
                {
                    mTransferNext.setBackgroundResource(R.drawable.selector_transfer_btn);
                    mTransferNext.setEnabled(true);
                }

            }
        });
        mTransNum.addTextChangedListener(mTextWatcher);
    }


    private void setKeyboardChangeLister()
    {
        KeyboardChangeListener listener = new KeyboardChangeListener(this);
                listener.setKeyBoardListener(new KeyboardChangeListener.KeyBoardListener()
        {
            @Override
            public void onKeyboardChange(boolean isShow, int keyboardHeight)
            {
                if (!isShow || keyboardHeight < 101)
                {
                    mRoot.setPadding(0, 0, 0, 0);
                } else if (STEP == 2)
                {
                    int height = getWindowManager().getDefaultDisplay().getHeight();
                    mRoot.setPadding(0, -height / 4, 0, 0);
                }
            }
        });
    }

    // 启动搜索用户昵称和账号功能
    private void onSearchAccount()
    {
//                    OLLogger.i(TAG, "ime:" + v.getText().toString());
        String curName = mEtNickOrAccount.getText().toString();
        if (userInfo != null && (curName.equals(userInfo.getUserId())
                || curName.length() == 56 && curName.equals(userInfo.getWalletAddr())))
        {
            // 如果想再次转账可以不用搜索，直接到转账详情界面了
            showDetail(true);
        } else if (checkAccountOK())
        {
            searchAccount();
        } else
        {
            mEtNickOrAccount.requestFocus();
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher()
    {
        public int editStart;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            editStart = mTransNum.getSelectionStart();
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count)
        {
            //处理不包含小数点整数部最大不能超过九位,小数点最大不能超过两位
            if(!charSequence.toString().contains(".") && charSequence.length() >= 10)
            {
                charSequence = charSequence.toString().subSequence(0,9);
                mTransNum.setText(charSequence);
                mTransNum.setSelection(charSequence.length());
            }

            if (charSequence.toString().contains("."))
            {
                if (charSequence.length() - 1 - charSequence.toString().indexOf(".") > DECIMAL_DIGITS)
                {
                    charSequence = charSequence.toString().subSequence(0,
                            charSequence.toString().indexOf(".") + DECIMAL_DIGITS + 1);
                    mTransNum.setText(charSequence);
                    mTransNum.setSelection(charSequence.length());
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
                        mTransNum.setText(charSequence);
                        mTransNum.setSelection(charSequence.length());
                    }
                }
            }

            if (charSequence.toString().trim().substring(0).equals("."))
            {
                charSequence = "0" + charSequence;
                mTransNum.setText(charSequence);
                mTransNum.setSelection(2);
            }

            if (charSequence.toString().startsWith("0")
                    && charSequence.toString().trim().length() > 1)
            {
                if (!charSequence.toString().substring(1, 2).equals("."))
                {
                    mTransNum.setText(charSequence.subSequence(0, 1));
                    mTransNum.setSelection(1);
                    return;
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s)
        {
            if (StringUtils.isEmpty(s.toString()))
            {
                mTransfer.setBackgroundResource(R.drawable.selector_bet_gray_btn);
                mTransfer.setEnabled(false);
            } else
            {
                mTransfer.setBackgroundResource(R.drawable.selector_transfer_btn);
                mTransfer.setEnabled(true);
            }
        }
    };


    private boolean checkAccountOK()
    {
        if (me == null)
        {
            Intent i_login = new Intent(TransferActivity.this, LoginActivity.class);
            startActivity(i_login);
            showToast(getString(R.string.setting_login_first));
            return false;
        }

        String name = mEtNickOrAccount.getText().toString();
        if (name.length() < 2)
        {
            showToast(getString(R.string.register_name_length_too_short));
            return false;
        }
//        else if (name.length() < 16)
//        {
//            if (StringUtils.isLetter(name))
//            {
//                showToast(getString(R.string.register_enter_user_name_only_letter));
//                return false;
//            } else if (StringUtils.isNumeric(name))
//            {
//                showToast(getString(R.string.register_enter_user_name_only_numer));
//                return false;
//            }
//        }

        if (name.length() > 16 && name.length() < 56)
        {
            showToast(getString(R.string.transfer_account_invalid));
            return false;
        }

        if (me.getWalletAddr().equals(name) || me.getUserId().equals(name))
        {
            showToast(getString(R.string.transfer_account_me_invalid));
            return false;
        }

        return true;
    }


    private void searchAccount()
    {
        final String name = mEtNickOrAccount.getText().toString();
        boolean hasNetwork = NetworkUtil.isNetworkConnected();
        boolean hasService = OneLotteryManager.isServiceConnect;
        if (hasNetwork && hasService)
        {
            showProgressDialog(getString(R.string.transfer_searching_account), false);
            // 获取用户信息
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    userInfo = new UserInfo();
                    ZXUserInfoRet ret = null;
                    boolean isHash = name.length() == 56;
                    if (!isHash)
                    {
                        userInfo.setUserId(name);
                        ret = OneLotteryApi.getUserInfo(name, null);
                        if (ret != null && ret.getCode() == OneLotteryApi.SUCCESS && ret.getData
                                () != null)
                        {
                            userInfo.setWalletAddr(ret.getData().getOwner());
                            OLLogger.i(TAG, "hash=：" + userInfo.getWalletAddr());
                        } else
                        {
                            userInfo = null;
                        }
                    } else
                    {
                        userInfo.setWalletAddr(name);
                        ret = OneLotteryApi.getUserInfo(null, name);
                        if (ret != null && ret.getCode() == OneLotteryApi.SUCCESS && ret.getData
                                () != null)
                        {
                            userInfo.setUserId(ret.getData().getUserId());
                            OLLogger.i(TAG, "userinfo=：" + userInfo.getUserId());
                        } else
                        {
                            // 默认他是黑户,或者有可能是网络错误
                            userInfo.setUserId("");
                        }
                    }
                    dismissProgressDialog();

                    final int code = ret != null ? ret.getCode() : 0;

                    // TODO 需要弹dialog形式
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (code == OneLotteryApi.NETWORK_ERR)
                            {
                                showToast(getString(R.string.transfer_network_error));
                            } else if (userInfo != null)
                            {
                                if (StringUtils.isEmpty(userInfo.getUserId()))
                                {
                                    Intent intent = new Intent(TransferActivity.this, AlertDialog.class);
                                    intent.putExtra(AlertDialog.TIP_MESSAGE, getString(R.string.transfer_account_inactivated2));
                                    intent.putExtra(AlertDialog.SHOW_CONFIRM_BTN, true);
                                    startActivityForResult(intent, TRANSFER_ACCOUNT_INACTIVATED);

//                                    showToast(getString(R.string.transfer_account_inactivated2));
                                }
                                else
                                {
                                    showDetail(true);
                                }
                            } else
                            {
                                showToast(getString(R.string.transfer_account_inactivated));
                            }
                        }
                    });
                }

            }).start();
        } else
        {
            if(!hasNetwork)
            {
                showToast(getString(R.string.no_network));
                return;
            }

            if(!hasService)
            {
                showToast(getString(R.string.no_service));
                return;
            }
        }

    }


    private void showDetail(boolean b)
    {
        if (b && userInfo != null && !StringUtils.isEmpty(userInfo.getWalletAddr()))
        {
            STEP = 2;
            mLyInputAccount.setVisibility(View.GONE);
            mLyTransDetail.setVisibility(View.VISIBLE);

            mUserName.setText(userInfo.getUserId());
            mUserAccount.setText(userInfo.getWalletAddr().substring(0, 28) + "\n" + userInfo
                    .getWalletAddr().substring(28));
            mTransNum.setText("");
            double s = ((double) me.getBalance() / ConstantCode.CommonConstant
                    .ONELOTTERY_MONEY_MULTIPLE);
            DecimalFormat df = new DecimalFormat("0.00");
            mBalance.setText(String.format(getString(R.string.transfer_my_balance), df.format(s)));

            mTransNum.requestFocus();
        } else
        {
            STEP = 1;
            mLyInputAccount.setVisibility(View.VISIBLE);
            mLyTransDetail.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == TRANSFER_ACCOUNT_INACTIVATED)
        {
            showDetail(true);
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.tv_recharge:
                // TODO 跳转充值中心
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

                Intent recharge = new Intent();
                recharge.setClass(TransferActivity.this, RechangeActivity.class);
                startActivity(recharge);

                break;

            case R.id.btn_transfer_next:
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

                onSearchAccount();

                break;
            case R.id.btn_confirm_transfer:
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

                transfer();
                break;
            default:
                break;
        }
    }

    private void transfer()
    {
        String num = mTransNum.getText().toString();
        if (StringUtils.isEmpty(num))
        {
            showToast(getString(R.string.transfer_num_enter));
            return;
        }

        if (!StringUtils.isDouble(num))
        {
            showToast(getString(R.string.transfer_num_enter_invalid));
            return;
        }

        double dnum = Double.parseDouble(num);
        if ((me.getBalance() - 100) < dnum * ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE)
        {
            showToast(getString(R.string.transfer_num_enter_too_max));
            return;
        }

        if (dnum <= 0d)
        {
            showToast(getString(R.string.transfer_num_enter_is_zero));
            return;
        }

        Transfer transfer = new Transfer(userInfo.getUserId(), userInfo.getWalletAddr(),
                (long) (dnum * ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE),
                null);

        inputPwdDialog = new InputPwdDialog(TransferActivity.this, InputPwdDialog
                .OPER_TYPE_TRANSFER, transfer);
        inputPwdDialog.show();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

        if (inputPwdDialog != null)
        {
            inputPwdDialog.dismiss();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(OLMessageModel model)
    {
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_TRANSFER_FINISH:

                // 无论是转账中,转账失败还是成功都跳转到个人中心。
                finish();
                break;

            case OLMessageModel.STMSG_MODEL_TRANSFER_CALLBACK:

                // 如果是停留在当前页面。显示转账结果（点击确定后就可以回个人中心，即上一步）
                WaitingDialog waitingDialog = getWaitingDialog();
                if (waitingDialog != null && waitingDialog.isShowing())
                {
                    boolean succ = (boolean) model.getEventObject();
                    getWaitingDialog().setBtnText(getString(succ ? R.string.transfer_success : R
                            .string.transfer_fail));
                }

                break;

            case OLMessageModel.STMSG_MODEL_TRANSFER_ACCOUNT_NOTIFY:
                me = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
                showDetail(true);
                break;
        }
    }
}
