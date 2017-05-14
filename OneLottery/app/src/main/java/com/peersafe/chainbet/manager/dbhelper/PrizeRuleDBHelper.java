package com.peersafe.chainbet.manager.dbhelper;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.db.PrizeRule;
import com.peersafe.chainbet.db.PrizeRuleDao;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * AUTHOR : sunhaitao.
 * DATA : 16/12/28
 * DESCRIPTION :
 */

public class PrizeRuleDBHelper
{
    private static PrizeRuleDBHelper instance;

    private PrizeRuleDBHelper()
    {

    }

    public static synchronized PrizeRuleDBHelper getInstance()
    {
        if (instance == null)
        {
            instance = new PrizeRuleDBHelper();
        }
        return instance;
    }

    public void insertPrizeRule(PrizeRule prizeRule)
    {
        OneLotteryApplication.getDaoSession().getPrizeRuleDao().insertOrReplace(prizeRule);
    }

    public void clearPrizeRule()
    {
        OneLotteryApplication.getDaoSession().getPrizeRuleDao().deleteAll();
    }

    public PrizeRule getPrizeRuleById(String ruleId)
    {
        return OneLotteryApplication.getDaoSession().getPrizeRuleDao().load(ruleId);
    }

    /**
     * 获取数据库内规则列表
     * @param all true:查询全部  false：查询只读
     * @return
     */
    public List<PrizeRule> getPrizeRules(boolean all)
    {
        QueryBuilder q = OneLotteryApplication.getDaoSession().getPrizeRuleDao().queryBuilder();
        if (!all)
        {
            q.where(PrizeRuleDao.Properties.IsHidden.eq(false));
        }
        q.orderAsc(PrizeRuleDao.Properties.RuleName);
        return q.build().list();
    }
}
