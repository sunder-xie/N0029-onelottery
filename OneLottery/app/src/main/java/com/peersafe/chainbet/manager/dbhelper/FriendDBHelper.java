package com.peersafe.chainbet.manager.dbhelper;

import android.app.Fragment;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.db.Friend;
import com.peersafe.chainbet.db.FriendDao;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * AUTHOR : sunhaitao.
 * DATA : 16/12/28
 * DESCRIPTION :
 */

public class FriendDBHelper
{
    private static FriendDBHelper instance;

    public static synchronized FriendDBHelper getInstance()
    {
        if (instance == null)
        {
            instance = new FriendDBHelper();
        }
        return instance;
    }

    public void insertFriend(Friend friend)
    {
        QueryBuilder q = OneLotteryApplication.getDaoSession().getFriendDao().queryBuilder();
        q.where(FriendDao.Properties.UserId.eq(friend.getUserId()),FriendDao.Properties.FriendHash.eq(friend.getFriendHash()));
        List<Friend> friends = q.build().list();
        if(friends != null && !friends.isEmpty())
        {
            for (Friend fri : friends)
            {
                deleteFriend(fri);
            }
        }

        OneLotteryApplication.getDaoSession().getFriendDao().insertOrReplace(friend);
    }

    public Friend getFriendById(String friendHash, String myUid)
    {
        if (StringUtils.isEmpty(friendHash) || StringUtils.isEmpty(myUid))
        {
            return null;
        }
        QueryBuilder queryBuilder = OneLotteryApplication.getDaoSession().getFriendDao().queryBuilder();
        queryBuilder.where(FriendDao.Properties.FriendHash.eq(friendHash), FriendDao.Properties.UserId.eq(myUid));

        List<Friend> friends = queryBuilder.build().list();
        if (friends != null && !friends.isEmpty())
        {
            return friends.get(0);
        }

        return null;
    }

    public Friend getFriendByName(String friendName, String myUid)
    {
        if (StringUtils.isEmpty(friendName) || StringUtils.isEmpty(myUid))
        {
            return null;
        }
        QueryBuilder queryBuilder = OneLotteryApplication.getDaoSession().getFriendDao()
                .queryBuilder();
        queryBuilder.where(FriendDao.Properties.FriendId.eq(friendName), FriendDao.Properties
                .UserId.eq(myUid));

        List<Friend> friends = queryBuilder.build().list();
        if (friends != null && !friends.isEmpty())
        {
            return friends.get(0);
        }

        return null;
    }

    /**
     * 根据公钥hash来判断是否含有这个好友
     *
     * @param attendeeHash
     */
    public boolean isMyFriend(String attendeeHash)
    {
        if (StringUtils.isEmpty(attendeeHash))
        {
            return false;
        }
        // TODO 应该添加UserID是不是自己，借用getFriendById
        QueryBuilder queryBuilder = OneLotteryApplication.getDaoSession().getFriendDao()
                .queryBuilder();
        String curUserId = OneLotteryApi.getCurUserId();
        queryBuilder.where(FriendDao.Properties.FriendHash.eq(attendeeHash), FriendDao.Properties
                .UserId.eq(curUserId));

        if (queryBuilder.count() > 0)
        {
            return true;
        } else
        {
            return false;
        }
    }

    public List<Friend> getAllFriendList()
    {
        UserInfo curPrimaryAccount = UserInfoDBHelper.getInstance().getCurPrimaryAccount();

        if (null != curPrimaryAccount)
        {
            QueryBuilder q = OneLotteryApplication.getDaoSession().getFriendDao().queryBuilder();
            q.where(FriendDao.Properties.UserId.eq(curPrimaryAccount.getUserId()));

            return q.build().list();
        }

        return null;
    }

    /**
     * 删除好友
     *
     * @param friend
     */
    public void deleteFriend(Friend friend)
    {
        OneLotteryApplication.getDaoSession().getFriendDao().delete(friend);
    }

    /**
     * 获取包括官方账户和登录用户在内的所有好友名字
     */
    public List<String> getAllFriendNames()
    {
        ArrayList<String> friendNames = new ArrayList<>();
        friendNames.add(ConstantCode.CommonConstant.ONELOTTERY_DEFAULT_OFFICAL_NAME);

        UserInfo curPrimaryAccount = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (null != curPrimaryAccount)
        {
            friendNames.add(curPrimaryAccount.getUserId());

            QueryBuilder q = OneLotteryApplication.getDaoSession().getFriendDao().queryBuilder();
            q.where(FriendDao.Properties.UserId.eq(curPrimaryAccount.getUserId()));

            List<Friend> flist = q.build().list();
            if (flist != null)
            {
                for (Friend friend : flist)
                {
                    friendNames.add(friend.getFriendId());
                }
            }

        }

        return friendNames;
    }
}
