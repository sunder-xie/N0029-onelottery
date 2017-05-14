package com.peersafe.chainbet.manager.dbhelper;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.db.UserInfoDao;
import com.peersafe.chainbet.utils.log.OLLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * AUTHOR : sunhaitao.
 * DATA : 16/12/13
 * DESCRIPTION :
 */

public class UserInfoDBHelper
{

    private static final String TAG = UserInfoDBHelper.class.getSimpleName();
    private static UserInfoDBHelper instance;

    private UserInfoDBHelper()
    {

    }

    public static synchronized UserInfoDBHelper getInstance()
    {
        if (instance == null)
        {
            instance = new UserInfoDBHelper();
        }
        return instance;
    }

    public void insertUserInfo(UserInfo userInfo)
    {
        OneLotteryApplication.getDaoSession().getUserInfoDao().insertOrReplace(userInfo);
    }

    public void deleteUser(String userId)
    {
        OneLotteryApplication.getDaoSession().getUserInfoDao().deleteByKey(userId);
    }

    public UserInfo getUserByUserId(String userId)
    {
        return OneLotteryApplication.getDaoSession().getUserInfoDao().load(userId);
    }

    /**
     * 设置当前用户为主账户
     *
     * @param userInfo
     * @sunhaitao
     */
    public void setCurPrimaryAccount(UserInfo userInfo)
    {
        if (userInfo == null)
        {
            return;
        }

        OLLogger.d(TAG,"setCurPrimaryAccount enter ");
        UserInfo curPrimaryAccount = getCurPrimaryAccount();
        if (null != curPrimaryAccount)
        {
            curPrimaryAccount.setIsCurUser(false);
            insertUserInfo(curPrimaryAccount);
        }
        userInfo.setIsCurUser(true);
        insertUserInfo(userInfo);
    }

    /**
     * 获取当前主用户
     *
     * @return
     * @sunhaitao
     */
    public UserInfo getCurPrimaryAccount()
    {
        OLLogger.d(TAG,"getCurPrimaryAccount enter ");
        QueryBuilder q = OneLotteryApplication.getDaoSession().getUserInfoDao().queryBuilder();
        q.where(UserInfoDao.Properties.IsCurUser.eq(true));
        List list = q.build().list();
        if (list.size() == 0)
        {
            return null;
        } else
        {
            return (UserInfo) list.get(0);
        }
    }

    /**
     * 获取当前用户的集合
     *
     * @param ContainPrimary true:包含主账户的全部用户ID, false:不包含主账户的全部用户ID
     */
    public List<String> getUserList(boolean ContainPrimary)
    {
        QueryBuilder q = OneLotteryApplication.getDaoSession().getUserInfoDao().queryBuilder();
        if (!ContainPrimary)
        {
            q.where(UserInfoDao.Properties.IsCurUser.eq(false));
        }
        List<String> list = new ArrayList<>();

        List<UserInfo> userInfoList = q.build().list();

        if (userInfoList != null && !userInfoList.isEmpty())
        {
            for (int i = 0; i < userInfoList.size(); i++)
            {
                list.add(userInfoList.get(i).getUserId());
            }
        }

        Collections.reverse(list);
        return list;
    }
}
