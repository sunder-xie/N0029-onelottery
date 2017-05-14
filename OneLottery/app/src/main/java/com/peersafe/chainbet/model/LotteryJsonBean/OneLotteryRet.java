package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/1/12
 * DESCRIPTION :
 */

public class OneLotteryRet
{
    /**
     * code : 0
     * message :
     * data : {"txnID":"c26625a6-aa3c-4799-b42e-a8eaaa179ad1","newTxnID":"","version":0,
     * "publisherName":"one_chain_admin",
     * "publisherHash":"3f363afeb0117eefefc1988b0b6aef4f4f16509d389129c059630674"}
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
         * txnID : c26625a6-aa3c-4799-b42e-a8eaaa179ad1
         * newTxnID :
         * version : 0
         * publisherName : one_chain_admin
         * publisherHash : 3f363afeb0117eefefc1988b0b6aef4f4f16509d389129c059630674
         */

        private String txnID;
        private String newTxnID;
        private String name;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        private int version;
        private String publisherName;
        private String publisherHash;

        public void setTxnID(String txnID)
        {
            this.txnID = txnID;
        }

        public void setNewTxnID(String newTxnID)
        {
            this.newTxnID = newTxnID;
        }

        public void setVersion(int version)
        {
            this.version = version;
        }

        public void setPublisherName(String publisherName)
        {
            this.publisherName = publisherName;
        }

        public void setPublisherHash(String publisherHash)
        {
            this.publisherHash = publisherHash;
        }

        public String getTxnID()
        {
            return txnID;
        }

        public String getNewTxnID()
        {
            return newTxnID;
        }

        public int getVersion()
        {
            return version;
        }

        public String getPublisherName()
        {
            return publisherName;
        }

        public String getPublisherHash()
        {
            return publisherHash;
        }
    }
}
