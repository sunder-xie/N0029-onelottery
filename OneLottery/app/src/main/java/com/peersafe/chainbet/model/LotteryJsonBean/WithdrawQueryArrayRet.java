package com.peersafe.chainbet.model.LotteryJsonBean;

import java.util.List;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/4/12
 * DESCRIPTION :
 */

public class WithdrawQueryArrayRet
{

    /**
     * code : 0
     * message : Success
     * data : [{"TxId":"da0459c2-6c05-4619-9124-856f40139a70","State":1},
     * {"TxId":"429ad97b-0603-4151-83f3-6c81272f9b1f","State":1}]
     */

    private int code;
    private String message;
    private List<DataBean> data;

    public void setCode(int code)
    {
        this.code = code;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public void setData(List<DataBean> data)
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

    public List<DataBean> getData()
    {
        return data;
    }

    public static class DataBean
    {
        /**
         * TxId : da0459c2-6c05-4619-9124-856f40139a70
         * State : 1
         */

        private String TxId;
        private int State;

        public void setTxId(String TxId)
        {
            this.TxId = TxId;
        }

        public void setState(int State)
        {
            this.State = State;
        }

        public String getTxId()
        {
            return TxId;
        }

        public int getState()
        {
            return State;
        }
    }
}
