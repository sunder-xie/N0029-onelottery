package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author moying
 * @Description 转账的交易详情
 * @date 2017/2/17 14:19
 */
public class TxDetailTransferRet extends TxDetailRet
{

    /**
     * userID : one_chain_admin
     * nameTo : zy5
     * userCertTo : 75add9264ff8e9ffc03c54ef54edc37045792400ecf36ab143244728
     * amount : 10000000
     * fee : 100
     * type : 0
     * remark :
     * time : 1486542923
     */

    private String userID;
    private String nameTo;
    private String userCertTo;
    private long amount;
    private long fee;
    private int type;
    private String remark;
    private long time;

    public String getUserID()
    {
        return userID;
    }

    public void setUserID(String userID)
    {
        this.userID = userID;
    }

    public String getNameTo()
    {
        return nameTo;
    }

    public void setNameTo(String nameTo)
    {
        this.nameTo = nameTo;
    }

    public String getUserCertTo()
    {
        return userCertTo;
    }

    public void setUserCertTo(String userCertTo)
    {
        this.userCertTo = userCertTo;
    }

    public long getAmount()
    {
        return amount;
    }

    public void setAmount(long amount)
    {
        this.amount = amount;
    }

    public long getFee()
    {
        return fee;
    }

    public void setFee(long fee)
    {
        this.fee = fee;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long time)
    {
        this.time = time;
    }
}
