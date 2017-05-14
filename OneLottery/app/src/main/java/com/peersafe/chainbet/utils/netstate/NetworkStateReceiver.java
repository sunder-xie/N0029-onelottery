/**   
 * Copyright (C) PeerSafe 2015 Technologies. All rights reserved.
 *
 * @Name: NetworkStateReceiver.java 
 * @Package: com.peersafe.shadowtalk.utils.netstate 
 * @Description: 是一个检测网络状态改变的广播，需要配置 <receiver
 *              android:name="com.peersafe.shadowtalk.utils.netstate.NetworkStateReceiver" >
 *              <intent-filter> <action
 *              android:name="android.net.conn.CONNECTIVITY_CHANGE" /> <action
 *              android:name="ps.android.net.conn.CONNECTIVITY_CHANGE" />
 *              </intent-filter> </receiver>
 * 
 *              需要开启权限 <uses-permission
 *              android:name="android.permission.CHANGE_NETWORK_STATE" />
 *              <uses-permission
 *              android:name="android.permission.CHANGE_WIFI_STATE" />
 *              <uses-permission
 *              android:name="android.permission.ACCESS_NETWORK_STATE" />
 *              <uses-permission
 *              android:name="android.permission.ACCESS_WIFI_STATE" />
 * @author zhangyang  
 * @date 2015年6月17日 下午2:35:18 
 */

package com.peersafe.chainbet.utils.netstate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.peersafe.chainbet.utils.netstate.NetworkUtil.netType;

import java.util.ArrayList;

/**
 * @Description
 * @author zhangyang
 * @date 2015年6月17日 下午2:35:18
 */

public class NetworkStateReceiver extends BroadcastReceiver
{
    private static Boolean mNetworkAvailable = false;
    private static netType mNetType;
    private static int souNetType;
    private static ArrayList<NetChangeObserver> mNetChangeObserverArrayList = new ArrayList<NetChangeObserver>();
    private final static String ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public final static String PS_ANDROID_NET_CHANGE_ACTION = "ps.android.net.conn.CONNECTIVITY_CHANGE";
    private static BroadcastReceiver mReceiver;

    private static BroadcastReceiver getReceiver()
    {
        if (mReceiver == null)
        {
            mReceiver = new NetworkStateReceiver();
        }
        return mReceiver;
    }

    /**
     * Description
     * 
     * @param context
     * @param intent
     * @see BroadcastReceiver#onReceive(Context,
     *      Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        mReceiver = NetworkStateReceiver.this;
        if (intent.getAction().equalsIgnoreCase(ANDROID_NET_CHANGE_ACTION)
                || intent.getAction().equalsIgnoreCase(
                        PS_ANDROID_NET_CHANGE_ACTION))
        {
            // TALogger.i(TANetworkStateReceiver.this, "网络状态改变.");
            if (!NetworkUtil.isNetworkAvailable())
            {
                // TALogger.i(TANetworkStateReceiver.this, "没有网络连接.");
                mNetworkAvailable = false;
            }
            else
            {
                // TALogger.i(TANetworkStateReceiver.this, "网络连接成功.");
                souNetType = NetworkUtil.getSouNeType(context);
                mNetworkAvailable = true;
            }
            notifyObserver();
        }
    }

    /**
     * @Description 通知观察者网路状态改变
     * @author zhangyang
     */
    private void notifyObserver()
    {
        for (int i = 0; i < mNetChangeObserverArrayList.size(); i++)
        {
            NetChangeObserver observer = mNetChangeObserverArrayList.get(i);
            if (observer != null)
            {
                if (isNetworkAvailable())
                {
                    observer.onConnect(souNetType);
                }
                else
                {
                    observer.onDisConnect();
                }
            }
        }
    }

    /**
     * @Description
     * @author zhangyang
     * @return
     */
    private boolean isNetworkAvailable()
    {
        return mNetworkAvailable;
    }

    /**
     * @Description 注册网络状态广播
     * @author zhangyang
     * @param mContext
     */
    public static void registerNetworkStateReceiver(Context mContext)
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PS_ANDROID_NET_CHANGE_ACTION);
        filter.addAction(ANDROID_NET_CHANGE_ACTION);
        mContext.getApplicationContext()
                .registerReceiver(getReceiver(), filter);
    }

    /**
     * @Description 注销网络状态广播
     * @author zhangyang
     * @param mContext
     */
    public static void unRegisterNetworkStateReceiver(Context mContext)
    {
        if (mReceiver != null)
        {
            try
            {
                mContext.getApplicationContext().unregisterReceiver(mReceiver);
            }
            catch (Exception e)
            {
                // TODO: handle exception
            }
        }
    }
    
    public static netType getAPNType()
    {
        return mNetType;
    }
    
     
    /** 
     * @Description 注册网络连接观察者
     * @author zhangyang
     * @param observer  
     */
    public static void registerObserver(NetChangeObserver observer)
    {
        if (null == mNetChangeObserverArrayList)
        {
            mNetChangeObserverArrayList = new ArrayList<NetChangeObserver>();
        }
        mNetChangeObserverArrayList.add(observer);
    }

    /** 
     * @Description removeRegisterObserver
     * @author zhangyang
     * @param observer  
     */
    public static void removeRegisterObserver(NetChangeObserver observer)
    {
        if (null != mNetChangeObserverArrayList)
        {
            mNetChangeObserverArrayList.remove(observer);
        }
    }
}
