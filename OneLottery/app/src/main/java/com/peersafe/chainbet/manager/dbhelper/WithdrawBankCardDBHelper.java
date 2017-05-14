package com.peersafe.chainbet.manager.dbhelper;

import android.app.AlertDialog;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.db.WithdrawBankCard;
import com.peersafe.chainbet.db.WithdrawBankCardDao;
import com.peersafe.chainbet.logic.OneLotteryApi;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/4/7
 * DESCRIPTION :
 */

public class WithdrawBankCardDBHelper
{
    private static WithdrawBankCardDBHelper instance;

    private WithdrawBankCardDBHelper()
    {

    }

    public static synchronized WithdrawBankCardDBHelper getInstance()
    {
        if (instance == null)
        {
            instance = new WithdrawBankCardDBHelper();
        }
        return instance;
    }

    public void insertWithdrawBankCard(WithdrawBankCard bankCard)
    {
        OneLotteryApplication.getDaoSession().insertOrReplace(bankCard);
    }

    public List<WithdrawBankCard> getAllBankCard()
    {
        QueryBuilder queryBuilder = OneLotteryApplication.getDaoSession().getWithdrawBankCardDao().queryBuilder();
        String userId = OneLotteryApi.getCurUserId();
        queryBuilder.where(WithdrawBankCardDao.Properties.UserId.eq(userId));
        queryBuilder.orderDesc(WithdrawBankCardDao.Properties.CreateTime);
        return queryBuilder.list();
    }

    public void setPrimaryBankCard(WithdrawBankCard bankCard)
    {
        QueryBuilder<WithdrawBankCard> q = OneLotteryApplication.getDaoSession().getWithdrawBankCardDao().queryBuilder();
        q.where(WithdrawBankCardDao.Properties.IsDefaultCard.eq(true));
        List list = q.list();
        if(list != null && list.size() > 0)
        {
            WithdrawBankCard card  = (WithdrawBankCard) list.get(0);
            card.setIsDefaultCard(false);
            insertWithdrawBankCard(card);
        }

        bankCard.setIsDefaultCard(true);
        insertWithdrawBankCard(bankCard);
    }

    public void delete(WithdrawBankCard card)
    {
        OneLotteryApplication.getDaoSession().delete(card);
    }
}
