package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.model
 * @description:
 * @date 21/11/16 AM11:19
 */
public class ZXCoinBalanceRet
{

    /**
     * code : 0
     * message : Success
     * data : {"Owner":"6624f464e5e6e50449a6e8236cd64f139912774efd64214b2d96ef65","Name":"zy10","Balance":1000000,"Reserved":0,"TxnIDs":"b9227234-cf56-4c04-b281-650adb9a0f0c ","BlockHeight":30,"PrevBlockHeight":30}
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
         * Owner : 6624f464e5e6e50449a6e8236cd64f139912774efd64214b2d96ef65
         * Name : zy10
         * Balance : 1000000
         * Reserved : 0
         * TxnIDs : b9227234-cf56-4c04-b281-650adb9a0f0c
         * BlockHeight : 30
         * PrevBlockHeight : 30
         */

        private String Owner;
        private String Name;
        private long Balance;
        private long Reserved;
        private String TxnIDs;
        private long BlockHeight;
        private long PrevBlockHeight;

        public String getOwner()
        {
            return Owner;
        }

        public void setOwner(String Owner)
        {
            this.Owner = Owner;
        }

        public String getName()
        {
            return Name;
        }

        public void setName(String Name)
        {
            this.Name = Name;
        }

        public long getBalance()
        {
            return Balance;
        }

        public void setBalance(long Balance)
        {
            this.Balance = Balance;
        }

        public long getReserved()
        {
            return Reserved;
        }

        public void setReserved(long Reserved)
        {
            this.Reserved = Reserved;
        }

        public String getTxnIDs()
        {
            return TxnIDs;
        }

        public void setTxnIDs(String TxnIDs)
        {
            this.TxnIDs = TxnIDs;
        }

        public long getBlockHeight()
        {
            return BlockHeight;
        }

        public void setBlockHeight(long BlockHeight)
        {
            this.BlockHeight = BlockHeight;
        }

        public long getPrevBlockHeight()
        {
            return PrevBlockHeight;
        }

        public void setPrevBlockHeight(long PrevBlockHeight)
        {
            this.PrevBlockHeight = PrevBlockHeight;
        }
    }
}