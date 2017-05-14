package com.peersafe.chainbet.logic;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.MessageNotify;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.db.WithdrawRecord;
import com.peersafe.chainbet.manager.dbhelper.MessageNotifyDBHelper;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.manager.dbhelper.WithdrawRecordDBHelper;
import com.peersafe.chainbet.model.LotteryJsonBean.AccountInfoRet;
import com.peersafe.chainbet.model.LotteryJsonBean.AppealDoneNotity;
import com.peersafe.chainbet.model.LotteryJsonBean.AppealRet;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryRet;
import com.peersafe.chainbet.model.LotteryJsonBean.WithdrawApplyNotify;
import com.peersafe.chainbet.model.LotteryJsonBean.WithdrawQueryArrayRet;
import com.peersafe.chainbet.model.LotteryJsonBean.WithdrawQueryRet;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.log.OLLogger;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import go.onechainmobile.Onechainmobile;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/4/10
 * DESCRIPTION :
 */

public class WithdrawLogic
{

    private static final String TAG = WithdrawLogic.class.getSimpleName();

    public static synchronized boolean queryWithdrawList()
    {
        String retMsg = Onechainmobile.oneChainZxCoinWithdrawQuery();
        OLLogger.d(TAG,"queryWithdrawList : " + retMsg);
        if(StringUtils.isEmpty(retMsg))
        {
            return false;
        }

        Gson gson = new Gson();
        try
        {
            WithdrawQueryArrayRet ret = gson.fromJson(retMsg, WithdrawQueryArrayRet.class);
            if(ret != null && ret.getCode() == OneLotteryApi.SUCCESS && ret.getData() != null)
            {
                List<WithdrawQueryArrayRet.DataBean> data = ret.getData();
                for (WithdrawQueryArrayRet.DataBean bean : data)
                {
                    //判断txId和status是否相同
                    WithdrawRecord record = WithdrawRecordDBHelper.getInstance().getRecordByKey(bean.getTxId());
                    if(record != null)
                    {
                        if(bean.getState() != record.getState())
                        {
                            queryWithdraw(bean.getTxId());
                        }
                    }else
                    {
                        queryWithdraw(bean.getTxId());
                    }
                }

                return true;
            }
        }catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return false;
    }

    /**
     * 提现回调接口是否成功
     *
     * @param retMsg
     * @param txId
     * @return
     */
    public static boolean oneChainWithdraw(String retMsg, String txId)
    {
        if (StringUtils.isEmpty(retMsg) || StringUtils.isEmpty(txId))
        {
            return false;
        }

        Gson gson = new Gson();
        try
        {
            OneLotteryRet ret = gson.fromJson(retMsg, OneLotteryRet.class);
            if (ret == null)
            {
                return false;
            }

            if (ret.getCode() == 0)
            {
                return true;
            }
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return false;
    }

    /**
     * 提现回调接口是否成功
     *
     * @param retMsg
     * @param txId
     * @return
     */
    public static WithdrawRecord oneChainWithdrawNotify(String retMsg, String txId)
    {
        if (StringUtils.isEmpty(retMsg) || StringUtils.isEmpty(txId))
        {
            return null;
        }

        UserInfo me = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (me == null)
        {
            return null;
        }

        Gson gson = new Gson();
        try
        {
            WithdrawApplyNotify ret = gson.fromJson(retMsg, WithdrawApplyNotify.class);
            if(ret != null && ret.getCode() == OneLotteryApi.SUCCESS && ret.getData() != null
                    && UserLogic.isLoginUser(ret.getData().getUserHash(), ret.getData().getUserName()))
            {
                WithdrawRecord record = WithdrawRecordDBHelper.getInstance().getRecordByKey(txId);
                if(record != null)
                {
                    record.setState(ConstantCode.WithdrawType.WITHDRAW_TYPE_APPLYING);
                    record.setModifyTime(new Date());
                    WithdrawRecordDBHelper.getInstance().insert(record);

                    return record;
                }
                else
                {
                    WithdrawRecord withdrawRecord = queryWithdraw(txId);
                    if(withdrawRecord != null)
                    {
                        return withdrawRecord;
                    }
                }
            }
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    /**
     * 查询query提现返回的结果
     *
     * @param txId
     * @return
     */
    public static WithdrawRecord queryWithdraw(final String txId)
    {
        if (StringUtils.isEmpty(txId))
        {
            return null;
        }
        String retMsg = Onechainmobile.oneChainZxCoinWithdrawInfoQuery(txId);

        if (StringUtils.isEmpty(retMsg))
        {
            return null;
        }

        Gson gson = new Gson();
        try
        {
            WithdrawQueryRet withdrawQueryRet = gson.fromJson(retMsg, WithdrawQueryRet.class);
            if (withdrawQueryRet != null && withdrawQueryRet.getCode() == OneLotteryApi.SUCCESS && withdrawQueryRet.getData() != null)
            {
                AccountInfoRet account = gson.fromJson(withdrawQueryRet.getData().getAccountInfo
                        (), AccountInfoRet.class);
                if (account != null)
                {
                    WithdrawRecord record = new WithdrawRecord();

                    record.setUserId(withdrawQueryRet.getData().getUserName());
                    record.setCreateTime(withdrawQueryRet.getData().getCreateTime() > 0
                            ? new Date(withdrawQueryRet.getData().getCreateTime()) : new Date());
                    record.setUserHash(withdrawQueryRet.getData().getUserHash());
                    record.setAmount(withdrawQueryRet.getData().getAmount());
                    record.setTxId(withdrawQueryRet.getData().getTxId());
                    record.setState(withdrawQueryRet.getData().getState());
                    record.setModifyTime(withdrawQueryRet.getData().getModifyTime() > 0
                            ? new Date(withdrawQueryRet.getData().getModifyTime()) : new Date());
                    record.setOpeningBankName(account.getBankName());
                    record.setAccountName(account.getAccountName());
                    record.setAccountId(account.getAccountId());
                    record.setRemitOrderNumber(withdrawQueryRet.getData().getRemitOrderNumber());
                    record.setRemark(withdrawQueryRet.getData().getRemark());

                    WithdrawRecordDBHelper.getInstance().insert(record);

                    return record;
                }
            }
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * 申诉返回
     * @param s
     */
    public static WithdrawRecord oneChainWithdrawAppeal(String s)
    {
        if(StringUtils.isEmpty(s))
        {
            return null;
        }

        Gson gson = new Gson();
        try
        {
            AppealRet ret = gson.fromJson(s,AppealRet.class);
            if(ret != null && ret.getCode() == OneLotteryApi.SUCCESS && ret.getData() != null)
            {
                String txId = ret.getData().toString();
                WithdrawRecord record = WithdrawRecordDBHelper.getInstance().getRecordByKey(txId);
                if(record != null)
                {
                    record.setState(ConstantCode.WithdrawType.WITHDRAW_TYPE_APPEAL);
                    record.setModifyTime(new Date());
                    WithdrawRecordDBHelper.getInstance().insert(record);

                    return record;
                }
                else
                {
                    WithdrawRecord withdrawRecord = queryWithdraw(ret.getData());
                    if(withdrawRecord != null)
                    {
                        return withdrawRecord;
                    }
                }
            }
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    /**
     * 获取体现退款
     *
     * @param s
     */
    public static WithdrawRecord oneChainRemitSuccessNotify(String s)
    {
        if (StringUtils.isEmpty(s))
        {
            return null;
        }

        Gson gson = new Gson();
        try
        {
            WithdrawQueryRet ret = gson.fromJson(s, WithdrawQueryRet.class);
            if (ret != null && ret.getCode() == OneLotteryApi.SUCCESS && ret.getData() != null)
            {
                WithdrawRecord record = WithdrawRecordDBHelper.getInstance().getRecordByKey(ret.getData().getTxId());
                if(record != null)
                {
                    record.setTxId(ret.getData().getTxId());
                    record.setRemitOrderNumber(ret.getData().getRemitOrderNumber());
                    record.setModifyTime(new Date(ret.getData().getModifyTime()));
                    record.setUserId(ret.getData().getUserName());
                    record.setState(ConstantCode.WithdrawType.WITHDRAW_TYPE_PAY);
                    record.setUserHash(ret.getData().getUserHash());
                    WithdrawRecordDBHelper.getInstance().insert(record);
                    return record;
                }//如果获取失败，联网请求(等sdk修改完成后再放开)
                else
                {
                    WithdrawRecord withdrawRecord = queryWithdraw(ret.getData().getTxId());
                    if(withdrawRecord != null)
                    {
                        return withdrawRecord;
                    }
                }
            }
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    /**
     * 提现失败的通知
     * @param s
     */
    public static WithdrawRecord oneChainWithdrawFailNotify(String s)
    {
        if(StringUtils.isEmpty(s))
        {
            return null;
        }

        Gson gson = new Gson();
        try
        {
            WithdrawQueryRet ret = gson.fromJson(s,WithdrawQueryRet.class);
            if(ret != null && ret.getCode() == OneLotteryApi.SUCCESS && ret.getData() != null)
            {
                WithdrawRecord record = WithdrawRecordDBHelper.getInstance().getRecordByKey(ret.getData().getTxId());
                if(record != null)
                {
                    record.setRemark(ret.getData().getRemark());
                    record.setTxId(ret.getData().getTxId());
                    record.setModifyTime(new Date(ret.getData().getModifyTime()));
                    record.setUserId(ret.getData().getUserName());
                    record.setState(ConstantCode.WithdrawType.WITHDRAW_TYPE_FAIL);
                    record.setUserHash(ret.getData().getUserHash());
                    WithdrawRecordDBHelper.getInstance().insert(record);
                    return record;
                } else
                {
                    WithdrawRecord withdrawRecord = queryWithdraw(ret.getData().getTxId());
                    if(withdrawRecord != null)
                    {
                        return withdrawRecord;
                    }
                }
            }
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    public static void setMessageNotify(double amount,String txId,int type,String reason)
    {
        String userId = OneLotteryApi.getCurUserId();
        MessageNotify messageNotify = new MessageNotify();
        messageNotify.setUserId(userId);
        messageNotify.setMsgId(UUID.randomUUID().toString());
        messageNotify.setNewTxId(txId);
        messageNotify.setIsRead(false);
        messageNotify.setTime(new Date());
        messageNotify.setType(type);

        double am = ((double) amount / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE);
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String zxCorn = StringUtils.refactorLotteryName(decimalFormat.format(am));

        Context mContext = OneLotteryApplication.getAppContext();

        switch (type)
        {
            case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_SEND_SUCCESS:
                messageNotify.setTitle(mContext.getString(R.string.message_withdraw_send_sucess_title));
                messageNotify.setContent(zxCorn + mContext.getString(R.string.message_withdraw_send_sucess_content));
                messageNotify.setHornContent(zxCorn + mContext.getString(R.string.message_withdraw_send_sucess_horn));
                break;
            case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_SEND_FAIL:
                messageNotify.setTitle(mContext.getString(R.string.message_withdraw_send_fail_title));
                messageNotify.setContent(zxCorn + mContext.getString(R.string.message_withdraw_send_fail_content));
                messageNotify.setHornContent(zxCorn + mContext.getString(R.string.message_withdraw_send_fail_content));
                break;
            case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_APPEAL_SEND_SUCCESS:
                messageNotify.setTitle(mContext.getString(R.string.message_appeal_send_success_title));
                messageNotify.setContent(zxCorn + mContext.getString(R.string.message_appeal_send_success_content));
                messageNotify.setHornContent(zxCorn + mContext.getString(R.string.message_appeal_send_success_horn));
                break;
            case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_APPEAL_SEND_FAIL:
                messageNotify.setTitle(mContext.getString(R.string.message_appeal_send_fail_title));
                messageNotify.setContent(zxCorn + mContext.getString(R.string.message_appeal_send_fail_content));
                messageNotify.setHornContent(zxCorn + mContext.getString(R.string.message_appeal_send_fail_horn));
                break;
            case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_SUCCESS:
                messageNotify.setTitle(mContext.getString(R.string.message_withdraw_success_title));
                messageNotify.setContent(zxCorn + mContext.getString(R.string.message_withdraw_success_content));
                messageNotify.setHornContent(zxCorn + mContext.getString(R.string.message_withdraw_success_content));
                break;
            case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_FAIL:
                messageNotify.setTitle(mContext.getString(R.string.message_withdraw_fail_title));
                messageNotify.setContent(mContext.getString(R.string.message_withdraw_fail_content) + reason);
                messageNotify.setHornContent(zxCorn + mContext.getString(R.string.message_withdraw_fail_horn));
                break;
        }

        MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify);
    }

    /**
     * 申诉处理接口
     * @param s
     */
    public static WithdrawRecord oneChainAppealDoneNotify(String s)
    {
        if(StringUtils.isEmpty(s))
        {
            return null;
        }

        Gson gson = new Gson();
        try
        {
            AppealDoneNotity ret = gson.fromJson(s,AppealDoneNotity.class);
            if(ret != null && ret.getCode() == OneLotteryApi.SUCCESS && ret.getData() != null)
            {
                WithdrawRecord record = WithdrawRecordDBHelper.getInstance().getRecordByKey(ret.getData().getTxId());
                if(record != null)
                {
                    record.setModifyTime(new Date(ret.getData().getModifyTime()));
                    record.setRemark(ret.getData().getRemark());
                    //result = 1 代表被驳回
                    if(ret.getData().getResult() == ConstantCode.AppealType.APPEAL_REFUSE)
                    {
                        record.setState(ConstantCode.WithdrawType.WITHDRAW_TYPE_CONFIRM);
                    }
                    //result = 2 代表接受申诉
                    else if(ret.getData().getResult() == ConstantCode.AppealType.APPEAL_ACCEPT)
                    {
                        record.setState(ConstantCode.WithdrawType.WITHDRAW_TYPE_FAIL);
                        record.setRemark(ret.getData().getRemark());
                    }
                    WithdrawRecordDBHelper.getInstance().insert(record);
                    return record;
                }
                else
                {
                    WithdrawRecord withdrawRecord = queryWithdraw(ret.getData().getTxId());
                    if(withdrawRecord != null)
                    {
                        return withdrawRecord;
                    }
                }
            }
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    /**
     * 确认提现
     * @param s
     */
    public static WithdrawRecord oneChainWithdrawConfrim(String s)
    {
        if(StringUtils.isEmpty(s))
        {
            return null;
        }
        Gson gson = new Gson();
        try
        {
            WithdrawQueryRet ret = gson.fromJson(s,WithdrawQueryRet.class);
            if(ret != null && ret.getCode() == OneLotteryApi.SUCCESS && ret.getData() != null)
            {
                WithdrawRecord record = WithdrawRecordDBHelper.getInstance().getRecordByKey(ret.getData().getTxId());
                if(record != null)
                {
                    record.setState(ConstantCode.WithdrawType.WITHDRAW_TYPE_CONFIRM);
                    record.setModifyTime(new Date());
                    WithdrawRecordDBHelper.getInstance().insert(record);

                    return record;
                }
                else
                {
                    WithdrawRecord withdrawRecord = queryWithdraw(ret.getData().getTxId());
                    if(withdrawRecord != null)
                    {
                        return withdrawRecord;
                    }
                }
            }
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }
}
