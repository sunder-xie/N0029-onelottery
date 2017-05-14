package com.peersafe.chainbet.ui.adapter;

import com.peersafe.chainbet.db.Friend;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.OneLotteryBet;
import com.peersafe.chainbet.model.AttendBean;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/1/13
 * DESCRIPTION :
 */

public class Entity
{
    OneLottery oneLottery;
    Friend friend;
    OneLotteryBet oneLotteryBet;
    AttendBean attendBean;

    public void setOneLottery(OneLottery oneLottery)
    {
        this.oneLottery = oneLottery;
    }

    public void setdFriend(Friend friend)
    {
        this.friend = friend;
    }

    public void setOneLotteryBet(OneLotteryBet oneLotteryBet)
    {
        this.oneLotteryBet = oneLotteryBet;
    }

    public void setAttendBean(AttendBean attendBean)
    {
        this.attendBean = attendBean;
    }

    public OneLottery getOneLottery()
    {
        return oneLottery;
    }

    public Friend getdFriend()
    {
        return friend;
    }

    public OneLotteryBet getOneLotteryBet()
    {
        return oneLotteryBet;
    }

    public AttendBean getAttendBean()
    {
        return attendBean;
    }
}
