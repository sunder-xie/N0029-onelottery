package com.peersafe.chainbet.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.widget
 * @description:
 * @date 28/12/16 PM6:29
 */
public class KeyBackCancelEditText extends EditText
{

    public KeyBackCancelEditText(Context context)
    {
        super(context);
    }

    public KeyBackCancelEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event)
    {
        if (mOnCancelDialogImp != null)
        {
            mOnCancelDialogImp.onCancelDialog();
        }
        return super.dispatchKeyEventPreIme(event);
    }

    private OnCancelDialogImp mOnCancelDialogImp;

    public void setOnCancelDialogImp(OnCancelDialogImp onCancelDialogImp)
    {
        mOnCancelDialogImp = onCancelDialogImp;
    }

    public interface OnCancelDialogImp
    {
        void onCancelDialog();
    }
}
