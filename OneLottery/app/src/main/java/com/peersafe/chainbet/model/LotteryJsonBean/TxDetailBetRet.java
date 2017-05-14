package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author moying
 * @Description 活动的投注的交易详情
 * @date 2017/2/17 14:19
 */
public class TxDetailBetRet extends TxDetailRet
{

    /**
     * lotteryID : 71a322c5-9535-4187-869b-c4655f34b1c0
     * amount : 10000
     * count : 1
     * userID : zy1
     * CreateTime : 1484732697
     */

    private String lotteryID;// 投注对应的活动ID
    private long amount;
    private int count;
    private String userID;
    private long CreateTime;

    public void setLotteryID(String lotteryID)
    {
        this.lotteryID = lotteryID;
    }

    public void setAmount(long amount)
    {
        this.amount = amount;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public void setUserID(String userID)
    {
        this.userID = userID;
    }

    public void setCreateTime(long CreateTime)
    {
        this.CreateTime = CreateTime;
    }

    public String getLotteryID()
    {
        return lotteryID;
    }

    public long getAmount()
    {
        return amount;
    }

    public int getCount()
    {
        return count;
    }

    public String getUserID()
    {
        return userID;
    }

    public long getCreateTime()
    {
        return CreateTime;
    }

    @Override
    public String toString()
    {
        return "TxDetailBetRet{" +
                "lotteryID='" + lotteryID + '\'' +
                ", amount=" + amount +
                ", count=" + count +
                ", userID='" + userID + '\'' +
                ", CreateTime=" + CreateTime +
                '}';
    }
}
