package com.peersafe.chainbet.model;

import com.peersafe.chainbet.ui.adapter.Entity;

import java.util.Date;

/**
 * Created by sunhaitao on 17/1/15.
 */

public class AttendBean extends Entity
{
    String attendName;
    int attendCount;
    Date attendTime;
    String lotteryId;
    String attendHash;
    String betNumbers;

    public String getAttendName()
    {
        return attendName;
    }

    public void setAttendName(String attendName)
    {
        this.attendName = attendName;
    }

    public int getAttendCount()
    {
        return attendCount;
    }

    public void setAttendCount(int attendCount)
    {
        this.attendCount = attendCount;
    }

    public Date getAttendTime()
    {
        return attendTime;
    }

    public void setAttendTime(Date attendTime)
    {
        this.attendTime = attendTime;
    }

    public String getLotteryId()
    {
        return lotteryId;
    }

    public void setLotteryId(String lotteryId)
    {
        this.lotteryId = lotteryId;
    }

    public String getAttendHash()
    {
        return attendHash;
    }

    public void setAttendHash(String attendHash)
    {
        this.attendHash = attendHash;
    }

    public String getBetNumbers()
    {
        return betNumbers;
    }

    public void setBetNumbers(String betNumbers)
    {
        this.betNumbers = betNumbers;
    }
}
