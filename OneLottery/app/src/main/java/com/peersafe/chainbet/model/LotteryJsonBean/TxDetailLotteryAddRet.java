package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author moying
 * @Description 添加活动的交易详情
 * @date 2017/2/17 14:19
 */
public class TxDetailLotteryAddRet extends TxDetailRet
{


    /**
     * name : Ssssssz
     * fee : 10000
     * publisherName : zy1
     * ruleType : PrizeRule
     * ruleId : bbc202ee-c554-4d48-8a2c-87c64b38a0a8
     * pictureIndex : 8
     * createTime : 1486622400
     * startTime : 1486622401
     * closeTime : 1486708801
     * minAttendeeCnt : 3
     * maxAttendeeCnt : 3
     * cost : 10000
     * Total : 30000
     * description : Ssss
     */

    private String name;
    private long fee;
    private String publisherName;
    private String ruleType;
    private String ruleId;
    private int pictureIndex;
    private long createTime;
    private long startTime;
    private long closeTime;
    private int minAttendeeCnt;
    private int maxAttendeeCnt;
    private long cost;
    private long Total;
    private String description;

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

    public String getRuleId()
    {
        return ruleId;
    }

    public void setRuleId(String ruleId)
    {
        this.ruleId = ruleId;
    }

    public int getPictureIndex()
    {
        return pictureIndex;
    }

    public void setPictureIndex(int pictureIndex)
    {
        this.pictureIndex = pictureIndex;
    }

    public long getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(long createTime)
    {
        this.createTime = createTime;
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
