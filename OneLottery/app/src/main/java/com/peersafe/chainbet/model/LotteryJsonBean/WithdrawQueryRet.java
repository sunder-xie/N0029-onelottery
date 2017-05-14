package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/4/10
 * DESCRIPTION :
 */

public class WithdrawQueryRet
{
    /**
     * code : 0
     * message : Success
     * data : {"TxId":"71ed5300-bc46-4caf-bb23-5ca710effc5a","State":1,
     * "AccountInfo":"{\"BankName\":\"中国银行\",\"AccountName\":\"孙海涛\",
     * \"AccountId\":\"12354458877588\"}","Amount":100000,"RemitOrderNumber":"","Remark":"",
     * "ModifyTime":1491789288,"CreateTime":1491789288,"UserName":"ghhg",
     * "UserHash":"7b63fe18ea1c3fc7a8ecbee971c66f493b02ce19a864512ec81da395"}
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
         * TxId : 71ed5300-bc46-4caf-bb23-5ca710effc5a
         * State : 1
         * AccountInfo : {"BankName":"中国银行","AccountName":"孙海涛","AccountId":"12354458877588"}
         * Amount : 100000
         * RemitOrderNumber :
         * Remark :
         * ModifyTime : 1491789288
         * CreateTime : 1491789288
         * UserName : ghhg
         * UserHash : 7b63fe18ea1c3fc7a8ecbee971c66f493b02ce19a864512ec81da395
         */

        private String TxId;
        private int State;
        private String AccountInfo;
        private Long Amount;
        private String RemitOrderNumber;
        private String Remark;
        private long ModifyTime;
        private long CreateTime;
        private String UserName;
        private String UserHash;

        public void setTxId(String TxId)
        {
            this.TxId = TxId;
        }

        public void setState(int State)
        {
            this.State = State;
        }

        public void setAccountInfo(String AccountInfo)
        {
            this.AccountInfo = AccountInfo;
        }

        public void setAmount(long Amount)
        {
            this.Amount = Amount;
        }

        public void setRemitOrderNumber(String RemitOrderNumber)
        {
            this.RemitOrderNumber = RemitOrderNumber;
        }

        public void setRemark(String Remark)
        {
            this.Remark = Remark;
        }

        public void setModifyTime(long ModifyTime)
        {
            this.ModifyTime = ModifyTime;
        }

        public void setCreateTime(long CreateTime)
        {
            this.CreateTime = CreateTime;
        }

        public void setUserName(String UserName)
        {
            this.UserName = UserName;
        }

        public void setUserHash(String UserHash)
        {
            this.UserHash = UserHash;
        }

        public String getTxId()
        {
            return TxId;
        }

        public int getState()
        {
            return State;
        }

        public String getAccountInfo()
        {
            return AccountInfo;
        }

        public long getAmount()
        {
            return Amount;
        }

        public String getRemitOrderNumber()
        {
            return RemitOrderNumber;
        }

        public String getRemark()
        {
            return Remark;
        }

        public long getModifyTime()
        {
            return ModifyTime;
        }

        public long getCreateTime()
        {
            return CreateTime;
        }

        public String getUserName()
        {
            return UserName;
        }

        public String getUserHash()
        {
            return UserHash;
        }
    }
}
