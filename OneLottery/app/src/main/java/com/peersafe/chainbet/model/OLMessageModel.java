package com.peersafe.chainbet.model;


public class OLMessageModel
{
    public final static int STMSG_MODEL_TRANSFER_ACCOUNT_CALLBACK = 0;
    public final static int STMSG_MODEL_TRANSFER_ACCOUNT_NOTIFY = 1;
    public final static int STMSG_MODEL_PRIZE_RULE_ADD_CALLBACK = 2;
    public final static int STMSG_MODEL_PRIZE_RULE_ADD_NOTIFY = 3;
    public final static int STMSG_MODEL_PRIZE_RULE_MODIFY_CALLBACK = 4;
    public final static int STMSG_MODEL_PRIZE_RULE_MODIFY_NOTIFY = 5;
    public final static int STMSG_MODEL_PRIZE_RULE_DELETE_CALLBACK = 6;
    public final static int STMSG_MODEL_PRIZE_RULE_DELETE_NOTIFY = 7;
    public final static int STMSG_MODEL_ONE_LOTTERY_ADD_NOTIFY = 9;
    public final static int STMSG_MODEL_ONE_LOTTERY_MODIFY_NOTIFY = 11;
    public final static int STMSG_MODEL_ONE_LOTTERY_DELETE_NOTIFY = 13;
    public final static int STMSG_MODEL_ONE_LOTTERY_BET_CALLBACK = 14;
    public final static int STMSG_MODEL_ONE_LOTTERY_BET_NOTIFY = 15;
    public final static int STMSG_MODEL_ONE_LOTTERY_REFUND_CALLBACK = 16;
    public final static int STMSG_MODEL_ONE_LOTTERY_REFUND_NOTIFY = 17;
    public final static int STMSG_MODEL_OPEN_REWARD_CALLBACK = 18;
    public final static int STMSG_MODEL_ONE_LOTTERY_CAN_REFUND_NOTIFY = 19;
    public final static int STMSG_MODEL_ONE_LOTTERY_CONSENSUS_OVER_NOTIFY = 20;
    public final static int STMSG_MODEL_ONE_LOTTERY_BET_OVER_NOTIFY = 21;
    public static final int STMSG_MODEL_ONE_LOTTERY_START_NOTIFY = 23;
    public final static int STMSG_MODEL_ONE_LOTTERY_GET_LOTTERIES_OVER = 22;
    public static final int STMSG_MODEL_CLOSE_ADVISE = 24;
    public static final int STMSG_MODEL_ONE_LOTTERYS_START_OR_END_NOTIFY = 25;
    //明细
    public static final int STMSG_MODEL_TRANSFER_DETAIL = 26;

    public static final int STMSG_MODEL_REFRSH_LOTTERY_DETAIL = 27;
    public final static int STMSG_MODEL_OPEN_REWARD_NOTIFY = 28;

    //主调活动回调
    public final static int STMSG_MODEL_ONE_LOTTERY_DELETE_CALLBACK_SUCCESS = 101;
    public final static int STMSG_MODEL_ONE_LOTTERY_ADD_CALLBACK_SUCCESS = 103;
    public final static int STMSG_MODEL_ONE_LOTTERY_ADD_CALLBACK_FAIL = 104;
    public final static int STMSG_MODEL_ONE_LOTTERY_MODIFY_CALLBACK_SUCCESS = 105;

    public final static int STMSG_MODEL_ONE_LOTTERY_MODIFY_CALLBACK_FAIL = 106;
    public static final int STMSG_MODEL_REFRSH_SETTING_BALANCE =107;

    public final static int STMSG_MODEL_ONE_LOTTERY_DELETE_CALLBACK_FAIL = 102;
    //创建活动-选择图片标签
    public static final int STMSG_MODEL_CREATE_LOTTERY_SELECT_ICON = 201;
    public static final int STMSG_MODEL_CREATE_LOTTERY_FINISH = 202;

    public static final int STMSG_MODEL_ADVISE_MESSAGE = 203;
    //转账
    public static final int STMSG_MODEL_TRANSFER_FINISH = 301; // 转账时逛一逛

    public static final int STMSG_MODEL_TRANSFER_CALLBACK = 302; // 转账回调的结果
    // wifi备份恢复
    public static final int STMSG_MODEL_IMPORT_WALLET_OK = 401;

    // sd卡备份恢复
    public static final int STMSG_MODEL_IMPORT_SD_WALLENT_OK = 400;

    public static final int STMSG_MODEL_IMPORT_WALLET_ERR = 402;
    public static final int STMSG_MODEL_SETTING_CHANGE_ACCOUNT = 501;

    public static final int STMSG_MODEL_SETTING_DELETE_ACCOUNT = 502;
    public static final int STMSG_MODEL_REFRSH_SETTING_RIGHT_FRAGMENT = 503;
    public static final int STMSG_MODEL_NET_WORK_CONNECT = 601;
    public static final int STMSG_MODEL_NET_WORK_DISCONNECT = 602;
    public static final int STMSG_MODEL_REFRESH_FRIEND_FRAGMENT = 603;

    public static final int STMSG_MODEL_REFRESH_BET_LIST = 604;
    //提现通知
    public static final int STMSG_MODEL_WITH_DRAW_CALLBACK = 701;
    public static final int STMSG_MODEL_DISMISS_WITHDRAW_DIALOG = 702;
    public static final int STMSG_MODEL_REMIT_SUCCES_NOTIFY = 703;
    public static final int STMSG_MODEL_WITH_DRAW_FAIL_NOTIFY = 704;
    public static final int STMSG_MODEL_WITH_DRAW_APPEAL_CALLBACK = 705;
    public static final int STMSG_MODEL_APPEAL_DONE_NOTIFY = 706;
    public static final int STMSG_MODEL_WITH_DRAW_CONFIRM = 708;
    public static final int STMSG_MODEL_WITH_DRAW_CONFIRM_NOTIFY = 709;
    public static final int STMSG_MODEL_WITH_DRAW_NOTIFY = 710;


    //导出sd卡
    public static final int STMSG_MODEL_EXPORT_SD_FILE =801 ;
    public static final int STMSG_MODEL_IMPORT_SD_FILE = 802;

    private int eventType;
    private Object eventObject;
    private String evenId;

    public String getEventId()
    {
        return evenId;
    }

    public void setEvenId(String evenId)
    {
        this.evenId = evenId;
    }

    public int getEventType()
    {
        return eventType;
    }

    public void setEventType(int eventType)
    {
        this.eventType = eventType;
    }

    public Object getEventObject()
    {
        return eventObject;
    }

    public void setEventObject(Object eventObject)
    {
        this.eventObject = eventObject;
    }
}
