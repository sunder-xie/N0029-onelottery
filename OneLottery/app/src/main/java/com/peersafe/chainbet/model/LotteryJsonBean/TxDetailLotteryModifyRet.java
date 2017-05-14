package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author moying
 * @Description 活动的修改的交易详情
 * @date 2017/2/17 14:19
 */
public class TxDetailLotteryModifyRet extends TxDetailRet
{


    /**
     * txnID : e35364c9-2a2f-4c5c-bee0-2bbb17f80372
     * name : Fro m
     * fee : 100
     * publisherName : zy2
     * ruleType : PrizeRule
     * pictureIndex : 2
     * ruleId : bbc202ee-c554-4d48-8a2c-87c64b38a0a8
     * updateTime : 1486722558
     * startTime : 1486722559
     * closeTime : 1486808959
     * minAttendeeCnt : 8
     * maxAttendeeCnt : 8
     * cost : 10000
     * Total : 80000
     * description :
     */

    private String txnID; // 表示的是活动ID
    private String name;
    private long fee;
    private String publisherName;
    private String ruleType;
    private int pictureIndex;
    private String ruleId;
    private long updateTime;
    private long startTime;
    private long closeTime;
    private int minAttendeeCnt;
    private int maxAttendeeCnt;
    private long cost;
    private long Total;
    private String description;

    public String getTxnID()
    {
        return txnID;
    }

    public void setTxnID(String txnID)
    {
        this.txnID = txnID;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public long getFee()
    {
        return fee;
    }

    public void setFee(long fee)
    {
        this.fee = fee;
    }

    public String getPublisherName()
    {
        return publisherName;
    }

    public void setPublisherName(String publisherName)
    {
        this.publisherName = publisherName;
    }

    public String getRuleType()
    {
        return ruleType;
    }

    public void setRuleType(String ruleType)
    {
        this.ruleType = ruleType;
    }

    public int getPictureIndex()
    {
        return pictureIndex;
    }

    public void setPictureIndex(int pictureIndex)
    {
        this.pictureIndex = pictureIndex;
    }

    public String getRuleId()
    {
        return ruleId;
    }

    public void setRuleId(String ruleId)
    {
        this.ruleId = ruleId;
    }

    public long getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(long updateTime)
    {
        this.updateTime = updateTime;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    public long getCloseTime()
    {
        return closeTime;
    }

    public void setCloseTime(long closeTime)
    {
        this.closeTime = closeTime;
    }

    public int getMinAttendeeCnt()
    {
        return minAttendeeCnt;
    }

    public void setMinAttendeeCnt(int minAttendeeCnt)
    {
        this.minAttendeeCnt = minAttendeeCnt;
    }

    public int getMaxAttendeeCnt()
    {
        return maxAttendeeCnt;
    }

    public void setMaxAttendeeCnt(int maxAttendeeCnt)
    {
        this.maxAttendeeCnt = maxAttendeeCnt;
    }

    public long getCost()
    {
        return cost;
    }

    public void setCost(long cost)
    {
        this.cost = cost;
    }

    public long getTotal()
    {
        return Total;
    }

    public void setTotal(long Total)
    {
        this.Total = Total;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
