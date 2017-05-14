/**
 * Copyright (C) PeerSafe 2015 Technologies. All rights reserved.
 *
 * @Name: BasicFragment.java
 * @Package: com.peersafe.shadowtalk.ui
 * @Description: Fragment基类，应用的其他fragment继承该类。该类实现公共方法
 * @author zhangyang
 * @date 2015年6月19日 下午5:37:41
 */

package com.peersafe.chainbet.ui;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.ui.lottery.LotteryBetDialog;
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

public abstract class BasicFragment extends Fragment
{
    // 用于做进度框显示
    private Dialog mProgressDialog;
    private Toast toast = null;

    //密码输入框
    private InputPwdDialog mInputPwdDialog = null;

    //等待框显示
    private WaitingDialog mWaitingDialog = null;

    //投注框显示
    private LotteryBetDialog mBetDialog = null;

    /**
     * Description
     *
     * @param savedInstanceState
     * @see android.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    /**
     * Description
     *
     * @see android.app.Fragment#onResume()
     */
    @Override
    public void onResume()
    {
        super.onResume();

        MobclickAgent.onResume(getActivity());
    }

    /**
     * Description
     *
     * @see android.app.Fragment#onPause()
     */
    @Override
    public void onPause()
    {
        super.onPause();

        MobclickAgent.onPause(getActivity());
    }

    protected void hideSoftKeyboard()
    {
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams
                .SOFT_INPUT_STATE_HIDDEN)
        {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (getActivity().getCurrentFocus() != null)
            {
                inputMethodManager.hideSoftInputFromWindow(getActivity()
                                .getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mBetDialog = null;
        mInputPwdDialog = null;
        mProgressDialog = null;
        mWaitingDialog = null;
    }

//    /**
//     * @param message
//     * @param canceledOntouchOut
//     * @Description 显示进度框
//     * @author zhangyang
//     */
//    protected void showProgressDialog(String message, boolean canceledOntouchOut)
//    {
//        if (null == mProgressDialog)
//        {
//            mProgressDialog = new LoadingDialog(getActivity());
//        }
//
//        mProgressDialog.setMessage(message);
//        mProgressDialog.setCanceledOnTouchOutside(canceledOntouchOut);
//        mProgressDialog.show();
//    }
//
//    /**
//     * @Description 关闭进度框
//     * @author zhangyang
//     */
//    protected void dismissProgressDialog()
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
            mProgressDialog = LoadingDialog.createLoadingDialog(getActivity(), message);
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
        if (null == mBetDialog)
        {
            mBetDialog = new LotteryBetDialog(context, oneLottery);
        }
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
    public void showWaitingDialog(Context context, int operType,Object object)
    {
        if (null == mWaitingDialog)
        {
            mWaitingDialog = new WaitingDialog(context, operType,object);
        }
        mWaitingDialog.show();
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
            toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //刷新
    public void onRefresh(){};

    public void requestPermisson(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            PermissionUtils.requestPermission(getActivity(), PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE,
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
        PermissionUtils.requestPermissionsResult(getActivity(), requestCode, permissions, grantResults,
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
            if (ContextCompat.checkSelfPermission(getActivity(), permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
