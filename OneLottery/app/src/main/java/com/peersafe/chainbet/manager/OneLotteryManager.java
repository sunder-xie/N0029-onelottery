/**
 * Copyright (C) PeerSafe 2015 Technologies. All rights reserved.
 *
 * @Name: ShadowTalkSdkMgr.java
 * @Package: com.peersafe.shadowtalk.manager
 * @Description: 此类用于初始化沙话sdk，并设置初始化参数和相应的监听器。
 * 全局仅有一个此类的实例存在，所以可以在任意地方通过getInstance()函数获取此全局实例
 * @author zhangyang
 * @date 2015年6月18日 上午9:48:10
 */

package com.peersafe.chainbet.manager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.PrizeRule;
import com.peersafe.chainbet.db.TransactionDetail;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.db.WithdrawRecord;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.logic.OneLotteryLogic;
import com.peersafe.chainbet.logic.PrizeRuleLogic;
import com.peersafe.chainbet.logic.UserLogic;
import com.peersafe.chainbet.logic.WithdrawLogic;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.manager.dbhelper.TransactionDetailDBHelper;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.CardAmountModel;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryRewardOverNotify;
import com.peersafe.chainbet.model.LotteryJsonBean.TxDetailBetRet;
import com.peersafe.chainbet.model.LotteryJsonBean.TxDetailLotteryAddRet;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.lottery.CreateLotteryActivity;
import com.peersafe.chainbet.ui.setting.withdraw.WithdrawActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.FileUtils;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.config.OLPreferenceUtil;
import com.peersafe.chainbet.utils.log.OLLogger;
import com.peersafe.chainbet.utils.netstate.NetChangeObserver;
import com.peersafe.chainbet.utils.netstate.NetworkStateReceiver;
import com.peersafe.chainbet.widget.InputPwdDialog;

import org.greenrobot.eventbus.EventBus;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import go.onechainmobile.OneChainCallBack;


/**
 * @author zhangyang
 * @Description
 * @date
 */
public class OneLotteryManager implements OneChainCallBack, NetChangeObserver
{
    private final String TAG = "OneLotteryManager";

    protected Context appContext = null;

    /**
     * init flag: test if the sdk has been inited before, we don't need to init
     * again
     */
    private boolean sdkInited = false;

    public static boolean isServiceConnect = false;

    /**
     * the global ShadowTalkManager instance
     */
    private static OneLotteryManager instance = null;

    //用于扫描未开始活动和进行中的活动的定时器，因为服务器不实时通知活动从未开始到进行中和从进行中
    //到结束的活动状态
    private Timer timer = new Timer();

    // 启动dameon的线程
    private OneLotteryDaemonThread mLotteryDaemonThread = new OneLotteryDaemonThread();

    private UserLogic userLogic = new UserLogic();

    private PrizeRuleLogic prizeRuleLogic = new PrizeRuleLogic();

    private OneLotteryLogic oneLotteryLogic = new OneLotteryLogic();

    /**
     * @return
     * @Description
     * @author zhangyang
     */
    public static OneLotteryManager getInstance()
    {
        if (null == instance)
        {
            instance = new OneLotteryManager();
        }
        return instance;
    }

    /**
     * @return boolean
     * @Description 初始化函数，返回true表示正确初始化，否则false，如果返回为false，
     * 请在后续的调用中不要调用任何沙话sdk的代码
     * @author zhangyang
     */
    public synchronized boolean init(Context context)
    {
        if (sdkInited)
        {
            return true;
        }

        appContext = context;

        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);
        OLLogger.d(TAG, "process app name : " + processAppName);

        // 如果app启用了远程的service，此application:onCreate会被调用2次
        // 为了防止SDK被初始化2次，加此判断会保证SDK被初始化1次
        // 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process
        // name就立即返回
        if (processAppName == null
                || !processAppName.equalsIgnoreCase(context.getPackageName()))
        {
            OLLogger.i(TAG, "enter the service process!");

            // 则此application::onCreate 是被service 调用的，直接返回
            return false;
        }

        sdkInited = true;

        NetworkStateReceiver.registerObserver(this);

        initOneLottery();

        timer.schedule(new LotteryStatusTimerTask(), 5000, 5000);

        return true;
    }

    public Context getAppContext()
    {
        return appContext;
    }

    /**
     * @param pID
     * @return String
     * @Description check the application process name if process name is not
     * qualified, then we think it is a service process and we will
     * not init SDK
     * @author zhangyang
     */
    private String getAppName(int pID)
    {
        String processName = null;
        ActivityManager am = (ActivityManager) appContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> l = am.getRunningAppProcesses();
        Iterator<RunningAppProcessInfo> i = l.iterator();
        while (i.hasNext())
        {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo)
                    (i.next());
            try
            {
                if (info.pid == pID)
                {
                    processName = info.processName;
                    return processName;
                }
            }
            catch (Exception e)
            {
                OLLogger.d("Process", "Error>> :" + e.toString());
            }
        }
        return processName;
    }

    /**
     * 初始化一元夺宝底层：1、将asset下面的文件夹拷贝到一元夺宝底层后续需要保存数据的目录下 2、调用一元夺宝初始化
     * 3、启动一个线程，调用一元夺宝的daemon接口，用于底层接收异步通知
     */
    private void initOneLottery()
    {
        try
        {
            if (!OLPreferenceUtil.getInstance(appContext).getHashInitOneLottery())
            {
                InputStream inStream = appContext.getAssets().open("onelottery.zip");
                String lotteryToStorePath = OneLotteryApplication.getAppContext().getFilesDir()
                        .getAbsolutePath();
                FileUtils.unZipFolder(inStream, lotteryToStorePath);

                OLPreferenceUtil.getInstance(appContext).setHasInitOneLottery(true);
            }

            if (OneLotteryApi.SUCCESS != OneLotteryApi.init())
            {
                OLLogger.e(TAG, "initOneLottery failed, OneLotteryApi.init() call failed!");
                return;
            }

            mLotteryDaemonThread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public class OneLotteryDaemonThread extends Thread
    {
        @Override
        public void run()
        {
            super.run();

            while (true)
            {
                long ret = OneLotteryApi.daemon();
                OLLogger.w(TAG, "OneLotteryApi.daemon() return:" + ret);

                if(ret == -2)
                {
                    isServiceConnect = false;
                }

                try
                {
                    sleep(2000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 活动状态检测的定时器
     */
    class LotteryStatusTimerTask extends TimerTask
    {
        @Override
        public void run()
        {
            List<OneLottery> lotteryListNotStart = OneLotteryDBHelper.getInstance()
                    .getNotStartedAndOnGoingLottery();
            if (null != lotteryListNotStart && lotteryListNotStart.size() > 0)
            {
                boolean isShoudUpdateLotteryList = false;

                for (int i = 0; i < lotteryListNotStart.size(); i++)
                {
                    boolean isShouldUpdateState = false;

                    OneLottery lottery = lotteryListNotStart.get(i);
                    if (lottery.getStartTime().getTime() <= System.currentTimeMillis()
                            && lottery.getState() == ConstantCode.OneLotteryState
                            .ONELOTTERY_STATE_NOT_STARTED)
                    {
                        OLLogger.d(TAG, "LotteryStatusTimerTask,lottery not started to start");
                        isShouldUpdateState = true;
                        isShoudUpdateLotteryList = true;
                        lottery.setState(ConstantCode.OneLotteryState.ONELOTTERY_STATE_ON_GOING);
                    } else if (lottery.getCloseTime().getTime() <= System.currentTimeMillis()
                            && lottery.getState() == ConstantCode.OneLotteryState
                            .ONELOTTERY_STATE_ON_GOING)
                    {
                        OLLogger.d(TAG, "LotteryStatusTimerTask,lottery started to refund");
                        isShouldUpdateState = true;
                        isShoudUpdateLotteryList = true;
                        lottery.setState(ConstantCode.OneLotteryState.ONELOTTERY_STATE_CAN_REFUND);
                    }

                    if (isShouldUpdateState)
                    {
                        OneLotteryDBHelper.getInstance().insertOneLottery(lottery);
                    }
                }

                if (isShoudUpdateLotteryList)
                {
                    SendEventBus(null, OLMessageModel.STMSG_MODEL_ONE_LOTTERYS_START_OR_END_NOTIFY);
                }
            }
        }
    }

    public boolean getPrizeRules()
    {
        return prizeRuleLogic.getRules();
    }

    public UserInfo getUserBalance()
    {
        UserInfo userInfo = userLogic.getUserBalance();
        if (userInfo != null)
        {
            SendEventBus(null, OLMessageModel.STMSG_MODEL_REFRSH_SETTING_BALANCE);
        }
        return userInfo;
    }

    public boolean getWithdrawList()
    {
        return WithdrawLogic.queryWithdrawList();
    }

    public boolean getLotteries(boolean isHistory, boolean needNotify)
    {
        boolean result = oneLotteryLogic.queryLotteryList(isHistory);
        if (needNotify)
        {
            SendEventBus(result, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_GET_LOTTERIES_OVER);
        }
        return result;
    }

    public boolean getLotteryDetail(String lotteryID)
    {
        return oneLotteryLogic.queryLotteryDetail(lotteryID, false, null) != null;
    }

    public boolean getLotteryHistoryDetail(String lotteryID)
    {
        return oneLotteryLogic.queryLotteryDetail(lotteryID, true, null) != null;
    }

    public void deleteLocalOneLottery(OneLottery ol)
    {
        oneLotteryLogic.deleteOneLotteryInDB(ol);
    }

    public void openReward(String lotteryID)
    {
        if (StringUtils.isEmpty(lotteryID))
        {
            return;
        }
        String txnID = OneLotteryApi.oneLotteryOpenReward(lotteryID);
        if (!StringUtils.isEmpty(txnID))
        {
            OneLottery ol = OneLotteryDBHelper.getInstance().getLotteryByLotterId(lotteryID);
            ol.setState(ConstantCode.OneLotteryState.ONELOTTERY_STATE_REWARD_ING);
//            ol.setNewTxId(txnID);// 注意：只有修改活动时才会更新这个newTxId
            OneLotteryDBHelper.getInstance().insertOneLottery(ol);

            // TODO 有时候收不到callback。。。
            SendEventBus(ol, OLMessageModel.STMSG_MODEL_OPEN_REWARD_CALLBACK);
        }
    }

    public void SendEventBus(Object obj, int type)
    {
        OLMessageModel event = new OLMessageModel();
        event.setEventType(type);
        event.setEventObject(obj);
        EventBus.getDefault().post(event);
    }

    public void SendEventBus(Object obj, int type, String eventId)
    {
        OLMessageModel event = new OLMessageModel();
        event.setEventType(type);
        event.setEventObject(obj);
        event.setEvenId(eventId);
        EventBus.getDefault().post(event);
    }

    // 查询该用户当前lastGetHeight以前的交易数据，并入交易表和消息表
    public ArrayList<TransactionDetail> getHistoryTransacetions(int REQUEST_COUNT)
    {
        return oneLotteryLogic.getHistoryTransacetions(REQUEST_COUNT);
    }

//    @Override
//    public void oneChainConsensusOverNotify(final String s, final String s1)
//    {
//        //可以开奖
//        // retMsg:{"code":0,"message":"","data":{"numbers":"10000001 10000000 10000003 10000002
//        // 10000004"}} txId:e3d0379d-bee7-4b94-9e4e-ef6057ecec5f
//        OLLogger.d(TAG, "OneChainConsensusOverNotify, retMsg:" + s + " txId:" + s1);
//        oneLotteryLogic.onLotteryConsensusOver(s, s1);
//    }

    @Override
    public void oneChainDeaemonShutDownNotify()
    {
        OLLogger.d(TAG, "oneChainDeaemonShutDownNotify, daemon is disConnect");
        SendEventBus(null, OLMessageModel.STMSG_MODEL_NET_WORK_DISCONNECT);
    }

    @Override
    public void oneChainDeaemonStartNotify(long l)
    {
        OLLogger.d(TAG, "oneChainDeaemonStartNotify, daemon is connect ? " + l);
        if (l == OneLotteryApi.SUCCESS)
        {
            isServiceConnect = true;
        }
    }

    @Override
    public void oneChainOneLotteryAddCallback(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainOneLotteryAddCallback, retMsg:" + s + " txId:" + s1);
        OneLottery oneLottery = oneLotteryLogic.onLotteryAdd(s, s1);
        OLLogger.d(TAG, "oneChainOneLotteryAddCallback,  :" + oneLottery);

        if (oneLottery != null)
        {
            UserInfo me = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
            TxDetailLotteryAddRet txDetailLotteryAddRet = new TxDetailLotteryAddRet();
            txDetailLotteryAddRet.setPublisherName(oneLottery.getPublisherName());
            txDetailLotteryAddRet.setName(oneLottery.getLotteryName());
            txDetailLotteryAddRet.setCreateTime(oneLottery.getCreateTime().getTime());

            oneLotteryLogic.setAddLotteryTransferDetail(s1, txDetailLotteryAddRet, me);

            if (!isCreateLotteryActivityDialogShowing())
            {
                setLotteryMsgWhenDialogHide(ConstantCode.MessageType.MESSAGE_TYPE_CREATE_SUCCESS,
                        s1);
//
//                oneLotteryLogic.
//                        setMessageNotify(oneLottery, s1, getString(R.string
// .message_create_success), oneLottery.getLotteryName()
//                                        + getString(R.string.message_create_lottery_success),
//                                ConstantCode.MessageType.MESSAGE_TYPE_CREATE_SUCCESS);
            }

            SendEventBus(s1, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_ADD_CALLBACK_SUCCESS);
        } else
        {

            if (!isCreateLotteryActivityDialogShowing())
            {
                setLotteryMsgWhenDialogHide(ConstantCode.MessageType.MESSAGE_TYPE_CREATE_FAIL, s1);

//                oneLotteryLogic.
//                        setMessageNotify(CreateLotteryActivity.lottery, s1, getString(R.string
// .message_create_fail),
//                                CreateLotteryActivity.lottery.getLotteryName() + getString(R
// .string.message_create_lottery_fail),
//                                ConstantCode.MessageType.MESSAGE_TYPE_CREATE_FAIL);
            }
            SendEventBus(s1, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_ADD_CALLBACK_FAIL);
        }

        OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).setAddLotteryName("");
    }

    private boolean isCreateLotteryActivityDialogShowing()
    {
//                CommonUtils.isExsitMianActivity(CreateLotteryActivity.class)&&
        boolean exist = CreateLotteryActivity.instance != null
                && CreateLotteryActivity.instance.getWaitingDialog() != null
                && CreateLotteryActivity.instance.getWaitingDialog().isShowing();
        OLLogger.i(TAG, "CreateLotteryActivity waitdialog showing? " + exist);
        return exist;
    }

    @Override
    public void oneChainOneLotteryAddNotify(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainOneLotteryAddNotify, retMsg:" + s + " txId:" + s1);
        OneLottery lottery = oneLotteryLogic.onLotteryAdd(s, s1);
        OLLogger.d(TAG, "OneChainOneLotteryAddNotify,  " + lottery);
        if (lottery != null)
        {
            SendEventBus(lottery, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_ADD_NOTIFY);
        }
    }

    @Override
    public void oneChainOneLotteryBetCallback(final String s, final String s1)
    {
        OLLogger.d(TAG, "OneChainOneLotteryBetCallback, retMsg:" + s + " txId:" + s1);
        // retMsg:{"code":0,"message":"","data":{"numbers":"10000014 10000015"}}
        // txId:3d274fa4-dafa-4402-88a5-5d73488eb230
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                oneLotteryLogic.onLotteryBetCallOrNotify(s, s1, true);
            }
        }).start();
    }

    @Override
    public void oneChainOneLotteryBetNotify(final String s, final String s1)
    {
        OLLogger.d(TAG, "OneChainOneLotteryBetNotify, retMsg:" + s + " txId:" + s1);
        // retMsg:{"code":0,"message":"","data":{"numbers":"10000001 10000009"}}
        // txId:f348eec4-5f8f-4b98-a29b-9cfc5f8cc3a0
        oneLotteryLogic.onLotteryBetCallOrNotify(s, s1, false);
    }

    @Override
    public void oneChainOneLotteryDeleteCallback(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainOneLotteryDeleteCallback, retMsg:" + s + " txId:" + s1);
        OneLottery oneLottery = OneLotteryApi.oneLotteryDeleteJson(s, s1);
        OLLogger.d(TAG, "oneChainOneLotteryDeleteCallback,  onlo:" + oneLottery);

        if (oneLottery != null)
        {

            if (!isCreateLotteryActivityDialogShowing())
            {
                setLotteryMsgWhenDialogHide(ConstantCode.MessageType.MESSAGE_TYPE_DELETE_SUCCESS,
                        s1);
            }

            SendEventBus(s1, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_DELETE_CALLBACK_SUCCESS);
        } else
        {

            if (!isCreateLotteryActivityDialogShowing())
            {
                setLotteryMsgWhenDialogHide(ConstantCode.MessageType.MESSAGE_TYPE_DELETE_FAIL, s1);
            }

            SendEventBus(s1, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_DELETE_CALLBACK_FAIL);
        }
    }

    @Override
    public void oneChainOneLotteryDeleteNotify(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainOneLotteryDeleteNotify, retMsg:" + s + " txId:" + s1);
        OneLottery lottery = oneLotteryLogic.onLotteryDelete(s, s1);
        if (lottery != null)
        {
            OLLogger.d(TAG, "OneChainOneLotteryDeleteNotify,  " + lottery.getLotteryId() + ", " +
                    lottery.getLotteryName());
            SendEventBus(lottery, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_DELETE_NOTIFY);
        }
    }

    @Override
    public void oneChainOneLotteryModifyCallback(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainOneLotteryModifyCallback, retMsg:" + s + " txId:" + s1);
        OneLottery oneLottery = OneLotteryApi.oneLotteryModify(s, s1);
        OLLogger.d(TAG, "oneChainOneLotteryModifyCallback,  result:" + oneLottery);
        if (oneLottery != null)
        {
//            if (!isCreateLotteryActivityDialogShowing())
//            {
//                oneLotteryLogic.
//                        setMessageNotify(oneLottery, s1, getString(R.string
//            .message_modify_success), oneLottery.getLotteryName()
//                                        + getString(R.string.message_modify_lottery_success),
//                                ConstantCode.MessageType.);
//            }
            if (!isCreateLotteryActivityDialogShowing())
            {
                setLotteryMsgWhenDialogHide(ConstantCode.MessageType.MESSAGE_TYPE_MODIFY_SUCCESS,
                        s1);
            }

            SendEventBus(s1, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_MODIFY_CALLBACK_SUCCESS);

            UserInfo me = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
            // 通知明细界面实时刷新
            TransactionDetail td = new TransactionDetail();
            td.setTxId(s1);
            td.setRemark(oneLottery.getLotteryName());
            td.setMyId(me != null ? me.getUserId() : "");
            td.setMyHash(me != null ? me.getWalletAddr() : "");
            td.setAmount((long) ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE / 100);
            td.setTime(new Date());
            td.setType(ConstantCode.TransactionType.TRANSACTION_TYPE_MODIFY_LOTTERY);
            oneLotteryLogic.checkTranTxId(s1, td);
            TransactionDetailDBHelper.getInstance().insertTransactionDetail(td);
        } else
        {

            if (!isCreateLotteryActivityDialogShowing())
            {
                setLotteryMsgWhenDialogHide(ConstantCode.MessageType.MESSAGE_TYPE_MODIFY_FAIL, s1);
            }

            SendEventBus(s1, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_MODIFY_CALLBACK_FAIL);
        }
    }

    // 活动创建修改，删除失败情况下，如果已经点击完先逛逛其它了，消息表入库
    public void setLotteryMsgWhenDialogHide(int messageType, String s1)
    {
        String title = "";
        String content = "";

        switch (messageType)
        {
            case ConstantCode.MessageType.MESSAGE_TYPE_CREATE_SUCCESS:
                title = getString(R.string.message_create_success);
                content = getString(R.string.message_create_lottery_success);
                break;
            case ConstantCode.MessageType.MESSAGE_TYPE_CREATE_FAIL:
                title = getString(R.string.message_create_fail);
                content = getString(R.string.message_create_lottery_fail);
                break;
            case ConstantCode.MessageType.MESSAGE_TYPE_MODIFY_SUCCESS:
                title = getString(R.string.message_modify_success);
                content = getString(R.string.message_modify_lottery_success);
                break;
            case ConstantCode.MessageType.MESSAGE_TYPE_MODIFY_FAIL:
                title = getString(R.string.message_modify_fail);
                content = getString(R.string.message_modify_lottery_fail);
                break;
            case ConstantCode.MessageType.MESSAGE_TYPE_DELETE_SUCCESS:
                title = getString(R.string.message_delete_success);
                content = getString(R.string.message_delete_lottery_success);
                break;
            case ConstantCode.MessageType.MESSAGE_TYPE_DELETE_FAIL:
                title = getString(R.string.message_delete_fail);
                content = getString(R.string.message_delete_lottery_fail);
                break;
        }

        if (InputPwdDialog.mCurOperObject instanceof OneLottery && InputPwdDialog.mCurOperObject != null)
        {
            OneLottery ol = (OneLottery) InputPwdDialog.mCurOperObject;
            oneLotteryLogic.setMessageNotify(ol, s1, title, StringUtils.refactorLotteryName(ol.getLotteryName()) + content, messageType);
        } else
        {
            String modLID = OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).getModLotteryId();
            if (!StringUtils.isEmpty(modLID))
            {
                OneLottery ol = OneLotteryDBHelper.getInstance().getLotteryByLotterId(modLID);
                if (ol != null)
                {
                    oneLotteryLogic.setMessageNotify(ol, s1, title, StringUtils.refactorLotteryName(ol.getLotteryName() + content), messageType);
                }
            } else
            {
                String name = OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).getAddLotteryName();
                if (!StringUtils.isEmpty(name))
                {
                    oneLotteryLogic.setMessageNotify(null, s1, title, StringUtils.refactorLotteryName(name + content), messageType);
                }
            }
        }
    }

    public void setBetMsgWhenDialogHide(boolean isSuccess, OneLottery oneLottery, String txID)
    {
        TxDetailBetRet ret = new TxDetailBetRet();
        ret.setCreateTime(new Date().getTime());
        oneLotteryLogic.setBetMessage(isSuccess, ret, oneLottery, txID);
    }

    @Override
    public void oneChainOneLotteryModifyNotify(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainOneLotteryModifyNotify, retMsg:" + s + " txId:" + s1);
        OneLottery lottery = oneLotteryLogic.onLotteryModify(s, s1);
        OLLogger.d(TAG, "OneChainOneLotteryModifyNotify,  " + lottery);
        if (lottery != null)
        {
            SendEventBus(lottery, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_MODIFY_NOTIFY);
        }
    }

    @Override
    public void oneChainOneLotteryOpenRewardCallback(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainOneLotteryOpenRewardCallback, retMsg:" + s + " txId:" + s1);
        // OneChainOneLotteryOpenRewardCallback, retMsg:{"code":1,"message":"Lottery
        // 419a7f88-ebb4-45de-bf39-c64ac84cd75cconsensus has not been over.","data":null}
        // txId:2d7d5c9d-3bf4-44ea-b9f3-35444ef75a14
        // 点击开奖的话，如果成功就不用特殊处理，但是如果失败的话，需要将失败的活动改回待开奖状态。

//        OneChainOneLotteryOpenRewardCallback, retMsg:{"code":0,"message":"","data":{"txnID":"43445002-ca90-4071-a909-a26df9242770",
//            "attendee":"97d44b330f6f7b5d2be1d14efbe030aa19acdfe1f6c81f2a15735026","attendeeName":"sjnc","numbers":"10000005",
//            "amount":70000,"CreateTime":1489474098575,"lotteryID":"f78d4a9a-d597-4234-9452-119b096f5000"}}
//        txId:4c654b5e-aaa8-4316-ac8e-3acd4157f221

        OneLotteryRewardOverNotify rewardOverNotify = oneLotteryLogic.onOpenReward(s, s1);
        if (rewardOverNotify != null && rewardOverNotify.getData() != null)
        {
            if (rewardOverNotify.getCode() == OneLotteryApi.SUCCESS && !StringUtils.isEmpty(rewardOverNotify.getData().getLotteryID()))
            {
                oneLotteryLogic.queryLotteryDetail(rewardOverNotify.getData().getLotteryID(), false, null);
            }
            SendEventBus(s, OLMessageModel.STMSG_MODEL_OPEN_REWARD_CALLBACK);
        } else if (rewardOverNotify != null && rewardOverNotify.getCode() != 0
                && !StringUtils.isEmpty(rewardOverNotify.getMessage()))
        {
            if (rewardOverNotify.getMessage().startsWith("Lottery ") && rewardOverNotify
                    .getMessage().length() >= 40)
            {
                String lotteryID = rewardOverNotify.getMessage().substring(8, 44);
                OneLottery ol = OneLotteryDBHelper.getInstance().getLotteryByLotterId(lotteryID);
                if (null != ol)
                {
                    ol.setState(ConstantCode.OneLotteryState.ONELOTTERY_STATE_CAN_REWARD);
                    OneLotteryDBHelper.getInstance().insertOneLottery(ol);
                    SendEventBus(s, OLMessageModel.STMSG_MODEL_OPEN_REWARD_CALLBACK);
                }
            }
        }
    }

    @Override
    public void oneChainOneLotteryOpenRewardNotify(String s, String s1)
    {
        OLLogger.d(TAG, "oneChainOneLotteryOpenRewardNotify, retMsg:" + s + " txId:" + s1);
//        oneChainOneLotteryOpenRewardNotify, retMsg:{"code":0,"message":"","data":{"txnID":"adf0838e-370a-450a-aafa-df44cd3faf2a",
//            "attendee":"45cf3707e42d72b32f0721ae8c6ff727b13d36673b3bbf0edde9493b","attendeeName":"hshxhdb","numbers":"10000000",
//            "amount":10000,"CreateTime":1489475083729,"lotteryID":"e8176c92-29dc-4bd7-af3f-3c250ccebe2a"}}
//        txId:4dfd6dc1-9ce2-446e-9b82-d7bed8651d7e
        OneLotteryRewardOverNotify rewardOverNotify = oneLotteryLogic.onOpenReward(s, s1);
        if (rewardOverNotify != null && rewardOverNotify.getCode() == OneLotteryApi.SUCCESS && rewardOverNotify.getData() != null)
        {
            SendEventBus(rewardOverNotify.getData().getLotteryID(), OLMessageModel.STMSG_MODEL_OPEN_REWARD_NOTIFY);
        }
    }

    @Override
    public void oneChainOneLotteryRefundCallback(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainOneLotteryRefundCallback, retMsg:" + s + " txId:" + s1);
        SendEventBus(s, OLMessageModel.STMSG_MODEL_ONE_LOTTERY_REFUND_CALLBACK);
    }

    @Override
    public void oneChainOneLotteryRefundNotify(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainOneLotteryRefundNotify, retMsg:" + s + " txId:" + s1);
        // 收到退款通知，处理退款
        OneLottery lottery = oneLotteryLogic.onLotteryRefund(s, s1);
        if (lottery != null)
        {
            SendEventBus(lottery.getLotteryId(), OLMessageModel.STMSG_MODEL_ONE_LOTTERY_REFUND_NOTIFY);
        }
    }

    @Override
    public void oneChainPrizeRuleAddCallback(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainPrizeRuleAddCallback, retMsg:" + s + " txId:" + s1);
        SendEventBus(s, OLMessageModel.STMSG_MODEL_PRIZE_RULE_ADD_CALLBACK);
    }

    @Override
    public void oneChainPrizeRuleAddNotify(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainPrizeRuleAddNotify, retMsg:" + s + " txId:" + s1);
        PrizeRule rule = prizeRuleLogic.onRuleChange(s, s1);
        if (rule != null)
        {
            SendEventBus(rule, OLMessageModel.STMSG_MODEL_PRIZE_RULE_ADD_NOTIFY);
        }
    }

    @Override
    public void oneChainPrizeRuleDeleteCallback(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainPrizeRuleDeleteCallback, retMsg:" + s + " txId:" + s1);
        SendEventBus(s, OLMessageModel.STMSG_MODEL_PRIZE_RULE_DELETE_CALLBACK);
    }

    @Override
    public void oneChainPrizeRuleDeleteNotify(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainPrizeRuleDeleteNotify, retMsg:" + s + " txId:" + s1);
        PrizeRule delRule = prizeRuleLogic.onRuleDelete(s, s1);
        if (delRule != null)
        {
            SendEventBus(delRule, OLMessageModel.STMSG_MODEL_PRIZE_RULE_DELETE_NOTIFY);
        }
    }

    @Override
    public void oneChainPrizeRuleModifyCallback(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainPrizeRuleModifyCallback, retMsg:" + s + " txId:" + s1);
        SendEventBus(s, OLMessageModel.STMSG_MODEL_PRIZE_RULE_MODIFY_CALLBACK);
    }

    @Override
    public void oneChainPrizeRuleModifyNotify(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainPrizeRuleModifyNotify, retMsg:" + s + " txId:" + s1);
        PrizeRule rule = prizeRuleLogic.onRuleChange(s, s1);
        if (rule != null)
        {
            SendEventBus(rule, OLMessageModel.STMSG_MODEL_PRIZE_RULE_MODIFY_NOTIFY);
        }
    }

    @Override
    public void oneChainTransferCallback(String s, String s1)
    {
        OLLogger.d(TAG, "OneChainTransferCallback, retMsg:" + s + " txId:" + s1);
        /***
         * retMsg:{"code":0,"message":"Success",
         * "data":{"owner":"b18d4105513a24856965e2de28a736ebc2a0d9ce703961928081fed3",
         * "ownUserId":"cmcc02",
         * "oppisite":"aa5556a323b6b4e54ba2da3d516c280c1272043f09ab078450c8917e",
         * "oppisiteUserId":"xm02",
         * "amount":10000,"fee":100}} txId:c2166411-f34a-4428-ad1e-3159beb3ddb5
         */
        // 通知转账界面的等待窗口更新状态
        // 需要处理转账失败的情况
        oneLotteryLogic.onTransfer(s, s1, true);
//        SendEventBus(s, OLMessageModel.STMSG_MODEL_TRANSFER_ACCOUNT_CALLBACK);
    }

    @Override
    public void oneChainTransferNotify(String s, String s1)
    {
        /**
         *
         retMsg:{"code":0,"message":"Success",
         "data":{"owner":"a94afe373b55f0c430f19a376fdd3a9063ee3e50b2a87992733a28cd",
         "ownUserId":"xm8","oppisite":"768c78dad5e701cb54234153caaea2a573623e909638f05674c015ba",
         "oppisiteUserId":"797979tcl",
         "amount":88000,"fee":100}} txId:b5f57dc9-3cb7-46aa-9365-5c4adfa98fe1
         */
        OLLogger.d(TAG, "OneChainTransferNotify, retMsg:" + s + " txId:" + s1);
        UserInfo info = userLogic.getUserBalance();
        if (info != null)
        {
            SendEventBus(info, OLMessageModel.STMSG_MODEL_TRANSFER_ACCOUNT_NOTIFY);
        }

        oneLotteryLogic.onTransfer(s, s1, false);
    }

    @Override
    public void oneChainZxCoinWithdraw(String s, String s1)
    {
        OLLogger.i(TAG,"oneChainZxCoinWithdraw " + "retMsg: " + s + "txId: " + s1);
        boolean result = WithdrawLogic.oneChainWithdraw(s, s1);
        if(result)
        {
            WithdrawRecord record = WithdrawLogic.queryWithdraw(s1);
            if(record != null)
            {
                getUserBalance();
            }

            SendEventBus(record,OLMessageModel.STMSG_MODEL_WITH_DRAW_CALLBACK);

            //设置喇叭消息
            if(!isWithdrawActivityDialogShowing())
            {
                if (InputPwdDialog.mCurOperObject instanceof CardAmountModel && InputPwdDialog.mCurOperObject != null)
                {
                    CardAmountModel model = (CardAmountModel) InputPwdDialog.mCurOperObject;
                    WithdrawLogic.setMessageNotify(model.getAmount(),s1, ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_SEND_SUCCESS,"");
                }
            }
        }
        else
        {
            SendEventBus(null,OLMessageModel.STMSG_MODEL_WITH_DRAW_CALLBACK);
            if(!isWithdrawActivityDialogShowing())
            {
                if (InputPwdDialog.mCurOperObject instanceof CardAmountModel && InputPwdDialog.mCurOperObject != null)
                {
                    CardAmountModel model = (CardAmountModel) InputPwdDialog.mCurOperObject;
                    WithdrawLogic.setMessageNotify(model.getAmount(), UUID.randomUUID().toString(), ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_SEND_FAIL,"");
                }
            }
        }
    }

    @Override
    public void oneChainZxCoinWithdrawAppeal(String s, String s1)
    {
        OLLogger.i(TAG,"oneChainZxCoinWithdrawAppeal " + "retMsg: " + s + "txId: " + s1);
//        {"code":0,"message":"Success","data":"5033d2e5-47c6-4840-bcf5-f0a9589b7866"}txId: 8a21708b-84f7-42c7-abe7-82abe5252fca
        WithdrawRecord record = WithdrawLogic.oneChainWithdrawAppeal(s);
        if(record != null)
        {
            getUserBalance();

            SendEventBus(record,OLMessageModel.STMSG_MODEL_WITH_DRAW_APPEAL_CALLBACK);
        }
        else
        {
            SendEventBus(null,OLMessageModel.STMSG_MODEL_WITH_DRAW_APPEAL_CALLBACK);
        }
    }

    @Override
    public void oneChainZxCoinWithdrawConfirm(String s, String s1)
    {
        OLLogger.i(TAG,"oneChainZxCoinWithdrawConfirm " + "retMsg: " + s + "txId: " + s1);

        WithdrawRecord record = WithdrawLogic.oneChainWithdrawConfrim(s);
        if(record != null)
        {
            insertWithDrawTran(record, ConstantCode.TransactionType.TRANSACTION_TYPE_WITHDRAW_CONFIRM);

            getUserBalance();

            SendEventBus(record,OLMessageModel.STMSG_MODEL_WITH_DRAW_CONFIRM);
        }
        else
        {
            SendEventBus(null,OLMessageModel.STMSG_MODEL_WITH_DRAW_CONFIRM);
        }
    }

    @Override
    public void oneChainZxCoinWithdrawFailNotify(String s, String s1)
    {
        OLLogger.i(TAG,"oneChainZxCoinWithdrawFailNotify " + "retMsg: " + s + "txId: " + s1);
        WithdrawRecord record = WithdrawLogic.oneChainWithdrawFailNotify(s);
        if(record != null)
        {
//            insertWithDrawTran(record, ConstantCode.TransactionType.TRANSACTION_TYPE_WITHDRAW_FAIL);
            getUserBalance();

            SendEventBus(null,OLMessageModel.STMSG_MODEL_WITH_DRAW_FAIL_NOTIFY);

            WithdrawLogic.setMessageNotify(record.getAmount(),record.getTxId(), ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_FAIL,record.getRemark());
        }
    }

    public void oneChainZxCoinWithdrawRemitSuccesNotify(String s, String s1)
    {
        OLLogger.i(TAG,"oneChainZxCoinWithdrawRemitSuccesNotify " + "retMsg: " + s + "txId: " + s1);
        WithdrawRecord record = WithdrawLogic.oneChainRemitSuccessNotify(s);
        if(record != null)
        {
            getUserBalance();

            SendEventBus(null,OLMessageModel.STMSG_MODEL_REMIT_SUCCES_NOTIFY);

            WithdrawLogic.setMessageNotify(record.getAmount(),record.getTxId(),ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_SUCCESS,"");
        }
    }

    @Override
    public void oneChainZxCoinWithdrawConfirmNotify(String s, String s1)
    {
        OLLogger.i(TAG,"oneChainZxCoinWithdrawConfirmNotify " + "retMsg: " + s + "txId: " + s1);
        WithdrawRecord record = WithdrawLogic.oneChainWithdrawConfrim(s);
        if(record != null)
        {
            insertWithDrawTran(record, ConstantCode.TransactionType.TRANSACTION_TYPE_WITHDRAW_AUTO_CONFIRM);

            getUserBalance();

            SendEventBus(null,OLMessageModel.STMSG_MODEL_WITH_DRAW_CONFIRM_NOTIFY);
        }
    }

    @Override
    public void oneChainZxCoinWithdrawAppealDoneNotify(String s, String s1)
    {
        OLLogger.i(TAG,"oneChainZxCoinWithdrawAppealDoneNotify " + "retMsg: " + s + "txId: " + s1);
        WithdrawRecord record = WithdrawLogic.oneChainAppealDoneNotify(s);
        if(record != null)
        {
            if (record.getState().intValue() == ConstantCode.WithdrawType.WITHDRAW_TYPE_APPLYING
                    || record.getState().intValue() == ConstantCode.WithdrawType.WITHDRAW_TYPE_CONFIRM)
            insertWithDrawTran(record, ConstantCode.TransactionType.TRANSACTION_TYPE_WITHDRAW_APPEAL_DONE);

            getUserBalance();

            SendEventBus(null,OLMessageModel.STMSG_MODEL_APPEAL_DONE_NOTIFY);

            if(record.getState().intValue() == ConstantCode.WithdrawType.WITHDRAW_TYPE_FAIL)
            {
                WithdrawLogic.setMessageNotify(record.getAmount(),record.getTxId(), ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_FAIL,record.getRemark());
            }
        }
    }

    @Override
    public void oneChainZxCoinWithdrawApplyNotify(String s, String s1) {
        OLLogger.i(TAG,"oneChainZxCoinWithdrawApplyNotify " + "retMsg: " + s + "txId: " + s1);
        // oneChainZxCoinWithdrawApplyNotify retMsg: {"code":0,"message":"Success","data":{"UserName":"oho","UserHash":"fbbfd5dd146499748299948d56c6e329b78985440014550bc0fc77b4"}}txId: cab9b5a3-ebf9-4a96-abc5-b7ab0fd54b87
        WithdrawRecord record = WithdrawLogic.oneChainWithdrawNotify(s, s1);
        if(record != null)
        {
            getUserBalance();

            SendEventBus(record,OLMessageModel.STMSG_MODEL_WITH_DRAW_NOTIFY);
        }
    }

    @Override
    public void oneChainZxCoinWithdrawRecall(String s, String s1)
    {
        //客户端不会接收到该回调，只空实现
        OLLogger.i(TAG,"客户端不会接收到该回调 oneChainZxCoinWithdrawRecall " + "retMsg: " + s + "txId: " + s1);
    }

    @Override
    public void oneChainZxCoinWithdrawRemitSucces(String s, String s1)
    {
        //客户端不会调用
        OLLogger.i(TAG,"客户端不会调用 oneChainZxCoinWithdrawRemitSucces " + "retMsg: " + s + "txId: " + s1);
    }

    @Override
    public void oneChainZxCoinWithdrawAppealDone(String s, String s1)
    {
        //客户端不会调用
        OLLogger.i(TAG,"客户端不会调用 oneChainZxCoinWithdrawAppealDone " + "retMsg: " + s + "txId: " + s1);
    }

    @Override
    public void oneChainZxCoinWithdrawFail(String s, String s1)
    {
        //客户端不会调用
        OLLogger.i(TAG,"客户端不会调用 oneChainZxCoinWithdrawFail " + "retMsg: " + s + "txId: " + s1);
    }

    public boolean isWithdrawActivityDialogShowing()
    {
        boolean exist = WithdrawActivity.instance != null
                && WithdrawActivity.instance.getWithdrawDialog() != null
                && WithdrawActivity.instance.getWithdrawDialog().isShowing();
        return exist;
    }

    @Override
    public void onConnect(int type)
    {
        OLLogger.d(TAG, "onConnect, net work is connect");
        SendEventBus(null, OLMessageModel.STMSG_MODEL_NET_WORK_CONNECT);
    }

    @Override
    public void onDisConnect()
    {
        OLLogger.d(TAG, "onDisContect, net work is disConnect");
        SendEventBus(null, OLMessageModel.STMSG_MODEL_NET_WORK_DISCONNECT);
    }

    private String getString(int msgId)
    {
        return OneLotteryApplication.getAppContext().getString(msgId);
    }

    public void insertWithDrawTran(WithdrawRecord record, int type)
    {
        if (record == null || StringUtils.isEmpty(record.getTxId())) {
            return;
        }
        /**
         * TxId : 71ed5300-bc46-4caf-bb23-5ca710effc5a
         * State : 1
         * AccountInfo : {"BankName":"中国银行","AccountName":"孙海涛","AccountId":"12354458877588"}
         * Amount : 100000
         * RemitOrderNumber :
         * Remark :
         * ModifyTime : 1491789288
         * CreateTime : 1491789288
         * UserName : ghhg
         * UserHash : 7b63fe18ea1c3fc7a8ecbee971c66f493b02ce19a864512ec81da395
         */


        // 通知明细界面实时刷新
        TransactionDetail td = new TransactionDetail();
        td.setTxId(record.getTxId());
        String accountId = StringUtils.nullToEmpty(record.getAccountId().replace(" ", ""));
        td.setRemark(record.getOpeningBankName() + "("
                + accountId.substring(accountId.length() -4) + ")");
        td.setMyId(record.getUserId());
        td.setMyHash(record.getUserHash());
        td.setAmount(record.getAmount());
        td.setTime(record.getModifyTime());
        td.setType(type);
        oneLotteryLogic.checkTranTxId(record.getTxId(), td);
        TransactionDetailDBHelper.getInstance().insertTransactionDetail(td);
    }
}
