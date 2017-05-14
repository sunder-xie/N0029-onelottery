package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author moying
 * @Description 获取活动列表和详情的返回json解析父类
 * @date 2017/1/12 13:54
 */
public class OneLotteryQueryRet
{
    protected int code;
    protected String message;

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

    public static class DataBean
    {
        /**
         * txnID : 88fb881d-ca57-4bc7-94e8-e3176499ebe8
         * newTxnID : // 最新的关于活动的交易ID存储
         * version : 0
         * lastCloseTime : 0
         * numbers :
         * balance : 0 // 已经募集到的总注数
         * prizeTxnID :
         * countTotal : 0 // 已经募集到的总金额
         * pictureIndex : 2
         * status : 0
         * updateTime : 0
         * blockHeight : 0
         * preBlockHeight : 0
         * txnIDs : // 最新的投注交易ID存储
         * createTime : 1483440601
         * name : 第二个官方活动
         * ruleType : PrizeRule
         * ruleID : 0462ce96-3d85-42ad-8fa0-1c5ee0370df7
         * publisherHash : 7cf9cdbb1811fe8931c0c04ddfa85569e99efdc2e26436a15dca9ad2
         * publisherName : one_chain_admin
         * startTime : 1483441201
         * closeTime : 1483549200
         * minAttendeeCnt : 2
         * maxAttendeeCnt : 2
         * cost : 10000
         * description : 第二个官方活动来了
         * fee : 0 // 修改活动时所扣费值
         */

        private String txnID;
        private String newTxnID;
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
        private String name;
        private String ruleType;
        private String ruleID;
        private String publisherHash;
        private String publisherName;
        private long startTime;
        private long closeTime;
        private int minAttendeeCnt;
        private int maxAttendeeCnt;
        private long cost;
        private String description;
        private long fee;

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

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getRuleType()
        {
            return ruleType;
        }

        public void setRuleType(String ruleType)
        {
            this.ruleType = ruleType;
        }

        public String getRuleID()
        {
            return ruleID;
        }

        public void setRuleID(String ruleID)
        {
            this.ruleID = ruleID;
        }

        public String getPublisherHash()
        {
            return publisherHash;
        }

        public void setPublisherHash(String publisherHash)
        {
            this.publisherHash = publisherHash;
        }

        public String getPublisherName()
        {
            return publisherName;
        }

        public void setPublisherName(String publisherName)
        {
            this.publisherName = publisherName;
        }

        public long getStartTime()
        {
            return startTime;
        }

        public void setStartTime(long startTime)
        {
            this.startTime = startTime;
        }

        public long getCloseTime()
        {
            return closeTime;
        }

        public void setCloseTime(long closeTime)
        {
            this.closeTime = closeTime;
        }

        public int getMinAttendeeCnt()
        {
            return minAttendeeCnt;
        }

        public void setMinAttendeeCnt(int minAttendeeCnt)
        {
            this.minAttendeeCnt = minAttendeeCnt;
        }

        public int getMaxAttendeeCnt()
        {
            return maxAttendeeCnt;
        }

        public void setMaxAttendeeCnt(int maxAttendeeCnt)
        {
            this.maxAttendeeCnt = maxAttendeeCnt;
        }

        public long getCost()
        {
            return cost;
        }

        public void setCost(long cost)
        {
            this.cost = cost;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public long getFee()
        {
            return fee;
        }

        public void setFee(long fee)
        {
            this.fee = fee;
        }

        @Override
        public String toString()
        {
            return "DataBean{" +
                    "txnID='" + txnID + '\'' +
                    ", newTxnID='" + newTxnID + '\'' +
                    ", version=" + version +
                    ", lastCloseTime=" + lastCloseTime +
                    ", numbers='" + numbers + '\'' +
                    ", balance=" + balance +
                    ", prizeTxnID='" + prizeTxnID + '\'' +
                    ", countTotal=" + countTotal +
//                    ", pictureIndex=" + pictureIndex +
                    ", status=" + status +
//                    ", updateTime=" + updateTime +
                    ", blockHeight=" + blockHeight +
                    ", preBlockHeight=" + preBlockHeight +
                    ", txnIDs='" + txnIDs + '\'' +
//                    ", createTime=" + createTime +
                    ", name='" + name + '\'' +
//                    ", ruleType='" + ruleType + '\'' +
//                    ", ruleID='" + ruleID + '\'' +
                    ", publisherHash='" + publisherHash + '\'' +
                    ", publisherName='" + publisherName + '\'' +
//                    ", startTime=" + startTime +
//                    ", closeTime=" + closeTime +
//                    ", minAttendeeCnt=" + minAttendeeCnt +
//                    ", maxAttendeeCnt=" + maxAttendeeCnt +
//                    ", cost=" + cost +
//                    ", description='" + description + '\'' +
//                    ", fee=" + fee +
                    '}';
        }
    }



}
