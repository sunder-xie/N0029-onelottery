package com.peersafe.chainbet.model.LotteryJsonBean;

import java.util.List;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.model.LotteryJsonBean
 * @description: 获取活动列表的返回json解析类
 * @date 24/11/16 PM6:17
 */
public class OneLotteryListRet extends OneLotteryQueryRet
{

    /**
     * code : 0
     * message :
     * data : [{"txnID":"88fb881d-ca57-4bc7-94e8-e3176499ebe8","newTxnID":"","version":0,"lastCloseTime":0,
     * "numbers":"","balance":0,"prizeTxnID":"","countTotal":0,"pictureIndex":2,"status":0,"updateTime":0,
     * "blockHeight":0,"preBlockHeight":0,"txnIDs":"","createTime":1483440601,"name":"第二个官方活动",
     * "ruleType":"PrizeRule","ruleID":"0462ce96-3d85-42ad-8fa0-1c5ee0370df7",
     * "publisherHash":"7cf9cdbb1811fe8931c0c04ddfa85569e99efdc2e26436a15dca9ad2","publisherName":"one_chain_admin",
     * "startTime":1483441201,"closeTime":1483549200,"minAttendeeCnt":2,"maxAttendeeCnt":2,"cost":10000,
     * "description":"第二个官方活动来了","fee":0}]
     */

    private List<DataBean> data;

    public List<DataBean> getData()
    {
        return data;
    }

    public void setData(List<DataBean> data)
    {
        this.data = data;
    }

}