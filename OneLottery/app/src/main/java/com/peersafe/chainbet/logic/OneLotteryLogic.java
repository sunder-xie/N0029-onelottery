package com.peersafe.chainbet.logic;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.Friend;
import com.peersafe.chainbet.db.MessageNotify;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.OneLotteryBet;
import com.peersafe.chainbet.db.PrizeRule;
import com.peersafe.chainbet.db.TransactionDetail;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.db.WithdrawRecord;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.FriendDBHelper;
import com.peersafe.chainbet.manager.dbhelper.MessageNotifyDBHelper;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryBetDBHelper;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.manager.dbhelper.PrizeRuleDBHelper;
import com.peersafe.chainbet.manager.dbhelper.TransactionDetailDBHelper;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.manager.dbhelper.WithdrawRecordDBHelper;
import com.peersafe.chainbet.model.AttendBean;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryBetDetailRet;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryBetOverNotify;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryDetailRet;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryListRet;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryOldHistoryQueryRet;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryRefundNotify;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryRet;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryRewardOverNotify;
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
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.config.OLPreferenceUtil;
import com.peersafe.chainbet.utils.log.OLLogger;
import com.peersafe.chainbet.widget.InputPwdDialog;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author moying
 * @Description
 * @date 2017/1/10 18:07
 */
public class OneLotteryLogic
{
    private static final String TAG = OneLotteryLogic.class.getSimpleName();

    private static String lastUpdateFlag = "";

    /**
     * @param isHistory 是否是历史活动
     * @Description 查询活动列表
     *
     * @return newUpdateFlag
     */
    public synchronized boolean queryLotteryList(boolean isHistory)
    {
        // 获取所有活动
        OneLotteryListRet olListRet = (OneLotteryListRet)
                (isHistory ? OneLotteryApi.oneLotteryHistoryQuery(null) : OneLotteryApi
                        .oneLotteryQuery(null));

        // 防止获取normalList的时候异常，导致newFlag被更新，historyList在更新的时候，就会用新newFlag，从而，旧的normalList的flag会被delete任务处理。
        // 导致正常的活动列表减少。
        boolean invalid = olListRet != null && olListRet.getCode() == OneLotteryApi.SUCCESS
                && olListRet.getData() != null && !olListRet.getData().isEmpty();
        if (!invalid)
        {
            return false;
        }

        String newFlag = "";
        if (isHistory)
        {
            newFlag = StringUtils.isEmpty(lastUpdateFlag) ? getCurLotteryNewFlag() : lastUpdateFlag;
        } else {
            newFlag = lastUpdateFlag = UUID.randomUUID().toString();
        }

        OLLogger.i(TAG, "queryLotteryList isHistory=" + isHistory + ", flag= " + newFlag);
        if (olListRet != null && olListRet.getData() != null && !olListRet.getData().isEmpty())
        {
            OLLogger.i(TAG, "queryLotteryList size=" + olListRet.getData().size() + ", newFlag=" + newFlag);

            for (OneLotteryDetailRet.DataBean data : olListRet.getData())
            {
                OLLogger.i(TAG, "queryLotteryList data== " + data);
                if (data != null && !StringUtils.isEmpty(data.getTxnID()))
                {
                    boolean isFriendOrMe = UserLogic.isOfficialFriendOrMe(data.getPublisherHash(), data.getPublisherName());
                    OLLogger.i(TAG, "Friend's lottery ver=" + data.getVersion() + ", newTxID=" +
                            data.getNewTxnID() + ", isfriendOrMe=" + isFriendOrMe);

                    OneLottery dbOl = OneLotteryDBHelper.getInstance().getLotteryByLotterId(data.getTxnID());

                    if (dbOl != null)
                    {
                        OLLogger.i(TAG, "queryLotteryList inDB " + dbOl);

                        if (dbOl.getVersion() != null)
                        {
                            if (dbOl.getVersion() != -1)
                            {
                                if (!data.getNewTxnID().equals(dbOl.getNewTxId()) || data.getVersion() > dbOl.getVersion())
                                {
                                    // 如果是已关闭的活动，有些字段是缺失的（见概要设计），需要使用NewTxId来查询活动详情
                                    queryLotteryDetail(data.getTxnID(), isHistory, newFlag);
                                } else
                                {
                                    OLLogger.i(TAG, "queryLotteryList 仅更新updateFlag标志=" + newFlag);

                                    // 仅更新updateFlag标志，标记这个数据是最新的，不需要修改
                                    dbOl.setUpdateFlag(newFlag);
                                    OneLotteryDBHelper.getInstance().insertOneLottery(dbOl);
                                }
                            } else if (isFriendOrMe)
                            {
                                queryLotteryDetail(data.getTxnID(), isHistory, newFlag);
                            } else {
                                OLLogger.i(TAG, "queryLotteryList 只更新updateFlag标志=" + newFlag);

                                // 仅更新updateFlag标志，标记这个数据是最新的，不需要修改
                                dbOl.setUpdateFlag(newFlag);
                                OneLotteryDBHelper.getInstance().insertOneLottery(dbOl);
                            }
                        }
                    } else
                    {
                        // 本地不存在此记录,则看是不是官方，我，好友的活动
                        // TODO 历史活动，暂时没下发publishName,等接口修改好后再做
                        if (isFriendOrMe)
                        {
                            queryLotteryDetail(data.getTxnID(), isHistory, newFlag);
                        } else
                        {
                            // 陌生人的活动只做(version=-1)的存储，不再取详情
                            OneLottery strange = new OneLottery();
                            strange.setLotteryId(data.getTxnID());
                            strange.setNewTxId(data.getNewTxnID());
                            strange.setVersion(-1);// 不要用data.getVersion()，初始活动用-1
                            strange.setPublisherName(data.getPublisherName());
                            strange.setUpdateFlag(newFlag);
                            strange.setLotteryName(data.getName());
                            OLLogger.i(TAG, "queryLotteryList 存储陌生人的活动:" + strange);

                            OneLotteryDBHelper.getInstance().insertOneLottery(strange);
                        }
                    }
                }
            }
        }

        // 删除与此次updateFlag不同的活动，以及活动相关的投注记录(不包括我参加和创建的)
        if (isHistory)
        {
            List<OneLottery> list = OneLotteryDBHelper.getInstance().getNotThisUpdateFlagLotteres(newFlag, isHistory);
            OLLogger.i(TAG, "queryLotteryList delete lottery:" + (list != null ? list.size() : "0") + ", newFlag=" + newFlag);
            if (list != null && !list.isEmpty())
            {

                OLLogger.i(TAG, "queryLotteryList delete lottery:" + list.size());

                for (OneLottery ol : list)
                {
                    // 需要判断我是否参与了,和我创建的活动
                    List<OneLotteryBet> myBetList = OneLotteryBetDBHelper.getInstance().getMyBetByLotteryId(ol.getLotteryId());
                    if ((myBetList != null && !myBetList.isEmpty())
                            || UserLogic.isLoginUser(ol.getPublisherHash(), ol.getPublisherName()))
                    {
                        // 我参与的情况下，如果状态是非4，7，8的状态需要更新活动详情和投注列表
                        if (ol.getState() == ConstantCode.OneLotteryState.ONELOTTERY_STATE_REFUND_ALREADY
                                || ol.getState() == ConstantCode.OneLotteryState.ONELOTTERY_STATE_FAIL
                                || ol.getState() == ConstantCode.OneLotteryState.ONELOTTERY_STATE_REWARD_ALREADY)
                        {
                            continue;
                        } else {
                            // 查询历史活动详情和投注详情
                            OneLotteryOldHistoryQueryRet oldHistory = OneLotteryApi.oneLotteryOldHistoryInfoQuery(ol.getLotteryId());
                            if (null != oldHistory && oldHistory.getCode() == OneLotteryApi.SUCCESS && oldHistory.getData() != null)
                            {

                                // TODO 根据活动中对应的区块，前区块进行回溯（直到查到本活动前区块号为止）
                                // 查找所有交易详情后，更新活动，投注，用户信息和交易流水表
                                OneLottery lottery = (OneLottery) ol.clone();
                                refreshBetAndTransactions(oldHistory, lottery, myBetList);

                                // 更新活动详情
                                ol.setNewTxId(oldHistory.getData().getNewTxnID());
                                ol.setLastCloseTime(new Date(oldHistory.getData().getLastCloseTime()));
                                ol.setUpdateTime(new Date(oldHistory.getData().getUpdateTime()));
                                ol.setCreateTime(new Date(oldHistory.getData().getCreateTime()));
                                ol.setRewardNumbers(oldHistory.getData().getNumbers());
                                ol.setCurBetAmount(oldHistory.getData().getBalance());
                                ol.setPrizeTxID(oldHistory.getData().getPrizeTxnID());
                                ol.setCurBetCount(oldHistory.getData().getCountTotal());
                                ol.setPictureIndex(oldHistory.getData().getPictureIndex());
                                ol.setState(oldHistory.getData().getStatus());
                                ol.setPrevBlockHeight(oldHistory.getData().getPreBlockHeight());
                                ol.setCurBlockHeight(oldHistory.getData().getBlockHeight());
                                ol.setBetTxnIDs(oldHistory.getData().getTxnIDs());
                                ol.setLotteryName(oldHistory.getData().getName());

                                OneLotteryDBHelper.getInstance().insertOneLottery(ol);

                            }

                        }
                    } else/*if (!UserLogic.isOfficialFriendOrMe(ol.getPublisherHash(), ol.getPublisherName()))*/
                    {
                        // 只删除非（官方，好友，我）的旧活动
                        OLLogger.i(TAG, "删除活动：" + ol.getPublisherName());
                        deleteOneLotteryInDB(ol);
                    }
                }
            }

            return true;
        }

        return false;
    }

    // 删除本地缓存活动记录以及相关投注记录
    public void deleteOneLotteryInDB(OneLottery ol)
    {
        if (ol == null)
        {
            return;
        }
        OneLotteryDBHelper.getInstance().deleteOneLottery(ol);
        List<OneLotteryBet> betList = OneLotteryBetDBHelper.getInstance()
                .getMyBetByLotteryId(ol.getLotteryId());
        if (betList != null)
        {
            boolean deleted = false;
            for (OneLotteryBet bet : betList)
            {
                deleted = true;
                OneLotteryBetDBHelper.getInstance().deleteOneLotteryBet(bet);
            }

            if(deleted)
            {
                OneLotteryManager.getInstance().SendEventBus(null,OLMessageModel.STMSG_MODEL_REFRESH_FRIEND_FRAGMENT);
            }
        }
    }

    // 查询活动详情
    public synchronized OneLottery queryLotteryDetail(String lotteryID, boolean isHistory, String
            newUpdateFlag)
    {
        OLLogger.i(TAG, "活动详情 queryLotteryDetail lotteryID=" + lotteryID);

        if (StringUtils.isEmpty(lotteryID))
        {
            return null;
        }

        OneLotteryDetailRet olDetailRet = (OneLotteryDetailRet) (isHistory ? OneLotteryApi
                .oneLotteryHistoryQuery(lotteryID) : OneLotteryApi
                .oneLotteryQuery(lotteryID));
        OLLogger.i(TAG, "活动详情 queryLotteryDetail lotteryID=" + olDetailRet);
        if (olDetailRet != null && olDetailRet.getData() != null)
        {
            OneLottery lottery = new OneLottery();
            lottery.setLotteryId(olDetailRet.getData().getTxnID());
            lottery.setNewTxId(olDetailRet.getData().getNewTxnID());
            lottery.setVersion(olDetailRet.getData().getVersion());
            lottery.setLotteryName(olDetailRet.getData().getName());
            lottery.setRuleId(olDetailRet.getData().getRuleID());
            lottery.setRuleType(olDetailRet.getData().getRuleType());
            lottery.setPublisherName(olDetailRet.getData().getPublisherName());
            lottery.setPublisherHash(olDetailRet.getData().getPublisherHash());
            lottery.setCreateTime(new Date(olDetailRet.getData().getCreateTime()));
            lottery.setUpdateTime(new Date(olDetailRet.getData().getUpdateTime()));
            lottery.setStartTime(new Date(olDetailRet.getData().getStartTime()));
            lottery.setCloseTime(new Date(olDetailRet.getData().getCloseTime()));
            lottery.setMinBetCount(olDetailRet.getData().getMinAttendeeCnt());
            lottery.setMaxBetCount(olDetailRet.getData().getMaxAttendeeCnt());
            lottery.setOneBetCost(olDetailRet.getData().getCost());
            lottery.setDescription(olDetailRet.getData().getDescription());
            lottery.setLastCloseTime(new Date(olDetailRet.getData().getLastCloseTime()));
            lottery.setCurBetAmount(olDetailRet.getData().getCountTotal() * olDetailRet.getData()
                    .getCost());
            lottery.setBetTotalAmount(olDetailRet.getData().getMaxAttendeeCnt() * olDetailRet
                    .getData().getCost());
            lottery.setCurBetCount(olDetailRet.getData().getCountTotal());
            lottery.setRewardNumbers(olDetailRet.getData().getNumbers());

            //服务器端活动未开始状态可能不对，需要判断是否已经到了开始时间，如果到了已经开始的时间，需要置为进行中
            //服务器端活动已开始状态可能不对，需要判断是否已经到了关闭时间，如果到了已经关闭的时间，需要置为可退款
            if (olDetailRet.getData().getStartTime() <= System.currentTimeMillis()
                    && olDetailRet.getData().getStatus() == ConstantCode.OneLotteryState.ONELOTTERY_STATE_NOT_STARTED)
            {
                lottery.setState(ConstantCode.OneLotteryState.ONELOTTERY_STATE_ON_GOING);
            }
            else if (olDetailRet.getData().getCloseTime() <= System.currentTimeMillis()
                    && olDetailRet.getData().getStatus() == ConstantCode.OneLotteryState.ONELOTTERY_STATE_ON_GOING)
            {
                lottery.setState(ConstantCode.OneLotteryState.ONELOTTERY_STATE_CAN_REFUND);
            }
            else
            {
                lottery.setState(olDetailRet.getData().getStatus());
            }

            lottery.setBetTxnIDs(olDetailRet.getData().getTxnIDs());
            lottery.setCurBlockHeight(olDetailRet.getData().getBlockHeight());
            lottery.setPrevBlockHeight(olDetailRet.getData().getPreBlockHeight());
//          lottery.setLastUpadteBlockHeight(oneLotteryDetailRet.getData().getPreBlockHeight());

// 存储lastUpdBlockHeight有什么用呢？
            if (!StringUtils.isEmpty(newUpdateFlag))
            {
                // 按理说不传的话表示不修改
                lottery.setUpdateFlag(newUpdateFlag);
            } else
            {
                OneLottery dbOL = OneLotteryDBHelper.getInstance().getLotteryByLotterId
                        (olDetailRet.getData().getTxnID());
                if (dbOL != null)
                {
                    OLLogger.i(TAG, "本地有" + dbOL.getLotteryId() + ", flag = " + dbOL
                            .getUpdateFlag());
                    lottery.setUpdateFlag(dbOL.getUpdateFlag());
                }
            }
            lottery.setPictureIndex(olDetailRet.getData().getPictureIndex());
            if (lottery.getMaxBetCount() != 0)
            {
                int progress = (int) Math.round((lottery.getCurBetCount() * 100d) / lottery.getMaxBetCount());
                lottery.setProgress((progress == 0 && (lottery.getCurBetCount().intValue() != 0)) ? 1 :
                        ((progress == 100 && (lottery.getCurBetCount().intValue() != lottery.getMaxBetCount().intValue())) ? 99 : progress));
            }
            lottery.setPrizeTxID(olDetailRet.getData().getPrizeTxnID());

            OneLotteryDBHelper.getInstance().insertOneLottery(lottery);

            queryLotteryBetList(lottery);

            OLLogger.i(TAG, "活动详情 queryLotteryDetail insert " + lottery);

            return lottery;
        }

        return null;
    }

    // 查询活动投注列表
    public synchronized boolean queryLotteryBetList(OneLottery lottery)
    {
        if (lottery == null || StringUtils.isEmpty(lottery.getLotteryId()))
        {
            return false;
        }
        OLLogger.i(TAG, "活动投注列表 queryLotteryBetList lotteryID=" + lottery.getLotteryId());

        OneLotteryBetDetailRet olBetDetailRet = OneLotteryApi.oneLotteryBetQuery(lottery
                .getLotteryId());
        if (olBetDetailRet != null && olBetDetailRet.getData() != null && !olBetDetailRet.getData
                ().isEmpty())
        {
            OLLogger.i(TAG, "queryLotteryBetList size=" + olBetDetailRet.getData().size());

            for (OneLotteryBetDetailRet.DataBean betDetail : olBetDetailRet.getData())
            {
                if (betDetail != null && !StringUtils.isEmpty(betDetail.getTxnID()))
                {
                    OneLotteryBet oneLotteryBet = new OneLotteryBet(betDetail.getTxnID());
                    // ... set
                    oneLotteryBet.setCreateTime(new Date(betDetail.getCreateTime()));
                    oneLotteryBet.setLotteryId(lottery.getLotteryId());
                    oneLotteryBet.setAttendeeHash(betDetail.getAttendee());
                    oneLotteryBet.setAttendeeName(betDetail.getAttendeeName());
                    oneLotteryBet.setBetNumbers(betDetail.getNumbers());
                    oneLotteryBet.setBetCost(betDetail.getAmount());
                    oneLotteryBet.setBetCount(betDetail.getNumbers().split(" ").length);
                    oneLotteryBet.setLotteryName(lottery.getLotteryName());
                    oneLotteryBet.setOneLottery(lottery);

                    OneLotteryBetDBHelper.getInstance().insertOneLotteryBet(oneLotteryBet);

//                    queryLotteryBetDetail(betDetail, lottery.getLotteryId());
                }
            }
            return true;
        }

        return false;
    }

    /**
     * 查询投注ID的交易详情 【与我有关的，超越7天的才查询】
     *
     * @param betDetailRet 交易ID
     * @param lotteryID    活动ID
     * @return
     */
    private boolean queryLotteryBetDetail(OneLotteryBetDetailRet.DataBean betDetailRet, String
            lotteryID)
    {
        if (betDetailRet == null || StringUtils.isEmpty(betDetailRet.getTxnID()))
        {
            return false;
        }
        OLLogger.i(TAG, "投注的交易详情 queryLotteryBetDetail ticketID=" + betDetailRet.getTxnID());
        TxDetailBetRet olTxnDetailRet = OneLotteryApi.oneLotteryBetDetailQuery
                (betDetailRet.getTxnID());
        OLLogger.i(TAG, "queryLotteryBetDetail TxDetailBetRet=" + olTxnDetailRet);

        if (olTxnDetailRet != null)
        {
            return true;
        }
        return false;
    }

    // 收到添加活动的消息处理逻辑
    public OneLottery onLotteryAdd(String retMsg, String txnID)
    {
        OneLotteryRet ret = OneLotteryApi.onLotteryChange(retMsg, txnID);
        if (ret == null || ret.getData() == null || StringUtils.isEmpty(ret.getData()
                .getTxnID()))
        {
            return null;
        }
        String dbFlag = getCurLotteryNewFlag();
        /*List<OneLottery> dbList = OneLotteryApplication.getDaoSession().getOneLotteryDao()
                .loadAll();
        if (dbList != null && !dbList.isEmpty())
        {
            for (int i = 0; i < dbList.size(); i++)
            {
                if (UserLogic.isOfficialFriendOrMe(dbList.get(i).getPublisherHash(), dbList.get(i).getPublisherName()))
                {
                    dbFlag = dbList.get(i).getUpdateFlag();
                    break;
                }
            }
        }*/

        OneLottery lottery = null;
        // 官方，或者好友的活动,需要获取活动详情，再存储
        if (UserLogic.isOfficialFriendOrMe(ret.getData().getPublisherHash(), ret.getData().getPublisherName()))
        {
            if (UserLogic.isLoginUser(ret.getData().getPublisherHash(), ret.getData().getPublisherName()))
            {
                // 添加活动,获取余额
                OneLotteryManager.getInstance().getUserBalance();
            }
            lottery = queryLotteryDetail(ret.getData().getTxnID(), false, dbFlag);

            // 空指针的问题
            try
            {
                OLLogger.i(TAG, "存储好友" + ret.getData().getPublisherName() + "的活动:" + lottery.getLotteryId()
                        + ", pubHash=" + lottery.getPublisherHash());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        } else
        {
            // 存储陌生人的活动
            lottery = new OneLottery();
            lottery.setLotteryId(ret.getData().getTxnID());
            lottery.setVersion(-1);// 不要用data.getVersion()，初始活动用-1
            lottery.setPublisherHash(ret.getData().getPublisherHash());
            lottery.setPublisherName(ret.getData().getPublisherName());
            lottery.setLotteryName(ret.getData().getName());
            lottery.setUpdateFlag(dbFlag);

            OLLogger.i(TAG, "存储陌生人" + ret.getData().getPublisherName() + "的活动:" + lottery.getLotteryId()
                    + ", pubHash=" + lottery.getPublisherHash());

            OneLotteryDBHelper.getInstance().insertOneLottery(lottery);
        }
        return lottery;
    }

    // 收到修改活动的消息处理逻辑
    public OneLottery onLotteryModify(String retMsg, String txnID)
    {
        OneLotteryRet ret = OneLotteryApi.onLotteryChange(retMsg, txnID);
        if (ret == null || ret.getData() == null || StringUtils.isEmpty(ret.getData()
                .getTxnID()))
        {
            return null;
        }
        String dbFlag = getCurLotteryNewFlag();
        OneLottery ol = OneLotteryDBHelper.getInstance().getLotteryByLotterId(ret.getData()
                .getTxnID());
        if (ol != null)
        {
            dbFlag = ol.getUpdateFlag();
        } else
        {
            // 本地没有此记录的话，应该去新增入库
            List<OneLottery> dbList = OneLotteryApplication.getDaoSession().getOneLotteryDao()
                    .loadAll();
            if (dbList != null && !dbList.isEmpty())
            {
                dbFlag = dbList.get(0).getUpdateFlag();
            }
        }

        // 查找有没有这个好友
        boolean isFriendOrMe = UserLogic.isOfficialFriendOrMe(ret.getData().getPublisherHash(), ret.getData().getPublisherName());
        OneLottery lottery = null;
        // 我的，官方，或者好友的活动,需要获取活动详情，再存储
        if (UserLogic.isOfficalUser(ret.getData().getPublisherHash(), null) || isFriendOrMe)
        {
            lottery = queryLotteryDetail(ret.getData().getTxnID(), false, dbFlag);
            if (UserLogic.isLoginUser(ret.getData().getPublisherHash(), ret.getData().getPublisherName()))
            {
                // 修改活动,获取余额
                OneLotteryManager.getInstance().getUserBalance();
            }
        } else
        {
            // 存储陌生人的活动
            lottery = new OneLottery();
            lottery.setLotteryId(ret.getData().getTxnID());
            // TODO 看修改活动时是否有newTxnID
//            lottery.setNewTxId(ret.getData().getLotteryID());
            lottery.setVersion(-1);// 不要用data.getVersion()，初始活动用-1
            lottery.setPublisherHash(ret.getData().getPublisherHash());
            lottery.setUpdateFlag(dbFlag);
            lottery.setLotteryName(ret.getData().getName());
            OLLogger.i(TAG, "存储陌生人的活动:" + lottery);

            OneLotteryDBHelper.getInstance().insertOneLottery(lottery);
        }
        return lottery;
    }

    // 收到删除活动的消息处理逻辑
    public OneLottery onLotteryDelete(String retMsg, String txnID)
    {
        return OneLotteryApi.oneLotteryDeleteJson(retMsg, txnID);
    }
/*

    // 执行了投注后逻辑处理, 包括界面交互，明细查询，活动查询，消息提醒，余额变化
    public OneLotteryBetNumbersRet betLottery(String retMsg, String txnID)
    {
        return OneLotteryApi.betLottery(retMsg, txnID);
    }
*/
    // 收到中奖通知后的逻辑处理
    public boolean onLotteryBetOver(String msg, final String txID)
    {
        if (StringUtils.isEmpty(msg) || StringUtils.isEmpty(txID))
        {
            return false;
        }

        // 解析json
        final OneLotteryBetOverNotify overNotify = OneLotteryApi.onLotteryBetOver(msg, txID);
        if (null == overNotify || overNotify.getData() == null)
        {
            return false;
        }

        // 本地无此记录，就不处理
        final String lotteryId = overNotify.getData().getLotteryID();
        final OneLottery lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId(lotteryId);
        if (null == lottery || (lottery.getVersion() != null && lottery.getVersion() == -1))
        {
            return false;
        }

        // 获取活动详情，并更新DB
        boolean result = OneLotteryManager.getInstance().getLotteryHistoryDetail(lotteryId);
        if (!result)
        {
            return false;
        }

        boolean isPrizeable = false;
        int prizeType = 0;
        MessageNotify messageNotify = new MessageNotify();
        //更新余额
        UserInfo curUser = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        UserInfo userBalance = null;
        if (overNotify.getData().getAttendee().equals(curUser.getWalletAddr()))
        {
            userBalance = OneLotteryManager.getInstance().getUserBalance();
            prizeType += ConstantCode.TransactionType.TRANSACTION_TYPE_PRIZE;
            isPrizeable = true;
        }

        //分成提醒(我的活动)
        PrizeRule prizeRule = PrizeRuleDBHelper.getInstance().getPrizeRuleById(lottery.getRuleId());
        long prizeAmount = 0, percentageAmount = 0;
        if (prizeRule != null && prizeRule.getPercentage() != null/* && prizeRule.getPercentage() < 100*/)
        {
            if (lottery.getPublisherHash().equals(curUser.getWalletAddr()))
            {
                if (userBalance == null)
                {
                    OneLotteryManager.getInstance().getUserBalance();
                }
                isPrizeable = true;
                prizeType += ConstantCode.TransactionType.TRANSACTION_TYPE_PERCENTAGE;
            }
            prizeAmount = lottery.getBetTotalAmount() * prizeRule.getPercentage() / 100;
            percentageAmount = lottery.getBetTotalAmount() - prizeAmount;
        }

        //更新message
        if (isPrizeable)
        {
            insertTDAndMsg(lotteryId, lottery.getLotteryName(), new Date(), txID, overNotify.getData().getAttendeeName(),
                    overNotify.getData().getAttendee(), prizeType, prizeAmount, percentageAmount);
        }

        OneLotteryManager.getInstance().SendEventBus(lotteryId, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_OVER_NOTIFY);

        return true;
    }

    public void checkTranTxId(String txID, TransactionDetail td)
    {
        TransactionDetail db = TransactionDetailDBHelper.getInstance().getTranByTxId(txID);
        if (db != null)
        {
            boolean isMe = StringUtils.nullToEmpty(db.getMyId()).equals(td.getOppositeUserId());
            if (isMe)
            {
                if (StringUtils.isEmpty(db.getOppositeUserId()))
                {
                    if (td.getMyHash().equals(db.getOppositeHash()))
                    {
                        db.setOppositeUserId(td.getMyId());
                        // 判断这个是转给我的，修改下db内容，添加id信息。以前只有hash的
                        TransactionDetailDBHelper.getInstance().insertTransactionDetail(db);
                    } else
                    {
                        // TODO 还有必要搜索么？
                        ZXUserInfoRet opUser = OneLotteryApi.getUserInfo(db.getOppositeUserId(), db.getOppositeHash());
                        if (opUser != null && opUser.getCode() == OneLotteryApi.SUCCESS && opUser.getData() != null)
                        {
                            db.setOppositeUserId(opUser.getData().getUserId());
                            TransactionDetailDBHelper.getInstance().insertTransactionDetail(db);
                        }
                    }
                }
                boolean isOp = StringUtils.nullToEmpty(db.getOppositeUserId()).equals(td.getMyId());
                if (isOp)
                {
                    td.setTxId(txID + "_1");
                }
            }

        }
    }

    private void checkMsgId(String txID, MessageNotify messageNotify)
    {
        MessageNotify db = MessageNotifyDBHelper.getInstance().getMsgByMsgId(txID);
        if (db != null && !(StringUtils.nullToEmpty(db.getUserId()).equals(messageNotify.getUserId())))
        {
            messageNotify.setMsgId(txID + "_1");
        }
    }

    private void checkWithdrawMsgId(String txID, MessageNotify messageNotify)
    {
        MessageNotify db = MessageNotifyDBHelper.getInstance().getMsgByMsgId(txID);
        if (db != null)
        {
            messageNotify.setMsgId(txID + "_1");
        }
    }

    // 收到退款的通知 new
    public OneLottery onLotteryRefund(String retMsg, String txId)
    {
        if (StringUtils.isEmpty(retMsg) || StringUtils.isEmpty(txId))
        {
            return null;
        }
        OneLotteryRefundNotify refundNotify = OneLotteryApi.canRefundNotify(retMsg, txId);
        if (null == refundNotify)
        {
            return null;
        }

        //本地没有该活动，则不处理
        String lotteryId = refundNotify.getData().getLotteryID();
        OneLottery lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId(lotteryId);
        if (lottery == null)
        {
            return null;
        }

        OneLottery result = queryLotteryDetail(lotteryId, true, null);
        if (result == null)
        {
            return null;
        }

        UserInfo curUser = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (curUser == null)
        {
            return lottery;
        }
        long amount = 0;
        for (OneLotteryRefundNotify.DataBean.ArrayBean bean : refundNotify.getData().getArray())
        {
            // 统计当前用户投注的所有数据
            if (curUser.getWalletAddr().equals(bean.getOppisite()))
            {
                amount += bean.getAmount();
            }
        }
        if (amount > 0)
        {
            // 刷新该用户余额
            OneLotteryManager.getInstance().getUserBalance();

            //更新message
            MessageNotify messageNotify = new MessageNotify();
            messageNotify.setTitle(OneLotteryApplication.getAppContext().getString(R.string
                    .message_can_refund_success));
            messageNotify.setContent(StringUtils.refactorLotteryName(lottery.getLotteryName()) +
                    OneLotteryApplication.getAppContext().getString(R.string.message_lottery_refund_ale) +
                    (amount / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE) + getString(R.string.transfer_zxc));
            messageNotify.setLotteryId(lotteryId);
            messageNotify.setHornContent(StringUtils.refactorLotteryName(lottery.getLotteryName()) + OneLotteryApplication
                    .getAppContext().getString(R.string.message_lottery_refund_ale));
            messageNotify.setType(ConstantCode.MessageType.MESSAGE_TYPE_REFUND);
            messageNotify.setTime(new Date());
            messageNotify.setIsRead(false);
            messageNotify.setMsgId(txId);

            checkMsgId(txId, messageNotify);

            messageNotify.setUserId(OneLotteryApi.getCurUserId());
            MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify);


            //更新明细
            TransactionDetail td = new TransactionDetail();
            td.setTxId(txId);
            td.setRemark(lottery.getLotteryName());
            td.setMyId(curUser != null ? curUser.getUserId() : "");
            td.setMyHash(curUser != null ? curUser.getWalletAddr() : "");
            td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_REFUND);
            td.setAmount(amount);
            td.setTime(new Date(refundNotify.getData().getCurrentTime()));
            checkTranTxId(txId, td);
            TransactionDetailDBHelper.getInstance().insertTransactionDetail(td);
        }
        return lottery;
    }

    // 收到退款通知 old
    public boolean onLotteryRefundOld(String retMsg, String txId)
    {
        if (StringUtils.isEmpty(retMsg) || StringUtils.isEmpty(txId))
        {
            return false;
        }

        String[] results = retMsg.split(OneLotteryApi.RES_SEP);
        String lotteryId = "";
        if (results.length == 3)
        {
            int idx = results[2].indexOf("lotteryID");
            if (idx >= 0)
            {
                try
                {
                    lotteryId = results[2].substring(idx + 12, idx + 48);
                }
                catch (IndexOutOfBoundsException e)
                {
                    e.printStackTrace();
                }
            }
        }

        if (StringUtils.isEmpty(lotteryId))
        {
            return false;
        }

        long myBetAmout = 0L;
        UserInfo curUser = UserInfoDBHelper.getInstance().getCurPrimaryAccount();

        OneLottery lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId(lotteryId);
        if (lottery == null || (lottery.getVersion() != null && lottery.getVersion() == -1))
        {
            //TODO isHistory 应该用 true吧。如果是退款了的，应该是失败的。
            lottery = queryLotteryDetail(lotteryId, true, null);
            if (lottery == null)
            {
                return false;
            } else {
                int myCount = 0;

                // 上一个接口以及获取到betList了，所以可以直接从db获取
                List<AttendBean> attendList = OneLotteryBetDBHelper.getInstance().getAttendLotteryByLotteryId(lotteryId);
                if (attendList != null && !attendList.isEmpty())
                {
                    for (AttendBean attendBean : attendList)
                    {
                        if (attendBean != null && attendBean.getAttendHash().equals(curUser.getWalletAddr()))
                        {
                            myCount += attendBean.getAttendCount();
                        }
                    }
                    myBetAmout = myCount * lottery.getOneBetCost();
                }
            }
        }

        //更新明细
        TransactionDetail td = new TransactionDetail();
        td.setTxId(txId);
        td.setRemark(lottery.getLotteryName());
        td.setMyId(curUser != null ? curUser.getUserId() : "");
        td.setMyHash(curUser != null ? curUser.getWalletAddr() : "");
        td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_REFUND);
        td.setAmount(myBetAmout);
        td.setTime(new Date());
        checkTranTxId(txId, td);
        TransactionDetailDBHelper.getInstance().insertTransactionDetail(td);

        return true;
    }

    //可以开奖的通知
    public boolean onLotteryConsensusOver(String retMsg, String txId)
    {
        if (StringUtils.isEmpty(txId))
        {
            return false;
        }

        TxDetailBetRet ret = (TxDetailBetRet) OneLotteryApi.getTxInfoByTxId(txId);
        OLLogger.i(TAG, "onLotteryConsensusOver " + ret);
        if (ret == null)
        {
            return false;
        }
        String lotteryId = ret.getLotteryID();
        OneLottery lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId(lotteryId);
        if (null == lottery)
        {
            return false;
        }

        OneLottery oneLottery = queryLotteryDetail(lotteryId, false, null);

        if (oneLottery != null)
        {
            if (oneLottery.getMaxBetCount() == oneLottery.getCurBetCount()
                    && UserLogic.isLoginUser(ret.getLauncherHash(), ret.getUserID()))
            {
                // 最后这一次投满的情况下看用户是否是当前用户投注的，如果是的话，编辑投注的明细和消息
                setBetMessage(true, ret, oneLottery, txId);
                setBetTransferDetail(ret, oneLottery, txId);
            }

            OneLotteryManager.getInstance().SendEventBus(txId, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_CONSENSUS_OVER_NOTIFY, lotteryId);
            return true;
        }

        return false;
    }

    //投注通知或者回调
    public boolean onLotteryBetCallOrNotify(String retMsg, String txId, boolean isCallback)
    {
        // TODO 处理投注失败的情况
        if (StringUtils.isEmpty(txId) || StringUtils.isEmpty(retMsg))
        {
            return betFail(retMsg, isCallback, txId);
        }

        TicketNumbersRet ticketNumbersRet = OneLotteryApi.parseTicketNumbersRet(retMsg);
        if (ticketNumbersRet == null || ticketNumbersRet.getCode() != OneLotteryApi.SUCCESS)
        {
            return betFail(retMsg, isCallback, txId);
        }

        TxDetailBetRet ret = null;
        try
        {
            ret = (TxDetailBetRet) OneLotteryApi.getTxInfoByTxId(txId);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (ret == null)
        {
            return betFail(retMsg, isCallback, txId);
        }

        final String lotteryId = ret.getLotteryID();
        OneLottery lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId(lotteryId);

        if (null == lottery)
        {
            return betFail(retMsg, isCallback, txId);
        }

        if (UserLogic.isLoginUser(null, ret.getUserID()))
        {
            // 投注,获取余额
            OneLotteryManager.getInstance().getUserBalance();
        }

        OneLottery oneLottery = queryLotteryDetail(lotteryId, false, null);
        if (oneLottery == null)
        {
            return false;
        }

        // 设置开奖时间
        if(oneLottery.getState() == ConstantCode.OneLotteryState.ONELOTTERY_STATE_CAN_REWARD)
        {
            OLLogger.d(TAG,"onLotteryBetCallOrNotify " + oneLottery.getState());
            oneLottery.setRewardCountDownTime(new Date(System.currentTimeMillis() + 1000 * 60));
            OneLotteryDBHelper.getInstance().insertOneLottery(oneLottery);
        }

        boolean result = oneLottery != null;

        //判断投注的活动是否我的好友，如果不是自动添加关注
        if(result && isCallback)
        {
            String publisherHash = oneLottery.getPublisherHash();
            boolean myFriend = FriendDBHelper.getInstance().isMyFriend(publisherHash);
            String adminHash = ConstantCode.CommonConstant.ONELOTTERY_DEFAULT_OFFICAL_HASH;
            String myHash = UserInfoDBHelper.getInstance().getCurPrimaryAccount().getWalletAddr();

            if(!myFriend && !publisherHash.equals(adminHash) && !publisherHash.equals(myHash))
            {
                Friend friend = new Friend();
                friend.setFriendHash(publisherHash);
                friend.setFriendId(oneLottery.getPublisherName());
                friend.setUserId(OneLotteryApi.getCurUserId());
                FriendDBHelper.getInstance().insertFriend(friend);
            }
        }

        if (isCallback)
        {
            setBetTransferDetail(ret, lottery, txId);
            OneLotteryManager.getInstance().SendEventBus(result, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_CALLBACK);
            setBetMessage(result, ret, lottery, txId);
        } else if (result)
        {
            OneLotteryManager.getInstance().SendEventBus(lotteryId, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_NOTIFY);
        }
        
        return result;
    }

    // 活动投注失败时处理
    private boolean betFail(String retMsg, boolean isCallback, String txId)
    {
        // TODO 如果static是空的话，去数据库查-1 state的活动
        if (isCallback && InputPwdDialog.mCurOperObject instanceof OneLottery)
        {
            // 入消息表,明细表在失败的情况下不需要入库
            final OneLottery oneLottery = (OneLottery) InputPwdDialog.mCurOperObject;
            TxDetailBetRet ret = new TxDetailBetRet();
            ret.setCreateTime(System.currentTimeMillis());
            ret.setAmount(oneLottery.getCurBetAmount() * ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE);

            setBetMessage(false, ret, oneLottery, txId);
            OneLotteryManager.getInstance().SendEventBus(false, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_CALLBACK);
        }
        return false;
    }

    /**
     * 设置交易明细
     *
     */
    private void setBetTransferDetail(TxDetailBetRet ret, OneLottery oneLottery, String txId)
    {
        UserInfo me = UserInfoDBHelper.getInstance().getCurPrimaryAccount();

        // 通知界面实时刷新
        TransactionDetail td = new TransactionDetail();
        td.setTxId(txId);
        td.setRemark(oneLottery != null ? oneLottery.getLotteryName() : "");
        td.setMyId(me != null ? me.getUserId() : "");
        td.setMyHash(me != null ? me.getWalletAddr() : "");
        td.setAmount(ret.getAmount());
        td.setTime(new Date(ret.getCreateTime()));
        td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_BET);
        checkTranTxId(txId, td);
        TransactionDetailDBHelper.getInstance().insertTransactionDetail(td);
    }

    /**
     * 设置投注消息
     *
     * @param isSuccess 是否投注成功
     * @param oneLottery
     */
    public void setBetMessage(boolean isSuccess, TxDetailBetRet ret, OneLottery oneLottery, String txID)
    {
        MessageNotify messageNotify = new MessageNotify();
        messageNotify.setIsRead(false);
        messageNotify.setTime(new Date(ret.getCreateTime()));
        messageNotify.setMsgId(txID);
        messageNotify.setContent(oneLottery.getLotteryName());
        messageNotify.setUserId(OneLotteryApi.getCurUserId());
        messageNotify.setLotteryId(oneLottery.getLotteryId());
        if (isSuccess)
        {
            messageNotify.setTitle(getString(R.string.message_bet_success));
            messageNotify.setType(ConstantCode.MessageType.MESSAGE_TYPE_BET_SUCCESS);
            messageNotify.setHornContent(StringUtils.refactorLotteryName(oneLottery.getLotteryName()) + getString(R.string
                    .message_lottery_bet_success));
        } else
        {
            messageNotify.setTitle(getString(R.string.message_bet_fail));
            messageNotify.setType(ConstantCode.MessageType.MESSAGE_TYPE_BET_FAIL);
            messageNotify.setHornContent(StringUtils.refactorLotteryName(oneLottery.getLotteryName()) + getString(R.string
                    .message_lottery_bet_fail));
        }
        checkMsgId(txID, messageNotify);
        MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify);
    }

    private String getString(int msgId)
    {
        return OneLotteryApplication.getAppContext().getString(msgId);
    }

    /**
     * 收到点击开奖或主动开奖的处理
     */
    public OneLotteryRewardOverNotify onOpenReward(String msg, String txID)
    {
        if (StringUtils.isEmpty(msg) || StringUtils.isEmpty(txID))
        {
            return null;
        }
        OneLotteryRewardOverNotify overNotify = OneLotteryApi.onOpenReward(msg, txID);
        if (null == overNotify || overNotify.getData() == null)
        {
            return null;
        }

        // 本地无此记录，就不处理
        final String lotteryId = overNotify.getData().getLotteryID();
        final OneLottery lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId(lotteryId);
        if (null == lottery || (lottery.getVersion() != null && lottery.getVersion() == -1))
        {
            return null;
        }

        // 获取活动详情，并更新DB
        boolean result = OneLotteryManager.getInstance().getLotteryHistoryDetail(lotteryId);
        if (!result)
        {
            return null;
        }

        boolean isPrizeable = false;
        int prizeType = 0;
        MessageNotify messageNotify = new MessageNotify();
        //更新余额
        UserInfo curUser = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        UserInfo userBalance = null;
        if (overNotify.getData().getAttendee().equals(curUser.getWalletAddr()))
        {
            userBalance = OneLotteryManager.getInstance().getUserBalance();
            prizeType += ConstantCode.TransactionType.TRANSACTION_TYPE_PRIZE;
            isPrizeable = true;
        }

        //分成提醒(我的活动)
        PrizeRule prizeRule = PrizeRuleDBHelper.getInstance().getPrizeRuleById(lottery.getRuleId());
        long prizeAmount = 0, percentageAmount = 0;
        if (prizeRule != null && prizeRule.getPercentage() != null/* && prizeRule.getPercentage() < 100*/)
        {
            if (lottery.getPublisherHash().equals(curUser.getWalletAddr()))
            {
                if (userBalance == null)
                {
                    OneLotteryManager.getInstance().getUserBalance();
                }
                isPrizeable = true;
                prizeType += ConstantCode.TransactionType.TRANSACTION_TYPE_PERCENTAGE;
            }
            prizeAmount = lottery.getBetTotalAmount() * prizeRule.getPercentage() / 100;
            percentageAmount = lottery.getBetTotalAmount() - prizeAmount;
        }

        //更新message
        if (isPrizeable)
        {
            insertTDAndMsg(lotteryId, lottery.getLotteryName(), new Date(), txID, overNotify.getData().getAttendeeName(),
                    overNotify.getData().getAttendee(), prizeType, prizeAmount, percentageAmount);
        }

        OneLotteryManager.getInstance().SendEventBus(lotteryId, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_OVER_NOTIFY);

        return overNotify;
    }

    public TransferNotify onTransfer(String msg, String txId, boolean isCallback)
    {
        // 获取成功后，还应该编辑消息中心的消息，通知消息中心UI更新。
        // 如果收到的是别人转账或者官方充值，也可以收到的话，就容易处理明细查询了,解析s
        TransferNotify notify = OneLotteryApi.transferNotify(msg);
        if (isCallback)
        {
            OneLotteryManager.getInstance().SendEventBus(notify != null && notify.getCode() == OneLotteryApi.SUCCESS,
                    OLMessageModel.STMSG_MODEL_TRANSFER_CALLBACK);
        }

        if (UserLogic.isLoginUser(notify.getData().getOwner(), notify.getData().getOwnUserId())
                || UserLogic.isLoginUser(notify.getData().getOppisite(), notify.getData().getOppisiteUserId()))
        {
            // 转账或者官方充值,更新余额
            OneLotteryManager.getInstance().getUserBalance();
            return notify;
        }

        UserInfo me = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (notify != null && notify.getCode() == OneLotteryApi.SUCCESS && notify.getData() != null && me != null)
        {
            TxDetailTransferRet txDetailRet = new TxDetailTransferRet();
            txDetailRet.setTime(System.currentTimeMillis());
            txDetailRet.setLauncherHash(notify.getData().getOwner());
            txDetailRet.setUserID(notify.getData().getOwnUserId());
            txDetailRet.setNameTo(notify.getData().getOppisiteUserId());
            txDetailRet.setUserCertTo(notify.getData().getOppisite());
            txDetailRet.setAmount(notify.getData().getAmount());
            txDetailRet.setFee(notify.getData().getFee());

            TD_coinTransfer(txDetailRet, txId, null, me, false);
        } else if (notify == null || notify.getCode() != OneLotteryApi.SUCCESS)
        {
            // 如果转账失败都设置消息(怎么获取所转用户的hash和name)，然后发送给小喇叭显示
            MessageNotify transferMsg = new MessageNotify();
            transferMsg.setMsgId(txId);

            String addressTo = OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).getTransferToUserHash();
            String nameTo = OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).getTransferToUserId();
            transferMsg.setContent(String.format(getString(R.string.message_transfer_message),
                    StringUtils.isEmpty(nameTo) ? StringUtils.getHeadTailString(addressTo)
                            : StringUtils.getHeadTailString(nameTo),
                    ((float) OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).getTransferAmount()
                            / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE) + ""));
             // 转账失败的话，没有交易ID，所以也就不跳转详情了
            transferMsg.setTitle(getString(R.string.transfer_fail));
            transferMsg.setHornContent(transferMsg.getContent() + getString(R.string.fail));
            transferMsg.setType(ConstantCode.MessageType.MESSAGE_TYPE_TRANSFOR_FAIL);
            transferMsg.setTime(new Date());
            transferMsg.setIsRead(false);
            transferMsg.setUserId(OneLotteryApi.getCurUserId());
            transferMsg.setLotteryId(txId);
            checkMsgId(txId, transferMsg);
            MessageNotifyDBHelper.getInstance().insertMessageNotify(transferMsg);
        }
        return notify;
    }

    public ArrayList<TransactionDetail> getHistoryTransacetions(int REQUEST_COUNT)
    {
        UserInfo me = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (me == null)
        {
            return null;
        }

        ArrayList<TransactionDetail> trans = new ArrayList<>();

        long blockHeight = me.getLastGetBlockHeight();
        // 说明后台线程在获取数据的时候还未结束
        if (blockHeight == 0)
        {
            // 那就从当前最新状态开始查找
            blockHeight = me.getCurBlockHeight();
            if (blockHeight == 0)
            {
                // 实在没有，说明还没有当前余额信息，那就不用处理了。
                return null;
            }
        }

        long preBlockHeight;

        List<String> myTxIds = getMyTxIDs(me);

        String newFlag = getCurLotteryNewFlag();

        String txIDS;

        ZXCoinBalanceRet.DataBean zxCoin = OneLotteryApi.getZXCoinStateByBlockID(blockHeight);
        if (zxCoin != null && UserLogic.isLoginUser(zxCoin.getOwner(), zxCoin.getName()))
        {
            txIDS = zxCoin.getTxnIDs();
            preBlockHeight = zxCoin.getPrevBlockHeight();
        } else
        {
            return null;
        }

        // 处理每个区块一直到当前的blockHeight和获取到的prevBlockHeight一致 （curBlockHeight内的交易IDS可能不全）
        while (blockHeight != preBlockHeight)
        {
            String[] txIdArr = txIDS.split(" ");
            if (txIdArr != null && txIdArr.length > 0)
            {
                List<TransactionDetail> ts = refetchTxInfo(me, myTxIds, newFlag, txIdArr, null);
                if (ts != null && !ts.isEmpty())
                {
                    trans.addAll(ts);
                }
                if (trans.size() >= REQUEST_COUNT)
                {
                    break;
                }
            }
            // 处理前一个区块
            blockHeight = preBlockHeight;
            zxCoin = OneLotteryApi.getZXCoinStateByBlockID(blockHeight);
            if (zxCoin != null && UserLogic.isLoginUser(zxCoin.getOwner(), zxCoin.getName()))
            {
                txIDS = zxCoin.getTxnIDs();
                preBlockHeight = zxCoin.getPrevBlockHeight();
            } else
            {
                break;
            }
        }


        // 重新获取一次，已防止覆盖别的代码有更新内容。
        me = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (me != null)
        {
            OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).setGetLastOver(me.getUserId());
        }

        if (me.getLastGetBlockHeight() == 0 || blockHeight < me.getLastGetBlockHeight())
        {
            // 如果往底层获取到新数据了，那就更新lastGetBlockHeight
            me.setLastGetBlockHeight(blockHeight);
            UserInfoDBHelper.getInstance().insertUserInfo(me);
        }

        // 已经获取到全部的交易数据了
        return trans;
    }

    protected void refreshTransactions(ZXCoinBalanceRet balanceRet, UserInfo userInfo, int REQUEST_COUNT)
    {
        if (balanceRet == null && balanceRet.getData() == null)
        {
            return;
        }
        // 不是当前账户，退出
        if (!UserLogic.isLoginUser(balanceRet.getData().getOwner(), balanceRet.getData().getName()))
        {
            return;
        }

        // 当前账户数据为最新，退出
        if (userInfo.getCurBlockHeight() != null && balanceRet.getData().getBlockHeight() == userInfo.getCurBlockHeight()
                && StringUtils.nullToEmpty(userInfo.getTxnIDs()).equals(balanceRet.getData().getTxnIDs()))
        {
            // 如果当前账户的余额是最新的，用户状态和刷新余额什么的不需要做了啊
//            UserInfoDBHelper.getInstance().insertUserInfo(userInfo);
//            OneLotteryManager.getInstance().SendEventBus(null,OLMessageModel.STMSG_MODEL_REFRSH_SETTING_BALANCE);
            return;
        }

        ArrayList<TransactionDetail> trans = new ArrayList<>();

        // 获取当前最新区块内的所有交易ID
        long blockHeight = balanceRet.getData().getBlockHeight();
        long preBlockHeight = balanceRet.getData().getPrevBlockHeight();

        List<String> myTxIds = getMyTxIDs(userInfo);

        String newFlag = getCurLotteryNewFlag();

        String txIDS = balanceRet.getData().getTxnIDs();

        // 处理每个区块一直到和本地的prevBlockHeight一致 （curBlockHeight内的交易IDS可能不全）
        while (blockHeight != userInfo.getPrevBlockHeight())
        {
            String[] txIdArr = txIDS.split(" ");
            if (txIdArr != null && txIdArr.length > 0)
            {
                List<TransactionDetail> ts = refetchTxInfo(userInfo, myTxIds, newFlag, txIdArr, null);
                if (ts != null && !ts.isEmpty())
                {
                    trans.addAll(ts);
                }
//                if (trans.size() >= REQUEST_COUNT)
//                {
//                    break;
//                }
            }
            // 处理前一个区块
            if (blockHeight == preBlockHeight)
            {
                break;
            }
            blockHeight = preBlockHeight;
            if (blockHeight != userInfo.getPrevBlockHeight())
            {
                ZXCoinBalanceRet.DataBean zxCoin = OneLotteryApi.getZXCoinStateByBlockID(blockHeight);
                if (zxCoin != null && UserLogic.isLoginUser(zxCoin.getOwner(), zxCoin.getName()))
                {
                    txIDS = zxCoin.getTxnIDs();
                    preBlockHeight = zxCoin.getPrevBlockHeight();
                } else
                {
                    break;
                }
            }
        }

        if (userInfo.getLastGetBlockHeight() == 0 || userInfo.getLastGetBlockHeight() > blockHeight)
        {
            userInfo.setCurBlockHeight(balanceRet.getData().getBlockHeight());
            userInfo.setPrevBlockHeight(balanceRet.getData().getPrevBlockHeight());
            userInfo.setBalance(balanceRet.getData().getBalance());
            userInfo.setTxnIDs(balanceRet.getData().getTxnIDs());

            // 标记当前用户处理到了哪个区块高度了
            userInfo.setLastGetBlockHeight(blockHeight);

            // 直到最新余额对应的递归区块高度和目前DB中的区块preBlockHeight相等，才能把长久未登录的数据给补上
            UserInfoDBHelper.getInstance().insertUserInfo(userInfo);
        }
    }

    /**
     * 获取交易的信息并插入活动投注，交易明细，消息表
     * */
    private List<TransactionDetail> refetchTxInfo(UserInfo userInfo, List<String> myTxIds, String newFlag, String[] txIdArr, List<OneLotteryBet> myBetList)
    {
        List<TransactionDetail> trans = new ArrayList<>();

        for (int i = 0; i < txIdArr.length; i++)
        {
            TransactionDetail td = null;
            List<TransactionDetail> tds = null;
            String txId = txIdArr[i];

            if (!StringUtils.isEmpty(txId) && i > 0 && txId.equals(txIdArr[i - 1]))
            {
                continue;
            }

            if (myTxIds.contains(txId))
            {
                continue;
            }
            TxDetailRet txDetailRet = OneLotteryApi.getTxInfoByTxId(txId);
            if (txDetailRet != null)
            {
                switch (txDetailRet.getTxType())
                {
                    case TxDetailRet.TYPE_TRANSFER_DETAIL_LOTTERY_ADD:
                        td = TD_addLottery((TxDetailLotteryAddRet) txDetailRet, txId, newFlag, userInfo);
                        break;
                    case TxDetailRet.TYPE_TRANSFER_DETAIL_LOTTERY_EDT:
                        td = TD_editLottery((TxDetailLotteryModifyRet) txDetailRet, txId, newFlag, userInfo);
                        break;
                    case TxDetailRet.TYPE_TRANSFER_DETAIL_BET:
                        td = TD_betLottery((TxDetailBetRet) txDetailRet, txId, newFlag, userInfo, myBetList);
                        break;
                    case TxDetailRet.TYPE_TRANSFER_DETAIL_COINTRANSFER:
                        td = TD_coinTransfer((TxDetailTransferRet) txDetailRet, txId, newFlag, userInfo, true);
                        break;
                    case TxDetailRet.TYPE_TRANSFER_DETAIL_BET_OVER:
                        tds = TD_betOver((TxDetailBetOverRet) txDetailRet, txId, newFlag, userInfo);
                        break;
                    case TxDetailRet.TYPE_TRANSFER_DETAIL_REFUND:
                        td = TD_refund((TxDetailRefundRet) txDetailRet, txId, newFlag, userInfo);
                        break;
                    case TxDetailRet.TYPE_TRANSFER_DETAIL_WITHDRAW:
                        td = TD_withdraw((TxDetailWithdrawRet) txDetailRet, txId, newFlag, userInfo);
                        break;
                    case TxDetailRet.TYPE_TRANSFER_DETAIL_WITHDRAW_CONFIRM:
                        td = TD_withdrawConfirm((TxDetailWithdrawConfirmRet) txDetailRet, txId, newFlag, userInfo);
                        break;
                    case TxDetailRet.TYPE_TRANSFER_DETAIL_WITHDRAW_APPEALDONE:
                        td = TD_withdrawAppealDone((TxDetailWithdrawAppealDoneRet) txDetailRet, txId, newFlag, userInfo);
                        break;
                    case TxDetailRet.TYPE_TRANSFER_DETAIL_WITHDRAW_FAIL:
                        td = TD_withdrawFail((TxDetailWithdrawFailRet) txDetailRet, txId, newFlag, userInfo);
                        break;

                    default:
                        break;
                }
            }
            if (td != null)
            {
                trans.add(td);
            }
            if (tds != null && !tds.isEmpty())
            {
                trans.addAll(tds);
            }
        }

        return trans;
    }

    // 获取当前用户的所有交易ID
    @NonNull
    private List<String> getMyTxIDs(UserInfo userInfo)
    {
        if (userInfo == null)
        {
            userInfo = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        }
        if (userInfo == null)
        {
            return new ArrayList<>();
        }

        List<TransactionDetail> myTxDetails = TransactionDetailDBHelper.getInstance()
                .getTransactionDetailByUserInfo(userInfo.getWalletAddr(), userInfo.getUserId());
        List<String> myTxIds = new ArrayList<>();
        if (myTxDetails != null && !myTxDetails.isEmpty())
        {
            for (TransactionDetail td : myTxDetails)
            {
                myTxIds.add(td.getTxId());
            }
        }
        return myTxIds;
    }

    // 获取当前最新的newFlag
    private String getCurLotteryNewFlag()
    {
        List<OneLottery> ols = OneLotteryDBHelper.getInstance().getNewestLotteresUpdateFlag();
        return ols != null && !ols.isEmpty() ? ols.get(0).getUpdateFlag() : UUID.randomUUID().toString();
    }

    /**
     * 更新长久未登录情况下活动详情和投注详情
     *
     * @param oldHistory
     * @param lottery
     * @param myBetList
     */
    private void refreshBetAndTransactions(OneLotteryOldHistoryQueryRet oldHistory, OneLottery lottery, List<OneLotteryBet> myBetList)
    {
        if (oldHistory == null || oldHistory.getData() == null || lottery == null)
        {
            return;
        }
        OneLotteryOldHistoryQueryRet.DataBean oldLotteryData = oldHistory.getData();

        // 当前活动的交易数据为最新，退出
        if (lottery.getCurBlockHeight() != null && oldLotteryData.getBlockHeight() == lottery.getCurBlockHeight()
                && StringUtils.nullToEmpty(oldLotteryData.getTxnIDs()).equals(lottery.getBetTxnIDs()))
        {
            return;
        }

        UserInfo userInfo = UserInfoDBHelper.getInstance().getCurPrimaryAccount();

        // 获取当前最新区块内的所有交易ID
        long blockHeight = oldHistory.getData().getBlockHeight();
        long preBlockHeight = oldHistory.getData().getPreBlockHeight();

        List<String> myTxIds = getMyTxIDs(userInfo);

        String txIDS = oldHistory.getData().getTxnIDs();

        String newFlag = getCurLotteryNewFlag();

        // 处理每个区块一直到和本地活动的prevBlockHeight一致 （curBlockHeight内的交易IDS可能不全）
        while (blockHeight != lottery.getPrevBlockHeight())
        {
            String[] txIdArr = txIDS.split(" ");
            if (txIdArr != null && txIdArr.length > 0)
            {
                refetchTxInfo(userInfo, myTxIds, newFlag, txIdArr, myBetList);
            }
            // 处理前一个区块
            if (blockHeight == preBlockHeight)
            {
                break;
            }
            blockHeight = preBlockHeight;
            if (blockHeight != lottery.getPrevBlockHeight())
            {
                OneLotteryOldHistoryQueryRet.DataBean preLotteryData =
                        OneLotteryApi.oneChainGetLotteryStateByBlockID(blockHeight, lottery.getLotteryId());

                if (preLotteryData != null)
                {
                    txIDS = preLotteryData.getTxnIDs();
                    preBlockHeight = preLotteryData.getPreBlockHeight();
                } else
                {
                    break;
                }
            }
        }
    }

    // 根据交易详情，将退款数据入库
    private TransactionDetail TD_refund(TxDetailRefundRet txDetailRet, String txId, String newFlag, UserInfo me)
    {
        if (txDetailRet == null)
        {
            return null;
        }
        OneLottery lottery = getOneLottery(txDetailRet.getLotteryID());
        if (lottery == null)
        {
            return null;
        }

        // 获取活动的投注列表，计算用户的所有投注
        queryLotteryBetList(lottery);

        long myBetAmout = 0L;

        int myCount = 0;

        // queryLotteryBetList获取到betList，所以可以直接从db获取
        List<AttendBean> attendList = OneLotteryBetDBHelper.getInstance().getAttendLotteryByLotteryId(lottery.getLotteryId());
        if (attendList != null && !attendList.isEmpty())
        {
            for (AttendBean attendBean : attendList)
            {
                if (attendBean != null && attendBean.getAttendHash().equals(me.getWalletAddr()))
                {
                    myCount += attendBean.getAttendCount();
                }
            }
            myBetAmout = myCount * lottery.getOneBetCost();
        }

        if (myBetAmout > 0)
        {
            //更新明细
            TransactionDetail td = new TransactionDetail();
            td.setTxId(txId);
            td.setRemark(lottery.getLotteryName());
            td.setMyId(me.getUserId());
            td.setMyHash(me.getWalletAddr());
            td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_REFUND);
            td.setAmount(myBetAmout);
            td.setTime(new Date(txDetailRet.getCurrentTime()));
            checkTranTxId(txId, td);
            TransactionDetailDBHelper.getInstance().insertTransactionDetail(td);

            //更新message
            MessageNotify messageNotify = new MessageNotify();
            messageNotify.setTitle(getString(R.string.message_can_refund_success));
            messageNotify.setContent(StringUtils.refactorLotteryName(lottery.getLotteryName()) + getString(R.string.message_lottery_refund_ale) +

                    td.getAmount() / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE + getString(R.string.transfer_zxc));
            messageNotify.setLotteryId(txDetailRet.getLotteryID());
            messageNotify.setHornContent(StringUtils.refactorLotteryName(lottery.getLotteryName()) + OneLotteryApplication
                    .getAppContext().getString(R.string.message_lottery_refund_ale));
            messageNotify.setType(ConstantCode.MessageType.MESSAGE_TYPE_REFUND);
            messageNotify.setTime(td.getTime());
            messageNotify.setIsRead(false);
            messageNotify.setMsgId(txId);

            messageNotify.setUserId(OneLotteryApi.getCurUserId());
            checkMsgId(txId, messageNotify);
            MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify);
            return td;
        }
        return null;
    }
    // 根据交易详情，将提现数据入库
    private TransactionDetail TD_withdraw(TxDetailWithdrawRet txDetailRet, String txId, String newFlag, UserInfo me)
    {
        if (txDetailRet == null)
        {
            return null;
        }
        WithdrawRecord record = WithdrawRecordDBHelper.getInstance().getRecordByKey(txDetailRet.getTxid());
        if (record == null)
        {
            record = new WithdrawRecord();
            record.setTxId(txDetailRet.getTxid());
            record.setAccountId(txDetailRet.getAccountInfoRet().getAccountId());
            record.setAccountName(txDetailRet.getAccountInfoRet().getAccountName());
            record.setOpeningBankName(txDetailRet.getAccountInfoRet().getBankName());
            record.setAmount(txDetailRet.getAmount());
            record.setCreateTime(new Date(txDetailRet.getModifyTime()));
            record.setModifyTime(new Date(txDetailRet.getModifyTime()));
            record.setRemark(txDetailRet.getTxid());
            record.setUserId(txDetailRet.getUserId());
            record.setUserHash(txDetailRet.getLauncherHash());
            record.setState(ConstantCode.WithdrawType.WITHDRAW_TYPE_APPLYING);

            WithdrawRecordDBHelper.getInstance().insert(record);
        }

        /*倒数据的时候不再入消息表
        // 申请提现需要如消息表即可
        // 更新message
        MessageNotify messageNotify = new MessageNotify();
        messageNotify.setUserId(txDetailRet.getUserId());
        messageNotify.setMsgId(txId);
        messageNotify.setNewTxId(txDetailRet.getTxid());
        messageNotify.setIsRead(false);
        messageNotify.setTime(new Date(txDetailRet.getModifyTime()));
        messageNotify.setType(ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_SEND_SUCCESS);
        messageNotify.setTitle(getString(R.string.message_withdraw_send_sucess_title));

        double am = ((double) txDetailRet.getAmount() / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE);
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String zxCorn = StringUtils.refactorLotteryName(decimalFormat.format(am));

        messageNotify.setContent(zxCorn + getString(R.string.message_withdraw_send_sucess_content));
        messageNotify.setHornContent(zxCorn + getString(R.string.message_withdraw_send_sucess_horn));

        checkWithdrawMsgId(txId, messageNotify);
        MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify);*/

        return null;
    }

    // 根据交易详情，将提现确认数据入库
    private TransactionDetail TD_withdrawConfirm(TxDetailWithdrawConfirmRet txDetailRet, String txId, String newFlag, UserInfo me)
    {
        if (txDetailRet == null)
        {
            return null;
        }
        WithdrawRecord record = WithdrawRecordDBHelper.getInstance().getRecordByKey(txDetailRet.getTxid());
        if (record == null)
        {
            record = WithdrawLogic.queryWithdraw(txDetailRet.getTxId());
        }
        if (record != null)
        {
            //更新明细
            TransactionDetail td = new TransactionDetail();
            td.setTxId(txId);
            td.setRemark(record.getTxId());
            td.setMyId(record.getUserId());
            td.setMyHash(record.getUserHash());
            if (ConstantCode.CommonConstant.ONELOTTERY_DEFAULT_OFFICAL_HASH.equals(txDetailRet.getLauncherHash()))
            {
                td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_WITHDRAW_AUTO_CONFIRM);
            } else
            {
                td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_WITHDRAW_CONFIRM);
            }
            td.setAmount(record.getAmount());
            td.setTime(record.getModifyTime());
            checkTranTxId(txId, td);
//            TransactionDetailDBHelper.getInstance().insertTransactionDetail(td);

            return td;
        }

        return null;
    }

    // 根据交易详情，将提现失败的结果数据入库
    private TransactionDetail TD_withdrawFail(TxDetailWithdrawFailRet txDetailRet, String txId, String newFlag, UserInfo me)
    {
        if (txDetailRet == null)
        {
            return null;
        }
        WithdrawRecord record = WithdrawRecordDBHelper.getInstance().getRecordByKey(txDetailRet.getTxid());
        if (record == null)
        {
            record = WithdrawLogic.queryWithdraw(txDetailRet.getTxId());
        }
        if (record != null)
        {

            // 提现失败的情况下，入消息表
            // 更新message
            MessageNotify messageNotify = new MessageNotify();
            messageNotify.setUserId(txDetailRet.getUserId());
            messageNotify.setMsgId(txId);
            messageNotify.setNewTxId(txDetailRet.getTxId());
            messageNotify.setIsRead(false);
            messageNotify.setTime(new Date(txDetailRet.getModifyTime()));
            messageNotify.setType(ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_FAIL);

            messageNotify.setTitle(getString(R.string.message_withdraw_fail_title));
            messageNotify.setContent(getString(R.string.message_withdraw_fail_content) + txDetailRet.getRemark());

            double am = ((double) record.getAmount() / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String zxCorn = StringUtils.refactorLotteryName(decimalFormat.format(am));

            messageNotify.setHornContent(zxCorn + getString(R.string.message_withdraw_fail_horn));

            checkWithdrawMsgId(txId, messageNotify);
            MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify);
        }

        return null;
    }
// 根据交易详情，将提现的申诉结果数据入库
    private TransactionDetail TD_withdrawAppealDone(TxDetailWithdrawAppealDoneRet txDetailRet, String txId, String newFlag, UserInfo me)
    {
        if (txDetailRet == null)
        {
            return null;
        }
        WithdrawRecord record = WithdrawRecordDBHelper.getInstance().getRecordByKey(txDetailRet.getTxid());
        if (record == null)
        {
            record = WithdrawLogic.queryWithdraw(txDetailRet.getTxId());
        }

        TransactionDetail td = null;

        if (record != null)
        {
            if (txDetailRet.getResult() == 1)
            {
                //更新明细
                td = new TransactionDetail();
                td.setTxId(txId);
                td.setRemark(record.getTxId());
                td.setMyId(record.getUserId());
                td.setMyHash(record.getUserHash());
                td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_WITHDRAW_APPEAL_DONE);
                td.setAmount(record.getAmount());
                td.setTime(record.getModifyTime());
                checkTranTxId(txId, td);
                TransactionDetailDBHelper.getInstance().insertTransactionDetail(td);

            } else if (txDetailRet.getResult() == 2)
            {
                //申诉失败的情况下，入消息表
                // 更新message
                MessageNotify messageNotify = new MessageNotify();
                messageNotify.setUserId(txDetailRet.getUserId());
                messageNotify.setMsgId(txId);
                messageNotify.setNewTxId(txDetailRet.getTxId());
                messageNotify.setIsRead(false);
                messageNotify.setTime(new Date(txDetailRet.getModifyTime()));
                messageNotify.setType(ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_FAIL);
                messageNotify.setTitle(getString(R.string.message_withdraw_fail_title));
                messageNotify.setContent(getString(R.string.message_withdraw_fail_content) + record.getRemark());

                double am = ((double) record.getAmount() / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE);
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                String zxCorn = StringUtils.refactorLotteryName(decimalFormat.format(am));

                messageNotify.setHornContent(zxCorn + getString(R.string.message_withdraw_fail_horn));

                checkWithdrawMsgId(txId, messageNotify);
                MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify);
            }

        }

        return td;
    }

    // 根据交易详情，将开奖数据入库
    private List<TransactionDetail> TD_betOver(TxDetailBetOverRet txDetailRet, String txId, String newFlag, UserInfo me)
    {
        if (txDetailRet == null)
        {
            return null;
        }
        // 本地无此记录，就不处理
        OneLottery lottery = getOneLottery(txDetailRet.getLotteryID());

        boolean isPrizeable = false;
        int prizeType = 0;
        //更新余额
        // 需要拿到中奖者ID，和创建者ID，只有这样才可以继续计算中奖或者分成或者两种都有
        String prizeTxID = lottery != null ? lottery.getPrizeTxID() : "";
        TxDetailRet detailRet = OneLotteryApi.getTxInfoByTxId(prizeTxID);
        if (null != detailRet && detailRet.getLauncherHash().equals(me.getWalletAddr()))
        {
            isPrizeable = true;
            prizeType += ConstantCode.TransactionType.TRANSACTION_TYPE_PRIZE;
        }

        //分成提醒(我的活动
        PrizeRule prizeRule = PrizeRuleDBHelper.getInstance().getPrizeRuleById(lottery.getRuleId());
        long prizeAmount = 0, percentageAmount = 0;
        if (prizeRule != null && prizeRule.getPercentage() != null/* && prizeRule.getPercentage() < 100*/)
        {
            if (lottery.getPublisherHash().equals(me.getWalletAddr()))
            {
                isPrizeable = true;
                prizeType += ConstantCode.TransactionType.TRANSACTION_TYPE_PERCENTAGE;
            }
            prizeAmount = lottery.getBetTotalAmount() * prizeRule.getPercentage() / 100;
            percentageAmount = lottery.getBetTotalAmount() - prizeAmount;
        }

        //更新message
        if (isPrizeable)
        {
            return insertTDAndMsg(txDetailRet.getLotteryID(), lottery.getLotteryName(), new Date(txDetailRet.getCurrentTime()), txId,
                    me.getUserId(), me.getWalletAddr(), prizeType, prizeAmount, percentageAmount);
        }

        return null;
    }

    private List<TransactionDetail> insertTDAndMsg(String lotteryID, String lotteryName, Date time, String txId, String uid, String hash, int prizeType,
                                long prizeAmount, long percentageAmount)
    {
        List<TransactionDetail> trans = new ArrayList<>();
        MessageNotify messageNotify = new MessageNotify();
        messageNotify.setTitle(getString(R.string.message_lottery_prize));
        messageNotify.setContent(getString(R.string.message_see_content));
        messageNotify.setHornContent(getString(R.string.message_lottery_prize)
                + "," + getString(R.string.message_see_content));
        messageNotify.setLotteryId(lotteryID);
        messageNotify.setTime(time);
        messageNotify.setIsRead(false);
        messageNotify.setMsgId(txId);
        messageNotify.setType(ConstantCode.MessageType.MESSAGE_TYPE_PRIZE);
        messageNotify.setUserId(OneLotteryApi.getCurUserId());

        //更新明细
        TransactionDetail td = new TransactionDetail();
        td.setTxId(txId);
        td.setRemark(lotteryName);
        td.setMyId(uid);
        td.setMyHash(hash);
        td.setTime(messageNotify.getTime());
        td.setAmount(prizeAmount);
        td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_PRIZE);

        TransactionDetail tdp = null;
        if (prizeType == ConstantCode.TransactionType.TRANSACTION_TYPE_PRIZE)
        {
            checkMsgId(txId, messageNotify);
            MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify);

            checkTranTxId(txId, td);
            TransactionDetailDBHelper.getInstance().insertTransactionDetail(td);
        } else if (prizeType == ConstantCode.TransactionType.TRANSACTION_TYPE_PERCENTAGE/* && percentageAmount > 0*/)
        {
            messageNotify.setTitle(OneLotteryApplication.getAppContext().getString(R.string
                    .message_lottery_percentage));
            messageNotify.setHornContent(OneLotteryApplication.getAppContext().
                    getString(R.string.message_my_create_lottery_prize));
            messageNotify.setType(ConstantCode.MessageType.MESSAGE_TYPE_PERCENTAGE);
            checkMsgId(txId, messageNotify);
            MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify);

            td.setAmount(percentageAmount);
            td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_PERCENTAGE);
            checkTranTxId(txId, td);
            TransactionDetailDBHelper.getInstance().insertTransactionDetail(td);
        } else if (prizeType == ConstantCode.TransactionType.TRANSACTION_TYPE_PERCENTAGE
                + ConstantCode.TransactionType.TRANSACTION_TYPE_PRIZE)
        {
            checkMsgId(txId, messageNotify);
            MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify);

            checkTranTxId(txId, td);
            TransactionDetailDBHelper.getInstance().insertTransactionDetail(td);

//            if (percentageAmount > 0)
            {
                MessageNotify mn = (MessageNotify) messageNotify.clone();
                mn.setTitle(OneLotteryApplication.getAppContext().getString(R.string
                        .message_lottery_percentage));
                mn.setHornContent(OneLotteryApplication.getAppContext().
                        getString(R.string.message_my_create_lottery_prize));
                mn.setType(ConstantCode.MessageType.MESSAGE_TYPE_PERCENTAGE);
                mn.setMsgId(mn.getMsgId() + "_1");//处理分成的时候就应该_1，不用再check了，麻烦
                MessageNotifyDBHelper.getInstance().insertMessageNotify(mn);


                tdp = (TransactionDetail) td.clone();
                tdp.setTxId(tdp.getTxId() + "_1");//处理分成的时候就应该_1，不用再check了，麻烦
                tdp.setAmount(percentageAmount);
                tdp.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_PERCENTAGE);
                TransactionDetailDBHelper.getInstance().insertTransactionDetail(tdp);
            }
        }

        trans.add(td);
        if (tdp != null)
        {
            trans.add(tdp);
        }
        return trans;
    }

    // 根据交易详情，将转账数据入库
    private TransactionDetail TD_coinTransfer(TxDetailTransferRet txDetailRet, String txId, String newFlag, UserInfo me, boolean isRollback)
    {
        if (txDetailRet == null || me == null)
        {
            return null;
        }
        // 通知明细界面实时刷新
        TransactionDetail td = new TransactionDetail();
        td.setTxId(txId);
        td.setMyId(me.getUserId());
        td.setMyHash(me.getWalletAddr());
        td.setTime(new Date(txDetailRet.getTime()));

        if (UserLogic.isLoginUser(txDetailRet.getLauncherHash(), txDetailRet.getUserID()))
        {
            // 我转账给别人
            td.setAmount(txDetailRet.getAmount() + txDetailRet.getFee()); // 如果是你给别人转账的话，需要将fee加进去
            td.setOppositeUserId(txDetailRet.getNameTo());
            td.setOppositeHash(txDetailRet.getUserCertTo());
            td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_TRANSFOR);

        } else if (UserLogic.isLoginUser(txDetailRet.getUserCertTo(), txDetailRet.getNameTo()))
        {
            td.setAmount(txDetailRet.getAmount());
            td.setOppositeUserId(txDetailRet.getUserID());
            td.setOppositeHash(txDetailRet.getLauncherHash());
            if (UserLogic.isOfficalUser(txDetailRet.getLauncherHash(), txDetailRet.getUserID()))
            {
                // 官方给我充值
                td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_TRANSFOR_FROM_ADMIN);
            } else {
                // 别人给我转账
                td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_TRANSFOR_FROM_OTHER);
            }
        }

        checkTranTxId(txId, td);

        TransactionDetailDBHelper.getInstance().insertTransactionDetail(td);

        if (isRollback && td.getType() == ConstantCode.TransactionType.TRANSACTION_TYPE_TRANSFOR)
        {
            return td;
        }

        // 如果转账成功,设置消息，然后发送给小喇叭显示
        MessageNotify transferMsg = new MessageNotify();
        transferMsg.setMsgId(txId);
        if (td.getType() == ConstantCode.TransactionType.TRANSACTION_TYPE_TRANSFOR)
        {
            transferMsg.setType(ConstantCode.MessageType.MESSAGE_TYPE_TRANSFOR_SUCCESS);

            transferMsg.setTitle(getString(R.string.transfer_success));
            transferMsg.setContent(String.format(getString(R.string.message_transfer_message),
                    StringUtils.isEmpty(txDetailRet.getNameTo()) ? StringUtils.getHeadTailString(txDetailRet.getUserCertTo())
                            : StringUtils.getHeadTailString(txDetailRet.getNameTo()),
                    ((float) (txDetailRet.getAmount()/* + txDetailRet.getFee()*/) / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE) + ""));
            transferMsg.setHornContent(transferMsg.getContent() + getString(R.string.success));
        } else
        {
            transferMsg.setType(ConstantCode.MessageType.MESSAGE_TYPE_TRANSFOR_FROM_OTHER);

            if(td.getType() == ConstantCode.TransactionType.TRANSACTION_TYPE_TRANSFOR_FROM_ADMIN)
            {
                transferMsg.setTitle(getString(R.string.transfer_type_transfer_from_admin));
                transferMsg.setContent(String.format(getString(R.string.message_save_wallet),(txDetailRet.getAmount()/10000)));
                transferMsg.setHornContent(String.format(getString(R.string.message_transfer_admin),(txDetailRet.getAmount()/10000)));
            }
            else
            {
                transferMsg.setTitle(getString(R.string.transfer_success));
                transferMsg.setContent(String.format(getString(R.string.message_transfer_in_message),
                        StringUtils.isEmpty(txDetailRet.getUserID()) ? StringUtils.getHeadTailString(txDetailRet.getLauncherHash())
                                : StringUtils.getHeadTailString(txDetailRet.getUserID()),
                        ((float) (txDetailRet.getAmount()/* + txDetailRet.getFee()*/) / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE) + ""));
                transferMsg.setHornContent(transferMsg.getContent() + getString(R.string.success));
            }
        }

        transferMsg.setTime(td.getTime());
        transferMsg.setIsRead(false);
        transferMsg.setLotteryId(txId);
        transferMsg.setUserId(OneLotteryApi.getCurUserId());

        checkMsgId(txId, transferMsg);
        MessageNotifyDBHelper.getInstance().insertMessageNotify(transferMsg);
        return td;
    }

    // 根据交易详情，将活动的投注数据入库
    private TransactionDetail TD_betLottery(TxDetailBetRet txDetailRet, String txId, String newFlag, UserInfo me, List<OneLotteryBet> myBetList)
    {
        if (txDetailRet == null)
        {
            return null;
        }

        OneLottery lottery = getOneLottery(txDetailRet.getLotteryID());
        if (lottery == null)
        {
            return null;
        }

        if (UserLogic.isLoginUser(txDetailRet.getLauncherHash(), txDetailRet.getUserID()))
        {
            // 通知明细界面实时刷新
            TransactionDetail td = new TransactionDetail();
            td.setTxId(txId);
            td.setRemark(lottery.getLotteryName());
            td.setMyHash(txDetailRet.getLauncherHash());
            td.setMyId(txDetailRet.getUserID());
            td.setAmount(txDetailRet.getAmount());
            td.setTime(new Date(txDetailRet.getCreateTime()));
            td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_BET);
            checkTranTxId(txId, td);
            TransactionDetailDBHelper.getInstance().insertTransactionDetail(td);

            /* 通知消息不入库
            MessageNotify messageNotify = new MessageNotify();
            messageNotify.setIsRead(false);
            messageNotify.setTime(td.getTime());
            messageNotify.setMsgId(txId);
            messageNotify.setContent(td.getRemark());
            messageNotify.setLotteryId(txDetailRet.getLotteryID());
            messageNotify.setTitle(getString(R.string.message_bet_success));
            messageNotify.setType(ConstantCode.MessageType.MESSAGE_TYPE_BET_SUCCESS);
            messageNotify.setHornContent(StringUtils.refactorLotteryName(td.getRemark()) + getString(R.string.message_lottery_bet_success));
            messageNotify.setUserId(OneLotteryApi.getCurUserId());
            checkMsgId(txId, messageNotify);
            MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify);
            */

            // 投注数据入库
            saveBetDataInDB(txDetailRet, txId, newFlag, me, myBetList);

            return td;
        }
        return null;
    }

    // 存储投注的活动交易
    private void saveBetDataInDB(TxDetailBetRet txDetailRet, String txId, String newFlag, UserInfo me, List<OneLotteryBet> myBetList)
    {
        if (myBetList == null)
        {
            myBetList = OneLotteryBetDBHelper.getInstance().getMyBetByLotteryId(txDetailRet.getLotteryID());
        }
        OneLotteryBet oneLotteryBet = OneLotteryBetDBHelper.getInstance().getBetByTxId(txId);
        if (oneLotteryBet == null)
        {
            oneLotteryBet = new OneLotteryBet();
            oneLotteryBet.setCreateTime(new Date(txDetailRet.getCreateTime()));
            oneLotteryBet.setAttendeeHash(txDetailRet.getLauncherHash());
            oneLotteryBet.setAttendeeName(txDetailRet.getUserID());
            oneLotteryBet.setBetCost(txDetailRet.getAmount());
            oneLotteryBet.setBetCount(txDetailRet.getCount());

            TicketNumbersRet numbers = OneLotteryApi.getTicketNumbers(txId);
            oneLotteryBet.setBetNumbers(numbers != null ? numbers.getData().getNumbers() : "");
            OLLogger.i(TAG, txId + ", save oneChainGetTicketNumbers:" + oneLotteryBet.getBetNumbers());

            oneLotteryBet.setLotteryId(txDetailRet.getLotteryID());
            oneLotteryBet.setTicketId(txId);

            OneLottery lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId(txDetailRet.getLotteryID());
            oneLotteryBet.setLotteryName(lottery != null ? lottery.getLotteryName() : "");
            oneLotteryBet.setOneLottery(lottery);

            OneLotteryBetDBHelper.getInstance().insertOneLotteryBet(oneLotteryBet);
        }

    }

    // 存储中奖的活动交易
    private void saveBetDataInDB(TxDetailBetOverRet txDetailRet, String txId, String newFlag, UserInfo me, List<OneLotteryBet> myBetList)
    {
        if (myBetList == null)
        {
            myBetList = OneLotteryBetDBHelper.getInstance().getMyBetByLotteryId(txDetailRet.getLotteryID());
        }
        OneLotteryBet oneLotteryBet = OneLotteryBetDBHelper.getInstance().getBetByTxId(txId);
        if (oneLotteryBet == null)
        {
            oneLotteryBet = new OneLotteryBet();

            // 从网络获取,怎么获取？
            OneLotteryOldHistoryQueryRet oldHistory = OneLotteryApi.oneLotteryOldHistoryInfoQuery(txDetailRet.getLotteryID());
            if (oldHistory.getCode() == OneLotteryApi.SUCCESS && oldHistory.getData() != null)
            {
                String prizeTID = oldHistory.getData().getPrizeTxnID();// 根据这个再去获取活动中奖的信息?
                OLLogger.i(TAG, "prizeTID = " + prizeTID);
                oneLotteryBet = OneLotteryBetDBHelper.getInstance().getBetByTxId(prizeTID);
            }
//            private String ticketId;
//            private String lotteryName;

            /*private String attendeeHash;
            private String attendeeName;
            private String betNumbers;
            private Integer betCost;
            private Integer betCount;
            private Integer prizeLevel;
            private Long bonus;*/

//            private java.util.Date createTime;
//            private String lotteryId;

//              中奖消息没必要再入库了，在前面获取活动和历史活动时，投注列表已经入库结束
//            oneLotteryBet.setCreateTime(new Date(txDetailRet.getCurrentTime()));
//            oneLotteryBet.setAttendeeHash(me.getWalletAddr());// TODO
//            oneLotteryBet.setAttendeeName(me.getUserId()); // TODO
//
//            // TODO 需要新接口获取投注号码,投注金额和注数
////            oneLotteryBet.setBetCost(txDetailRet.getAmount());
////            oneLotteryBet.setBetCount(txDetailRet.getCount());
//
//            TicketNumbersRet numbers = OneLotteryApi.getTicketNumbers(txId);
//            oneLotteryBet.setBetNumbers(numbers != null ? numbers.getData().getNumbers() : "");
//            OLLogger.i(TAG, txId + ", save betOver oneChainGetTicketNumbers:" + oneLotteryBet.getBetNumbers());
//
//            oneLotteryBet.setLotteryId(txDetailRet.getLotteryID());
//            oneLotteryBet.setTicketId(txId);
//
//            OneLottery lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId(txDetailRet.getLotteryID());
//            oneLotteryBet.setLotteryName(lottery != null ? lottery.getLotteryName() : "");
//            oneLotteryBet.setOneLottery(lottery);
//
//            OneLotteryBetDBHelper.getInstance().insertOneLotteryBet(oneLotteryBet);
        }

    }

    // 根据交易详情，将修改的活动数据入库
    private TransactionDetail TD_editLottery(TxDetailLotteryModifyRet txDetailRet, String txId, String newFlag, UserInfo me)
    {
        if (txDetailRet == null || StringUtils.isEmpty(txDetailRet.getTxnID()))
        {
            return null;
        }

        boolean isFriendOrMe = UserLogic.isOfficialFriendOrMe(null, txDetailRet.getName());

        boolean isHistory = txDetailRet.getCloseTime() > System.currentTimeMillis();

        OneLottery dbOl = OneLotteryDBHelper.getInstance().getLotteryByLotterId(txDetailRet.getTxnID());

        if (dbOl != null || isFriendOrMe)
        {
            queryLotteryDetail(txDetailRet.getTxnID(), isHistory, newFlag);
        } else
        {
            // 本地不存在此记录,则看是不是官方，我，好友的活动
            // TODO 历史活动，暂时没下发publishName,等接口修改好后再做
            // 陌生人的活动只做(version=-1)的存储，不再取详情
            OneLottery strange = new OneLottery();
            strange.setLotteryId(txDetailRet.getTxnID());
            strange.setLotteryName(txDetailRet.getName());
            strange.setDescription(txDetailRet.getDescription());
            strange.setRuleId(txDetailRet.getRuleId());
            strange.setRuleType(txDetailRet.getRuleType());
            strange.setUpdateTime(new Date(txDetailRet.getUpdateTime()));
            strange.setStartTime(new Date(txDetailRet.getStartTime()));
            strange.setCloseTime(new Date(txDetailRet.getCloseTime()));
            strange.setPictureIndex(txDetailRet.getPictureIndex());// 不要用data.getVersion()，初始活动用-1
            strange.setMinBetCount(txDetailRet.getMinAttendeeCnt());
            strange.setMaxBetCount(txDetailRet.getMaxAttendeeCnt());
            strange.setOneBetCost(txDetailRet.getCost());
            strange.setBetTotalAmount(txDetailRet.getTotal());
            strange.setVersion(-1);// 不要用data.getVersion()，初始活动用-1
            strange.setPublisherName(txDetailRet.getPublisherName());
            strange.setUpdateFlag(newFlag);

            OneLotteryDBHelper.getInstance().insertOneLottery(strange);
        }

        // 通知明细界面实时刷新
        TransactionDetail td = new TransactionDetail();
        td.setTxId(txId);
        td.setRemark(StringUtils.refactorLotteryName(txDetailRet.getName()));
        td.setMyId(me != null ? me.getUserId() : "");
        td.setMyHash(me != null ? me.getWalletAddr() : "");
        td.setAmount(100L);
        td.setTime(new Date(txDetailRet.getUpdateTime()));
        td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_MODIFY_LOTTERY);
        checkTranTxId(txId, td);
        TransactionDetailDBHelper.getInstance().insertTransactionDetail(td);


        /* 倒数据的时候不再入消息表
        // 通知消息界面实时刷新
        MessageNotify messageNotify = new MessageNotify();
        messageNotify.setIsRead(false);
        messageNotify.setMsgId(txId);
        messageNotify.setHornContent(StringUtils.refactorLotteryName(txDetailRet.getName()) + getString(R.string.message_modify_lottery_success));
        messageNotify.setLotteryId(txDetailRet.getTxnID());
        messageNotify.setContent(getString(R.string.message_modify_lottery_success));
        messageNotify.setTime(td.getTime());
        messageNotify.setType(ConstantCode.MessageType.MESSAGE_TYPE_MODIFY_SUCCESS);
        messageNotify.setUserId(OneLotteryApi.getCurUserId());
        MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify);
        */
        return td;
    }

    // 根据交易详情，将创建的活动数据入库
    private TransactionDetail TD_addLottery(TxDetailLotteryAddRet txDetailRet, String txId, String newFlag, UserInfo me)
    {
        if (txDetailRet == null)
        {
            return null;
        }

        boolean isFriendOrMe = UserLogic.isOfficialFriendOrMe(null, txDetailRet.getName());

        boolean isHistory = txDetailRet.getCloseTime() > System.currentTimeMillis();

        OneLottery dbOl = OneLotteryDBHelper.getInstance().getLotteryByLotterId(txId);

        if (dbOl != null || isFriendOrMe)
        {
            queryLotteryDetail(txId, isHistory, newFlag);
        } else
        {
            // 本地不存在此记录,则看是不是官方，我，好友的活动
            // TODO 历史活动，暂时没下发publishName,等接口修改好后再做
            // 陌生人的活动只做(version=-1)的存储，不再取详情
            OneLottery strange = new OneLottery();
            strange.setLotteryId(txId);
            strange.setLotteryName(txDetailRet.getName());
            strange.setDescription(txDetailRet.getDescription());
            strange.setRuleId(txDetailRet.getRuleId());
            strange.setRuleType(txDetailRet.getRuleType());
            strange.setCreateTime(new Date(txDetailRet.getCreateTime()));
            strange.setStartTime(new Date(txDetailRet.getStartTime()));
            strange.setCloseTime(new Date(txDetailRet.getCloseTime()));
            strange.setPictureIndex(txDetailRet.getPictureIndex());// 不要用data.getVersion()，初始活动用-1
            strange.setMinBetCount(txDetailRet.getMinAttendeeCnt());
            strange.setMaxBetCount(txDetailRet.getMaxAttendeeCnt());
            strange.setOneBetCost(txDetailRet.getCost());
            strange.setBetTotalAmount(txDetailRet.getTotal());
            strange.setVersion(-1);// 不要用data.getVersion()，初始活动用-1
            strange.setPublisherName(txDetailRet.getPublisherName());
            strange.setUpdateFlag(newFlag);

            OneLotteryDBHelper.getInstance().insertOneLottery(strange);
        }

        /* 倒数据的时候不再入消息表 */
//        setAddLotteryMessage(txId, txDetailRet, me);
        return setAddLotteryTransferDetail(txId, txDetailRet, me);
    }

    //设置消息列表消息
    public void setMessageNotify(OneLottery lottery, String txId, String title, String hornContent, int type)
    {
        MessageNotify messageNotify = new MessageNotify();
        messageNotify.setIsRead(false);
        messageNotify.setMsgId(txId);
        messageNotify.setHornContent(hornContent);
        // 创建活动的时候Lottery对象是没有LotteryID的，所以需要用到txID
        if (type == ConstantCode.MessageType.MESSAGE_TYPE_CREATE_SUCCESS)
        {
            messageNotify.setLotteryId(txId);
        } else
        {
            messageNotify.setLotteryId(lottery != null ? lottery.getLotteryId() : "");
        }
        messageNotify.setTitle(title);
        if (type == ConstantCode.MessageType.MESSAGE_TYPE_DELETE_SUCCESS)
        {
            messageNotify.setContent(lottery != null ? StringUtils.refactorLotteryName(lottery.getLotteryName()) : "");
        } else if (type != ConstantCode.MessageType.MESSAGE_TYPE_CREATE_FAIL)
        {
            messageNotify.setContent(getString(R.string.message_see_content));
        }
        messageNotify.setTime(new Date());
        messageNotify.setType(type);
        messageNotify.setUserId(OneLotteryApi.getCurUserId());
        checkMsgId(txId, messageNotify);
        MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify);
    }

    public void setAddLotteryMessage(String txId, TxDetailLotteryAddRet txDetailRet, UserInfo me)
    {
        MessageNotify messageNotify = new MessageNotify();
        messageNotify.setIsRead(false);
        messageNotify.setMsgId(txId);
        messageNotify.setHornContent(StringUtils.refactorLotteryName(txDetailRet.getName()) + getString(R.string.message_create_lottery_success));
        messageNotify.setLotteryId(txId);
        messageNotify.setTitle(getString(R.string.message_create_success));
        messageNotify.setContent(getString(R.string.message_see_content));
        messageNotify.setTime(new Date(txDetailRet.getCreateTime()));
        messageNotify.setUserId(OneLotteryApi.getCurUserId());
        messageNotify.setType(ConstantCode.MessageType.MESSAGE_TYPE_CREATE_SUCCESS);
        messageNotify.setUserId(OneLotteryApi.getCurUserId());
        checkMsgId(txId, messageNotify);
        MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify);
    }

    public TransactionDetail setAddLotteryTransferDetail(String txId, TxDetailLotteryAddRet txDetailRet, UserInfo me)
    {
        TransactionDetail td = new TransactionDetail();
        td.setTxId(txId);
        td.setRemark(txDetailRet.getName());
        td.setMyId(me != null ? me.getUserId() : "");
        td.setMyHash(me != null ? me.getWalletAddr() : "");
        td.setAmount((long) ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE);
        td.setTime(new Date(txDetailRet.getCreateTime()));
        td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_CREATE_LOTTERY);
        checkTranTxId(txId, td);
        TransactionDetailDBHelper.getInstance().insertTransactionDetail(td);

        return td;
    }

    @Nullable
    private OneLottery getOneLottery(String lotteryId)
    {
        if (StringUtils.isEmpty(lotteryId))
        {
            return null;
        }
        // 本地无此记录，就不处理
        OneLottery lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId(lotteryId);
        if (null == lottery)
        {
            // 暂时用getTxInfoByTxId，以后可以用新增加的获取7天以后的活动接口获取详情，如果是
            TxDetailLotteryAddRet txDetailLotteryAddRet = (TxDetailLotteryAddRet) OneLotteryApi.getTxInfoByTxId(lotteryId);
            if (txDetailLotteryAddRet == null)
            {
                return null;
            }
            // TODO 活动有可能后续有更新，这个应该用edit后的名字啊
            lottery = new OneLottery();
            lottery.setLotteryName(txDetailLotteryAddRet.getName());
        }
        return lottery;
    }

}