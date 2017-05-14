/**   
 * Copyright (C) PeerSafe Technologies. All rights reserved.
 * 
 * @Name: NetworkUtil.java 
 * @Package: com.peersafe.shadowtalk.utils 
 * @Description: 网络状态工具类
 * @author zhangyang  
 * @date 2015年6月17日 上午11:33:35 
 */

package com.peersafe.chainbet.utils.netstate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.peersafe.chainbet.OneLotteryApplication;

/**
 * @Description
 * @author zhangyang
 * @date 2015年6月17日 上午11:33:35
 */

public class NetworkUtil
{
    public static enum netType
    {
        wifi, G2, G3, G4,unKnow
    }

    /**
     * @Description 网路是否可用
     * @author zhangyang
     * @return boolean
     */
    public static boolean isNetworkAvailable()
    {
        ConnectivityManager mgr = (ConnectivityManager) OneLotteryApplication.getAppContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = mgr.getAllNetworkInfo();
        if (null != info)
        {
            for (int i = 0; i < info.length; i++)
            {
                if (info[i].getState() == NetworkInfo.State.CONNECTED)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @Description 判断是否有网路连接
     * @author zhangyang
     * @return boolean
     */
    public static boolean isNetworkConnected()
    {
        ConnectivityManager mgr = (ConnectivityManager) OneLotteryApplication.getAppContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = mgr.getActiveNetworkInfo();
        if (null != info)
        {
            return info.isAvailable();
        }
        return false;
    }

    /**
     * @Description 判断wifi网路是否可用
     * @author sunhaitao
     * @param context
     * @return boolean
     */
    public static boolean isWifiConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiNetworkInfo)
        {
            return wifiNetworkInfo.isAvailable();
        }
        return false;
    }

    /**
     * @Description 判断mobile网路是否可用
     * @author sunhaitao
     * @param context
     * @return boolean
     */
    public static boolean isMobileConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (null != networkInfo)
        {
            return networkInfo.isAvailable();
        }
        return false;
    }

    /**
     * @Description 获取当前网络连接的类型信息
     * @author sunhaitao
     * @param context
     * @return int
     */
    public static int getConnectedType(Context context)
    {
        if (context != null)
        {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager
                    .getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable())
            {
                return networkInfo.getType();
            }
        }
        return -1;
    }

    /**
     * @Description 获取当前的网络状态
     * @author sunhaitao
     * @param context
     * @return netType
     */
    public static int getSouNeType(Context context)
    {
        ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null)
        {
            return TelephonyManager.NETWORK_TYPE_UNKNOWN;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE)
        {
            TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getNetworkType();
        } 
        else if (nType == ConnectivityManager.TYPE_WIFI)
        {
            return 1; //wifi
        }
        return TelephonyManager.NETWORK_TYPE_UNKNOWN;
    }
    public static netType getAPNType(Context context)
    {
        ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null)
        {
            return netType.unKnow;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE)
        {
            int itype=isFastMobileNetwork(context);
            switch (itype)
            {
                case 2:
                    return netType.G2;
                case 3:
                    return netType.G3;
                case 4:
                    return netType.G4;
                case 5:
                    return netType.unKnow;
                default:
                    break;
            } 
        }
        else if (nType == ConnectivityManager.TYPE_WIFI)
        {
            return netType.wifi;
        }
        return netType.unKnow;
    }
    
    private static int isFastMobileNetwork(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            switch (telephonyManager.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return 2; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return 2; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return 3; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return 3; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return 3; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return 2; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return 3; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return 3; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return 3; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return 3; // ~ 400-7000 kbps
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                    return 3; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    return 3; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return 3; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return 2; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return 4; // ~ 10+ Mbps
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    return 5;  //暂时把未知类型，归为2G
                default:
                    return 5;   //不在里面类型里面的暂时归为3G
                }
            }
}
