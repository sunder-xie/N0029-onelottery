package com.peersafe.chainbet.ui.recent;

import android.support.v4.app.Fragment;
import android.widget.Toast;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/2/18
 * DESCRIPTION :
 */

public abstract class LazyFragment extends Fragment
{
    private Toast toast = null;
    protected boolean isVisible;
    /**
     * 在这里实现Fragment数据的缓加载.
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }

    protected void onVisible(){
        lazyLoad();
    }

    protected abstract void lazyLoad();

    protected void onInvisible(){}

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

}
