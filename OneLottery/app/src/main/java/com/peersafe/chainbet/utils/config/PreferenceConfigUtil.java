/**   
 * Copyright (C) PeerSafe 2015 Technologies. All rights reserved.
 *
 * @Name: PreferenceCommonConfig.java 
 * @Package: com.peersafe.shadowtalk.utils.config 
 * @Description: Preference类型配置文件公共操作类
 * @author zhangyang  
 * @date 2015年6月18日 上午10:53:23 
 */

package com.peersafe.chainbet.utils.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * @Description
 * @author zhangyang
 * @date 2015年6月18日 上午10:53:23
 */

public class PreferenceConfigUtil
{
    private static PreferenceConfigUtil mPreferenceConfig = null;
    private Context mContext;
    private Editor edit = null;
    private SharedPreferences mSharedPreferences;
    private String filename = "ChainBet";
    private Boolean isLoad = false;

    private PreferenceConfigUtil(Context context)
    {
        this.mContext = context;
    }

    /**
     * @Description
     * @author zhangyang
     * @param context
     * @return
     */
    public static PreferenceConfigUtil getInstance(Context context)
    {
        if (mPreferenceConfig == null)
        {
            mPreferenceConfig = new PreferenceConfigUtil(context);
        }
        return mPreferenceConfig;
    }

    /**
     * @Description 初始化公共操作类
     * @author zhangyang
     */
    public void loadConfig()
    {
        try
        {
            mSharedPreferences = mContext.getSharedPreferences(filename,
                    Context.MODE_PRIVATE);
            edit = mSharedPreferences.edit();
            isLoad = true;
        }
        catch (Exception e)
        {
            isLoad = false;
        }

    }

    public Boolean isLoadConfig()
    {
        return isLoad;
    }

    /**
     * @Description 设置String类型的配置值
     * @author zhangyang
     * @param key
     * @param value
     */
    public void setString(String key, String value)
    {
        edit.putString(key, value);
        edit.commit();
    }

    /**
     * @Description 设置int类型的配置值
     * @author zhangyang
     * @param key
     * @param value
     */
    public void setInt(String key, int value)
    {
        edit.putInt(key, value);
        edit.commit();
    }

    /**
     * @Description 设置Boolean类型的配置值
     * @author zhangyang
     * @param key
     * @param value
     */
    public void setBoolean(String key, Boolean value)
    {
        edit.putBoolean(key, value);
        edit.commit();
    }

    /**
     * @Description 设置Long类型的配置值
     * @author zhangyang
     * @param key
     * @param value
     */
    public void setLong(String key, long value)
    {
        // TODO Auto-generated method stub
        edit.putLong(key, value);
        edit.commit();
    }

    /**
     * @Description 设置Float类型的配置值
     * @author zhangyang
     * @param key
     * @param value
     */
    public void setFloat(String key, float value)
    {
        edit.putFloat(key, value);
        edit.commit();
    }

    /**
     * @Description 设置Byte类型的配置值
     * @author zhangyang
     * @param key
     * @param value
     */
    public void setByte(String key, byte[] value)
    {
        try
        {
            setString(key, new String(value, "UTF-8"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @Description 设置Short类型的配置值
     * @author zhangyang
     * @param key
     * @param value
     */
    public void setShort(String key, short value)
    {
        setString(key, String.valueOf(value));
    }

    /**
     * @Description 设置Double类型的配置值
     * @author zhangyang
     * @param key
     * @param value
     */
    public void setDouble(String key, double value)
    {
        setString(key, String.valueOf(value));
    }

    /**
     * @Description 获取String类型的配置值
     * @author zhangyang
     * @param key
     * @param defaultValue
     * @return
     */
    public String getString(String key, String defaultValue)
    {
        return mSharedPreferences.getString(key, defaultValue);
    }

    /**
     * @Description 获取int类型的配置值
     * @author zhangyang
     * @param key
     * @param defaultValue
     * @return
     */
    public int getInt(String key, int defaultValue)
    {
        return mSharedPreferences.getInt(key, defaultValue);
    }

    /**
     * @Description 获取Boolean类型的配置值
     * @author zhangyang
     * @param key
     * @param defaultValue
     * @return
     */
    public boolean getBoolean(String key, Boolean defaultValue)
    {
        return mSharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * @Description 获取Byte类型的配置值
     * @author zhangyang
     * @param key
     * @param defaultValue
     * @return
     */
    public byte[] getByte(String key, byte[] defaultValue)
    {
        try
        {
            String result = getString(key, "");
            if (result.equals(""))
            {
                return defaultValue;
            }
            return result.getBytes("UTF8");
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
        return defaultValue;
    }

    /**
     * @Description 获取Short类型的配置值
     * @author zhangyang
     * @param key
     * @param defaultValue
     * @return
     */
    public short getShort(String key, Short defaultValue)
    {
        try
        {
            return Short.valueOf(getString(key, ""));
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
        return defaultValue;
    }

    /**
     * @Description 获取Long类型的配置值
     * @author zhangyang
     * @param key
     * @param defaultValue
     * @return
     */
    public long getLong(String key, Long defaultValue)
    {
        return mSharedPreferences.getLong(key, defaultValue);
    }

    /**
     * @Description 获取Float类型的配置值
     * @author zhangyang
     * @param key
     * @param defaultValue
     * @return
     */
    public float getFloat(String key, Float defaultValue)
    {
        return mSharedPreferences.getFloat(key, defaultValue);
    }

    /**
     * @Description 获取Double类型的配置值
     * @author zhangyang
     * @param key
     * @param defaultValue
     * @return
     */
    public double getDouble(String key, Double defaultValue)
    {
        try
        {
            return Double.valueOf(getString(key, ""));
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
        return defaultValue;
    }

    /**
     * @Description 删除配置值
     * @author zhangyang
     * @param key
     */
    public void remove(String key)
    {
        edit.remove(key);
        edit.commit();
    }

    /**
     * @Description 删除一组配置值
     * @author zhangyang
     * @param keys
     */
    public void remove(String... keys)
    {
        for (String key : keys)
            remove(key);
    }

    /**
     * @Description 清除所有配置
     * @author zhangyang
     */
    public void clear()
    {
        edit.clear();
        edit.commit();
    }
}
