package com.peersafe.chainbet.utils.common;


import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.PrizeRule;
import com.peersafe.chainbet.manager.dbhelper.PrizeRuleDBHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;

/**
 * Created by sunhaitao on 16/7/12.
 */
public class Defaultcontent
{
    public static String date_format = "yyyy/MM/dd HH:mm:ss";
    public static String separator = "&";
    public static String PEERSAFE = "peersafe";

    public static String getUrl(boolean isActivity, OneLottery lottery)
    {
        if (isActivity)
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat(date_format);
            String strartTime = dateFormat.format(lottery.getStartTime());
            String closeTime = dateFormat.format(lottery.getCloseTime());
            int pictureIndex = lottery.getPictureIndex();

            String ruleId = lottery.getRuleId();
            PrizeRule prizeRule = PrizeRuleDBHelper.getInstance().getPrizeRuleById(ruleId);
            Integer percentage = prizeRule.getPercentage();

            String description = lottery.getDescription();
            String decs = "";
            try
            {
                decs = URLEncoder.encode(description, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }

            String publishHash = lottery.getPublisherName();
            long betTotal = lottery.getMaxBetCount();

            String url = "http://oneadmin.peersafe.cn/share.html?isActivity=1" + separator + "startTime=" +
                    strartTime + separator
                    + "closeTime=" + closeTime + separator + "pictureIndex=" + pictureIndex +
                    separator + "percentage=" + percentage + separator + "description=" +
                    decs + separator + "publisherHash=" + publishHash + separator +
                    "total=" + betTotal + separator + "downLoadType=" + PEERSAFE;

            return url;
        }

        return "http://oneadmin.peersafe.cn/share.html?downLoadType=" + PEERSAFE;
    }
}
