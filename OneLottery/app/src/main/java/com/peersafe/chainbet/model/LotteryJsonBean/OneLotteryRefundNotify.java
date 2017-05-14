package com.peersafe.chainbet.model.LotteryJsonBean;


import java.util.List;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.model.LotteryJsonBean  canRefundNotify用
 * @description:
 * @date 24/11/16 PM7:21
 */
public class OneLotteryRefundNotify
{


    /**
     * code : 0
     * message : Success
     * data : {"array":[{"owner":"79450fe68537df611de66d2a7b46aca775fa571961bb25759d4d034f",
     * "oppisite":"79450fe68537df611de66d2a7b46aca775fa571961bb25759d4d034f","amount":50000},
     * {"owner":"79450fe68537df611de66d2a7b46aca775fa571961bb25759d4d034f",
     * "oppisite":"79450fe68537df611de66d2a7b46aca775fa571961bb25759d4d034f","amount":50000}],
     * "lotteryID":"cae560e2-e891-448c-9411-25a32a29b6b7","currentTime":1489564877480}
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
         * array : [{"owner":"79450fe68537df611de66d2a7b46aca775fa571961bb25759d4d034f",
         * "oppisite":"79450fe68537df611de66d2a7b46aca775fa571961bb25759d4d034f","amount":50000},
         * {"owner":"79450fe68537df611de66d2a7b46aca775fa571961bb25759d4d034f",
         * "oppisite":"79450fe68537df611de66d2a7b46aca775fa571961bb25759d4d034f","amount":50000}]
         * lotteryID : cae560e2-e891-448c-9411-25a32a29b6b7
         * currentTime : 1489564877480
         */

        private String lotteryID;
        private long currentTime;
        private List<ArrayBean> array;

        public String getLotteryID()
        {
            return lotteryID;
        }

        public void setLotteryID(String lotteryID)
        {
            this.lotteryID = lotteryID;
        }

        public long getCurrentTime()
        {
            return currentTime;
        }

        public void setCurrentTime(long currentTime)
        {
            this.currentTime = currentTime;
        }

        public List<ArrayBean> getArray()
        {
            return array;
        }

        public void setArray(List<ArrayBean> array)
        {
            this.array = array;
        }

        public static class ArrayBean
        {
            /**
             * owner : 79450fe68537df611de66d2a7b46aca775fa571961bb25759d4d034f
             * oppisite : 79450fe68537df611de66d2a7b46aca775fa571961bb25759d4d034f
             * amount : 50000
             */

            private String owner;
            private String oppisite;
            private int amount;

            public String getOwner()
            {
                return owner;
            }

            public void setOwner(String owner)
            {
                this.owner = owner;
            }

            public String getOppisite()
            {
                return oppisite;
            }

            public void setOppisite(String oppisite)
            {
                this.oppisite = oppisite;
            }

            public int getAmount()
            {
                return amount;
            }

            public void setAmount(int amount)
            {
                this.amount = amount;
            }
        }
    }
}
