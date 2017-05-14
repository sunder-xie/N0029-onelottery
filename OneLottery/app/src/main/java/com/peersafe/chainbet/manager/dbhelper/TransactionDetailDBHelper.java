package com.peersafe.chainbet.manager.dbhelper;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.db.TransactionDetail;
import com.peersafe.chainbet.db.TransactionDetailDao;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.utils.common.StringUtils;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * AUTHOR : caozhongzheng.
 * DATA : 17/2/8
 * DESCRIPTION : 交易表辅助类
 */

public class TransactionDetailDBHelper
{
    private static TransactionDetailDBHelper instance;

    public static synchronized TransactionDetailDBHelper getInstance()
    {
        if (instance == null)
        {
            instance = new TransactionDetailDBHelper();
        }
        return instance;
    }

    public void insertTransactionDetail(TransactionDetail transactionDetail)
    {
        OneLotteryApplication.getDaoSession().getTransactionDetailDao().insertOrReplace(transactionDetail);

        OneLotteryManager.getInstance().SendEventBus(transactionDetail, OLMessageModel.STMSG_MODEL_TRANSFER_DETAIL);
    }

    public void deleteTransactionDetail(TransactionDetail transactionDetail)
    {
        OneLotteryApplication.getDaoSession().getTransactionDetailDao().delete(transactionDetail);
    }

    public List<TransactionDetail> getTransactionDetailByUserInfo(String hash, String uid)
    {
        boolean emptyHash = StringUtils.isEmpty(hash);
        boolean emptyUid = StringUtils.isEmpty(uid);
        if (emptyHash && emptyUid)
        {
            return null;
        }
        if (emptyHash)
        {
            hash = StringUtils.nullToEmpty(hash);
        }
        if (emptyUid)
        {
            uid = StringUtils.nullToEmpty(uid);
        }

        QueryBuilder<TransactionDetail> q = OneLotteryApplication.getDaoSession()
                .getTransactionDetailDao().queryBuilder();
        q.whereOr(TransactionDetailDao.Properties.MyHash.eq(hash), TransactionDetailDao.Properties.MyHash.eq(uid));

        q.orderDesc(TransactionDetailDao.Properties.Time, TransactionDetailDao.Properties.Type);
        return q.list();
    }

    public TransactionDetail getTranByTxId(String txId)
    {
        return OneLotteryApplication.getDaoSession().getTransactionDetailDao().load(txId);
    }
}
