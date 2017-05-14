package com.peersafe.chainbet.manager.dbhelper;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.OneLotteryDao;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.log.OLLogger;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * AUTHOR : sunhaitao.
 * DATA : 16/12/22
 * DESCRIPTION :
 */

public class OneLotteryDBHelper
{
    private static OneLotteryDBHelper instance;

    private OneLotteryDBHelper()
    {

    }

    public static synchronized OneLotteryDBHelper getInstance()
    {
        if (instance == null)
        {
            instance = new OneLotteryDBHelper();
        }
        return instance;
    }

    public void insertOneLottery(OneLottery oneLottery)
    {
        OneLotteryApplication.getDaoSession().getOneLotteryDao().insertOrReplace(oneLottery);
    }

    public void deleteOneLottery(OneLottery oneLottery)
    {
        OneLotteryApplication.getDaoSession().getOneLotteryDao().delete(oneLottery);
    }

    /**
     * 获取不是这个updateFlag的活动
     *
     * @return
     */
    public List<OneLottery> getNotThisUpdateFlagLotteres(String updteFlag, boolean isHistory)
    {
        QueryBuilder q = OneLotteryApplication.getDaoSession().getOneLotteryDao().queryBuilder();
//        if (isHistory)
//        {
//            q.where(OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
//                    .ONELOTTERY_STATE_REWARD_ALREADY));
//        } else
//        {
//            // TODO 和 queryLotteryList 接口返回的是包括什么状态的一致
//            q.whereOr(OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
//                            .ONELOTTERY_STATE_NOT_STARTED),
//                    OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
//                            .ONELOTTERY_STATE_ON_GOING));
//        }
        q.where(OneLotteryDao.Properties.UpdateFlag.notEq(updteFlag));
//        UserInfo curPrimaryAccount = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
//        if (null != curPrimaryAccount)
//        {
//            q.where(OneLotteryDao.Properties.PublisherHash.notEq(curPrimaryAccount
// .getWalletAddr()));
//        }
        return q.build().list();
    }

    /**
     * 获取最新的活动
     *
     * @return
     */
    public List<OneLottery> getNewestLotteres()
    {
        QueryBuilder q = OneLotteryApplication.getDaoSession().getOneLotteryDao().queryBuilder();
        q.whereOr(OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                .ONELOTTERY_STATE_NOT_STARTED), OneLotteryDao.Properties.State.eq(ConstantCode
                .OneLotteryState.ONELOTTERY_STATE_ON_GOING));
        q.where(OneLotteryDao.Properties.PublisherHash.eq(ConstantCode.CommonConstant
                .ONELOTTERY_DEFAULT_OFFICAL_HASH));
        q.orderDesc(OneLotteryDao.Properties.CreateTime, OneLotteryDao.Properties.UpdateTime);
        return q.build().list();
    }

    /**
     * 获取最快的活动
     *
     * @return
     */
    public List<OneLottery> getFastestLotteres()
    {
        QueryBuilder q = OneLotteryApplication.getDaoSession().getOneLotteryDao().queryBuilder();

        q.where(OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                        .ONELOTTERY_STATE_ON_GOING),
                OneLotteryDao.Properties.PublisherHash.eq(ConstantCode.CommonConstant
                        .ONELOTTERY_DEFAULT_OFFICAL_HASH));

        q.orderDesc(OneLotteryDao.Properties.Progress, OneLotteryDao.Properties.UpdateTime);

        return q.build().list();
    }

    /**
     * 根据id获取活动
     *
     * @param lotterId
     * @return
     */
    public OneLottery getLotteryByLotterId(String lotterId)
    {
        if (StringUtils.isEmpty(lotterId))
        {
            return null;
        }
        return OneLotteryApplication.getDaoSession().getOneLotteryDao().load(lotterId);
    }

    public List<OneLottery> getRewardLotteres()
    {
        QueryBuilder q = OneLotteryApplication.getDaoSession().
                getOneLotteryDao().queryBuilder();
        q.whereOr(OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                        .ONELOTTERY_STATE_CAN_REWARD),
                OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                        .ONELOTTERY_STATE_REWARD_ING));
        q.where(OneLotteryDao.Properties.Version.notEq(-1),
                OneLotteryDao.Properties.PublisherName.in(FriendDBHelper.getInstance()
                        .getAllFriendNames()));
        q.orderDesc(OneLotteryDao.Properties.UpdateTime);
        return q.list();
    }

    public List<OneLottery> getFailLotteres()
    {
        QueryBuilder q = OneLotteryApplication.getDaoSession().
                getOneLotteryDao().queryBuilder();
        q.whereOr(OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                        .ONELOTTERY_STATE_FAIL),
                OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                        .ONELOTTERY_STATE_REFUND_ALREADY),
                OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                        .ONELOTTERY_STATE_CAN_REFUND),
                OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                        .ONELOTTERY_STATE_REFUND_ING));
        q.where(OneLotteryDao.Properties.Version.notEq(-1),
                OneLotteryDao.Properties.CloseTime.gt(System.currentTimeMillis() - 604800000),
                OneLotteryDao.Properties.PublisherName.in(FriendDBHelper.getInstance()
                        .getAllFriendNames()));
        q.orderDesc(OneLotteryDao.Properties.CloseTime);
        return q.list();
    }

    public List<OneLottery> getHistoryLotteres()
    {
        QueryBuilder q = OneLotteryApplication.getDaoSession().
                getOneLotteryDao().queryBuilder();
        q.where(OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                        .ONELOTTERY_STATE_REWARD_ALREADY),
                OneLotteryDao.Properties.Version.notEq(-1),
                OneLotteryDao.Properties.CloseTime.gt(System.currentTimeMillis() - 604800000),
                OneLotteryDao.Properties.PublisherName.in(FriendDBHelper.getInstance()
                        .getAllFriendNames()));
        q.orderDesc(OneLotteryDao.Properties.LastCloseTime);
        return q.list();
    }

    public List<OneLottery> getFriendLotteres()
    {
        UserInfo curPrimaryAccount = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (null != curPrimaryAccount)
        {
            QueryBuilder q = OneLotteryApplication.getDaoSession().getOneLotteryDao()
                    .queryBuilder();
            q.whereOr(OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                            .ONELOTTERY_STATE_ON_GOING),
                    OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                            .ONELOTTERY_STATE_NOT_STARTED));
            q.where(OneLotteryDao.Properties.PublisherHash.notEq(curPrimaryAccount.getWalletAddr()),
                    OneLotteryDao.Properties.PublisherHash.notEq(ConstantCode.CommonConstant
                            .ONELOTTERY_DEFAULT_OFFICAL_HASH),
                    OneLotteryDao.Properties.PublisherName.notEq(ConstantCode.CommonConstant
                            .ONELOTTERY_DEFAULT_OFFICAL_NAME),
                    OneLotteryDao.Properties.Version.notEq(-1),
                    OneLotteryDao.Properties.PublisherName.in(FriendDBHelper.getInstance()
                            .getAllFriendNames()));
            q.orderDesc(OneLotteryDao.Properties.CreateTime);
            return q.build().list();
        } else
        {
            return null;
        }
    }

    //获取我的活动
    public List<OneLottery> getMyLotteres()
    {
        UserInfo curPrimaryAccount = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (null != curPrimaryAccount)
        {
            QueryBuilder q = OneLotteryApplication.getDaoSession().getOneLotteryDao()
                    .queryBuilder();
            q.where(OneLotteryDao.Properties.State.notEq(ConstantCode.OneLotteryState
                    .ONELOTTERY_STATE_CREATE_ING));
            q.orderDesc(OneLotteryDao.Properties.CreateTime, OneLotteryDao.Properties.UpdateTime);
            return q.where(OneLotteryDao.Properties.PublisherHash.eq(curPrimaryAccount
                    .getWalletAddr())).list();
        } else
        {
            return null;
        }
    }

    //根据发布者的hash获取进行中活动
    public List<OneLottery> getLotteryByPublishHash(String friendHash)
    {
        QueryBuilder q = OneLotteryApplication.getDaoSession().getOneLotteryDao().queryBuilder();
        q.where(OneLotteryDao.Properties.PublisherHash.eq(friendHash));
        q.whereOr(OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                        .ONELOTTERY_STATE_NOT_STARTED),
                OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                        .ONELOTTERY_STATE_ON_GOING));
        return q.list();
    }

    public OneLottery getLotteryByNewTxID(String txId)
    {
        QueryBuilder queryBuilder = OneLotteryApplication.getDaoSession().getOneLotteryDao()
                .queryBuilder();
        queryBuilder.where(OneLotteryDao.Properties.NewTxId.eq(txId));
        List<OneLottery> list = queryBuilder.list();
        if (list.size() != 0)
        {
            return (OneLottery) queryBuilder.list().get(0);
        }
        return null;
    }

    public List<OneLottery> getMyOnGoingLottery()
    {
        QueryBuilder queryBuilder = OneLotteryApplication.getDaoSession().getOneLotteryDao()
                .queryBuilder();
        UserInfo curPrimaryAccount = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (null != curPrimaryAccount)
        {
            queryBuilder.where(OneLotteryDao.Properties.PublisherHash.eq(curPrimaryAccount
                    .getWalletAddr()));
            queryBuilder.whereOr(OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                            .ONELOTTERY_STATE_NOT_STARTED),
                    OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                            .ONELOTTERY_STATE_ON_GOING),
                    OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                            .ONELOTTERY_STATE_CAN_REWARD),
                    OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                            .ONELOTTERY_STATE_REWARD_ING));
        }

        return queryBuilder.list();
    }

    public List<OneLottery> getNotStartedAndOnGoingLottery()
    {
        QueryBuilder queryBuilder = OneLotteryApplication.getDaoSession().getOneLotteryDao()
                .queryBuilder();
        queryBuilder.where(OneLotteryDao.Properties.Version.notEq(-1),
                queryBuilder.or(OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                                .ONELOTTERY_STATE_NOT_STARTED),
                        OneLotteryDao.Properties.State.eq(ConstantCode.OneLotteryState
                                .ONELOTTERY_STATE_ON_GOING)));

        return queryBuilder.list();
    }

    /**
     * 获取活动最新的updateFlag
     *
     * @return
     */
    public List<OneLottery> getNewestLotteresUpdateFlag()
    {
        QueryBuilder q = OneLotteryApplication.getDaoSession().getOneLotteryDao().queryBuilder();
        q.where(OneLotteryDao.Properties.State.notEq(ConstantCode.OneLotteryState
                .ONELOTTERY_STATE_CREATE_ING));
        q.orderAsc(OneLotteryDao.Properties.State);
        return q.build().list();
    }

    public List<OneLottery> getLocalLottery(String string, boolean isLocal)
    {
        QueryBuilder q = OneLotteryApplication.getDaoSession().getOneLotteryDao().queryBuilder();
        if (isLocal)
        {
            q.where(OneLotteryDao.Properties.Version.notEq(-1));
        }
        else
        {
            q.where(OneLotteryDao.Properties.Version.eq((-1)));
        }

        q.orderDesc(OneLotteryDao.Properties.CreateTime);

        List<OneLottery> lotterys = new ArrayList<>();
        List<OneLottery> list = q.build().list();
        if (list != null && list.size() != 0)
        {
            for (OneLottery lottery : list)
            {
                String name = lottery.getLotteryName().toLowerCase();
                String pubName = lottery.getPublisherName().toLowerCase();

                if(name == null || pubName == null)
                {
                    continue;
                }

                if ((name.contains(string.toLowerCase())) || ((pubName.contains(string.toLowerCase()) &&
                        !pubName.equals(ConstantCode.CommonConstant.ONELOTTERY_DEFAULT_OFFICAL_NAME))))
                {
                    lotterys.add(lottery);
                }
            }
        }

        return lotterys;
    }

}

