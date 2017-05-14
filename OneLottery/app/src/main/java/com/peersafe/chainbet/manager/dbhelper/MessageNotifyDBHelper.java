package com.peersafe.chainbet.manager.dbhelper;

import android.os.Handler;
import android.os.Message;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.db.MessageNotify;
import com.peersafe.chainbet.db.MessageNotifyDao;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.log.OLLogger;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/1/12
 * DESCRIPTION :
 */

public class MessageNotifyDBHelper
{
    private static MessageNotifyDBHelper instance;

    public static synchronized MessageNotifyDBHelper getInstance()
    {
        if (instance == null)
        {
            instance = new MessageNotifyDBHelper();
        }
        return instance;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            OneLotteryManager.getInstance().SendEventBus(null, OLMessageModel
                    .STMSG_MODEL_ADVISE_MESSAGE);
        }
    };

    public void insertMessageNotify(MessageNotify messageNotify)
    {
        OneLotteryApplication.getDaoSession().insertOrReplace(messageNotify);
        handler.sendEmptyMessageDelayed(0,1000);
    }

    public void deleteMessageNotify(MessageNotify messageNotify)
    {
        OneLotteryApplication.getDaoSession().getMessageNotifyDao().delete(messageNotify);
    }

    public List<String> getUnreadMessages()
    {
        List<String> list = new ArrayList<>();
        String curUserId = OneLotteryApi.getCurUserId();
        if (StringUtils.isEmpty(curUserId))
        {
            return list;
        }

        QueryBuilder<MessageNotify> q = OneLotteryApplication.getDaoSession().getMessageNotifyDao
                ().queryBuilder();
        q.where(MessageNotifyDao.Properties.IsRead.eq(false), MessageNotifyDao.Properties.UserId
                .eq(curUserId));
        List<MessageNotify> messageNotifyList = q.list();
        if (messageNotifyList != null && !messageNotifyList.isEmpty())
        {
            for (MessageNotify msg : messageNotifyList)
            {
                list.add(msg.getHornContent() + OneLotteryApi.RES_SEP + msg.getType() +
                        OneLotteryApi.RES_SEP + msg.getLotteryId() + OneLotteryApi.RES_SEP + msg.getMsgId() +
                        OneLotteryApi.RES_SEP + (msg.getNewTxId() == null ? " " : msg.getNewTxId()));
            }
        }
        return list;
    }

    public List<MessageNotify> getAllMessages()
    {
        String curUserId = OneLotteryApi.getCurUserId();
        if (StringUtils.isEmpty(curUserId))
        {
            return new ArrayList<>();
        }

        QueryBuilder<MessageNotify> q = OneLotteryApplication.getDaoSession().getMessageNotifyDao
                ().queryBuilder();
        q.whereOr(MessageNotifyDao.Properties.UserId.eq(curUserId),
                MessageNotifyDao.Properties.Type.eq(ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_SEND_SUCCESS),
                MessageNotifyDao.Properties.Type.eq(ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_FAIL),
                MessageNotifyDao.Properties.Type.eq(ConstantCode.TransactionType.TRANSACTION_TYPE_WITHDRAW_APPEAL_DONE)
        );
        q.orderDesc(MessageNotifyDao.Properties.Time);
        return q.list();
    }

    public MessageNotify getMsgByMsgId(String msgId)
    {
        QueryBuilder queryBuilder = OneLotteryApplication.getDaoSession().getMessageNotifyDao().queryBuilder();
        queryBuilder.where(MessageNotifyDao.Properties.MsgId.eq(msgId));
        List<MessageNotify> list = queryBuilder.build().list();
        if(list != null && !list.isEmpty())
        {
            return list.get(0);
        }
        return null;
    }

    public MessageNotify getMsgByMsgId(String msgId, String lotterId, Integer type, String horn)
    {
        String curUserId = OneLotteryApi.getCurUserId();
        if (StringUtils.isEmpty(curUserId))
        {
            return null;
        }

        QueryBuilder<MessageNotify> q = OneLotteryApplication.getDaoSession().getMessageNotifyDao().queryBuilder();
        q.where(MessageNotifyDao.Properties.UserId.eq(curUserId), MessageNotifyDao.Properties.MsgId.eq(msgId), MessageNotifyDao
                .Properties.LotteryId.eq(lotterId), MessageNotifyDao.Properties.Type.eq(type), MessageNotifyDao.Properties.HornContent.eq
                (horn));
        q.limit(1);
        return q.build().unique();
    }
}
