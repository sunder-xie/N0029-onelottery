
/**   
 * Copyright (C) 2015 PeerSafe Technologies. All rights reserved.
 * @Name: OLLogger.java
 * @Package: com.peersafe.shadowtalk.utils.log 
 * @Description: TODO
 * @author zhangyang  
 * @date 2015年8月7日 下午5:40:44 
 */


package com.peersafe.chainbet.utils.log;

import android.util.Log;

/** 
 * @Description 
 * @author zhangyang
 * @date
 */

public class OLLogger
{
    public static final int VERBOSE = 1;

    public static final int DEBUG = 2;

    public static final int INFO = 3;

    public static final int WARN = 4;

    public static final int ERROR = 5;
    
    private static int mLogLevel = VERBOSE;
    
    private static boolean mIsCloseLog = false;
    
    public static void v(String tag, String message)
    {
        if (mLogLevel <= VERBOSE && !mIsCloseLog)
        {
            Log.v(tag, message);
        }
    }
    
    public static void d(String tag, String message)
    {
        if (mLogLevel <= DEBUG && !mIsCloseLog)
        {
            Log.d(tag, message);
        }
    }
    
    public static void i(String tag, String message)
    {
        if (mLogLevel <= INFO && !mIsCloseLog)
        {
            Log.i(tag, message);
        }
    }

    public static void w(String tag, String message)
    {
        if (mLogLevel <= WARN && !mIsCloseLog)
        {
            Log.w(tag, message);
        }
    }

    public static void e(String tag, String message)
    {
        if (mLogLevel <= ERROR && !mIsCloseLog)
        {
            Log.e(tag, message);
        }
    }
    
    public void setLogLevel(int level)
    {
        mLogLevel = level;
    }
    
    public int getLogLevel()
    {
        return mLogLevel;
    }
    
    public void setCloseLog(boolean isClose)
    {
        mIsCloseLog = isClose;
    }
    
    public boolean getCloseLog()
    {
        return mIsCloseLog;
    }
}
