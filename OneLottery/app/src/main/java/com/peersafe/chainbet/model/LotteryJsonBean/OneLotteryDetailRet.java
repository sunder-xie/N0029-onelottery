package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.model.LotteryJsonBean
 * @description: 获取活动详情的返回json解析类
 * @date 24/11/16 PM6:17
 */
public class OneLotteryDetailRet extends OneLotteryQueryRet
{

    /**
     * code : 0
     * message :
     * data : {"txnID":"1414507b-bf86-4a4d-898d-8dcac9bbe769","newTxnID":"","version":0,"lastCloseTime":0,
     * "numbers":"","balance":0,"prizeTxnID":"","countTotal":0,"pictureIndex":3,"status":0,"updateTime":0,
     * "blockHeight":0,"preBlockHeight":0,"txnIDs":"","createTime":1484211001,"name":"第三个官方活动",
     * "ruleType":"PrizeRule","ruleID":"32341b10-9181-4531-b163-12a51e267c84",
     * "publisherHash":"54ceb678c6e246a542098b369e450822226c3dffa4b298a069079ca2","publisherName":"one_chain_admin",
     * "startTime":1484211601,"closeTime":1484413200,"minAttendeeCnt":2,"maxAttendeeCnt":2,"cost":10000,
     * "description":"第三官方活动来了","fee":0}
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

    @Override
    public String toString()
    {
        return "OneLotteryDetailRet{" +
                "data=" + data +
                '}';
    }
}