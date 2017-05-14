/**
 * Copyright (C) PeerSafe 2015 Technologies. All rights reserved.
 *
 * @Name: BasicActivity.java
 * @Package: com.peersafe.shadowtalk.ui
 * @Description: Activity基类，其他Activity继承该类。该类实现Activity的公共功能，例如数据统计等
 * @author zhangyang
 * @date 2015年6月19日 下午5:25:51
 */

package com.peersafe.chainbet.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.ui.lottery.LotteryBetDialog;
import com.peersafe.chainbet.ui.setting.withdraw.WithdrawDialog;
import com.peersafe.chainbet.utils.common.PermissionUtils;
import com.peersafe.chainbet.widget.InputPwdDialog;
import com.peersafe.chainbet.widget.LoadingDialog;
import com.peersafe.chainbet.widget.WaitingDialog;
import com.umeng.analytics.MobclickAgent;

/**
 * @author zhangyang
 * @Description
 * @date
 */

public abstract class BasicActivity extends AppCompatActivity
{
    //用于做进度框显示
    private Dialog mProgressDialog;

    //密码输入框
    private InputPwdDialog mInputPwdDialog;

    //等待框显示
    private WaitingDialog mWaitingDialog;

    //投注框显示
    private LotteryBetDialog mBetDialog;

    //体现等待框
    private WithdrawDialog mWithdrawDialog;

    private Toast toast = null;

    /**
     * Description
     *
     * @param savedInstanceState
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    /**
     * Description
     *
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume()
    {
        super.onResume();

        MobclickAgent.onResume(this);
    }

    /**
     * Description
     *
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause()
    {
        super.onPause();

        MobclickAgent.onPause(this);
    }

    /**
     * Description
     *
     * @see android.app.Activity#onStop()
     */

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    public void hideSoftKeyboard()
    {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams
                .SOFT_INPUT_STATE_HIDDEN)
        {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null)
            {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

//    /**
//     * @param message
//     * @param canceledOntouchOut
//     * @Description 显示进度框
//     * @author zhangyang
//     */
//    public void showProgressDialog(String message, boolean canceledOntouchOut)
//    {
//        if (null == mProgressDialog)
//        {
//            mProgressDialog = new LoadingDialog(this);
//        }
//
//        mProgressDialog.setMessage(message);
//        mProgressDialog.setCanceledOnTouchOutside(canceledOntouchOut);
//        try
//        {
//            mProgressDialog.show();
//        }
//        catch (Exception e)
//        {
//            // WindowManager$BadTokenException
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * @Description 关闭进度框
//     * @author zhangyang
//     */
//    public void dismissProgressDialog()
//    {
//        if (null != mProgressDialog)
//        {
//            mProgressDialog.dismiss();
//        }
//        mProgressDialog = null;
//    }

    public void showProgressDialog(String message, boolean canceledOntouchOut)
    {
        if (mProgressDialog == null)
        {
            mProgressDialog = LoadingDialog.createLoadingDialog(this, message);
            mProgressDialog.show();
            mProgressDialog.setCanceledOnTouchOutside(canceledOntouchOut);
        }
    }

    /**
     * 关闭Dialog
     */
    public void dismissProgressDialog()
    {
        if (mProgressDialog != null)
        {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    /**
     * @param context
     * @param oneLottery
     * @Descrition 显示投注框
     */
    public void showBetDialog(Context context, OneLottery oneLottery)
    {
        if (mBetDialog != null)
        {
            mBetDialog = null;
        }
        mBetDialog = new LotteryBetDialog(context, oneLottery);
        mBetDialog.show();
    }


    /**
     * @Descrition 关闭投注框
     */
    public void dismissBetDialog()
    {
        if (null != mBetDialog)
        {
            mBetDialog.dismiss();
        }
        mBetDialog = null;
    }

    public LotteryBetDialog getBetDialog()
    {
        return mBetDialog;
    }

    /**
     * @param context
     * @param operType
     * @param operObject
     * @Description 密码输入框显示
     */
    public void showInputPwdDialog(Context context, int operType, Object operObject)
    {
        if (null == mInputPwdDialog)
        {
            mInputPwdDialog = new InputPwdDialog(context, operType, operObject);
        }
        mInputPwdDialog.show();
    }

    /**
     * @Description 关闭进度框
     * @author sunhaitao
     */
    public void dismissInputDialog()
    {
        if (null != mInputPwdDialog)
        {
            mInputPwdDialog.dismiss();
        }
        mInputPwdDialog = null;
    }

    /**
     * @param context
     * @param operType
     * @Description 等待框显示
     */
    public void showWaitingDialog(Context context, int operType, Object object)
    {
        if (mWaitingDialog != null)
        {
            mWaitingDialog = null;
        }
        mWaitingDialog = new WaitingDialog(context, operType, object);
        mWaitingDialog.show();
    }

    /**
     * @param context
     */
    public void showWithdrawDialog(Context context)
    {
        if (mWithdrawDialog != null)
        {
            mWithdrawDialog = null;
        }
        mWithdrawDialog = new WithdrawDialog(context);
        mWithdrawDialog.show();
    }

    /**
     * @Description 关闭等待框
     */
    public void dismissWithdrawDialog()
    {
        if (null != mWithdrawDialog)
        {
            mWithdrawDialog.dismiss();
        }
        mWithdrawDialog = null;
    }

    /**
     * @Description 关闭等待框
     */
    public void dismissWaitingDialog()
    {
        if (null != mWaitingDialog)
        {
            mWaitingDialog.dismiss();
        }
        mWaitingDialog = null;
    }

    /**
     * @return
     * @Description 是否显示等待框
     */
    public WaitingDialog getWaitingDialog()
    {
        return mWaitingDialog;
    }

    /**
     * 是否现在显示体现等待框
     *
     * @return
     */
    public WithdrawDialog getWithdrawDialog()
    {
        return mWithdrawDialog;
    }

    /**
     * @param message
     * @Description 显示toast提示
     * @author zhangyang
     */
    public void showToast(String message)
    {
        if (toast != null)
        {
            toast.setText(message);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        } else
        {
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void requestPermisson(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            PermissionUtils.requestPermission(this, PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE,
                    mPermissionGrant);
        }
    }

    private PermissionUtils.PermissionGrant mPermissionGrant = new PermissionUtils.PermissionGrant()
    {
        @Override
        public void onPermissionGranted(int requestCode)
        {
            switch (requestCode)
            {
                case PermissionUtils.CODE_RECORD_AUDIO:
//                    showToast("Result Permission Grant CODE_RECORD_AUDIO");
                    break;
                case PermissionUtils.CODE_GET_ACCOUNTS:
//                    showToast("Result Permission Grant CODE_GET_ACCOUNTS");
                    break;
                case PermissionUtils.CODE_READ_PHONE_STATE:
//                    showToast("Result Permission Grant CODE_READ_PHONE_STATE");
                    break;
                case PermissionUtils.CODE_CALL_PHONE:
//                    showToast("Result Permission Grant CODE_CALL_PHONE");
                    break;
                case PermissionUtils.CODE_CAMERA:
//                    showToast("Result Permission Grant CODE_CAMERA");
                    break;
                case PermissionUtils.CODE_ACCESS_FINE_LOCATION:
//                    showToast("Result Permission Grant CODE_ACCESS_FINE_LOCATION");
                    break;
                case PermissionUtils.CODE_ACCESS_COARSE_LOCATION:
//                    showToast("Result Permission Grant CODE_ACCESS_COARSE_LOCATION");
                    break;
                case PermissionUtils.CODE_READ_EXTERNAL_STORAGE:
//                    showToast("Result Permission Grant CODE_READ_EXTERNAL_STORAGE");
                    break;
                case PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE:
//                    showToast("Result Permission Grant CODE_WRITE_EXTERNAL_STORAGE");
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        PermissionUtils.requestPermissionsResult(this, requestCode, permissions, grantResults,
                mPermissionGrant);
    }

    /**
     * 检测所有的权限是否都已授权
     *
     * @param permissions
     * @return
     */
    public boolean checkPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
