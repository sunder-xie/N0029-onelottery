package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author moying
 * @Description 提现失败的交易详情
 * @date 2017/4/18 18:19
 */
public class TxDetailWithdrawFailRet extends TxDetailRet
{

    /**
     * TxId : 37877497-56ce-47ca-a715-1a4846be82e6
     * Remark : 拒绝给你退款��
     * UserId : one_chain_admin
     * Extras : admin
     * ModifyTime : 1492500105800
     */

    private String TxId;
    private String Remark;
    private String UserId;
    private String Extras;
    private long ModifyTime;

    public String getTxId()
    {
        return TxId;
    }

    public void setTxId(String TxId)
    {
        this.TxId = TxId;
    }

    public String getRemark()
    {
        return Remark;
    }

    public void setRemark(String Remark)
    {
        this.Remark = Remark;
    }

    public String getUserId()
    {
        return UserId;
    }

    public void setUserId(String UserId)
    {
        this.UserId = UserId;
    }

    public String getExtras()
    {
        return Extras;
    }

    public void setExtras(String Extras)
    {
        this.Extras = Extras;
    }

    public long getModifyTime()
    {
        return ModifyTime;
    }

    public void setModifyTime(long ModifyTime)
    {
        this.ModifyTime = ModifyTime;
    }
}
