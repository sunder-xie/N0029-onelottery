package com.peersafe.chainbet.model.LotteryJsonBean;

import java.util.List;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.model
 * @description:
 * @date 21/11/16 AM11:39
 */
public class ZXCoinDetailRet
{

    /**
     * Code : 0
     * CodeMessage : Success
     * Array : [{"TransactionID":"24e46390-7392-483c-86a0-654ce48b6214-1","Owner":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","Opposite":"7362932a505dd156b345b044705b854048aec6c5f7aaaac8704ad970","OppositeUserId":"admin","Account":10000000,"Balance":10000000,"Resvered":0,"Time":1480037188,"Remark":"transfer test","Type":0},{"TransactionID":"28036a7e-3dc1-477c-bff4-16493b1af6c4-1","Owner":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","Opposite":"7362932a505dd156b345b044705b854048aec6c5f7aaaac8704ad970","OppositeUserId":"admin","Account":10000000,"Balance":20000000,"Resvered":0,"Time":1480037504,"Remark":"transfer test","Type":0},{"TransactionID":"63170d85-a136-442d-8369-45d122d68b79-0","Owner":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","Opposite":"","OppositeUserId":"","Account":-10000,"Balance":19990000,"Resvered":0,"Time":1480038645,"Remark":"","Type":2},{"TransactionID":"ad011f91-5ca6-4503-ad8f-cce5d8762d06-0","Owner":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","Opposite":"","OppositeUserId":"","Account":-10000,"Balance":19980000,"Resvered":0,"Time":1480039025,"Remark":"","Type":2},{"TransactionID":"37997b01-9074-4b2d-97e8-f8eb8b2b0c9f-0","Owner":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","Opposite":"","OppositeUserId":"","Account":-10000,"Balance":19970000,"Resvered":0,"Time":1480040111,"Remark":"","Type":2},{"TransactionID":"d04905bf-36d3-4f88-b798-c1d2ffda914b-0","Owner":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","Opposite":"","OppositeUserId":"","Account":-10000,"Balance":19960000,"Resvered":0,"Time":1480041993,"Remark":"","Type":2},{"TransactionID":"459fdb53-d858-46a0-8038-c396d3d2a94c-5","Owner":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","Opposite":"","OppositeUserId":"","Account":-10000,"Balance":19950000,"Resvered":10000,"Time":1480042628,"Remark":"投注冻结","Type":1},{"TransactionID":"459fdb53-d858-46a0-8038-c396d3d2a94c-1","Owner":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","Opposite":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","OppositeUserId":"zhangyang","Account":10000,"Balance":19960000,"Resvered":0,"Time":1480042628,"Remark":"发起活动扣费","Type":6},{"TransactionID":"459fdb53-d858-46a0-8038-c396d3d2a94c-0","Owner":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","Opposite":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","OppositeUserId":"zhangyang","Account":-10000,"Balance":19950100,"Resvered":0,"Time":1480042628,"Remark":"发起活动扣费","Type":6},{"TransactionID":"40128641-8de0-4b7f-a16c-2746b93e31f0-1","Owner":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","Opposite":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","OppositeUserId":"zhangyang","Account":10000,"Balance":19950000,"Resvered":10000,"Time":1480042703,"Remark":"发起活动扣费","Type":6},{"TransactionID":"40128641-8de0-4b7f-a16c-2746b93e31f0-0","Owner":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","Opposite":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","OppositeUserId":"zhangyang","Account":-10000,"Balance":19940100,"Resvered":10000,"Time":1480042703,"Remark":"发起活动扣费","Type":6},{"TransactionID":"40128641-8de0-4b7f-a16c-2746b93e31f0-5","Owner":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","Opposite":"","OppositeUserId":"","Account":-10000,"Balance":19940000,"Resvered":20000,"Time":1480042703,"Remark":"投注冻结","Type":1},{"TransactionID":"c4056a3e-7086-4739-aee6-1a41305a5b7b-5","Owner":"0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13","Opposite":"","OppositeUserId":"","Account":-10000,"Balance":19930000,"Resvered":30000,"Time":1480042734,"Remark":"投注冻结","Type":1}]
     */

    private int Code;
    private String CodeMessage;
    private List<ArrayBean> Array;

    public int getCode()
    {
        return Code;
    }

    public void setCode(int Code)
    {
        this.Code = Code;
    }

    public String getCodeMessage()
    {
        return CodeMessage;
    }

    public void setCodeMessage(String CodeMessage)
    {
        this.CodeMessage = CodeMessage;
    }

    public List<ArrayBean> getArray()
    {
        return Array;
    }

    public void setArray(List<ArrayBean> Array)
    {
        this.Array = Array;
    }

    public static class ArrayBean
    {
        /**
         * TransactionID : 24e46390-7392-483c-86a0-654ce48b6214-1
         * Owner : 0445bf9d246ee156cd3a2f4a7a6dee2defac5e1c9311fb14fc114d13
         * Opposite : 7362932a505dd156b345b044705b854048aec6c5f7aaaac8704ad970
         * OppositeUserId : admin
         * Account : 10000000
         * Balance : 10000000
         * Resvered : 0
         * Time : 1480037188
         * Remark : transfer test
         * Type : 0
         */

        private String TransactionID;
        private String Owner;
        private String Opposite;
        private String OppositeUserId;
        private long Account;
        private long Balance;
        private long Resvered;
        private long Time;
        private String Remark;
        private int Type;

        public String getTransactionID()
        {
            return TransactionID;
        }

        public void setTransactionID(String TransactionID)
        {
            this.TransactionID = TransactionID;
        }

        public String getOwner()
        {
            return Owner;
        }

        public void setOwner(String Owner)
        {
            this.Owner = Owner;
        }

        public String getOpposite()
        {
            return Opposite;
        }

        public void setOpposite(String Opposite)
        {
            this.Opposite = Opposite;
        }

        public String getOppositeUserId()
        {
            return OppositeUserId;
        }

        public void setOppositeUserId(String OppositeUserId)
        {
            this.OppositeUserId = OppositeUserId;
        }

        public long getAccount()
        {
            return Account;
        }

        public void setAccount(long Account)
        {
            this.Account = Account;
        }

        public long getBalance()
        {
            return Balance;
        }

        public void setBalance(long Balance)
        {
            this.Balance = Balance;
        }

        public long getResvered()
        {
            return Resvered;
        }

        public void setResvered(long Resvered)
        {
            this.Resvered = Resvered;
        }

        public long getTime()
        {
            return Time;
        }

        public void setTime(long Time)
        {
            this.Time = Time;
        }

        public String getRemark()
        {
            return Remark;
        }

        public void setRemark(String Remark)
        {
            this.Remark = Remark;
        }

        public int getType()
        {
            return Type;
        }

        public void setType(int Type)
        {
            this.Type = Type;
        }
    }
}