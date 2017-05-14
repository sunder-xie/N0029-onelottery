/**
 * Copyright (C) PeerSafe 2015 Technologies. All rights reserved.
 *
 * @Name: ShadowTalkApplication.java
 * @Package: com.peersafe.shadowtalk
 * @Description:
 * @author zhangyang
 * @date 2015年6月17日 下午5:08:34
 */

package com.peersafe.chainbet;

import android.app.Application;
import android.content.Context;

import com.peersafe.chainbet.db.DaoMaster;
import com.peersafe.chainbet.db.DaoSession;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.ali.ALIFeedBack;
import com.peersafe.chainbet.utils.update.UpdateUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.PlatformConfig;

import cn.beecloud.BCPay;
import cn.beecloud.BeeCloud;


/**
 * @author zhangyang
 * @Description
 * @date 2015年6月17日 下午5:08:34
 */

public class OneLotteryApplication extends Application
{
    public static Context mApplicationContext;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;
    // 数据库名，表名是自动被创建的
    public static final String DB_NAME = "OneLottery.db";

    /**
     * Description
     *
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate()
    {
        super.onCreate();

        mApplicationContext = this;

        //初始化sdk和全局配置等
        OneLotteryManager.getInstance().init(mApplicationContext);

        //自动升级的初始化
        UpdateUtil.init(mApplicationContext);

        //用户反馈界面
        ALIFeedBack.init(mApplicationContext);

        PlatformConfig.setWeixin("wxf45a0a27e5a9822c", "9214161be68740b40710e3f60c30cff1");

        //微信初始化(用于支付)
        BCPay.initWechatPay(mApplicationContext, "wxf45a0a27e5a9822c");

//      PlatformConfig.setQQZone("1105919955","TsdLO72SHxsWjAUg");

        //BeeClound充值
        BeeCloud.setAppIdAndSecret("7894f83b-cc29-4d76-ad57-f6d20722e6b6", "5f74570f-5359-4e89-b300-839541db9307");

        //友盟统计
        //该接口默认参数是true，即采集mac地址，但如果开发者需要在googleplay发布，
        // 考虑到审核风险，可以调用该接口，参数设置为 false 就不会采集mac地址
        MobclickAgent.setCheckDevice(false);
        MobclickAgent.setScenarioType(getAppContext(), MobclickAgent.EScenarioType.E_UM_NORMAL); //普通场景类型
    }

    public static Context getAppContext()
    {
        return mApplicationContext;
    }

    /**
     * Description
     *
     * @see android.app.Application#onTerminate()
     */

    @Override
    public void onTerminate()
    {
        super.onTerminate();
    }

    public synchronized static DaoMaster getDaoMaster()
    {
        if (daoMaster == null)
        {
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(mApplicationContext,
                    DB_NAME, null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return daoMaster;
    }

    public synchronized static DaoSession getDaoSession()
    {
        if (daoSession == null)
        {
            daoSession = getDaoMaster().newSession();
        }
        return daoSession;
    }
}
