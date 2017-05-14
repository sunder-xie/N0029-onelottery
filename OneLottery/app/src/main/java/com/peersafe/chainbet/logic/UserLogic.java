package com.peersafe.chainbet.logic;

import com.peersafe.chainbet.db.Friend;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.manager.dbhelper.FriendDBHelper;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.LotteryJsonBean.ZXCoinBalanceRet;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.log.OLLogger;

/**
 * @author moying
 * @Description
 * @date 2017/1/12 10:16
 */
public class UserLogic
{
    private static final String TAG = "userLogic";

    /**
     * Description 获取余额
     */
    public UserInfo getUserBalance()
    {
        // 判断是否有主账户，先从DB获取
        final UserInfo userInfo = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (userInfo != null)
        {
            final ZXCoinBalanceRet zxCoinBalanceRet = OneLotteryApi.queryZxCoinBalance();
            // 刷新用户余额，判断如果网络的余额和本地不同的话，就获取新的，然后入库
            OLLogger.e(TAG, "ZXCoinBalanceRet=" + zxCoinBalanceRet);

            if (zxCoinBalanceRet != null && zxCoinBalanceRet.getData() != null
                    && UserLogic.isLoginUser(zxCoinBalanceRet.getData().getOwner(), zxCoinBalanceRet.getData().getName())
                    && (zxCoinBalanceRet.getData().getBalance() != userInfo.getBalance() ||
                    zxCoinBalanceRet.getData().getBlockHeight() != userInfo.getCurBlockHeight()))
            {
                UserInfo oldUserInfo = null;
                try
                {
                   oldUserInfo = (UserInfo) userInfo.clone();
                }
                catch (CloneNotSupportedException e)
                {
                    e.printStackTrace();
                }

                userInfo.setBalance(zxCoinBalanceRet.getData().getBalance());
                userInfo.setCurBlockHeight(zxCoinBalanceRet.getData().getBlockHeight());
                userInfo.setPrevBlockHeight(zxCoinBalanceRet.getData().getPrevBlockHeight());
                userInfo.setTxnIDs(zxCoinBalanceRet.getData().getTxnIDs());

                UserInfoDBHelper.getInstance().insertUserInfo(userInfo);


                if (oldUserInfo != null)
                {
                    final UserInfo finalOldUserInfo = oldUserInfo;
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // TODO 根据余额中对应的区块，前区块进行回溯（直到查到本地当前区块号为止）查找所有交易详情后，更新用户信息和交易流水表
                            OneLotteryLogic oneLotteryLogic = new OneLotteryLogic();
                            oneLotteryLogic.refreshTransactions(zxCoinBalanceRet, finalOldUserInfo, ConstantCode.PAGE_SIZE);
                        }
                    }).start();
                }

            }
        }

        return userInfo;
    }

    // 判断是否是官方用户，好友或者本人
    public static boolean isOfficialFriendOrMe(String hash, String name)
    {
        UserInfo me = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        Friend friend = null;
        if (me != null)
        {
            friend = FriendDBHelper.getInstance().getFriendByName(name, me.getUserId());
        }
        return ConstantCode.CommonConstant.ONELOTTERY_DEFAULT_OFFICAL_NAME.equals(name)
                || (me != null && (me.getWalletAddr().equals(hash) || me.getUserId().equals(name)))
                || friend != null;
    }

    // 判断是否是官方用户
    public static boolean isOfficalUser(String hash, String name)
    {
        return ConstantCode.CommonConstant.ONELOTTERY_DEFAULT_OFFICAL_HASH.equals(hash)
                || ConstantCode.CommonConstant.ONELOTTERY_DEFAULT_OFFICAL_NAME.equals(name);
    }

    // 判断是否是游客
    public static boolean isTourist()
    {
        String uid = OneLotteryApi.getCurUserId();
        return StringUtils.isEmpty(uid)
                || ConstantCode.CommonConstant.ONELOTTERY_DEFAULT_USERNAME.equalsIgnoreCase(uid);
    }

    public static boolean isLoginUser(String hash, String name)
    {
        if (StringUtils.isEmpty(hash) && StringUtils.isEmpty(name))
        {
            return false;
        }

        UserInfo me = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (me != null)
        {
            return StringUtils.nullToEmpty(hash).equals(me.getWalletAddr()) || StringUtils.nullToEmpty(name).equals(me.getUserId());
        }
        return false;
    }

}
