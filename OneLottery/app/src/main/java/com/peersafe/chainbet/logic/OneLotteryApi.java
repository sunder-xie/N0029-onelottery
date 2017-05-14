package com.peersafe.chainbet.logic;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.peersafe.chainbet.LoginActivity;
import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.RegisterActivity;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.OneLotteryBet;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryBetDBHelper;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.LotteryJsonBean.AccountInfoRet;
import com.peersafe.chainbet.model.LotteryJsonBean.ConsensusOverRet;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryBetDetailRet;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryBetOverNotify;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryDeleteRet;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryDetailRet;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryListRet;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryOldHistoryQueryRet;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryQueryRet;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryRefundNotify;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryRet;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryRewardOverNotify;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryStartNotify;
import com.peersafe.chainbet.model.LotteryJsonBean.PrizeRuleListRet;
import com.peersafe.chainbet.model.LotteryJsonBean.TicketNumbersRet;
import com.peersafe.chainbet.model.LotteryJsonBean.TransferNotify;
import com.peersafe.chainbet.model.LotteryJsonBean.TxDetailBetOverRet;
import com.peersafe.chainbet.model.LotteryJsonBean.TxDetailBetRet;
import com.peersafe.chainbet.model.LotteryJsonBean.TxDetailLotteryAddRet;
import com.peersafe.chainbet.model.LotteryJsonBean.TxDetailLotteryModifyRet;
import com.peersafe.chainbet.model.LotteryJsonBean.TxDetailRefundRet;
import com.peersafe.chainbet.model.LotteryJsonBean.TxDetailRet;
import com.peersafe.chainbet.model.LotteryJsonBean.TxDetailTransferRet;
import com.peersafe.chainbet.model.LotteryJsonBean.TxDetailWithdrawAppealDoneRet;
import com.peersafe.chainbet.model.LotteryJsonBean.TxDetailWithdrawConfirmRet;
import com.peersafe.chainbet.model.LotteryJsonBean.TxDetailWithdrawFailRet;
import com.peersafe.chainbet.model.LotteryJsonBean.TxDetailWithdrawRet;
import com.peersafe.chainbet.model.LotteryJsonBean.ZXCoinBalanceRet;
import com.peersafe.chainbet.model.LotteryJsonBean.ZXUserInfoRet;
import com.peersafe.chainbet.ui.lottery.CreateLotteryActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.config.OLPreferenceUtil;
import com.peersafe.chainbet.utils.log.OLLogger;
import com.peersafe.chainbet.widget.InputPwdDialog;

import java.util.List;

import go.onechainmobile.Onechainmobile;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.logic
 * @description:
 * @date 18/11/16 PM3:25
 */
public class OneLotteryApi
{
    public static final String TAG = "OneLotteryApi";

    public static final long DEPLOY_SECURE_NOT_LOGIN = -3L;
    public static final long ENROLL_ALREADY_EXIST = 2L;
    public static final long INTERNAL_ERR = -2L;
    public static final long PARA_ERR = -1L;
    public static final long REGISTR_ALREADY_EXIST = 1L;
    public static final long SUCCESS = 0L;
    public static final int NETWORK_ERR = -8;

    public static final String RES_SEP = "&X&";

    public static final String RuleType_Prize = "PrizeRule";
    public static final String RuleType_Ball = "BallRule";

    /**
     * 一元夺宝初始化接口
     *
     * @return
     */
    public static long init()
    {
        String lotteryStorePath = OneLotteryApplication.getAppContext().getFilesDir()
                .getAbsolutePath() + "/onelottery/";
        return Onechainmobile.oneChainInit(lotteryStorePath, lotteryStorePath);
    }

    /**
     * 启动daemon,需要在一个单独的线程中启动
     *
     * @return
     */
    public static long daemon()
    {
        return Onechainmobile.oneChainDaemon(OneLotteryManager.getInstance());
    }

    /**
     * 注册并且登记用户
     *
     * @param userId 需要注册和登记的用户名
     * @param pwd    需要注册和登记的用户的密码
     * @return
     */
    public static long registerAndEnroll(String userId, String pwd)
    {
        if (StringUtils.isEmpty(userId))
        {
            OLLogger.d(TAG, "registerAndEnroll,param error");
            return PARA_ERR;
        }
        return Onechainmobile.oneChainRegisterAndEnroll(userId, "institutions", pwd);
    }

    /**
     * 用户登录
     *
     * @param userId
     * @param pwd
     * @return
     */
    public static long login(String userId, String pwd)
    {
        if (StringUtils.isEmpty(userId))
        {
            OLLogger.d(TAG, "login, param error");
            return PARA_ERR;
        }
        return Onechainmobile.oneChainUserLogin(userId, pwd);
    }

    /**
     * 获取用户的公钥hash
     *
     * @param userId
     * @return
     */
    public static String getPubkeyHash(String userId)
    {
        if (StringUtils.isEmpty(userId))
        {
            OLLogger.d(TAG, "getPubkeyHash,param error");
            return "";
        }

        String retMsg = Onechainmobile.oneChainGetPubkeyHash(userId);
        OLLogger.d(TAG, "getPubkeyHash,retMsg is:" + retMsg);

        String[] results = retMsg.split(RES_SEP);
        String pubkeyHash = "";
        if (results.length == 2)
        {
            if (Integer.parseInt(results[0]) >= SUCCESS)
            {
                pubkeyHash = results[1];
            }
        }

        return pubkeyHash;
    }

    /**
     * 根据用户nameId和用户hash获取用户的信息
     *
     * @param userName
     * @param userAddress
     * @return
     * @sunhaitao
     */
    public static ZXUserInfoRet getUserInfo(String userName, String userAddress)
    {
        if (StringUtils.isEmpty(userName) && StringUtils.isEmpty(userAddress))
        {
            OLLogger.d(TAG, "getUserByName opsName, param error");
            return null;
        }


        String retMsg = Onechainmobile.oneChainZxCoinGetUserInfo(userName, userAddress);
        OLLogger.i(TAG, "getUserInfo:" + retMsg);// 增加return值 eg:-8&X&{code***}
        ZXUserInfoRet zxUserInfoRet = null;


        if (!StringUtils.isEmpty(retMsg))
        {
            String[] results = retMsg.split(RES_SEP);
            if (results.length == 2)
            {
                if (Integer.parseInt(results[0]) == NETWORK_ERR)
                {
                    zxUserInfoRet = new ZXUserInfoRet();
                    zxUserInfoRet.setCode(NETWORK_ERR);
                } else if (Integer.parseInt(results[0]) == SUCCESS)
                {
                    Gson gson = new Gson();
                    try
                    {
                        zxUserInfoRet = gson.fromJson(results[1], ZXUserInfoRet.class);
                    }
                    catch (JsonSyntaxException exception)
                    {
                        exception.printStackTrace();
                    }
                }
            }
        }
        return zxUserInfoRet;
    }

    /**
     * 转账给他人
     *
     * @param nameTo    转账的目的用户的名字
     * @param addressTo 转换的目的用户的公钥hash
     * @param amount    转账金额，实际金额*10000
     * @param remark    转账备注
     * @param time      转账时间，距1970.1.1后的秒数
     * @param pwd       用户的登录密码
     * @return
     */
    public static String transferAccount(String nameTo, String addressTo, long amount, String
            remark, long time, String pwd)
    {
        if ((StringUtils.isEmpty(nameTo) && StringUtils.isEmpty(addressTo)) || amount <= 0 ||
                time <= 0)
        {
            OLLogger.d(TAG, "transferAccount,param error");
            return "";
        }

        String retMsg = Onechainmobile.oneChainTransferAccount(nameTo, addressTo, amount, 0,
                remark, time, pwd);
        OLLogger.d(TAG, "transMSG=" + retMsg);


        String[] results = retMsg.split(RES_SEP);
        String txId = "";
        if (results.length == 2)
        {
            if (Integer.parseInt(results[0]) == SUCCESS)
            {
                txId = results[1];
            }
        }

        return txId;
    }

    /**
     * 接收到官方或别人的转账后处理明细和消息
     *
     * @param retMsg
     * @return
     */
    public static TransferNotify transferNotify(String retMsg)
    {
        if (StringUtils.isEmpty(retMsg))
        {
            return null;
        }
        TransferNotify transferNotify = null;
        Gson gson = new Gson();
        try
        {
            transferNotify = gson.fromJson(retMsg, TransferNotify.class);
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }
        return transferNotify;
    }

    /**
     * 查询众享币余额
     *
     * @return 查找成功则返回json对应的结构，否则返回空
     */
    public static ZXCoinBalanceRet queryZxCoinBalance()
    {
        String retMsg = Onechainmobile.oneChainBalanceQuery();
        OLLogger.d(TAG, "queryZxCoinBalance,retMsg is:" + retMsg);

        ZXCoinBalanceRet zxCoinBalanceRet = null;
        Gson gson = new Gson();
        try
        {
            zxCoinBalanceRet = gson.fromJson(retMsg, ZXCoinBalanceRet.class);
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return zxCoinBalanceRet;
    }

    /**
     * 查询活动规则列表
     *
     * @return 查找成功则返回json对应的结构，否则返回空
     */
    public static PrizeRuleListRet prizeRuleQuery()
    {
        String retMsg = Onechainmobile.oneChainPrizeRuleQuery();
        OLLogger.d(TAG, "prizeRuleQuery,retMsg is:" + retMsg);

        PrizeRuleListRet prizeRuleListRet = null;
        Gson gson = new Gson();
        try
        {
            prizeRuleListRet = gson.fromJson(retMsg, PrizeRuleListRet.class);
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return prizeRuleListRet;
    }

    /**
     * 添加一元夺宝活动
     *
     * @param lotteryName    活动的名称
     * @param ruleType       规则类型. "PrizeRule":prize rule "BallRule":ball rule 目前只有prizeRule
     * @param ruleId         规则id
     * @param pictureIndex   活动使用的图片索引
     * @param createTime
     * @param startTime
     * @param closeTime
     * @param minAttendeeCnt 最小参与人数
     * @param maxAttendeeCnt 0:no limit
     * @param cost           每次投注花费 实际金额*10000
     * @param total          投注总募集金额，为maxAttendeeCnt*cost
     * @param description
     * @param pwd            用户的登录密码
     * @return 查找成功则返回json对应的结构，否则返回空
     */
    public static String oneLotteryAdd(String lotteryName, String ruleType,
                                       String ruleId, int pictureIndex, long createTime, long startTime,
                                       long closeTime, int minAttendeeCnt, int maxAttendeeCnt,
                                       int cost, long total, String description, String pwd)
    {
        if (StringUtils.isEmpty(lotteryName) ||
                (ruleType != RuleType_Prize && ruleType != RuleType_Ball) ||
                StringUtils.isEmpty(ruleId) || createTime == 0 || startTime == 0 || closeTime ==
                0 ||
                minAttendeeCnt == 0 || cost == 0 || minAttendeeCnt > maxAttendeeCnt
                || createTime > closeTime || startTime > closeTime ||
                ((long) cost * maxAttendeeCnt) != total)
        {
            OLLogger.d(TAG, "oneLotteryAdd,param error");
            return "";
        }

        OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).setAddLotteryName(lotteryName);

        String retMsg = Onechainmobile.oneChainOneLotteryAdd(lotteryName, ruleType, ruleId,
                pictureIndex,
                createTime, startTime, closeTime,
                minAttendeeCnt, maxAttendeeCnt,
                cost, total, description, pwd);

        String[] results = retMsg.split(RES_SEP);
        String txId = "";
        if (results.length == 2)
        {
            if (Integer.parseInt(results[0]) == SUCCESS)
            {
                txId = results[1];
            }
        }

        if (StringUtils.isEmpty(txId))
        {
            OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).setAddLotteryName("");
        }

        return txId;
    }

    /**
     * 修改一元夺宝活动
     *
     * @param lotteryId      活动id
     * @param lotteryName    活动的名称
     * @param ruleType       规则类型. "1":prize rule "2":ball rule
     * @param ruleId         规则id
     * @param pictureIndex   活动使用的图片索引
     * @param updateTime
     * @param startTime
     * @param closeTime
     * @param minAttendeeCnt
     * @param maxAttendeeCnt 0:no limit
     * @param cost           每次投注花费 实际金额*10000
     * @param total          投注总募集金额，为maxAttendeeCnt*cost
     * @param description
     * @param pwd            用户的登录密码
     * @return txId
     */
    public static String oneLotteryModify(String lotteryId, String lotteryName,
                                          String ruleType, String ruleId, int pictureIndex, long
                                                  updateTime,
                                          long startTime, long closeTime, int minAttendeeCnt, int
                                                  maxAttendeeCnt,
                                          int cost, long total, String description, String pwd)
    {
        if (StringUtils.isEmpty(lotteryId) || StringUtils.isEmpty(lotteryName) ||
                (ruleType != RuleType_Prize && ruleType != RuleType_Ball) ||
                StringUtils.isEmpty(ruleId) || updateTime == 0 || startTime == 0 || closeTime ==
                0 ||
                minAttendeeCnt == 0 || cost == 0 || minAttendeeCnt > maxAttendeeCnt
                || updateTime > closeTime || startTime > closeTime ||
                ((long) cost * maxAttendeeCnt) != total)
        {
            OLLogger.d(TAG, "oneLotteryModify,param error");
            return "";
        }

        String retMsg = Onechainmobile.oneChainOneLotteryModify(lotteryId, lotteryName,
                ruleType, ruleId, pictureIndex, updateTime, startTime,
                closeTime, minAttendeeCnt, maxAttendeeCnt,
                cost, total, description, pwd);

        String[] results = retMsg.split(RES_SEP);
        String txId = "";
        if (results.length == 2)
        {
            if (Integer.parseInt(results[0]) == SUCCESS)
            {
                txId = results[1];
            }
        }

        return txId;
    }

    /**
     * 删除一元夺宝活动
     *
     * @param lotteryId 活动id
     * @param pwd       用户的登录密码
     * @return txId
     */
    public static String oneLotteryDelete(String lotteryId, String pwd)
    {
        if (StringUtils.isEmpty(lotteryId))
        {
            OLLogger.d(TAG, "oneLotteryDelete,param error");
            return "";
        }

        String retMsg = Onechainmobile.oneChainOneLotteryDelete(lotteryId, pwd);

        String[] results = retMsg.split(RES_SEP);
        String txId = "";
        if (results.length == 2)
        {
            if (Integer.parseInt(results[0]) == SUCCESS)
            {
                txId = results[1];
            }
        }

        return txId;
    }

    /**
     * 查询活动列表或详情
     *
     * @param lotteryId
     * @return
     */
    public static OneLotteryQueryRet oneLotteryQuery(String lotteryId)
    {
        String retMsg = Onechainmobile.oneChainOneLotteryQuery(lotteryId);
        OLLogger.e("QUERYLOTTERY", "queryLotteryList,retMsg is:" + retMsg);
        OneLotteryQueryRet oneLotteryQueryRet = null;

        Gson gson = new Gson();
        try
        {
            if (StringUtils.isEmpty(lotteryId))
            {
                oneLotteryQueryRet = gson.fromJson(retMsg, OneLotteryListRet.class);
            } else
            {
                oneLotteryQueryRet = gson.fromJson(retMsg, OneLotteryDetailRet.class);
            }
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return oneLotteryQueryRet;
    }

    /**
     * 查询历史活动列表或详情
     *
     * @param lotteryId
     * @return
     */
    public static OneLotteryQueryRet oneLotteryHistoryQuery(String lotteryId)
    {
        String retMsg = Onechainmobile.oneChainOneLotteryHistoryQuery(lotteryId);
        OLLogger.e("QUERYLOTTERY", "oneLotteryHistoryQuery,retMsg is:" + retMsg);
        OneLotteryQueryRet oneLotteryQueryRet = null;

        Gson gson = new Gson();
        try
        {
            if (StringUtils.isEmpty(lotteryId))
            {
                oneLotteryQueryRet = gson.fromJson(retMsg, OneLotteryListRet.class);
            } else
            {
                oneLotteryQueryRet = gson.fromJson(retMsg, OneLotteryDetailRet.class);
            }
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return oneLotteryQueryRet;
    }

    /**
     * 查询不在7天内历史活动列表或详情
     *
     * @param lotteryId
     * @return
     */
    public static OneLotteryOldHistoryQueryRet oneLotteryOldHistoryInfoQuery(String lotteryId)
    {
        String retMsg = Onechainmobile.oneChainOneLotteryOldHistoryQuery(lotteryId);
        OLLogger.d(TAG, "oneLotteryOldHistoryInfoQuery,retMsg is:" + retMsg);
        OneLotteryOldHistoryQueryRet oldHistoryQueryRet = null;

        Gson gson = new Gson();
        try
        {
            oldHistoryQueryRet = gson.fromJson(retMsg, OneLotteryOldHistoryQueryRet.class);
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return oldHistoryQueryRet;
    }


    /**
     * 查询区块内详情
     *
     * @param blockID  区块id
     * @param lotteryId  活动id
     * @return
     */
    public static OneLotteryOldHistoryQueryRet.DataBean oneChainGetLotteryStateByBlockID(long blockID, String lotteryId)
    {
        String retMsg = Onechainmobile.oneChainGetLotteryStateByBlockID(blockID, lotteryId);
        OLLogger.d(TAG, "oneChainGetLotteryStateByBlockID ,retMsg is:" + retMsg);
        /**
         * oneChainGetLotteryStateByBlockID ,retMsg is:0&X&{"txnID":"9562a5b6-95b8-4892-9a08-22ea8821b35d","newTxnID":"","version":3,
         * "lastCloseTime":0,"numbers":"","balance":30000,"prizeTxnID":"","countTotal":3,"pictureIndex":3,"status":1,
         * "updateTime":1487734701,"blockHeight":334,"preBlockHeight":333,"txnIDs":" d4a5845a-26a1-4089-953b-b3ae18fe46d9","createTime":1487204101}
         */
        OneLotteryOldHistoryQueryRet.DataBean oldHistoryData = null;

        String[] results = retMsg.split(RES_SEP);
        if (results.length == 2)
        {
            if (Integer.parseInt(results[0]) == SUCCESS)
            {
                Gson gson = new Gson();
                try
                {
                    oldHistoryData = gson.fromJson(results[1], OneLotteryOldHistoryQueryRet.DataBean.class);
                }
                catch (JsonSyntaxException exception)
                {
                    exception.printStackTrace();
                }
            }
        }

        return oldHistoryData;
    }

    /**
     * 一元夺宝活动投注
     *
     * @param amount     投注花费 实际金额*10000
     * @param lotteryId  活动id
     * @param count      投注数
     * @param createTime 投注时间
     * @param pwd        用户的登录密码
     * @return txId
     */
    public static String oneLotteryBet(long amount, String lotteryId, int count, long createTime,
                                       String pwd)
    {
        if (amount == 0 || StringUtils.isEmpty(lotteryId) || count == 0 || createTime == 0)
        {
            OLLogger.d(TAG, "oneLotteryBet,param error amount=" + amount
                    + ", id=" + lotteryId
                    + ", count=" + count
                    + ", time=" + createTime
            );
            return "";
        }

        String retMsg = Onechainmobile.oneChainOneLotteryBet(amount, lotteryId, count,
                createTime, pwd);
        OLLogger.i(TAG, "oneLotteryBet retmsg=" + retMsg);

        String[] results = retMsg.split(RES_SEP);
        String txId = "";
        if (results.length == 2)
        {
            if (Integer.parseInt(results[0]) == SUCCESS)
            {
                txId = results[1];
            }
        }

        return txId;
    }

    /**
     * 查询一元夺宝活动投注列表
     *
     * @param lotteryId 活动的id
     * @return 查找成功则返回json对应的结构，否则返回空
     */
    public static OneLotteryBetDetailRet oneLotteryBetQuery(String lotteryId)
    {
        if (StringUtils.isEmpty(lotteryId))
        {
            OLLogger.d(TAG, "oneLotteryBetQuery,param error");
            return null;
        }

        String retMsg = Onechainmobile.oneChainOneLotteryBetQuery(null, lotteryId);
        OLLogger.d(TAG, "oneLotteryBetQuery,retMsg is:" + retMsg);

        OneLotteryBetDetailRet oneLotteryBetDetailRet = null;
        Gson gson = new Gson();
        try
        {
            oneLotteryBetDetailRet = gson.fromJson(retMsg, OneLotteryBetDetailRet.class);
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return oneLotteryBetDetailRet;
    }

    /**
     * 查询一元夺宝活动投注的交易详情
     *
     * @param ticketId 投注的交易id
     * @return 查找成功则返回json对应的结构，否则返回空
     */
    public static TxDetailBetRet oneLotteryBetDetailQuery(String ticketId)
    {
        if (StringUtils.isEmpty(ticketId))
        {
            OLLogger.d(TAG, "oneLotteryBetDetailQuery,param error");
            return null;
        }

        // TODO 后台还没有提供获取交易详情接口
        String retMsg = Onechainmobile.oneChainOneLotteryBetQuery(null, ticketId);
        OLLogger.d(TAG, "oneLotteryBetDetailQuery,retMsg is:" + retMsg);

        TxDetailBetRet oneLotteryTxnDetailRet = null;
        Gson gson = new Gson();
        try
        {
            oneLotteryTxnDetailRet = gson.fromJson(retMsg, TxDetailBetRet.class);
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return oneLotteryTxnDetailRet;
    }

    /**
     * 查询交易详情
     *
     * @param txId 交易id
     * @return 查找成功则返回json对应的结构，否则返回空
     */
    public static TxDetailRet getTxInfoByTxId(final String txId)
    {
        if (StringUtils.isEmpty(txId))
        {
            OLLogger.d(TAG, "getTxInfoByTxId,param error");
            return null;
        }

        String retMsg = Onechainmobile.oneChainGetTxInfoByTxId(txId);
        OLLogger.i(TAG, "getTxInfoByTxId retMsg=" + retMsg);
        //eg: getTxInfoByTxId retMsg=oneLotteryBet&X&aa5556a323b6b4e54ba2da3d516c280c1272043f09ab078450c8917e&X&{"lotteryID":"e90f2239-03f3-412c-a771-99afedb4aeee","amount":250000,"count":5,"userID":"xm02","CreateTime":1487419259}
        //eg: getTxInfoByTxId retMsg=oneLotteryRefund&X&f87d1460e96b9ed5c1c0239327f4dd43910acc6cce309708217afc37&X&{"lotteryID":"0dee93ca-4ba0-475e-b372-c3e6d873ed2d","currentTime":1492687680}
        //eg: zxCoinWithdrawAppealDone&X&da3db62271b0a9e3ee70d2ad627f895d9231e6be4e5645236664fa00&X&{"TxId":"fcd053b5-c040-4145-a85d-b368f5203ba3","Result":1,"Remark":"\na\nb\n\nc\nd","UserId":"one_chain_admin","Extras":"admin","ModifyTime":1492503839617}
        //eg: zxCoinWithdraw&X&16aad46555a4e655d0faa77520425b88070899180073d8b13b2c0f8d&X&{"AccountInfo":"{\"BankName\":\"北京银行\",\"AccountName\":\"大圣\",\"AccountId\":\"6225 1425 3769 8054\"}","UserId":"tuv","Amount":2000,"ModifyTime":1492503760497}
        //eg: zxCoinWithdrawConfirm&X&16aad46555a4e655d0faa77520425b88070899180073d8b13b2c0f8d&X&{"TxId":"95982dd9-70e5-4e54-8980-e8c2f6ed09d5","ModifyTime":1492503733198,"Extras":""}
        //eg: zxCoinWithdrawFail&X&da3db62271b0a9e3ee70d2ad627f895d9231e6be4e5645236664fa00&X&{"TxId":"37877497-56ce-47ca-a715-1a4846be82e6","Remark":"拒绝给你退款��","UserId":"one_chain_admin","Extras":"admin","ModifyTime":1492500105800}

        if (StringUtils.isEmpty(retMsg))
        {
            return null;
        }

        String[] split = retMsg.split(RES_SEP);
        String type = "";
        if (split.length == 3)
        {
            retMsg = split[2];
            type = split[0];
        } else {
            return null;
        }

        TxDetailRet txDetailRet = null;
        Gson gson = new Gson();
        try
        {
            switch (type)
            {
                case TxDetailRet.TYPE_TRANSFER_DETAIL_LOTTERY_ADD:
                    txDetailRet = gson.fromJson(retMsg, TxDetailLotteryAddRet.class);
                    break;
                case TxDetailRet.TYPE_TRANSFER_DETAIL_LOTTERY_EDT:
                    txDetailRet = gson.fromJson(retMsg, TxDetailLotteryModifyRet.class);
                    break;
                case TxDetailRet.TYPE_TRANSFER_DETAIL_BET:
                    txDetailRet = gson.fromJson(retMsg, TxDetailBetRet.class);
                    break;
                case TxDetailRet.TYPE_TRANSFER_DETAIL_COINTRANSFER:
                    txDetailRet = gson.fromJson(retMsg, TxDetailTransferRet.class);
                    break;
                case TxDetailRet.TYPE_TRANSFER_DETAIL_BET_OVER:
                    txDetailRet = gson.fromJson(retMsg, TxDetailBetOverRet.class);
                    break;
                case TxDetailRet.TYPE_TRANSFER_DETAIL_REFUND:
                    txDetailRet = gson.fromJson(retMsg, TxDetailRefundRet.class);
                    break;
                case TxDetailRet.TYPE_TRANSFER_DETAIL_WITHDRAW:
                    txDetailRet = gson.fromJson(retMsg, TxDetailWithdrawRet.class);
                    AccountInfoRet accountInfoRet = gson.fromJson(((TxDetailWithdrawRet)txDetailRet).getAccountInfo(), AccountInfoRet.class);
                    ((TxDetailWithdrawRet)txDetailRet).setAccountInfoRet(accountInfoRet);
                    txDetailRet.setTxid(txId);
                    break;
                case TxDetailRet.TYPE_TRANSFER_DETAIL_WITHDRAW_APPEALDONE:
                    txDetailRet = gson.fromJson(retMsg, TxDetailWithdrawAppealDoneRet.class);
                    txDetailRet.setTxid(txId);
                    break;
                case TxDetailRet.TYPE_TRANSFER_DETAIL_WITHDRAW_CONFIRM:
                    txDetailRet = gson.fromJson(retMsg, TxDetailWithdrawConfirmRet.class);
                    txDetailRet.setTxid(txId);
                    break;
                case TxDetailRet.TYPE_TRANSFER_DETAIL_WITHDRAW_FAIL:
                    txDetailRet = gson.fromJson(retMsg, TxDetailWithdrawFailRet.class);
                    txDetailRet.setTxid(txId);
                    break;

                default:
                    break;
            }

            if(txDetailRet != null)
            {
                txDetailRet.setTxType(type);
                txDetailRet.setLauncherHash(split[1]);
            }
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }
        return txDetailRet;
    }

    /**
     * 获取这个区块内余额变化的交易信息
     *
     * @param blockHeight 余额相关的区块高度
     */
    public static ZXCoinBalanceRet.DataBean getZXCoinStateByBlockID(long blockHeight)
    {
        String retMsg = Onechainmobile.oneChainGetZXCoinStateByBlockID(blockHeight);
        OLLogger.i(TAG, "getZXCoinStateByBlockID retMsg: " + retMsg);
        // getZXCoinStateByBlockID retMsg: 0&X&{"Balance":7629199,"Reserved":50000,"Name":"xmk2","BlockHeight":627,
        // "TxnIDs":"f20dd2e1-e395-4d48-8639-294fedfe5e15 ","PrevBlockHeight":626,
        // "Owner":"bdd11f2c927c76b252c55fab346a3e9b8e46cf968c0974eb9f295765"}
        String[] results = retMsg.split(RES_SEP);
        ZXCoinBalanceRet.DataBean zxCoinStateRet = null;
        if (results.length == 2)
        {
            if (Integer.parseInt(results[0]) == SUCCESS)
            {
                Gson gson = new Gson();
                try
                {
                    zxCoinStateRet = gson.fromJson(results[1], ZXCoinBalanceRet.DataBean.class);
                    if (null != zxCoinStateRet)
                    {
                        return zxCoinStateRet;
                    }
                }
                catch (JsonSyntaxException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * 根据交易ID获取投注号码
     * @param txId
     * @return
     */
    public static TicketNumbersRet getTicketNumbers(String txId)
    {
        if (StringUtils.isEmpty(txId))
        {
            return null;
        }
        String retMsg = Onechainmobile.oneChainGetTicketNumbers(txId);
        OLLogger.i(TAG, "getZXCoinStateByBlockID retMsg: " + retMsg);
        // getZXCoinStateByBlockID retMsg: 0&X&{"Balance":7629199,"Reserved":50000,"Name":"xmk2","BlockHeight":627,
        // "TxnIDs":"f20dd2e1-e395-4d48-8639-294fedfe5e15 ","PrevBlockHeight":626,
        // "Owner":"bdd11f2c927c76b252c55fab346a3e9b8e46cf968c0974eb9f295765"}
        return parseTicketNumbersRet(retMsg);
    }

    @Nullable
    public static TicketNumbersRet parseTicketNumbersRet(String retMsg)
    {
        TicketNumbersRet ticketNumbersRet = null;
        Gson gson = new Gson();
        try
        {
            ticketNumbersRet = gson.fromJson(retMsg, TicketNumbersRet.class);
            if (null != ticketNumbersRet && ticketNumbersRet.getCode() == SUCCESS)
            {
                return ticketNumbersRet;
            }
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * 一元夺宝活动退款
     *
     * @param lotteryId  活动id
     * @param refundTime 退款时间 距1970.1.1后的秒数
     * @param pwd        用户的登录密码
     * @return txId
     */
    public static String oneLotteryRefund(String lotteryId, long refundTime, String pwd)
    {
        if (StringUtils.isEmpty(lotteryId) || refundTime == 0)
        {
            OLLogger.d(TAG, "oneLotteryRefund,param error");
            return "";
        }

        String retMsg = Onechainmobile.oneChainOneLotteryRefund(lotteryId, refundTime, pwd);

        String[] results = retMsg.split(RES_SEP);
        String txId = "";
        if (results.length == 2)
        {
            if (Integer.parseInt(results[0]) == SUCCESS)
            {
                txId = results[1];
            }
        }

        return txId;
    }

    /**
     * 一元夺宝活动开奖
     *
     * @param lotteryId 活动id
     * @return txId
     */
    public static String oneLotteryOpenReward(String lotteryId)
    {
        OLLogger.d(TAG, "oneLotteryOpenReward, " + lotteryId);
        if (StringUtils.isEmpty(lotteryId))
        {
            OLLogger.d(TAG, "oneLotteryOpenReward,param error");
            return "";
        }

        String retMsg = Onechainmobile.oneChainOpenReward(lotteryId);
        OLLogger.d(TAG, "oneLotteryOpenReward, " + retMsg);

        String[] results = retMsg.split(RES_SEP);
        String txId = "";
        if (results.length == 2)
        {
            if (Integer.parseInt(results[0]) == SUCCESS)
            {
                txId = results[1];
            }
        }
        return txId;
    }

    /**
     * 提现主调
     * @param openingBank
     * @param accountName
     * @param bankCardId
     * @param amount
     * @param pwd
     */
    public static String oneChainZxCoinWithdraw(String openingBank, String accountName, String
            bankCardId, long amount, String pwd)
    {
        if(StringUtils.isEmpty(openingBank) || StringUtils.isEmpty(accountName) ||
                StringUtils.isEmpty(bankCardId) || StringUtils.isEmpty(pwd))
        {
            return "";
        }

        if(amount < 0)
        {
            return "";
        }

        String retMsg = Onechainmobile.oneChainZxCoinWithdraw(openingBank, accountName, bankCardId,
                amount, pwd);

        String[] results = retMsg.split(RES_SEP);
        String txId = "";
        if (results.length == 2)
        {
            if (Integer.parseInt(results[0]) == SUCCESS)
            {
                txId = results[1];
            }
        }

        return txId;
    }

    /**
     * 申诉主调
     * @param txId
     * @param remark
     * @param pwd
     * @return
     */
    public static String oneChainZxCoinWithdrawAppeal(String txId, String remark, String pwd)
    {
        if(StringUtils.isEmpty(txId) || StringUtils.isEmpty(remark))
        {
            return "";
        }

        String retMsg = Onechainmobile.oneChainZxCoinWithdrawAppeal(txId, remark, pwd);

        String[] results = retMsg.split(RES_SEP);
        String newTxId = "";
        if (results.length == 2)
        {
            if (Integer.parseInt(results[0]) == SUCCESS)
            {
                newTxId = results[1];
            }
        }

        return newTxId;
    }

    /**
     * 确认提现主调
     * @param txId
     * @param pwd
     * @return
     */
    public static String oneChainZxCoinWithdrawConfirm(String txId,String pwd)
    {
        if(StringUtils.isEmpty(txId))
        {
            return "";
        }

        String retMsg = Onechainmobile.oneChainZxCoinWithdrawConfirm(txId, pwd);

        String[] results = retMsg.split(RES_SEP);
        String newTxId = "";
        if (results.length == 2)
        {
            if (Integer.parseInt(results[0]) == SUCCESS)
            {
                newTxId = results[1];
            }
        }

        return newTxId;
    }

    /**
     * 获取当前用户的id
     *
     * @return
     */
    public static String getCurUserId()
    {
        return Onechainmobile.oneChainGetCurUser();
    }

    /**
     * 验证当前密码
     * @param pwd
     */
    public static long checkCurUserPwd(String userId,String pwd)
    {
        if (StringUtils.isEmpty(pwd) || StringUtils.isEmpty(userId))
        {
            OLLogger.d(TAG, "checkCurUserPwd, param error");
            return PARA_ERR;
        }
        long l = Onechainmobile.oneChainCheckUserPwd(userId,pwd);
        OLLogger.d(TAG, "checkCurUserPwd, " + l);
        return l;
    }

    /**
     * 设置当前用户的id
     *
     * @param userId
     */
    public static void setCurUserId(String userId)
    {
        Onechainmobile.oneChainSetCurUser(userId);
    }

    //添加活动解析回调字符串
    public static boolean oneLotterAdd(String ret, String txId)
    {
        if (StringUtils.isEmpty(ret))
        {
            return false;
        }

        Gson gson = new Gson();
        try
        {
            OneLotteryRet detailRet = gson.fromJson(ret, OneLotteryRet.class);
            if (null == detailRet || null == detailRet.getData())
            {
                return false;
            }
            OneLottery oneLottery = (OneLottery) InputPwdDialog.mCurOperObject;
            if (detailRet.getCode() == 0)
            {
                if (null != oneLottery)
                {
                    //添加活动回调更新字段
                    oneLottery.setLotteryId(detailRet.getData().getTxnID());
                    oneLottery.setPublisherHash(detailRet.getData().getPublisherHash());
//                    oneLottery.setNewTxId(txId);// 因为是创建活动，不需要设置newTxId
                    oneLottery.setVersion(1);
                    oneLottery.setCurBetCount(0);
                    oneLottery.setPublisherName(OneLotteryApplication.getAppContext().getString(R
                            .string.tab_bar_me));
                    OneLotteryDBHelper.getInstance().insertOneLottery(oneLottery);

                    return true;
                }
            } else
            {
                OneLotteryDBHelper.getInstance().deleteOneLottery(oneLottery);
                return false;
            }
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return false;
    }

    //修改活动解析回调字符串
    public static OneLottery oneLotteryModify(String ret, String txId)
    {
        if (StringUtils.isEmpty(ret))
        {
            return null;
        }

        Gson gson = new Gson();
        try
        {
            OneLotteryRet detailRet = gson.fromJson(ret, OneLotteryRet.class);
            if (null == detailRet || null == detailRet.getData())
            {
                return null;
            }

            if(detailRet.getCode() == 0)
            {
                OneLotteryLogic oneLotteryLogic = new OneLotteryLogic();
                OneLottery oneLottery = oneLotteryLogic.queryLotteryDetail(CreateLotteryActivity
                        .lottery.getLotteryId(), false, null);
                return oneLottery;
            }
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    //删除活动解析回调字符串
    public static OneLottery oneLotteryDeleteJson(String ret, String txId)
    {
        if (StringUtils.isEmpty(ret))
        {
            return null;
        }

        Gson gson = new Gson();
        try
        {
            OneLotteryDeleteRet detailRet = gson.fromJson(ret, OneLotteryDeleteRet.class);
            if (null == detailRet || null == detailRet.getData() || detailRet.getData().isEmpty())
            {
                return null;
            }

            if (detailRet.getCode() == 0)
            {
                OneLottery lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId
                        (detailRet.getData());
                if (lottery != null)
                {
                    OneLotteryManager.getInstance().deleteLocalOneLottery(lottery);
                }

                List<OneLotteryBet> betList = OneLotteryBetDBHelper.getInstance()
                        .getMyBetByLotteryId(detailRet.getData());
                if (betList != null)
                {
                    for (OneLotteryBet bet : betList)
                    {
                        OneLotteryBetDBHelper.getInstance().deleteOneLotteryBet(bet);
                    }
                }
                return lottery;
            }
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    /**
     * 解析创建，修改活动的json（callback，notify时通用）
     *
     * @param retMsg
     * @param txnID
     * @return
     */
    public static OneLotteryRet onLotteryChange(String retMsg, String txnID)
    {
        if (StringUtils.isEmpty(retMsg))
        {
            return null;
        }

        Gson gson = new Gson();
        try
        {
            OneLotteryRet olRet = gson.fromJson(retMsg, OneLotteryRet.class);
            if (null == olRet || null == olRet.getData())
            {
                return null;
            }
            return olRet;
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }
/*

    //处理登录用户投注的逻辑
    public static OneLotteryBetNumbersRet betLottery(String retMsg, String txnID)
    {
        if (StringUtils.isEmpty(retMsg) || StringUtils.isEmpty(txnID))
        {
            return null;
        }

        Gson gson = new Gson();
        try
        {
            OneLotteryBetNumbersRet betNumbersRet = gson.fromJson(retMsg, OneLotteryBetNumbersRet
                    .class);
            if (betNumbersRet == null || betNumbersRet.getData() == null)
            {
                return null;
            }
            return betNumbersRet;
        }
        catch (JsonSyntaxException e)
        {
            e.printStackTrace();
        }

        return null;
    }
*/

    //可退款的通知
    public static OneLotteryRefundNotify canRefundNotify(String retMsg, String txnId)
    {
        if (StringUtils.isEmpty(retMsg) || StringUtils.isEmpty(txnId))
        {
            return null;
        }

        Gson gson = new Gson();
        try
        {
            OneLotteryRefundNotify refundNotify = gson.fromJson(retMsg, OneLotteryRefundNotify
                    .class);
            if (refundNotify == null || refundNotify.getData() == null || refundNotify.getData()
                    .getArray() == null)
            {
                return null;
            }

            return refundNotify;
        }
        catch (JsonSyntaxException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static OneLotteryBetOverNotify onLotteryBetOver(String msg, String txID)
    {
        if (StringUtils.isEmpty(msg) || StringUtils.isEmpty(txID))
        {
            return null;
        }

        Gson gson = new Gson();
        try
        {
            OneLotteryBetOverNotify overNorify = gson.fromJson(msg, OneLotteryBetOverNotify.class);
            if (overNorify == null || overNorify.getData() == null)
            {
                return null;
            }
            return overNorify;
        }
        catch (JsonSyntaxException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static ConsensusOverRet conSensusOverNotify(String msg, String txId)
    {

        if (StringUtils.isEmpty(msg) || StringUtils.isEmpty(txId))
        {
            return null;
        }

        Gson gson = new Gson();
        try
        {
            ConsensusOverRet consensusOverRet = gson.fromJson(msg, ConsensusOverRet.class);
            if (consensusOverRet == null || consensusOverRet.getData() == null)
            {
                return null;
            }
            return consensusOverRet;
        }
        catch (JsonSyntaxException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static OneLotteryRewardOverNotify onOpenReward(String msg, String s)
    {
        if (StringUtils.isEmpty(msg) || StringUtils.isEmpty(s))
        {
            return null;
        }
        Gson gson = new Gson();
        try
        {
            return gson.fromJson(msg, OneLotteryRewardOverNotify.class);
        }
        catch (JsonSyntaxException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static OneLotteryStartNotify onelotteryStartNotify(String msg, String txId)
    {
        if (StringUtils.isEmpty(msg) || StringUtils.isEmpty(txId))
        {
            return null;
        }
        Gson gson = new Gson();
        try
        {
            OneLotteryStartNotify lottery = gson.fromJson(msg,
                    OneLotteryStartNotify.class);
            if (lottery != null)
            {
                return lottery;
            }
        }
        catch (JsonSyntaxException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isVisitor(Context context)
    {
        String curUserId = OneLotteryApi.getCurUserId();
        if (StringUtils.isEmpty(curUserId) || curUserId.equals(ConstantCode.CommonConstant
                .ONELOTTERY_DEFAULT_USERNAME))
        {
            List<String> userList = UserInfoDBHelper.getInstance().getUserList(true);
            if (userList.isEmpty())
            {
                Intent register = new Intent(context, RegisterActivity.class);
                register.putExtra(ConstantCode.CommonConstant.TYPE, true);
                context.startActivity(register);
            } else
            {
                Intent login = new Intent(context, LoginActivity.class);
                login.putExtra(ConstantCode.CommonConstant.TYPE, true);
                context.startActivity(login);
            }
            return true;
        }
        return false;
    }

    public static byte[] encryptFile(String encryptPwd, byte[] bytes)
    {
        return Onechainmobile.oneChainEncrypt(encryptPwd, bytes);
    }

    public static byte[] decryptFile(String encryptPwd, byte[] bytes)
    {
        return Onechainmobile.oneChainDecrypt(encryptPwd, bytes);
    }

}
