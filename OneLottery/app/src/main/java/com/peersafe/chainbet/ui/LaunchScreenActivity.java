package com.peersafe.chainbet.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.peersafe.chainbet.LoginActivity;
import com.peersafe.chainbet.MainActivity;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.RegisterActivity;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.utils.common.ImageUtils;
import com.peersafe.chainbet.utils.config.OLPreferenceUtil;

import java.util.List;

public class LaunchScreenActivity extends BasicActivity
{

    private static final int sleepTime = 1000;

    private Bitmap mBgBitmap; // 启动背景的Bitmap对象
    private Bitmap mTitleBitmap; // 启动文字标题的Bitmap对象
    private Bitmap mImageBitmap; // 启动图片的Bitmap对象
    private Bitmap[] bitmaps = {mBgBitmap, mTitleBitmap, mImageBitmap};
    public int[] imageIDs = {R.drawable.splash_bg, R.drawable.splash_webet, R.drawable.splash_content};
    public int[] viewIDs = {R.id.iv_launch_bg, R.id.iv_launch_webet, R.id.iv_launch_content};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);

        for (int i = 0; i < bitmaps.length; i++)
        {
            ImageUtils.setBitmap(LaunchScreenActivity.this, bitmaps[i], viewIDs[i], imageIDs[i]);
        }
    }


    /**
     * Description
     *
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart()
    {
        super.onStart();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(sleepTime);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                // 清除上次创建中的活动标记（以防止弱网环境下消息收不到的情况）
                OLPreferenceUtil.getInstance(LaunchScreenActivity.this).setAddLotteryName("");

                // 如果是第一次进入程序或者是程序更新进入，则进入引导页面，否则进入主页面
                if (OLPreferenceUtil.getInstance(getApplicationContext()).getWelcomeShowFlag())
                {
                    // 进入欢迎页面
                    Intent intent = new Intent(LaunchScreenActivity.this, GuideActivity.class);
                    startActivity(intent);
                } else
                {
                    // 判断是否有主账户
                    UserInfo curPrimaryAccount = UserInfoDBHelper.getInstance().
                            getCurPrimaryAccount();

                    if (null != curPrimaryAccount)
                    {
                        // 首次进入没必要，后面再进有必要?
                        OneLotteryApi.setCurUserId(curPrimaryAccount.getUserId());

                        // 进入主界面
                        Intent intent = new Intent(LaunchScreenActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else
                    {
                        List<String> userList = UserInfoDBHelper.getInstance().getUserList(true);
                        if (userList.isEmpty())
                        {
                            Intent register = new Intent(LaunchScreenActivity.this, RegisterActivity.class);
                            startActivity(register);
                        } else
                        {
                            Intent login = new Intent(LaunchScreenActivity.this, LoginActivity.class);
                            startActivity(login);
                        }
                    }
                }

                finish();
            }
        }).start();
    }

    /**
     * Description
     *
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy()
    {
        for (int i = 0; i < bitmaps.length; i++)
        {
            if (null != bitmaps[i] && !bitmaps[i].isRecycled())
            {
                bitmaps[i].recycle();
                bitmaps[i] = null;
            }
        }

        System.gc();

        super.onDestroy();
    }
}
