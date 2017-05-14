package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author moying
 * @Description 提现的交易详情
 * @date 2017/4/18 18:19
 */
public class TxDetailWithdrawRet extends TxDetailRet
{

    /**
     * AccountInfo : {"BankName":"北京银行","AccountName":"大圣","AccountId":"6225 1425 3769 8054"}
     * UserId : tuv
     * Amount : 2000
     * ModifyTime : 1492503760497
     */

    private AccountInfoRet accountInfoRet;
    private String AccountInfo;
    private String UserId;
    private long Amount;
    private long ModifyTime;

    public String getAccountInfo()
    {
        return AccountInfo;
    }

    public void setAccountInfo(String AccountInfo)
    {
        this.AccountInfo = AccountInfo;
    }

    public String getUserId()
    {
        return UserId;
    }

    public void setUserId(String UserId)
    {
        this.UserId = UserId;
    }

    public long getAmount()
    {
        return Amount;
    }

    public void setAmount(long Amount)
    {
        this.Amount = Amount;
    }

    public long getModifyTime()
    {
        return ModifyTime;
    }

    public void setModifyTime(long ModifyTime)
    {
        this.ModifyTime = ModifyTime;
    }

    public AccountInfoRet getAccountInfoRet()
    {
        return accountInfoRet;
    }

    public void setAccountInfoRet(AccountInfoRet accountInfoRet)
    {
        this.accountInfoRet = accountInfoRet;
    }
}
