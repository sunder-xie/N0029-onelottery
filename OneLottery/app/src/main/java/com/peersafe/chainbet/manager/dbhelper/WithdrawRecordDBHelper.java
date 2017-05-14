package com.peersafe.chainbet.manager.dbhelper;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.db.WithdrawBankCard;
import com.peersafe.chainbet.db.WithdrawRecord;
import com.peersafe.chainbet.db.WithdrawRecordDao;
import com.peersafe.chainbet.logic.OneLotteryApi;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/4/7
 * DESCRIPTION :
 */

public class WithdrawRecordDBHelper
{
    private static WithdrawRecordDBHelper instance;

    private WithdrawRecordDBHelper()
    {

    }

    public static synchronized WithdrawRecordDBHelper getInstance()
    {
        if (instance == null)
        {
            instance = new WithdrawRecordDBHelper();
        }
        return instance;
    }

    public void insert(WithdrawRecord record)
    {
        OneLotteryApplication.getDaoSession().getWithdrawRecordDao().insertOrReplace(record);
    }

    public List getAllRecordList()
    {
        QueryBuilder q = OneLotteryApplication.getDaoSession().getWithdrawRecordDao().queryBuilder();
        String userId = OneLotteryApi.getCurUserId();
        q.where(WithdrawRecordDao.Properties.UserId.eq(userId));
        q.orderDesc(WithdrawRecordDao.Properties.CreateTime);

        if(q.list() == null)
        {
            return new ArrayList();
        }
        return q.list();
    }

    public WithdrawRecord getRecordByKey(String s1)
    {
        return OneLotteryApplication.getDaoSession().getWithdrawRecordDao().load(s1);
    }

    public void delete(WithdrawBankCard bankCard)
    {
        OneLotteryApplication.getDaoSession().getWithdrawBankCardDao().delete(bankCard);
    }
}
