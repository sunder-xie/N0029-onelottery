package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.model
 * @description:
 * @date 21/11/16 AM11:14
 */
public class ZXCoinTransferResponse
{

    /**
     * Owner : 7362932a505dd156b345b044705b854048aec6c5f7aaaac8704ad970
     * Oppisite : 0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13
     * Code : 0
     * CodeMessage : Success
     */

    private String Owner;
    private String Oppisite;
    private int Code;
    private String CodeMessage;

    public String getOwner()
    {
        return Owner;
    }

    public void setOwner(String Owner)
    {
        this.Owner = Owner;
    }

    public String getOppisite()
    {
        return Oppisite;
    }

    public void setOppisite(String Oppisite)
    {
        this.Oppisite = Oppisite;
    }

    public int getCode()
    {
        return Code;
    }

    public void setCode(int Code)
    {
        this.Code = Code;
    }

    public String getCodeMessage()
    {
        return CodeMessage;
    }

    public void setCodeMessage(String CodeMessage)
    {
        this.CodeMessage = CodeMessage;
    }
}
