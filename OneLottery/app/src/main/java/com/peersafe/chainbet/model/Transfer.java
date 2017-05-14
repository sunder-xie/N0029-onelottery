package com.peersafe.chainbet.model;

/**
 * @author caozhongzheng
 * @Description 转账信息
 * @date 2017/2/7 17:00
 */
public class Transfer
{
    String nameTo; // 转账的目的用户的名字
    String addressTo; // 转账的目的用户的公钥hash地址
    long amount; // 转账金额，实际金额*10000
    String remark; // 转账备注

    public Transfer(String nameTo, String addressTo, long amount, String remark)
    {
        this.nameTo = nameTo;
        this.addressTo = addressTo;
        this.amount = amount;
        this.remark = remark;
    }

    public String getNameTo()
    {
        return nameTo;
    }

    public void setNameTo(String nameTo)
    {
        this.nameTo = nameTo;
    }

    public String getAddressTo()
    {
        return addressTo;
    }

    public void setAddressTo(String addressTo)
    {
        this.addressTo = addressTo;
    }

    public long getAmount()
    {
        return amount;
    }

    public void setAmount(long amount)
    {
        this.amount = amount;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    @Override
    public String toString()
    {
        return "Transfer{" +
                "nameTo='" + nameTo + '\'' +
                ", addressTo='" + addressTo + '\'' +
                ", amount=" + amount +
                ", remark='" + remark + '\'' +
                '}';
    }
}
