package com.peersafe.chainbet.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.MessageNotify;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.db.WithdrawBankCard;
import com.peersafe.chainbet.db.WithdrawRecord;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.logic.OneLotteryLogic;
import com.peersafe.chainbet.logic.WithdrawLogic;
import com.peersafe.chainbet.logic.sdbackrestore.SdBackRestoreLogic;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.MessageNotifyDBHelper;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.CardAmountModel;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.model.Transfer;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.setting.withdraw.WithdrawDialog;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.config.OLPreferenceUtil;
import com.peersafe.chainbet.utils.log.OLLogger;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import static android.R.attr.type;
import static com.peersafe.chainbet.ui.lottery.CreateLotteryActivity.lottery;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.widget
 * @description:
 * @date 28/12/16 PM5:20
 */
public class InputPwdDialog extends Dialog implements View.OnClickListener
{
    //投注
    public final static int OPER_TYPE_BET = 0;

    //创建活动
    public final static int CREATE_LOTTERY_TYPE = 1;

    //修改活动
    public static final int MODIFY_LOTTERY_TYPE = 2;

    //删除活动
    public static final int DELETE_LOTTERY_TYPE = 3;

    //转账
    public static final int OPER_TYPE_TRANSFER = 4;

    //切换账号
    public static final int CHANGE_ACCOUNT = 5;

    //删除账号
    public static final int DELETE_ACCOUNT = 6;

    //体现
    public static final int WITH_DRAW = 7;

    //申诉
    public static final int WITH_DRAW_APPEAL = 8;

    //提现确认
    public static final int WITH_DRAW_CONFIRM = 9;

    //导出sd
    public static final int EXPORT_SD = 10;

    //导入
    public static final int IMPORT_SD = 11;

    private KeyBackCancelEditText mEtInputPwd;

    private BasicActivity mContext;

    //当前输入密码对应的操作类型
    private int mCurOperType;

    private TextView mTvLotteryCost;

    private Button mConfirm;

    //当前输入密码对应操作的相应对象
    public static Object mCurOperObject;

    public boolean isPwdRight = false;// 切换或删除账户时验证密码正确与否

    //在构造方法里预加载我们的样式，这样就不用每次创建都指定样式了
    public InputPwdDialog(Context context, int operType, Object operObject)
    {
        this(context, R.style.StyleLotteryBetDialog);
        mContext = (BasicActivity) context;
        mCurOperType = operType;
        mCurOperObject = operObject;
    }

    public InputPwdDialog(Context context, int themeResId)
    {
        super(context, themeResId);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.input_pwd_pop);

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
        mEtInputPwd = (KeyBackCancelEditText) findViewById(R.id.et_input_pwd);
        mTvLotteryCost = (TextView) findViewById(R.id.tv_create_lottery_cost);
        findViewById(R.id.btn_close).setOnClickListener(this);
        mConfirm = (Button) findViewById(R.id.btn_confirm);
        mConfirm.setOnClickListener(this);

        if (mCurOperType == CREATE_LOTTERY_TYPE)
        {
            mTvLotteryCost.setVisibility(View.VISIBLE);
            String string = getContext().getString(R.string.create_lottery_cost_text);
            mTvLotteryCost.setText(string);
        } else if (mCurOperType == MODIFY_LOTTERY_TYPE)
        {
            mTvLotteryCost.setVisibility(View.VISIBLE);
            String string = getContext().getString(R.string.modify_lottery_cost_text);
            mTvLotteryCost.setText(string);
        } else if (mCurOperType == OPER_TYPE_TRANSFER)
        {
            mTvLotteryCost.setVisibility(View.VISIBLE);
            String string = getContext().getString(R.string.transfer_cost_text);
            mTvLotteryCost.setText(string);
        } else if (mCurOperType == WITH_DRAW_CONFIRM)
        {
            mTvLotteryCost.setVisibility(View.VISIBLE);
            String string = getContext().getString(R.string.withdraw_confirm_warning);
            mTvLotteryCost.setText(string);
        } else
        {
            mTvLotteryCost.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.
                    LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = getContext().getResources().getDimensionPixelSize(R.dimen.x57);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            mConfirm.setLayoutParams(layoutParams);
        }

        mEtInputPwd.setFilters(new InputFilter[]{StringUtils.getFilter(),
                new InputFilter.LengthFilter(16)});

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mEtInputPwd.setFocusable(true);
                mEtInputPwd.setFocusableInTouchMode(true);
                mEtInputPwd.requestFocus();

                InputMethodManager inputManager =
                        (InputMethodManager) mEtInputPwd.getContext().getSystemService(Context
                                .INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mEtInputPwd, 0);
            }
        }, 200);

        mEtInputPwd.setOnCancelDialogImp(new KeyBackCancelEditText.OnCancelDialogImp()
        {
            @Override
            public void onCancelDialog()
            {
                mContext.hideSoftKeyboard();
                dismiss();
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_close:
                dismiss();
                break;

            case R.id.btn_confirm:
                if (!NetworkUtil.isNetworkConnected() && mCurOperType != IMPORT_SD &&
                        mCurOperType != EXPORT_SD)
                {
                    Toast.makeText(mContext, mContext.getString(R.string.check_network), Toast
                            .LENGTH_SHORT).show();
                    break;
                }

                if (!OneLotteryManager.getInstance().isServiceConnect && mCurOperType !=
                        IMPORT_SD && mCurOperType != EXPORT_SD)
                {
                    Toast.makeText(OneLotteryApplication.getAppContext(), mContext.getString(R
                            .string.check_service), Toast.LENGTH_SHORT).show();
                    break;
                }

                onClickConfirm();
                break;
        }
    }

    private void onClickConfirm()
    {
        //首先验证密码
        final String pwd = mEtInputPwd.getText().toString().trim();
        if (StringUtils.isEmpty(pwd))
        {
            Toast.makeText(getContext(), getContext().getString(R.string.common_enter_password),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (pwd.length() < 8)
        {
            Toast.makeText(getContext(), getContext().getString(R.string
                    .register_password_length_not_correct), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCurOperType == DELETE_ACCOUNT || mCurOperType == CHANGE_ACCOUNT)
        {
            String userId = (String) mCurOperObject;
            long isRight = OneLotteryApi.checkCurUserPwd(userId, pwd);
            if (isRight != OneLotteryApi.SUCCESS)
            {
                Toast.makeText(getContext(), getContext().getString(R.string.login_fail), Toast
                        .LENGTH_SHORT).show();
                return;
            }
            isPwdRight = true;
            OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).setDeleteUid
                    (userId);

            if (mCurOperType == CHANGE_ACCOUNT)
            {
                UserInfo use = UserInfoDBHelper.getInstance().getUserByUserId(userId);
                UserInfoDBHelper.getInstance().setCurPrimaryAccount(use);
                OneLotteryApi.setCurUserId(userId);
            }

            dismiss();
        } else
        {
            //验证当前用户的密码
            long isSuccess = OneLotteryApi.login(OneLotteryApi.getCurUserId(), pwd);

            //导入文件验证密码
            if (mCurOperType == IMPORT_SD)
            {
                isSuccess = OneLotteryApi.SUCCESS;
            }

            if (isSuccess != OneLotteryApi.SUCCESS)
            {
                Toast.makeText(getContext(), getContext().getString(R.string.login_fail), Toast
                        .LENGTH_SHORT).show();
                return;
            }
        }


        if (mCurOperType == CREATE_LOTTERY_TYPE)
        {
            mContext.showWaitingDialog(getContext(), WaitingDialog.WAITING_DIALOG_CREATE, null);

            final OneLottery oneLottery = (OneLottery) mCurOperObject;

            //调用创建活动的接口
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    String txId = OneLotteryApi.oneLotteryAdd(
                            oneLottery.getLotteryName(),
                            oneLottery.getRuleType(),
                            oneLottery.getRuleId(),
                            oneLottery.getPictureIndex(),
                            oneLottery.getCreateTime().getTime(),
                            oneLottery.getStartTime().getTime(),
                            oneLottery.getCloseTime().getTime(),
                            oneLottery.getMinBetCount(),
                            oneLottery.getMaxBetCount(),
                            (int) (oneLottery.getOneBetCost().longValue()),
                            oneLottery.getBetTotalAmount(),
                            oneLottery.getDescription(),
                            pwd);

                    if (checkFailWhenDialogHide(txId, R.string.message_create_fail))
                    {
                        OneLotteryManager.getInstance().setLotteryMsgWhenDialogHide(
                                ConstantCode.MessageType.MESSAGE_TYPE_CREATE_FAIL, oneLottery
                                        .getLotteryId() + "_" + System.currentTimeMillis());
                    }

                }
            }).start();
            dismiss();
        } else if (mCurOperType == MODIFY_LOTTERY_TYPE)
        {
            mContext.showWaitingDialog(getContext(), WaitingDialog.WAITING_DIALOG_MODIFY, null);

            final OneLottery oneLottery = (OneLottery) mCurOperObject;

            OLPreferenceUtil.getInstance(mContext).setModLotteryId(oneLottery.getLotteryId());

            //调用修改活动的接口
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    final String txId = OneLotteryApi.oneLotteryModify(
                            oneLottery.getLotteryId(),
                            oneLottery.getLotteryName(),
                            oneLottery.getRuleType(),
                            oneLottery.getRuleId(),
                            oneLottery.getPictureIndex(),
                            oneLottery.getUpdateTime().getTime(),
                            oneLottery.getStartTime().getTime(),
                            oneLottery.getCloseTime().getTime(),
                            oneLottery.getMinBetCount(),
                            oneLottery.getMaxBetCount(),
                            (int) (oneLottery.getOneBetCost().longValue()),
                            oneLottery.getBetTotalAmount(),
                            oneLottery.getDescription(),
                            pwd);
                    if (!StringUtils.isEmpty(txId))
                    {
                        oneLottery.setNewTxId(txId);
                        OneLotteryDBHelper.getInstance().insertOneLottery(oneLottery);
                    }

                    if (checkFailWhenDialogHide(txId, R.string.message_modify_lottery_fail))
                    {
                        OneLotteryManager.getInstance().setLotteryMsgWhenDialogHide(
                                ConstantCode.MessageType.MESSAGE_TYPE_MODIFY_FAIL, oneLottery
                                        .getLotteryId() + "_" + System.currentTimeMillis());
                    }
                }
            }).start();
            dismiss();
        } else if (mCurOperType == DELETE_LOTTERY_TYPE)
        {
            mContext.showWaitingDialog(getContext(), WaitingDialog.WAITING_DIALOG_DELETE, null);

            final OneLottery oneLottery = (OneLottery) mCurOperObject;

            OLPreferenceUtil.getInstance(mContext).setModLotteryId(oneLottery.getLotteryId());

            //调用删除活动的接口
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    String txId = OneLotteryApi.oneLotteryDelete(
                            oneLottery.getLotteryId(),
                            pwd);
                    if (checkFailWhenDialogHide(txId, R.string.message_delete_fail))
                    {
                        OneLotteryManager.getInstance().setLotteryMsgWhenDialogHide(
                                ConstantCode.MessageType.MESSAGE_TYPE_DELETE_FAIL, oneLottery
                                        .getLotteryId() + "_" + System.currentTimeMillis());
                    }

                }
            }).start();
            dismiss();
        } else if (mCurOperType == OPER_TYPE_BET)
        {
            //投注等待窗口
            mContext.showWaitingDialog(getContext(), WaitingDialog.WAITING_DIALOG_BET, null);

            final OneLottery oneLottery = (OneLottery) mCurOperObject;

            OLLogger.i("input", "bet name=" + oneLottery.getLotteryName()
                    + ", id=" + oneLottery.getLotteryId()
                    + ", desc=" + oneLottery.getDescription()
                    + ", count=" + oneLottery.getCurBetCount()
                    + ", amount=" + oneLottery.getCurBetAmount()
                    + ", time=" + oneLottery.getUpdateTime()
                    + ", pwd=" + pwd
            );

            //起线程调用投注接口，进行投注
            // 应该在消息回调中处理 bugON-475
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    String txId = OneLotteryApi.oneLotteryBet(oneLottery.getCurBetAmount()
                                    * ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE,
                            oneLottery.getLotteryId(),
                            oneLottery.getCurBetCount(), oneLottery.getUpdateTime().getTime(), pwd);

                    if (checkFailWhenDialogHide(txId, R.string.message_bet_fail))
                    {
                        OneLotteryManager.getInstance().setBetMsgWhenDialogHide(false,
                                oneLottery, oneLottery.getLotteryId() + "_" + System
                                        .currentTimeMillis());
                    }

                }
            }).start();
            dismiss();
        } else if (mCurOperType == OPER_TYPE_TRANSFER)
        {
            //转账等待窗口
            mContext.showWaitingDialog(getContext(), WaitingDialog.WAITING_DIALOG_TRANSFER, null);

            final Transfer tra = (Transfer) mCurOperObject;

            OLLogger.i("transfer", "准备转账给 name=" + tra.getNameTo()
                    + ", addr=" + tra.getAddressTo()
                    + ", num=" + tra.getAmount()
            );

            //调用转账接口
            // 应该在消息回调中处理 bugON-475
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    String txId = OneLotteryApi.transferAccount(tra.getNameTo(), tra.getAddressTo
                                    (), tra.getAmount(),
                            tra.getRemark(), System.currentTimeMillis(), pwd);

                    OLLogger.i("transfer", "transfer ids=" + txId);

                    OLPreferenceUtil.getInstance(mContext).setTransferAmount(tra.getAmount());
                    OLPreferenceUtil.getInstance(mContext).setTransferToUserHash(tra.getAddressTo
                            ());
                    OLPreferenceUtil.getInstance(mContext).setTransferToUserId(tra.getNameTo());

                    if (checkFailWhenDialogHide(txId, R.string.transfer_fail))
                    {
                        MessageNotify transferMsg = new MessageNotify();
                        String txid = String.valueOf(UUID.randomUUID());
                        transferMsg.setMsgId(txid);

                        String addressTo = OLPreferenceUtil.getInstance(OneLotteryApplication
                                .getAppContext()).getTransferToUserHash();
                        String nameTo = OLPreferenceUtil.getInstance(OneLotteryApplication
                                .getAppContext()).getTransferToUserId();
                        transferMsg.setContent(String.format(mContext.getString(R.string
                                        .message_transfer_message),
                                StringUtils.isEmpty(nameTo) ? StringUtils.getHeadTailString
                                        (addressTo)
                                        : StringUtils.getHeadTailString(nameTo),
                                ((float) OLPreferenceUtil.getInstance(OneLotteryApplication
                                        .getAppContext()).getTransferAmount()
                                        / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE)
                                        + ""));
                        // 转账失败的话，没有交易ID，所以也就不跳转详情了
                        transferMsg.setTitle(mContext.getString(R.string.transfer_fail));
                        transferMsg.setHornContent(transferMsg.getContent() + mContext.getString
                                (R.string.fail));
                        transferMsg.setType(ConstantCode.MessageType.MESSAGE_TYPE_TRANSFOR_FAIL);
                        transferMsg.setTime(new Date());
                        transferMsg.setIsRead(false);
                        transferMsg.setUserId(OneLotteryApi.getCurUserId());
                        transferMsg.setLotteryId(txid);
                        MessageNotifyDBHelper.getInstance().insertMessageNotify(transferMsg);
                    }

                }
            }).start();
            dismiss();
        } else if (mCurOperType == WITH_DRAW)
        {
            mContext.showWithdrawDialog(getContext());

            final CardAmountModel cardAmountModel = (CardAmountModel) mCurOperObject;

            OLLogger.d("withdraw", "CardAmountModel{" +
                    "bankCard=" + cardAmountModel.getBankCard() +
                    ", amount=" + cardAmountModel.getAmount() + '}');

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    final String txId = OneLotteryApi.oneChainZxCoinWithdraw(cardAmountModel
                                    .getBankCard()
                                    .getOpeningBank(),
                            cardAmountModel.getBankCard().getAccountName(),
                            cardAmountModel.getBankCard().getBankCardId(),
                            (long) (cardAmountModel.getAmount()), pwd);

                    if (StringUtils.isEmpty(txId))
                    {
                        final WithdrawDialog withdrawDialog = mContext.getWithdrawDialog();
                        mContext.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (withdrawDialog != null && withdrawDialog.isShowing())
                                {
                                    withdrawDialog.setWithdrawStatus(WithdrawDialog.FAIL);
                                } else if (withdrawDialog != null && !withdrawDialog.isShowing())
                                {
                                    WithdrawLogic.setMessageNotify(cardAmountModel.getAmount(),
                                            StringUtils.isEmpty(txId) ? UUID.randomUUID()
                                                    .toString() : txId,
                                            ConstantCode.MessageType
                                                    .MESSAGE_TYPE_WITH_DRAW_SEND_FAIL, "");
                                }
                            }
                        });
                    }
                }
            }).start();
            dismiss();
        } else if (mCurOperType == WITH_DRAW_APPEAL)
        {
            mContext.showWithdrawDialog(getContext());

            final WithdrawRecord record = (WithdrawRecord) mCurOperObject;

            OLLogger.d("appeal", "WithdrawRecord{" +
                    "txId='" + record.getTxId() + '\'' +
                    ", state=" + record.getState() +
                    ", openingBankName='" + record.getOpeningBankName() + '\'' +
                    ", accountName='" + record.getAccountName() + '\'' +
                    ", accountId='" + record.getAccountId() + '\'' +
                    ", userId='" + record.getUserId() + '\'' +
                    ", userHash='" + record.getUserHash() + '\'' +
                    ", amount=" + record.getAmount() +
                    ", remitOrderNumber='" + record.getRemitOrderNumber() + '\'' +
                    ", remark='" + record.getRemark() + '\'' +
                    ", createTime=" + record.getCreateTime() +
                    ", modifyTime=" + record.getModifyTime() +
                    '}');

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    String txId = OneLotteryApi.oneChainZxCoinWithdrawAppeal(record.getTxId(),
                            record.getRemark(), pwd);
                    if (StringUtils.isEmpty(txId))
                    {
                        final WithdrawDialog withdrawDialog = mContext.getWithdrawDialog();
                        mContext.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (withdrawDialog != null && withdrawDialog.isShowing())
                                {
                                    withdrawDialog.setWithdrawStatus(WithdrawDialog.FAIL);
                                } else if (withdrawDialog != null && !withdrawDialog.isShowing())
                                {
                                    WithdrawLogic.setMessageNotify(record.getAmount(), record
                                                    .getTxId(),
                                            ConstantCode.MessageType
                                                    .MESSAGE_TYPE_WITH_DRAW_APPEAL_SEND_FAIL, "");
                                }
                            }
                        });
                    }
                }
            }).start();
            dismiss();
        } else if (mCurOperType == WITH_DRAW_CONFIRM)
        {
            mContext.showWithdrawDialog(getContext());

            final WithdrawRecord record = (WithdrawRecord) mCurOperObject;

            OLLogger.d("appeal", "WithdrawRecord{" +
                    "txId='" + record.getTxId() + '\'' +
                    ", state=" + record.getState() +
                    ", openingBankName='" + record.getOpeningBankName() + '\'' +
                    ", accountName='" + record.getAccountName() + '\'' +
                    ", accountId='" + record.getAccountId() + '\'' +
                    ", userId='" + record.getUserId() + '\'' +
                    ", userHash='" + record.getUserHash() + '\'' +
                    ", amount=" + record.getAmount() +
                    ", remitOrderNumber='" + record.getRemitOrderNumber() + '\'' +
                    ", remark='" + record.getRemark() + '\'' +
                    ", createTime=" + record.getCreateTime() +
                    ", modifyTime=" + record.getModifyTime() +
                    '}');

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    String txId = OneLotteryApi.oneChainZxCoinWithdrawConfirm(record.getTxId(),
                            pwd);

                    if (StringUtils.isEmpty(txId))
                    {
                        final WithdrawDialog withdrawDialog = mContext.getWithdrawDialog();
                        if (withdrawDialog != null && withdrawDialog.isShowing())
                        {
                            mContext.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    withdrawDialog.setWithdrawStatus(WithdrawDialog.FAIL);
                                }
                            });
                        }
                    }
                }
            }).start();
            dismiss();
        } else if (mCurOperType == EXPORT_SD)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    SdBackRestoreLogic logic = new SdBackRestoreLogic();
                    boolean result = logic.processBackFile(pwd, 1);
                    OneLotteryManager.getInstance().SendEventBus(result, OLMessageModel
                            .STMSG_MODEL_EXPORT_SD_FILE);
                }
            }).start();
            dismiss();
        } else if (mCurOperType == IMPORT_SD)
        {
            final File file = (File) mCurOperObject;
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    SdBackRestoreLogic logic = new SdBackRestoreLogic();
                    boolean result = logic.processRestoreFile(pwd, file);
                    OneLotteryManager.getInstance().SendEventBus(result, OLMessageModel
                            .STMSG_MODEL_IMPORT_SD_FILE);
                }
            }).start();
            dismiss();
        }
    }

    private boolean checkFailWhenDialogHide(String txId, final int msgId)
    {
        if (StringUtils.isEmpty(txId))
        {
            return setWaitingDialog(msgId);
        }
        return false;
    }

    private boolean setWaitingDialog(final int msgId)
    {
        final WaitingDialog waitingDialog = mContext.getWaitingDialog();
        if (waitingDialog != null && waitingDialog.isShowing())
        {
            mContext.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    waitingDialog.setBtnText(mContext.getString(msgId));
                }
            });
            return false;
        }
        return true;
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();

        if (mCurOperType == DELETE_ACCOUNT && isPwdRight)
        {
            isPwdRight = false;
            OneLotteryManager.getInstance().SendEventBus(null, OLMessageModel
                    .STMSG_MODEL_SETTING_DELETE_ACCOUNT);
        } else if (mCurOperType == CHANGE_ACCOUNT && isPwdRight)
        {
            isPwdRight = false;
            OneLotteryManager.getInstance().SendEventBus(null, OLMessageModel
                    .STMSG_MODEL_SETTING_CHANGE_ACCOUNT);
        }
    }
}
