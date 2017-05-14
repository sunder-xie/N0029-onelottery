package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/4/11
 * DESCRIPTION :
 */

public class AppealDoneNotity
{

    /**
     * code : 0
     * message : Success
     * data : {"TxId":"6aacdb7c-2254-43da-a3d9-9ed23ea2beb2","ModifyTime":1491909604,"Remark":"",
     * "UserName":"bbdddd","UserHash":"b43f3021c882a271bf476c593edc25d09a5bda7064e39d755b918c83",
     * "Result":1}
     */

    private int code;
    private String message;
    private DataBean data;

    public void setCode(int code)
    {
        this.code = code;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public void setData(DataBean data)
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

    public DataBean getData()
    {
        return data;
    }

    public static class DataBean
    {
        /**
         * TxId : 6aacdb7c-2254-43da-a3d9-9ed23ea2beb2
         * ModifyTime : 1491909604
         * Remark :
         * UserName : bbdddd
         * UserHash : b43f3021c882a271bf476c593edc25d09a5bda7064e39d755b918c83
         * Result : 1
         */

        private String TxId;
        private Long ModifyTime;
        private String Remark;
        private String UserName;
        private String UserHash;
        private int Result;

        public void setTxId(String TxId)
        {
            this.TxId = TxId;
        }

        public void setModifyTime(Long ModifyTime)
        {
            this.ModifyTime = ModifyTime;
        }

        public void setRemark(String Remark)
        {
            this.Remark = Remark;
        }

        public void setUserName(String UserName)
        {
            this.UserName = UserName;
        }

        public void setUserHash(String UserHash)
        {
            this.UserHash = UserHash;
        }

        public void setResult(int Result)
        {
            this.Result = Result;
        }

        public String getTxId()
        {
            return TxId;
        }

        public Long getModifyTime()
        {
            return ModifyTime;
        }

        public String getRemark()
        {
            return Remark;
        }

        public String getUserName()
        {
            return UserName;
        }

        public String getUserHash()
        {
            return UserHash;
        }

        public int getResult()
        {
            return Result;
        }
    }
}
