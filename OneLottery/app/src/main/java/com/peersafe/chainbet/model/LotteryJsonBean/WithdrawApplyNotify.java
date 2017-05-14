package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * AUTHOR : czz.
 * DATA : 17/4/21
 * DESCRIPTION : 多设备登录时收到的提现申请
 */

public class WithdrawApplyNotify
{

    /**
     * code : 0
     * message : Success
     * data : {"UserName":"oho","UserHash":"fbbfd5dd146499748299948d56c6e329b78985440014550bc0fc77b4"}
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
         * UserName : oho
         * UserHash : fbbfd5dd146499748299948d56c6e329b78985440014550bc0fc77b4
         */

        private String UserName;
        private String UserHash;

        public String getUserName()
        {
            return UserName;
        }

        public void setUserName(String UserName)
        {
            this.UserName = UserName;
        }

        public String getUserHash()
        {
            return UserHash;
        }

        public void setUserHash(String UserHash)
        {
            this.UserHash = UserHash;
        }
    }
}
