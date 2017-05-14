package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/4/10
 * DESCRIPTION :
 */

public class AccountInfoRet
{
    /**
     * BankName : 中国银行
     * AccountName : 孙海涛
     * AccountId : 12354458877588
     */

    private String BankName;
    private String AccountName;
    private String AccountId;

    public void setBankName(String BankName)
    {
        this.BankName = BankName;
    }

    public void setAccountName(String AccountName)
    {
        this.AccountName = AccountName;
    }

    public void setAccountId(String AccountId)
    {
        this.AccountId = AccountId;
    }

    public String getBankName()
    {
        return BankName;
    }

    public String getAccountName()
    {
        return AccountName;
    }

    public String getAccountId()
    {
        return AccountId;
    }
}
