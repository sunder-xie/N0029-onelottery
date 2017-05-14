package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author caozhongzheng
 * @Description 删除规则的消息
 * @date 2017/1/19 14:36
 */
public class PrizeRuleDelRet
{

    /**
     * code : 0
     * message :
     * data : afe283f8-5181-4332-9e23-4c7affb7b156
     */

    private int code;
    private String message;
    private String data;

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }
}
