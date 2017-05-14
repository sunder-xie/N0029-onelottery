package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author moying
 * @Description 活动的投注的交易详情
 * @date 2017/2/17 14:19
 */
public class TxDetailBetOverRet extends TxDetailRet
{

    /**
     * lotteryID : 7daa4ca5-5db2-4dfd-9e1b-1ec5310332eb
     * currentTime : 1486628257
     */

    private String lotteryID;
    private long currentTime;

    public String getLotteryID()
    {
        return lotteryID;
    }

    public void setLotteryID(String lotteryID)
    {
        this.lotteryID = lotteryID;
    }

    public long getCurrentTime()
    {
        return currentTime;
    }

    public void setCurrentTime(long currentTime)
    {
        this.currentTime = currentTime;
    }
}
