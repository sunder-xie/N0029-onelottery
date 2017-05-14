package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.model
 * @description:
 * @date 22/11/16 AM10:32
 */
public class PrizeRuleDetailRet extends PrizeRuleRet
{

    /**
     * code : 0
     * message :
     * data : [{"ruleID":"0b875dba-0f94-4e79-87ae-6ba7ef699722","percentage":80,"hide":false},{"ruleID":"807d64b1-4941-40fd-98c1-ca94c34cb6d4","percentage":90,"hide":false}]
     */

    private DataBean data;

    public DataBean getData()
    {
        return data;
    }

    public void setData(DataBean data)
    {
        this.data = data;
    }
}
