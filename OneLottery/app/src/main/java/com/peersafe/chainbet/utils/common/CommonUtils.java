/**
 * Copyright (C) 2015 PeerSafe Technologies. All rights reserved.
 *
 * @Name: CommonUtils.java
 * @Package: com.peersafe.onelottery.utils.common
 * @Description: TODO
 * @author zhangyang
 * @date 2015年7月9日 下午5:17:16
 */

package com.peersafe.chainbet.utils.common;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.utils.log.OLLogger;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * @author zhangyang
 * @Description
 * @date 2015年7月9日 下午5:17:16
 */

public class CommonUtils
{
    public static boolean isWifiConnected(WifiManager wifiManager, WifiInfo wifiInfo)
    {
        int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
        return wifiManager.isWifiEnabled() && ipAddress != 0;
    }

    public static String int2Ip(int ip)
    {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
    }


    /**
     * @return
     * @Description 当前系统语言是否是中文
     * @author zhangyang
     */
    public static boolean isLanguageChinese()
    {
        Locale curLocale = OneLotteryApplication.getAppContext().getResources().getConfiguration().locale;
        String languageCode = curLocale.getLanguage();
        boolean isChinese = languageCode.contains("zh");
        if (isChinese)
        {
            return true;
        }

        return false;
    }

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }

        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }
}
