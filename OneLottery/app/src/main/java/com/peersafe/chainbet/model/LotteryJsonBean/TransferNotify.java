package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author caozhongzheng
 * @Description 接收到官方或别人给自己的转账消息
 * @date 2017/2/14 14:35
 */
public class TransferNotify
{

    /**
     * code : 0
     * message : Success
     * data : {"owner":"a94afe373b55f0c430f19a376fdd3a9063ee3e50b2a87992733a28cd","ownUserId":"xm8",
     * "oppisite":"768c78dad5e701cb54234153caaea2a573623e909638f05674c015ba","oppisiteUserId":"797979tcl","amount":88000,"fee":100}
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
         * owner : a94afe373b55f0c430f19a376fdd3a9063ee3e50b2a87992733a28cd
         * ownUserId : xm8
         * oppisite : 768c78dad5e701cb54234153caaea2a573623e909638f05674c015ba
         * oppisiteUserId : 797979tcl
         * amount : 88000
         * fee : 100
         */

        private String owner;
        private String ownUserId;
        private String oppisite;
        private String oppisiteUserId;
        private long amount;
        private long fee;

        public String getOwner()
        {
            return owner;
        }

        public void setOwner(String owner)
        {
            this.owner = owner;
        }

        public String getOwnUserId()
        {
            return ownUserId;
        }

        public void setOwnUserId(String ownUserId)
        {
            this.ownUserId = ownUserId;
        }

        public String getOppisite()
        {
            return oppisite;
        }

        public void setOppisite(String oppisite)
        {
            this.oppisite = oppisite;
        }

        public String getOppisiteUserId()
        {
            return oppisiteUserId;
        }

        public void setOppisiteUserId(String oppisiteUserId)
        {
            this.oppisiteUserId = oppisiteUserId;
        }

        public long getAmount()
        {
            return amount;
        }

        public void setAmount(long amount)
        {
            this.amount = amount;
        }

        public long getFee()
        {
            return fee;
        }

        public void setFee(long fee)
        {
            this.fee = fee;
        }
    }
}
