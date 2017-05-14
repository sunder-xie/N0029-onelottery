package com.peersafe.chainbet.widget;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.peersafe.chainbet.R;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.utils.common.PermissionUtils;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;

public class AlertDialog extends BasicActivity
{
    public final static String TIP_MESSAGE = "msg";
    public final static String TIP_TITLE = "title";
    public final static String SHOW_CANCEL_BTN = "show_cancel_btn";
    public final static String SHOW_CONFIRM_BTN = "show_confirm_btn";
    public final static String SHOW_SKIP_BTN = "show_skip_btn";
    public final static String SHOW_EMPORT_BTN = "show_emport_btn";
    public final static String SKIP_BUTTON_TYPE = "skip_button_type";
    public final static String BUNDLE_EXTRA = "bundle_extra";

    private TextView mTitle;
    private TextView mTipMessage;

    private String tipMessage;
    private String title;
    private boolean showCancel;
    private boolean showConfirm;
    private boolean showSkip;
    private boolean showEmport;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_dialog);
        mTitle = (TextView) findViewById(R.id.alert_title);
        mTipMessage = (TextView) findViewById(R.id.alert_message);

        tipMessage = getIntent().getStringExtra(TIP_MESSAGE);
        title = getIntent().getStringExtra(TIP_TITLE);
        showCancel = getIntent().getBooleanExtra(SHOW_CANCEL_BTN, false);
        showConfirm = getIntent().getBooleanExtra(SHOW_CONFIRM_BTN, false);
        showSkip = getIntent().getBooleanExtra(SHOW_SKIP_BTN, false);
        showEmport = getIntent().getBooleanExtra(SHOW_EMPORT_BTN, false);
        bundle = getIntent().getBundleExtra(BUNDLE_EXTRA);

        if (StringUtils.isEmpty(title))
        {
            mTitle.setVisibility(View.GONE);
        } else
        {
            mTitle.setText(title);
        }
        mTipMessage.setText(tipMessage);

        if (showCancel)
        {
            findViewById(R.id.alert_cancel).setVisibility(View.VISIBLE);
        }
        if (showConfirm)
        {
            findViewById(R.id.alert_ok).setVisibility(View.VISIBLE);
        }
        if (showSkip)
        {
            findViewById(R.id.alert_skip).setVisibility(View.VISIBLE);
        }
        if (showEmport)
        {
            findViewById(R.id.alert_emport).setVisibility(View.VISIBLE);
        }
    }

    public void ok(View view)
    {
        Intent intent = new Intent();
        intent.putExtra(BUNDLE_EXTRA, bundle);
        setResult(RESULT_OK, intent);
        hideSoftKeyboard();
        finish();
    }

    public void cancel(View view)
    {
        hideSoftKeyboard();
        finish();
    }

    public void skip(View view)
    {
        Intent intent = new Intent();
        intent.putExtra(SKIP_BUTTON_TYPE, SKIP_BUTTON_TYPE);
        setResult(RESULT_OK, intent);
        hideSoftKeyboard();
        finish();
    }

    public void emport(View view)
    {
        boolean result = checkPermissions(new String[]{PermissionUtils.PERMISSION_WRITE_EXTERNAL_STORAGE, PermissionUtils.PERMISSION_READ_EXTERNAL_STORAGE});
        if (result)
        {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            hideSoftKeyboard();
            finish();
        } else
        {
            requestPermisson(this);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (showSkip)
            {
                Intent intent = new Intent();
                intent.putExtra(SKIP_BUTTON_TYPE, SKIP_BUTTON_TYPE);
                setResult(RESULT_OK, intent);
                hideSoftKeyboard();
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
