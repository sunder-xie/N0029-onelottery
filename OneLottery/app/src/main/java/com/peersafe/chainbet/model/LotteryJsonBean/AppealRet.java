package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/4/11
 * DESCRIPTION :
 */

public class AppealRet
{
    /**
     * code : 0
     * message : Success
     * data : 8abc8db0-b911-4fd9-b906-18d414693ee0
     */

    private int code;
    private String message;
    private String data;

    public void setCode(int code)
    {
        this.code = code;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public int getCode()
    {
        return code;
    }

    public String getMessage()
    {
        return message;
    }

    public String getData()
    {
        return data;
    }
}
