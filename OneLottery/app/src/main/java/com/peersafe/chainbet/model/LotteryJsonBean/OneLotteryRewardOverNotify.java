package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.model.LotteryJsonBean
 * @description:
 * @date 25/11/16 AM11:42
 */
public class OneLotteryRewardOverNotify
{
    /**
     * code : 0
     * message :
     * data : {"txnID":"5107e8e3-1223-4311-80d5-4ef1fcde090b",
     * "attendee":"989281f2f7c3978a0ec741db547e06f67a2a159e00e96ca40c6c8a89",
     * "attendeeName":"one_chain_admin","numbers":"10000000","amount":10000,"CreateTime":1489474098575,
     * "lotteryID":"f78d4a9a-d597-4234-9452-119b096f5000"}
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
         * txnID : 5107e8e3-1223-4311-80d5-4ef1fcde090b
         * attendee : 989281f2f7c3978a0ec741db547e06f67a2a159e00e96ca40c6c8a89
         * attendeeName : one_chain_admin
         * numbers : 10000000
         * amount : 10000
         * CreateTime : 1489474098575
         * lotteryID : f78d4a9a-d597-4234-9452-119b096f5000
         */

        private String txnID;
        private String attendee;
        private String attendeeName;
        private String numbers;
        private long amount;
        private long CreateTime;
        private String lotteryID;

        public void setTxnID(String txnID)
        {
            this.txnID = txnID;
        }

        public void setAttendee(String attendee)
        {
            this.attendee = attendee;
        }

        public void setAttendeeName(String attendeeName)
        {
            this.attendeeName = attendeeName;
        }

        public void setNumbers(String numbers)
        {
            this.numbers = numbers;
        }

        public void setAmount(long amount)
        {
            this.amount = amount;
        }

        public void setCreateTime(long CreateTime)
        {
            this.CreateTime = CreateTime;
        }

        public String getTxnID()
        {
            return txnID;
        }

        public String getAttendee()
        {
            return attendee;
        }

        public String getAttendeeName()
        {
            return attendeeName;
        }

        public String getNumbers()
        {
            return numbers;
        }

        public long getAmount()
        {
            return amount;
        }

        public long getCreateTime()
        {
            return CreateTime;
        }

        public String getLotteryID()
        {
            return lotteryID;
        }

        public void setLotteryID(String lotteryID)
        {
            this.lotteryID = lotteryID;
        }
    }
}
