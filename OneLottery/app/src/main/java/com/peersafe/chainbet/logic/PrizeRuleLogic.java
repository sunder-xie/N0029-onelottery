package com.peersafe.chainbet.logic;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.db.PrizeRule;
import com.peersafe.chainbet.manager.dbhelper.PrizeRuleDBHelper;
import com.peersafe.chainbet.model.LotteryJsonBean.PrizeRuleDelRet;
import com.peersafe.chainbet.model.LotteryJsonBean.PrizeRuleDetailRet;
import com.peersafe.chainbet.model.LotteryJsonBean.PrizeRuleListRet;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.log.OLLogger;

import java.util.List;

/**
 * @author moying
 * @Description
 * @date 2017/1/11 17:32
 */
public class PrizeRuleLogic
{
    /**
     * Description 收到他人添加或修改规则的回调
     *
     * @param retMsg 返回的信息，对应json字符串
     * @param txId 交易id
     */
    public PrizeRule onRuleChange(String retMsg, String txId)
    {
        if (StringUtils.isEmpty(retMsg))
        {
            return null;
        }

        Gson gson = new Gson();
        try
        {
            PrizeRuleDetailRet ret = gson.fromJson(retMsg, PrizeRuleDetailRet.class);
            OLLogger.e("prl", "ret" + ret);
            if (ret == null || ret.getData() == null || StringUtils.isEmpty(ret.getData().getRuleID()))
            {
                return null;
            }
            PrizeRule prizeRule = new PrizeRule();
            prizeRule.setRuleId(ret.getData().getRuleID());
            prizeRule.setPercentage(ret.getData().getPercentage());
            prizeRule.setHidden(ret.getData().isHide());

            PrizeRule local = PrizeRuleDBHelper.getInstance().getPrizeRuleById(prizeRule.getRuleId());
            if (local == null)
            {
                List<PrizeRule> ruleList = PrizeRuleDBHelper.getInstance().getPrizeRules(true);
                if (ruleList != null)
                {
//                    prizeRule.setRuleName("规则" + (ruleList.size() + 1));
                    // TODO 实际上不会有delete rule的需求，不过为了满足delete，暂时将规则名往上自增
                    if (ruleList.size() > 0)
                    {
                        int curMax = Integer.parseInt(ruleList.get(ruleList.size() - 1).getRuleName().substring(2));
                        prizeRule.setRuleName(String.valueOf(curMax + 1));
                    }
                }
            } else
            {
                prizeRule.setRuleName(local.getRuleName());
            }
            OLLogger.e("prl", "change prizeRule=" + prizeRule);
            PrizeRuleDBHelper.getInstance().insertPrizeRule(prizeRule);

            return prizeRule;
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    // 获取服务器最新的规则
    public boolean getRules()
    {
        PrizeRuleListRet prizeRuleListRet = OneLotteryApi.prizeRuleQuery();
        if (prizeRuleListRet != null && prizeRuleListRet.getData() != null && !prizeRuleListRet.getData()
                .isEmpty())
        {
            PrizeRuleDBHelper.getInstance().clearPrizeRule();

            int i = 1;
            for (PrizeRuleDetailRet.DataBean data : prizeRuleListRet.getData())
            {
                if (data != null && !StringUtils.isEmpty(data.getRuleID()))
                {
                    PrizeRule prizeRule = new PrizeRule(data.getRuleID());
                    prizeRule.setRuleName(String.valueOf(i++));
                    prizeRule.setPercentage(data.getPercentage());
                    prizeRule.setHidden(data.isHide());
                    PrizeRuleDBHelper.getInstance().insertPrizeRule(prizeRule);
                }
            }

            return true;
        }

        return false;
    }

    public PrizeRule onRuleDelete(String retMsg, String txnID)
    {
        if (StringUtils.isEmpty(retMsg))
        {
            return null;
        }

        Gson gson = new Gson();
        try
        {
            PrizeRuleDelRet ret = gson.fromJson(retMsg, PrizeRuleDelRet.class);
            if (ret == null || ret.getCode() != OneLotteryApi.SUCCESS || StringUtils.isEmpty(ret.getData()))
            {
                return null;
            }
            PrizeRule prizeRule = new PrizeRule(ret.getData());
            OneLotteryApplication.getDaoSession().getPrizeRuleDao().delete(prizeRule);
            if (null == PrizeRuleDBHelper.getInstance().getPrizeRuleById(ret.getData()))
            {
                OLLogger.e("prl", "delete prizeRule=" + ret.getData());
            }

            return prizeRule;
        }
        catch (JsonSyntaxException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

}
