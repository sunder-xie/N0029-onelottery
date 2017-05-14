package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author caozhongzheng
 * @package: com.peersafe.onelottery.model
 * @description: 获取用户信息的解析类
 */
public class ZXUserInfoRet
{

    /**
     * code : 0
     * message : Success
     * data : {"owner":"e5686ca528593945e7b5105fba1077e71545e31ee1dff72b7a4406bd","userId":"zy3"}
     */

    private int code;
    private String message;
    private DataBean data;

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

    public DataBean getData()
    {
        return data;
    }

    public void setData(DataBean data)
    {
        this.data = data;
    }

    public static class DataBean
    {
        /**
         * owner : e5686ca528593945e7b5105fba1077e71545e31ee1dff72b7a4406bd
         * userId : zy3
         */

        private String owner;
        private String userId;

        public String getOwner()
        {
            return owner;
        }

        public void setOwner(String owner)
        {
            this.owner = owner;
        }

        public String getUserId()
        {
            return userId;
        }

        public void setUserId(String userId)
        {
            this.userId = userId;
        }
    }
}