package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author moying
 * @Description 活动的投注的交易详情
 * @date 2017/2/17 14:19
 */
public class TxDetailRefundRet extends TxDetailRet
{

    /**
     * lotteryID : 2544feaf-3cdd-4d66-9824-45ef5df6f022
     * currentTime : 1486638300
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
