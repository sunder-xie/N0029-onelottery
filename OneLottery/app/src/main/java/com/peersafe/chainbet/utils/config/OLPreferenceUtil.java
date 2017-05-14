/**   
 * Copyright (C) PeerSafe 2015 Technologies. All rights reserved.
 *
 * @Name: OLPreferenceUtil.java
 * @Package: com.peersafe.shadowtalk.utils.config 
 * @Description: 沙话相关的共享配置项处理。包括键定义和相应配置的设置和获取
 * @author zhangyang  
 * @date 2015年6月18日 上午11:23:27 
 */

package com.peersafe.chainbet.utils.config;

import android.content.Context;

/**
 * @Description
 * @author zhangyang
 * @date 2015年6月18日 上午11:23:27
 */

public class OLPreferenceUtil
{
    private static OLPreferenceUtil mInstance;
    /* 定义是否初始化过onelottery */
    private final static String SHARED_KEY_HAS_INIT_ONE_LOTTERY = "shared_key_has_init_one_lottery";
    /* 是否是第一次启动应用的标识 */
    private static final String SHARED_KEY_WELCOME_SHOW_FLAG = "shared_key_welcome_show_flag";
    /* 向谁转账的userID */
    private static final String SHARED_KEY_TRANSFER_TO_USER_ID = "shared_key_transfer_user_id";
    /* 向谁转账的userHash */
    private static final String SHARED_KEY_TRANSFER_TO_USER_HASH = "shared_key_transfer_user_hash";

    /* 所创建活动的活动名 */
    private static final String SHARED_KEY_ADD_LOTTERY_NAME = "shared_key_add_lottery_name";

    /* 向谁转账的众享币 */
    private static final String SHARED_KEY_TRANSFER_TO_AMOUNT = "shared_key_transfer_amount";

    /* 所修改或删除活动的活动ID */
    private static final String SHARED_KEY_MOD_LOTTERY_ID = "shared_key_mod_lottery_id";

    /* 选择下一次再说 */
    private static final String SHARED_KEY_UPDATE_CHOOSE_NEXT = "shared_key_update_choose_next";

    /* 选择忽略该版本 */
    private static final String SHARED_KEY_UPDATE_IGNORE_VERSION = "shared_key_update_ignore_version";


    private static final String SHARED_KEY_UPDATE_SYSTEM_TIME = "shared_key_update_system_time";

    private static final String SHARED_KEY_DELETE_ACCOUNT_UID = "shared_key_delete_account_uid";

    private static final String SHARED_KEY_LAST_GET_BLOCK_OVER = "shared_key_last_getblock_over";

    private static PreferenceConfigUtil mPreferenceConfigUtil;

    /**
     * 单例模式，获取instance实例
     *
     * @param cxt
     * @return
     */
    public synchronized static OLPreferenceUtil getInstance(Context cxt)
    {
        if (mInstance == null)
        {
            mInstance = new OLPreferenceUtil();
            mPreferenceConfigUtil = PreferenceConfigUtil.getInstance(cxt);
            mPreferenceConfigUtil.loadConfig();
        }

        return mInstance;
    }

    /**
     * @Description
     * @author zhangyang
     * @param isFirst
     */
    public void setHasInitOneLottery(boolean isFirst)
    {
        mPreferenceConfigUtil.setBoolean(SHARED_KEY_HAS_INIT_ONE_LOTTERY, isFirst);
    }

    /**
     * @Description
     * @author zhangyang
     * @return
     */
    public boolean getHashInitOneLottery()
    {
        return mPreferenceConfigUtil.getBoolean(SHARED_KEY_HAS_INIT_ONE_LOTTERY,
                false);
    }

    public void setWelcomeShowFlag(boolean flag)
    {
        mPreferenceConfigUtil.setBoolean(SHARED_KEY_WELCOME_SHOW_FLAG,flag);
    }

    public boolean getWelcomeShowFlag()
    {
        return mPreferenceConfigUtil.getBoolean(SHARED_KEY_WELCOME_SHOW_FLAG,true);
    }

    String transferToUserId,transferToUserHash;

    public String getTransferToUserId()
    {
        return mPreferenceConfigUtil.getString(SHARED_KEY_TRANSFER_TO_USER_ID, "");
    }

    public void setTransferToUserId(String transferToUserId)
    {
        mPreferenceConfigUtil.setString(SHARED_KEY_TRANSFER_TO_USER_ID, transferToUserId);
    }

    public String getTransferToUserHash()
    {
        return mPreferenceConfigUtil.getString(SHARED_KEY_TRANSFER_TO_USER_HASH, "");
    }

    public void setTransferToUserHash(String transferToUserHash)
    {
        mPreferenceConfigUtil.setString(SHARED_KEY_TRANSFER_TO_USER_HASH, transferToUserHash);
    }

    public long getTransferAmount()
    {
        return mPreferenceConfigUtil.getLong(SHARED_KEY_TRANSFER_TO_AMOUNT, 0L);
    }

    public void setTransferAmount(long transferAmount)
    {
        mPreferenceConfigUtil.setLong(SHARED_KEY_TRANSFER_TO_AMOUNT, transferAmount);
    }

    public String getAddLotteryName()
    {
        return mPreferenceConfigUtil.getString(SHARED_KEY_ADD_LOTTERY_NAME, "");
    }

    public void setAddLotteryName(String lotteryName)
    {
        mPreferenceConfigUtil.setString(SHARED_KEY_ADD_LOTTERY_NAME, lotteryName);
    }

    public String getModLotteryId()
    {
        return mPreferenceConfigUtil.getString(SHARED_KEY_MOD_LOTTERY_ID, "");
    }

    public void setModLotteryId(String lotteryID)
    {
        mPreferenceConfigUtil.setString(SHARED_KEY_MOD_LOTTERY_ID, lotteryID);
    }

    /**
     * @Description 保存下次再说的标识
     * @author sunhaitao
     * @return
     */
    public void setUpdateNext(int version)
    {
        mPreferenceConfigUtil.setInt(SHARED_KEY_UPDATE_CHOOSE_NEXT, version);
    }

    /**
     * @Description 获取"下次再说"的标识
     * @author sunhaitao
     * @return
     */
    public int getUpdateNext()
    {
        return mPreferenceConfigUtil.getInt(SHARED_KEY_UPDATE_CHOOSE_NEXT, 0);
    }


    /**
     * @Description 保存忽略此次版本的版本号
     * @author sunhaitao
     * @return
     */
    public void setUpdateIgnoreVersion(int version)
    {
        mPreferenceConfigUtil.setInt(SHARED_KEY_UPDATE_IGNORE_VERSION, version);
    }

    /**
     * @Description 获取忽略此次版本的版本号
     * @author sunhaitao
     * @return
     */
    public int getUpdateIgnoreVersion()
    {
        return mPreferenceConfigUtil.getInt(SHARED_KEY_UPDATE_IGNORE_VERSION, 0);
    }

    /**
     * @Description
     * * @author sunhaitao
     * @return
     */
    public long getUpdateSystemTime()
    {

        return mPreferenceConfigUtil.getLong(SHARED_KEY_UPDATE_SYSTEM_TIME, 0l);
    }

    /**
     * @Description
     * @author sunhaitao
     * @param systemTime
     */
    public void setUpdateSystemTime(Long systemTime)
    {
        mPreferenceConfigUtil.setLong(SHARED_KEY_UPDATE_SYSTEM_TIME, systemTime);
    }


    public String getDeleteUid()
    {
        return mPreferenceConfigUtil.getString(SHARED_KEY_DELETE_ACCOUNT_UID, "");
    }

    public void setDeleteUid(String deleteUid)
    {
        mPreferenceConfigUtil.setString(SHARED_KEY_DELETE_ACCOUNT_UID, deleteUid);
    }

    public void setGetLastOver(String userId)
    {
        mPreferenceConfigUtil.setBoolean(SHARED_KEY_LAST_GET_BLOCK_OVER + "_" + userId, true);
    }

    public boolean getGetLastOver(String userId)
    {
        return mPreferenceConfigUtil.getBoolean(SHARED_KEY_LAST_GET_BLOCK_OVER + "_" + userId, false);
    }
}
