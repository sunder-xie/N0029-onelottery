package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author moying
 * @Description 收到开奖消息
 * @date 2017/1/17 13:54
 */
public class OneLotteryBetOverNotify
{
    /**
     * code : 0
     * message :
     * data : {"txnID":"ee116b54-00c0-41cb-9b1d-0936d5777495",
     * "attendee":"b20af620842c7ec70b543a1cbf897fe12fdfe2f4af5ea5629f7d4cbb",
     * "attendeeName":"123456789fff","numbers":"10000002","amount":10000,"CreateTime":1484647884,
     * "lotteryID":"4e1f3000-58b6-45c6-902d-fb3c5aae8588"}
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
         * txnID : ee116b54-00c0-41cb-9b1d-0936d5777495
         * attendee : b20af620842c7ec70b543a1cbf897fe12fdfe2f4af5ea5629f7d4cbb
         * attendeeName : 123456789fff
         * numbers : 10000002
         * amount : 10000
         * CreateTime : 1484647884
         * lotteryID : 4e1f3000-58b6-45c6-902d-fb3c5aae8588
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

        public void setLotteryID(String lotteryID)
        {
            this.lotteryID = lotteryID;
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
    }
}
