package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/1/22
 * DESCRIPTION :
 */

public class OneLotteryStartNotify
{
    /**
     * code : 0
     * message :
     * data : 12abc2cb-f125-469a-9a47-2843fca1e641
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
