package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author moying
 * @Description 提现的确认交易详情
 * @date 2017/4/18 18:19
 */
public class TxDetailWithdrawConfirmRet extends TxDetailRet
{


    /**
     * TxId : 95982dd9-70e5-4e54-8980-e8c2f6ed09d5
     * ModifyTime : 1492503733198
     * Extras :
     */

    private String TxId;
    private long ModifyTime;
    private String Extras;

    public String getTxId()
    {
        return TxId;
    }

    public void setTxId(String TxId)
    {
        this.TxId = TxId;
    }

    public long getModifyTime()
    {
        return ModifyTime;
    }

    public void setModifyTime(long ModifyTime)
    {
        this.ModifyTime = ModifyTime;
    }

    public String getExtras()
    {
        return Extras;
    }

    public void setExtras(String Extras)
    {
        this.Extras = Extras;
    }
}
