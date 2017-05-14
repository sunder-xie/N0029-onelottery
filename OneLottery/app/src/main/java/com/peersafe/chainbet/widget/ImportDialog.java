package com.peersafe.chainbet.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.peersafe.chainbet.MainActivity;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.RegisterActivity;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.setting.ImportAmoutActivity;
import com.peersafe.chainbet.ui.setting.WifiBackRestoreActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.log.OLLogger;

import java.io.File;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/4/18
 * DESCRIPTION :
 */

public class ImportDialog extends Dialog implements View.OnClickListener, DialogInterface
        .OnCancelListener
{
    private static final String TAG = ImportDialog.class.getSimpleName();
    private static final int IMPORT_FILE_FLAG = 2;
    public final static int EXPORT_FILE = 1;
    public final static int IMPORT_FILE = 3;

    private BasicActivity mContext;

    private Button mWifi, mSd, mCancel;

    private int mCurType;

    public final static String ENTER_MAIN = "main";
    public final static String CALL_IMPORT = "call";

    private String mCurObject;

    public ImportDialog(Context context, int type, String object)
    {
        this(context, R.style.StyleLotteryBetDialog);
        mContext = (BasicActivity) context;
        mCurType = type;
        mCurObject = object;
    }

    public ImportDialog(Context context, int themeResId)
    {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_and_export_dialog);

        // 预先设置Dialog的一些属性
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        Display display = dialogWindow.getWindowManager().getDefaultDisplay();
        p.gravity = Gravity.BOTTOM;
        p.width = display.getWidth();
        dialogWindow.setAttributes(p);

        mWifi = (Button) findViewById(R.id.wifi);
        mSd = (Button) findViewById(R.id.sd);
        mCancel = (Button) findViewById(R.id.cancel);

        mWifi.setOnClickListener(this);
        mSd.setOnClickListener(this);
        mCancel.setOnClickListener(this);

        if (mCurObject.equals(ENTER_MAIN) && mCurType == EXPORT_FILE)
        {
            this.setOnCancelListener(this);
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.wifi:
                Intent intent_import = new Intent(mContext, WifiBackRestoreActivity.class);
                intent_import.putExtra(WifiBackRestoreActivity.KEY_IS_IMPORT, mCurType ==
                        IMPORT_FILE);
                if (mCurObject.equals(ENTER_MAIN))
                {
                    intent_import.putExtra(WifiBackRestoreActivity.KEY_ENTER_MAIN, true);
                    mContext.startActivity(intent_import);
                    RegisterActivity.instance.finish();
                } else if (mCurObject.equals(CALL_IMPORT))
                {
                    intent_import.putExtra(WifiBackRestoreActivity.KEY_CALL_IMPORT, true);
                    mContext.startActivityForResult(intent_import, IMPORT_FILE_FLAG);
                } else
                {
                    mContext.startActivity(intent_import);
                }
                dismiss();
                break;

            case R.id.sd:
                InputPwdDialog dialog = null;
                if (mCurType == IMPORT_FILE)
                {
                    //判断是否含有sd中的账号
                    File file = new File(ConstantCode.CommonConstant.EXTER_PATH);
                    boolean isEmpty = true;
                    if (file.exists() && file.listFiles().length != 0)
                    {
                        isEmpty = false;
                    }

                    if (!file.exists())
                    {
                        File file1 = new File(ConstantCode.CommonConstant.INNER_PATH);
                        if (file1.exists() && file1.listFiles().length != 0)
                        {
                            isEmpty = false;
                        }
                    }

                    if (isEmpty)
                    {
                        Toast.makeText(mContext, mContext.getString(R.string
                                .setting_import_wallent_fail), Toast.LENGTH_SHORT).show();
                        dismiss();
                        break;
                    }

                    Intent imp = new Intent(mContext, ImportAmoutActivity.class);
                    mContext.startActivity(imp);
                } else if (mCurType == EXPORT_FILE)
                {
                    dialog = new InputPwdDialog(mContext, InputPwdDialog.EXPORT_SD, null);
                    dialog.show();
                }
                dismiss();
                break;

            case R.id.cancel:
                if (mCurObject.equals(ENTER_MAIN) && mCurType == EXPORT_FILE)
                {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    mContext.startActivity(intent);
                    RegisterActivity.instance.finish();
                    break;
                }

                dismiss();
                break;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog)
    {
        Intent intent = new Intent(mContext, MainActivity.class);
        mContext.startActivity(intent);
        RegisterActivity.instance.finish();
    }
}
