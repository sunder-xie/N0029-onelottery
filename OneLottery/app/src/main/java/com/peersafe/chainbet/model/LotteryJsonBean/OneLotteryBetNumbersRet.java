package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * @author moying
 * @Description 用户投注后得到的投注号列表
 * @date 2017/1/16 09:48
 */
public class OneLotteryBetNumbersRet
{
    /**
     * code : 0
     * message :
     * data : {"numbers":"10000006 10000008"}
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
         * numbers : 10000006 10000008
         */

        private String numbers;

        public String getNumbers()
        {
            return numbers;
        }

        public void setNumbers(String numbers)
        {
            this.numbers = numbers;
        }
    }
}
