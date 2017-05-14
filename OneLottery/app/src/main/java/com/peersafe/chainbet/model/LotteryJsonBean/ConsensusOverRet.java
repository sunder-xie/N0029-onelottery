package com.peersafe.chainbet.model.LotteryJsonBean;

import com.peersafe.chainbet.utils.common.StringUtils;

/**
 * @author moying
 * @Description 活动的投注或可开奖的callback
 * @date 2017/2/17 14:19
 */
public class ConsensusOverRet
{


    /**
     * code : 0
     * message :
     * data : {"numbers":"10000001"}
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
         * numbers : 10000001
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

    public String[] getNumbers()
    {
        if (data != null && StringUtils.isEmpty(data.getNumbers()))
        {
            return data.getNumbers().split(" ");
        }
        return null;
    }

}
