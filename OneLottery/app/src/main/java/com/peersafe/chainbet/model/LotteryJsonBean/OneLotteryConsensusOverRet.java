package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/1/17
 * DESCRIPTION :
 */

public class OneLotteryConsensusOverRet
{
    /**
     * code : 0
     * message :
     * data : {"numbers":"10000005 10000009 10000001 10000004 10000000"}
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
         * numbers : 10000005 10000009 10000001 10000004 10000000
         */

        private String numbers;

        public void setNumbers(String numbers)
        {
            this.numbers = numbers;
        }

        public String getNumbers()
        {
            return numbers;
        }
    }
}
