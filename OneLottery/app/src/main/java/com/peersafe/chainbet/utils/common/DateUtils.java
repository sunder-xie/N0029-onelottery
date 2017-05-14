package com.peersafe.chainbet.utils.common;

import java.util.Calendar;
import java.util.Date;

public class DateUtils
{
    /**
     * 获取当前时间距1970.1.1的描述
     *
     * @return
     */
    public static Long getCurrentTimeSeconds()
    {
        long curTime = (new Date().getTime());
        return curTime / 1000;
    }

    /**
     * @param time
     * @return
     * @Description 将long类型的时间转换为Date类型
     * @author zhangyang
     */
    public static Date getDateFromLong(long time)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        return calendar.getTime();
    }
}