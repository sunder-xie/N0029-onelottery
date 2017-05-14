package com.peersafe.chainbet.model.LotteryJsonBean;

import java.util.List;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.model.LotteryJsonBean
 * @description:
 * @date 24/11/16 PM6:40
 */
public class OneLotteryBetDetailRet
{

    /**
     * code : 0
     * message :
     * data : [{"txnID":"b201112b-5630-4f6b-97ae-98a2999aa0fc","attendee":"2196c1a128d2fb64a6c38aba2e858bb4ad95216fe17ebb29eb784a7c","attendeeName":"one_chain_admin","numbers":"10000005 10000002","amount":20000,"CreateTime":1483463340},{"txnID":"39261ecb-9c71-47e1-89ca-a081f7e15623","attendee":"2196c1a128d2fb64a6c38aba2e858bb4ad95216fe17ebb29eb784a7c","attendeeName":"one_chain_admin","numbers":"10000000 10000003","amount":20000,"CreateTime":1483463340},{"txnID":"409661e9-a5b8-471f-8534-1855b652ccc3","attendee":"2196c1a128d2fb64a6c38aba2e858bb4ad95216fe17ebb29eb784a7c","attendeeName":"one_chain_admin","numbers":"10000001 10000004","amount":20000,"CreateTime":1483463340}]
     */

    private int code;
    private String message;
    private List<DataBean> data;

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

    public List<DataBean> getData()
    {
        return data;
    }

    public void setData(List<DataBean> data)
    {
        this.data = data;
    }

    public static class DataBean
    {
        /**
         * txnID : b201112b-5630-4f6b-97ae-98a2999aa0fc
         * attendee : 2196c1a128d2fb64a6c38aba2e858bb4ad95216fe17ebb29eb784a7c
         * attendeeName : one_chain_admin
         * numbers : 10000005 10000002
         * amount : 20000
         * CreateTime : 1483463340
         */

        private String txnID;
        private String attendee;
        private String attendeeName;
        private String numbers;
        private long amount;
        private long CreateTime;

        public String getTxnID()
        {
            return txnID;
        }

        public void setTxnID(String txnID)
        {
            this.txnID = txnID;
        }

        public String getAttendee()
        {
            return attendee;
        }

        public void setAttendee(String attendee)
        {
            this.attendee = attendee;
        }

        public String getAttendeeName()
        {
            return attendeeName;
        }

        public void setAttendeeName(String attendeeName)
        {
            this.attendeeName = attendeeName;
        }

        public String getNumbers()
        {
            return numbers;
        }

        public void setNumbers(String numbers)
        {
            this.numbers = numbers;
        }

        public long getAmount()
        {
            return amount;
        }

        public void setAmount(long amount)
        {
            this.amount = amount;
        }

        public long getCreateTime()
        {
            return CreateTime;
        }

        public void setCreateTime(long CreateTime)
        {
            this.CreateTime = CreateTime;
        }
    }
}
