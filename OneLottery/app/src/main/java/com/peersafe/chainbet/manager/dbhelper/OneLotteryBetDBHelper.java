package com.peersafe.chainbet.manager.dbhelper;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.OneLotteryBet;
import com.peersafe.chainbet.db.OneLotteryBetDao;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.model.AttendBean;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * AUTHOR : sunhaitao.
 * DATA : 16/12/28
 * DESCRIPTION :
 */

public class OneLotteryBetDBHelper
{
    private static OneLotteryBetDBHelper instance;

    private OneLotteryBetDBHelper()
    {

    }

    public static synchronized OneLotteryBetDBHelper getInstance()
    {
        if (instance == null)
        {
            instance = new OneLotteryBetDBHelper();
        }
        return instance;
    }

    public void insertOneLotteryBet(OneLotteryBet oneLotteryBet)
    {
        OneLotteryApplication.getDaoSession().getOneLotteryBetDao().insertOrReplace(oneLotteryBet);
    }

    public void deleteOneLotteryBet(OneLotteryBet oneLotteryBet)
    {
        OneLotteryApplication.getDaoSession().getOneLotteryBetDao().delete(oneLotteryBet);
    }

    public List<OneLotteryBet> getMyBetByLotteryId(String lotterId)
    {
        UserInfo curUser = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (curUser == null)
        {
            return null;
        }
        QueryBuilder q = OneLotteryApplication.getDaoSession().getOneLotteryBetDao().queryBuilder();
        q.where(OneLotteryBetDao.Properties.LotteryId.eq(lotterId),
                OneLotteryBetDao.Properties.AttendeeHash.eq(curUser.getWalletAddr()));
        List<OneLotteryBet> list = q.list();
        if (list.size() != 0)
        {
            return list;
        }

        return null;
    }

    public OneLotteryBet getBetByTxId(String txID)
    {
        QueryBuilder q = OneLotteryApplication.getDaoSession().getOneLotteryBetDao().queryBuilder();
        q.where(OneLotteryBetDao.Properties.TicketId.eq(txID));
        List<OneLotteryBet> list = q.list();
        if (list.size() != 0)
        {
            return list.get(0);
        }

        return null;
    }

    public OneLotteryBet getRewardOneLotteryBet(String lotterId)
    {
        QueryBuilder q = OneLotteryApplication.getDaoSession().
                getOneLotteryBetDao().queryBuilder();
        q.where(OneLotteryBetDao.Properties.LotteryId.eq(lotterId),
                OneLotteryBetDao.Properties.PrizeLevel.eq(4));
        List<OneLotteryBet> list = q.list();
        if (list.size() != 0)
        {
            return list.get(0);
        }
        return null;
    }

    public List<OneLotteryBet> getOneLotteryBet(String lotterId)
    {
        QueryBuilder queryBuilder = OneLotteryApplication.getDaoSession().
                getOneLotteryBetDao().queryBuilder();
        queryBuilder.where(OneLotteryBetDao.Properties.LotteryId.eq(lotterId));

        List<OneLotteryBet> list = queryBuilder.list();
        return list;
    }

    public OneLotteryBet getLotteryBetByTxID(String prizeTxID)
    {
        return OneLotteryApplication.getDaoSession().getOneLotteryBetDao().load(prizeTxID);
    }

    public List<AttendBean> getAttendLotteryByLotteryId(String lotterId)
    {
        QueryBuilder queryBuilder = OneLotteryApplication.getDaoSession().getOneLotteryBetDao()
                .queryBuilder();
        queryBuilder.where(OneLotteryBetDao.Properties.LotteryId.eq(lotterId));
        queryBuilder.orderDesc(OneLotteryBetDao.Properties.CreateTime);

        List<AttendBean> list = new ArrayList<>();
        List<OneLotteryBet> betList = queryBuilder.list();
        if (null != betList && !betList.isEmpty())
        {
            for (OneLotteryBet oneLotteryBet : betList)
            {
                AttendBean bean = new AttendBean();
                bean.setAttendName(oneLotteryBet.getAttendeeName());
                bean.setAttendHash(oneLotteryBet.getAttendeeHash());
                bean.setAttendCount(getPerBetNumbers(oneLotteryBet.getBetNumbers()).size());
                bean.setAttendTime(oneLotteryBet.getCreateTime());
                bean.setLotteryId(lotterId);
                bean.setBetNumbers(oneLotteryBet.getBetNumbers());
                list.add(bean);
            }
        }
        return list;
    }

    public List<String> getPerBetNumbers(String numbers)
    {
        List<String> list = new ArrayList<>();
        if (StringUtils.isEmpty(numbers))
        {
            return list;
        }
        String[] split = numbers.split("\\s+");

        for (String each : split)
        {
            list.add(each);
        }
        return list;
    }

    public List<String> getBetNumbers(String attendName, String lotterId)
    {
        QueryBuilder queryBuilder = OneLotteryApplication.getDaoSession().getOneLotteryBetDao()
                .queryBuilder();
        queryBuilder.where(OneLotteryBetDao.Properties.AttendeeName.eq(attendName),
                OneLotteryBetDao.Properties.LotteryId.eq(lotterId));
        List<String> list = new ArrayList<>();
        List<OneLotteryBet> oneLotteryBetlist = queryBuilder.build().list();
        if (null != oneLotteryBetlist && !oneLotteryBetlist.isEmpty())
        {
            for (OneLotteryBet oneLotteryBet : oneLotteryBetlist)
            {
                String betNumbers = oneLotteryBet.getBetNumbers();
                if (!StringUtils.isEmpty(betNumbers) && null != betNumbers)
                {
                    String[] split = betNumbers.split("\\s+");
                    for (String each : split)
                    {
                        list.add(each);
                    }
                } else
                {
                    list.add(betNumbers);
                }
            }
        }
        return list;
    }

    public List<AttendBean> getOneHundredBetInfo(String lotterId)
    {
        QueryBuilder queryBuilder = OneLotteryApplication.getDaoSession().getOneLotteryBetDao()
                .queryBuilder();
        queryBuilder.where(OneLotteryBetDao.Properties.LotteryId.eq(lotterId));
        queryBuilder.orderDesc(OneLotteryBetDao.Properties.CreateTime);
        List<AttendBean> list = new ArrayList<>();
        List<OneLotteryBet> oneLotteryBetlist = queryBuilder.build().list();
        if (null != oneLotteryBetlist && !oneLotteryBetlist.isEmpty())
        {
            for (OneLotteryBet oneLotteryBet : oneLotteryBetlist)
            {
                String betNumbers = oneLotteryBet.getBetNumbers();
                if (!StringUtils.isEmpty(betNumbers) && null != betNumbers)
                {
                    String[] split = betNumbers.split("\\s+");
                    for (String each : split)
                    {
                        AttendBean bean = new AttendBean();
                        bean.setAttendName(oneLotteryBet.getAttendeeName());
                        bean.setAttendTime(oneLotteryBet.getCreateTime());
                        list.add(bean);

                        if (list.size() == 100)
                        {
                            return list;
                        }
                    }
                } else
                {
                    AttendBean bean = new AttendBean();
                    bean.setAttendName(oneLotteryBet.getAttendeeName());
                    bean.setAttendTime(oneLotteryBet.getCreateTime());
                    list.add(bean);

                    if (list.size() == 100)
                    {
                        return list;
                    }
                }
            }
        }
        return list;
    }

    public List<OneLotteryBet> getMyAllBet()
    {
        List<OneLotteryBet> list = new ArrayList<>();
        QueryBuilder q = OneLotteryApplication.getDaoSession().getOneLotteryBetDao().queryBuilder();
        String curUserId = OneLotteryApi.getCurUserId();
        if (!StringUtils.isEmpty(curUserId))
        {
            q.where(OneLotteryBetDao.Properties.AttendeeName.eq(curUserId));
            q.orderDesc(OneLotteryBetDao.Properties.CreateTime);
        }

        List<OneLotteryBet> oneLotteryBetList = q.list();

        if (null != oneLotteryBetList && !oneLotteryBetList.isEmpty())
        {
            for (OneLotteryBet oneLotteryBet : oneLotteryBetList)
            {
                OneLottery oneLottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId
                        (oneLotteryBet.getLotteryId());
                if (oneLottery != null)
                {
                    oneLotteryBet.setOneLottery(oneLottery);
                    insertOneLotteryBet(oneLotteryBet);
                    list.add(oneLotteryBet);
                }
            }
        }
        return list;
    }

    public List<OneLotteryBet> getMyAllDurBet()
    {
        List<OneLotteryBet> list = new ArrayList<>();
        List<OneLotteryBet> myAllBet = getMyAllBet();
        if (null != myAllBet && myAllBet.size() > 0)
        {
            for (OneLotteryBet oneLotteryBet : myAllBet)
            {
                if (oneLotteryBet.getOneLottery().getState().equals(ConstantCode
                        .OneLotteryState.ONELOTTERY_STATE_ON_GOING))
                {
                    list.add(oneLotteryBet);
                }
            }
        }

        return list;
    }

    public List<OneLotteryBet> getMyFinishBet()
    {
        List<OneLotteryBet> list = new ArrayList<>();
        List<OneLotteryBet> myAllBet = getMyAllBet();
        if (null != myAllBet && myAllBet.size() > 0)
        {
            for (OneLotteryBet oneLotteryBet : myAllBet)
            {
                if (oneLotteryBet.getOneLottery().getState().equals(ConstantCode
                        .OneLotteryState.ONELOTTERY_STATE_REWARD_ALREADY) && oneLotteryBet
                        .getOneLottery().getPrizeTxID().equals(oneLotteryBet.getTicketId()))
                {
                    list.add(oneLotteryBet);
                }
            }
        }
        return list;
    }
}
