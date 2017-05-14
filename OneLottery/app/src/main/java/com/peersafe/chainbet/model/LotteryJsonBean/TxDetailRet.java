package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author moying
 * @Description 交易详情
 * @date 2017/2/17 14:19
 */
public class TxDetailRet
{
    public static final String TYPE_TRANSFER_DETAIL_LOTTERY_ADD = "oneLotteryAdd";
    public static final String TYPE_TRANSFER_DETAIL_LOTTERY_EDT = "oneLotteryEdit";
    public static final String TYPE_TRANSFER_DETAIL_BET = "oneLotteryBet";
    public static final String TYPE_TRANSFER_DETAIL_COINTRANSFER = "zxCoinTransfer";
    public static final String TYPE_TRANSFER_DETAIL_BET_OVER = "oneLotteryBetOver";
    public static final String TYPE_TRANSFER_DETAIL_REFUND = "oneLotteryRefund";
    public static final String TYPE_TRANSFER_DETAIL_WITHDRAW = "zxCoinWithdraw";
    public static final String TYPE_TRANSFER_DETAIL_WITHDRAW_APPEALDONE = "zxCoinWithdrawAppealDone";
    public static final String TYPE_TRANSFER_DETAIL_WITHDRAW_CONFIRM = "zxCoinWithdrawConfirm";
    public static final String TYPE_TRANSFER_DETAIL_WITHDRAW_FAIL = "zxCoinWithdrawFail";


    // eg: oneLotteryAdd&X&5d0108a4d1adcb6fc2ba2edbc0866322eaaf0c0ff1f856eb018d9b34&X&{***}
    private String txType; // 交易类型,详见子类 eg： "zxCoinTransfer"
    private String launcherHash;// 发起者hash
    private String txid;// 提现等交易id等

    public String getTxType()
    {
        return txType;
    }

    public void setTxType(String txType)
    {
        this.txType = txType;
    }

    public String getLauncherHash()
    {
        return launcherHash;
    }

    public void setLauncherHash(String launcherHash)
    {
        this.launcherHash = launcherHash;
    }

    public String getTxid()
    {
        return txid;
    }

    public void setTxid(String txid)
    {
        this.txid = txid;
    }
}
