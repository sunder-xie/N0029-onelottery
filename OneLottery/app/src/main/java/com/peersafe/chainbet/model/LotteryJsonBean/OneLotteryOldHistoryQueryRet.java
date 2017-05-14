package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author caozhongzheng
 * @Description 超过7天的历史活动数据详情
 * @date 2017/2/20 10:45
 */
public class OneLotteryOldHistoryQueryRet
{

    /**
     * code : 0
     * message :
     * data : {"txnID":"3ec08693-43f0-41df-9b3c-0929e0707027","newTxnID":"","version":2,"lastCloseTime":1487415762,"numbers":"10000003",
     * "balance":50000,"prizeTxnID":"9c5da3d5-4d4b-4cee-ab9c-5307854a2716","countTotal":5,"pictureIndex":1,"status":4,
     * "updateTime":1487415695,"blockHeight":7,"preBlockHeight":7,"txnIDs":" 9c5da3d5-4d4b-4cee-ab9c-5307854a2716","createTime":1487415695}
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
         * txnID : 3ec08693-43f0-41df-9b3c-0929e0707027
         * newTxnID :
         * version : 2
         * lastCloseTime : 1487415762
         * numbers : 10000003
         * balance : 50000
         * prizeTxnID : 9c5da3d5-4d4b-4cee-ab9c-5307854a2716
         * countTotal : 5
         * pictureIndex : 1
         * status : 4
         * updateTime : 1487415695
         * blockHeight : 7
         * preBlockHeight : 7
         * txnIDs :  9c5da3d5-4d4b-4cee-ab9c-5307854a2716
         * createTime : 1487415695
         */

        private String txnID;
        private String newTxnID;
        private String name;
        private int version;
        private long lastCloseTime;
        private String numbers;
        private long balance;
        private String prizeTxnID;
        private int countTotal;
        private int pictureIndex;
        private int status;
        private long updateTime;
        private long blockHeight;
        private long preBlockHeight;
        private String txnIDs;
        private long createTime;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }
        public String getTxnID()
        {
            return txnID;
        }

        public void setTxnID(String txnID)
        {
            this.txnID = txnID;
        }

        public String getNewTxnID()
        {
            return newTxnID;
        }

        public void setNewTxnID(String newTxnID)
        {
            this.newTxnID = newTxnID;
        }

        public int getVersion()
        {
            return version;
        }

        public void setVersion(int version)
        {
            this.version = version;
        }

        public long getLastCloseTime()
        {
            return lastCloseTime;
        }

        public void setLastCloseTime(long lastCloseTime)
        {
            this.lastCloseTime = lastCloseTime;
        }

        public String getNumbers()
        {
            return numbers;
        }

        public void setNumbers(String numbers)
        {
            this.numbers = numbers;
        }

        public long getBalance()
        {
            return balance;
        }

        public void setBalance(long balance)
        {
            this.balance = balance;
        }

        public String getPrizeTxnID()
        {
            return prizeTxnID;
        }

        public void setPrizeTxnID(String prizeTxnID)
        {
            this.prizeTxnID = prizeTxnID;
        }

        public int getCountTotal()
        {
            return countTotal;
        }

        public void setCountTotal(int countTotal)
        {
            this.countTotal = countTotal;
        }

        public int getPictureIndex()
        {
            return pictureIndex;
        }

        public void setPictureIndex(int pictureIndex)
        {
            this.pictureIndex = pictureIndex;
        }

        public int getStatus()
        {
            return status;
        }

        public void setStatus(int status)
        {
            this.status = status;
        }

        public long getUpdateTime()
        {
            return updateTime;
        }

        public void setUpdateTime(long updateTime)
        {
            this.updateTime = updateTime;
        }

        public long getBlockHeight()
        {
            return blockHeight;
        }

        public void setBlockHeight(long blockHeight)
        {
            this.blockHeight = blockHeight;
        }

        public long getPreBlockHeight()
        {
            return preBlockHeight;
        }

        public void setPreBlockHeight(long preBlockHeight)
        {
            this.preBlockHeight = preBlockHeight;
        }

        public String getTxnIDs()
        {
            return txnIDs;
        }

        public void setTxnIDs(String txnIDs)
        {
            this.txnIDs = txnIDs;
        }

        public long getCreateTime()
        {
            return createTime;
        }

        public void setCreateTime(long createTime)
        {
            this.createTime = createTime;
        }
    }
}
