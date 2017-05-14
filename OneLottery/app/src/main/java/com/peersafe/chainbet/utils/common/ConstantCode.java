
/**
 * Copyright (C) 2015 PeerSafe Technologies. All rights reserved.
 *
 * @Name: ConstantCode.java
 * @Package: com.peersafe.shadowtalk.utils.common
 * @Description: 程序中使用的常量集合
 * @author zhangyang
 * @date 2015年7月3日 下午2:38:39
 */

package com.peersafe.chainbet.utils.common;

import android.os.Environment;

/**
 * @author zhangyang
 * @Description
 * @date
 */

public interface ConstantCode
{
    //每一页展示多少条数据
    int PAGE_SIZE = 20;

    interface CommonConstant
    {
        String ONELOTTERY_DEFAULT_USERNAME = "WebAppAdmin";

        int ONELOTTERY_MONEY_MULTIPLE = 10000;    //数据库和传到底层的金额倍数，需要实际金额*10000

        String ONELOTTERY_DEFAULT_OFFICAL_NAME = "one_chain_admin";

        String ONELOTTERY_DEFAULT_OFFICAL_HASH = "4dccfe94aba5bc3c55a4c411b9a86c7f3e9450ad89542518756fc185";

        String USER_ID = "userId";

        String LOTTERYID = "LotteryId";

        String TYPE = "Type";

        String SIMPLE_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

        String SIMPLE_DATE_FORMAT1 = "yyyy/MM/dd";

        String SIMPLE_DATE_FORMAT2 = "yyyy/MM/dd HH:mm:ss SSS";

        String SIMPLE_DATE_FORMAT3 = "yyyy-MM-dd HH:mm";

        String BANNER_URL = "http://139.224.57.28:8004/getBanner";

        String BANNER_WEB = "http://139.224.57.28:8004/banner/";

        String BANK_CARD = "bankcard";

        String WITH_DRAW_RECORD = "with_draw_record";

        String EXTER_PATH = Environment.getExternalStorageDirectory() + "/" + "onelottery_sd";

        String INNER_PATH = "/sdcard/onelottery_sd";
    }

    interface OneLotteryState
    {
        //活动创建中
        int ONELOTTERY_STATE_CREATE_ING = -1;

        //活动未开始
        int ONELOTTERY_STATE_NOT_STARTED = 0;

        //活动进行中
        int ONELOTTERY_STATE_ON_GOING = 1;

        //可开奖
        int ONELOTTERY_STATE_CAN_REWARD = 2;

        //开奖中
        int ONELOTTERY_STATE_REWARD_ING = 3;

        //活动已经开奖
        int ONELOTTERY_STATE_REWARD_ALREADY = 4;

        //能退款
        int ONELOTTERY_STATE_CAN_REFUND = 5;

        //退款中
        int ONELOTTERY_STATE_REFUND_ING = 6;

        //已退款
        int ONELOTTERY_STATE_REFUND_ALREADY = 7;

        //活动失败
        int ONELOTTERY_STATE_FAIL = 8;
    }

    interface MessageType
    {
        int MESSAGE_TYPE_CREATE_FAIL = 1;

        int MESSAGE_TYPE_CREATE_SUCCESS = 2;

        int MESSAGE_TYPE_MODIFY_FAIL = 3;

        int MESSAGE_TYPE_MODIFY_SUCCESS = 4;

        int MESSAGE_TYPE_DELETE_FAIL = 5;

        int MESSAGE_TYPE_DELETE_SUCCESS = 6;

        int MESSAGE_TYPE_REFUND = 7;

        int MESSAGE_TYPE_PRIZE = 8;

        int MESSAGE_TYPE_PERCENTAGE = 9;

        int MESSAGE_TYPE_BET_SUCCESS = 10;

        int MESSAGE_TYPE_BET_FAIL = 11;

        int MESSAGE_TYPE_TRANSFOR_SUCCESS = 12;

        int MESSAGE_TYPE_TRANSFOR_FAIL = 13;

        int MESSAGE_TYPE_TRANSFOR_FROM_OTHER = 14;


        int MESSAGE_TYPE_WITH_DRAW_SEND_SUCCESS = 15;

        int MESSAGE_TYPE_WITH_DRAW_SEND_FAIL = 16;

        int MESSAGE_TYPE_WITH_DRAW_SUCCESS = 17;

        int MESSAGE_TYPE_WITH_DRAW_FAIL = 18;

        int MESSAGE_TYPE_WITH_DRAW_APPEAL_SEND_SUCCESS = 19;

        int MESSAGE_TYPE_WITH_DRAW_APPEAL_SEND_FAIL = 20;
    }

    interface TransactionType
    {
        int TRANSACTION_TYPE_TRANSFOR = 1;//转账给他人
        int TRANSACTION_TYPE_TRANSFOR_FROM_OTHER = 2;//他人给我转账
        int TRANSACTION_TYPE_TRANSFOR_FROM_ADMIN = 3;//充值（官方给我转账）
        int TRANSACTION_TYPE_CREATE_LOTTERY = 4;//创建活动扣款
        int TRANSACTION_TYPE_MODIFY_LOTTERY = 5;//修改活动扣款
        int TRANSACTION_TYPE_BET = 6;//投注
        int TRANSACTION_TYPE_REFUND = 7;//退款
        int TRANSACTION_TYPE_PRIZE = 8;//中奖
        int TRANSACTION_TYPE_PERCENTAGE = 9;//创建的活动分成
        int TRANSACTION_TYPE_WITHDRAW_CONFIRM = 10;//提现的用户已确认
        int TRANSACTION_TYPE_WITHDRAW_FAIL = 11;//提现失败
        int TRANSACTION_TYPE_WITHDRAW_AUTO_CONFIRM = 12;//提现的后台自动确认
        int TRANSACTION_TYPE_WITHDRAW_APPEAL_DONE = 13;//提现的申诉驳回

    }

    interface WithdrawType
    {
        int WITHDRAW_TYPE_APPLYING = 1;
        int WITHDRAW_TYPE_PAY = 2;
        int WITHDRAW_TYPE_CONFIRM =3;
        int WITHDRAW_TYPE_CANCEL = 4;
        int WITHDRAW_TYPE_FAIL = 5;
        int WITHDRAW_TYPE_APPEAL = 6;
    }

    interface  AppealType
    {
        int APPEAL_ACCEPT = 2;

        int APPEAL_REFUSE = 1;
    }
}
