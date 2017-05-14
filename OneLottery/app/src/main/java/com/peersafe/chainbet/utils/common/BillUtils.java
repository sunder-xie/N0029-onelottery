package com.peersafe.chainbet.utils.common;

import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/2/27
 * DESCRIPTION :
 */

public class BillUtils
{
    public static String genBillNum(String wall, long mills)
    {
        String s = wall + mills + "5f74570f-5359-4e89-b300-839541db9307";
        return CommonUtils.md5(s);
    }
}
