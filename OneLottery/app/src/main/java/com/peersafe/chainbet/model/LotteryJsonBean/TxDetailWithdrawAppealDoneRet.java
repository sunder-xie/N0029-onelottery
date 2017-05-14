package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author moying
 * @Description 提现的申诉处理结果详情
 * @date 2017/4/19 09:59
 */
public class TxDetailWithdrawAppealDoneRet extends TxDetailRet
{


    /**
     * TxId : fcd053b5-c040-4145-a85d-b368f5203ba3
     * Result : 1
     * Remark :
     a
     b

     c
     d
     * UserId : one_chain_admin
     * Extras : admin
     * ModifyTime : 1492503839617
     */

    private String TxId;
    private int Result;
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

    public int getResult()
    {
        return Result;
    }

    public void setResult(int Result)
    {
        this.Result = Result;
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
