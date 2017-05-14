package com.peersafe.chainbet.utils.update;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.model.VersionModel;
import com.peersafe.chainbet.ui.update.UpdateDialogActivity;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.config.OLPreferenceUtil;
import com.peersafe.chainbet.utils.log.OLLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import im.fir.sdk.FIR;
import im.fir.sdk.VersionCheckCallback;

/**
 * Created by sunhaitao on 2016/5/9.
 */
public class UpdateUtil
{
    private static final String TAG = UpdateUtil.class.getSimpleName();

    private static boolean isEnter = false;

    /**
     * update的管理逻辑类
     *
     * @param context
     * @return
     */
    public static String updateManager(final Context context, final boolean fromMain)
    {
        final String firToken = "76994d14ac4dafc02d3a54e8bb102a80";
        if (isEnter)
        {
            return "";
        }

        long updateSystemTime = OLPreferenceUtil.getInstance(context).getUpdateSystemTime();
        if(fromMain && updateSystemTime != 0)
        {
            long nowSystemTime = System.currentTimeMillis();
            if((nowSystemTime - updateSystemTime) <= 24 * 60 * 60 * 1000)
            {
                return "";
            }
        }

        FIR.checkForUpdateInFIR(firToken, new VersionCheckCallback()
        {
            @Override
            public void onSuccess(String versionJson)
            {
                OLLogger.e(TAG, "start onsuccess" + System.currentTimeMillis());
                //判断当前的版本和获得的最新版本比较
                int newVersion = 0;
                try
                {
                    Gson gson = new Gson();
                    VersionModel versionModel = gson.fromJson(versionJson, VersionModel.class);

                    // 解析失败
                    if (versionModel == null)
                    {
                        return;
                    }

                    int nowVersion = getVersion();
                    newVersion = Integer.parseInt(versionModel.getVersion());
                    if (newVersion > nowVersion)
                    {
                        Intent intentUpdate = new Intent(context, UpdateDialogActivity.class);
                        intentUpdate.putExtra("version", versionModel);
                        intentUpdate.putExtra("from",fromMain);
                        context.startActivity(intentUpdate);
                    } else if (newVersion == nowVersion)
                    {
                        Toast.makeText(context,
                                context.getString(R.string.fir_no_update),
                                Toast.LENGTH_SHORT).show();
                    } else
                    {
                        Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JsonSyntaxException exception)
                {
                    exception.printStackTrace();
                }
            }

            @Override
            public void onFail(Exception exception)
            {
                OLLogger.e(TAG, "onFail" + exception.toString());
            }

            @Override
            public void onStart()
            {
                OLLogger.e(TAG, "onStart");
                isEnter = true;
            }

            @Override
            public void onFinish()
            {
                OLLogger.e(TAG, "onFinish");
                isEnter = false;
            }
        });

        return "";
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    private static int getVersion()
    {
        try
        {
            PackageManager manager = OneLotteryApplication.getAppContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(OneLotteryApplication.getAppContext()
                    .getPackageName(), 0);
            int version = info.versionCode;
            return version;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * FIR.im的初始化
     */

    public static void init(Context context)
    {
        FIR.init(context);
    }

    public static boolean isHasUpdateFunc()
    {
        return true;
    }
}
