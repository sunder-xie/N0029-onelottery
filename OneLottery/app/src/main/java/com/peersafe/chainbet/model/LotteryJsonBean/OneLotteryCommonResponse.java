package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.model.LotteryJsonBean
 * @description: 一元链回调公共响应。对应的回调和通知有：规则添加，修改，删除；活动的添加，修改，删除；投注以及共识结束的回调和通知
 * @date 25/11/16 AM11:37
 */
public class OneLotteryCommonResponse
{
    /**
     * code : 0
     * message :
     * data : 6c86a6e4-1736-4b8e-889e-1a9b2cfea324
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
