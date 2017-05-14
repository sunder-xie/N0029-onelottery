package com.peersafe.chainbet.model.LotteryJsonBean;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.model
 * @description: 规则列表或规则详情的的返回json解析父类
 * @date 22/11/16 AM10:32
 */
public class PrizeRuleRet
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
         * ruleID : 0b875dba-0f94-4e79-87ae-6ba7ef699722
         * percentage : 80
         * hide : false
         */

        private String ruleID;
        private int percentage;
        private boolean hide;

        public String getRuleID()
        {
            return ruleID;
        }

        public void setRuleID(String ruleID)
        {
            this.ruleID = ruleID;
        }

        public int getPercentage()
        {
            return percentage;
        }

        public void setPercentage(int percentage)
        {
            this.percentage = percentage;
        }

        public boolean isHide()
        {
            return hide;
        }

        public void setHide(boolean hide)
        {
            this.hide = hide;
        }
    }
}
