package com.peersafe.chainbet.manager.ali;

import android.app.Application;
import android.content.Context;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;

/**
 * AUTHOR : sunhaitao.
 * DATA : 16/9/14
 * DESCRIPTION :
 */
public class ALIFeedBack
{
    //阿里百川的用户反馈的初始化
    public static void init(Context context)
    {
        final String APP_KEY = "23633131";
        FeedbackAPI.init((Application) context, APP_KEY);
    }

    //开启阿里百川的用户反馈的界面
    public static void startFeedBackView(Context context)
    {
        FeedbackAPI.openFeedbackActivity();
    }
}
