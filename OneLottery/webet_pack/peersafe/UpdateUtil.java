package com.peersafe.chainbet.utils.update;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.ui.update.UpdateDialogActivity;
import com.peersafe.chainbet.utils.config.OLPreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import im.fir.sdk.FIR;
import im.fir.sdk.VersionCheckCallback;

/**
 * Created by sunhaitao on 2016/5/9.
 */
public class UpdateUtil
{
    /**
     * update的管理逻辑类
     * @param context
     * @return
     */
    public static String updateManager(final Context context, final String config)
    {
        final String firToken = "76994d14ac4dafc02d3a54e8bb102a80";
        FIR.checkForUpdateInFIR(firToken, new VersionCheckCallback() {
            @Override
            public void onSuccess(String versionJson) {
                //判断当前的版本和获得的最新版本比较
                int newVersion = 0;
                try {
                    JSONObject jsonObject = new JSONObject(versionJson);
                    newVersion = jsonObject.getInt("version");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int oldVersion = getVersion();
                if(newVersion > oldVersion)
                {
                    int updateConfig = OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).getUpdateNext();
                    int version = OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).getUpdateIgnoreVersion();
                    if(version == newVersion && config.equals(""))
                    {
                        return;
                    }
                    else
                    {
                        if(updateConfig == 1 && config.equals(""))
                        {
                            //当点击下次再说时 停留一个小时后再显示更新的对话框
                            Long systemTime = System.currentTimeMillis();
                            Long updateSystemTime = OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).getUpdateSystemTime();
                            Long time = systemTime - updateSystemTime;
                            if(time > 12*60*60*1000)
                            {
                                OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).setUpdateNext(0);
                                Intent intentUpdate  = new Intent(context,UpdateDialogActivity.class);
                                intentUpdate.putExtra("version",versionJson);
                                context.startActivity(intentUpdate);
                            }
                        }
                        else
                        {
                            //得到新版本后将返回的json数据解析传给UpdateActivity
                            Intent intentUpdate  = new Intent(context,UpdateDialogActivity.class);
                            intentUpdate.putExtra("version",versionJson);
                            context.startActivity(intentUpdate);
                        }
                    }
                }
                else
                {
                    if (!config.equals(""))
                    {
                        Toast.makeText(context,
                                context.getString(R.string.fir_no_update),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFail(Exception exception) {
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

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
            PackageInfo info = manager.getPackageInfo(OneLotteryApplication.getAppContext().getPackageName(), 0);
            int version = info
                    .versionCode;
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
