package com.peersafe.chainbet.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;

import com.github.jdsjlzx.view.ArrowRefreshHeader;
import com.peersafe.chainbet.R;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.widget
 * @description:
 * @date 20/12/16 PM6:34
 */
public class OLRefreshHeader extends ArrowRefreshHeader
{
    private ImageView mRefreshImageView;
    private AnimationDrawable mAnimationDrawable;

    public OLRefreshHeader(Context context)
    {
        super(context);

        mRefreshImageView = (ImageView) findViewById(R.id.iv_refreshing);
    }

    @Override
    public void setState(int state)
    {
        if (state == mState)
        {
            return;
        }

        if (state == STATE_REFRESHING)
        {
            smoothScrollTo(mMeasuredHeight);
        }
        
//        if (null == mAnimationDrawable)
//        {
//            mAnimationDrawable = (AnimationDrawable) mRefreshImageView.getDrawable();
//            mAnimationDrawable.start();
//        }

        switch (state)
        {
            case STATE_NORMAL:
                break;
            case STATE_RELEASE_TO_REFRESH:
//                if (mState != STATE_RELEASE_TO_REFRESH)
//                {
//                }
                break;
            case STATE_REFRESHING:
                if (null == mAnimationDrawable)
                {
                    mRefreshImageView.setImageResource(R.drawable.refresh_header_refreshing_anim);
                    mAnimationDrawable = (AnimationDrawable)mRefreshImageView.getDrawable();
                    mAnimationDrawable.start();
                }
                break;
            case STATE_DONE:
                if (null != mAnimationDrawable)
                {
                    ObjectAnimator.ofFloat(mRefreshImageView,"scaleX",1f,0f,1f).setDuration(650).start();
                    ObjectAnimator.ofFloat(mRefreshImageView,"scaleY",1f,0f,1f).setDuration(650).start();
                    mAnimationDrawable.stop();
                    mAnimationDrawable = null;
                }
                break;
            default:
        }

        mState = state;
    }

    @Override
    public void onMove(float delta) {
        if(getVisibleHeight() > 0 || delta > 0)
        {
            setVisibleHeight((int) delta + getVisibleHeight());
            if (mState <= STATE_RELEASE_TO_REFRESH) {
                if (getVisibleHeight() > mMeasuredHeight)
                {
                    setState(STATE_RELEASE_TO_REFRESH);
                }
                else
                {
                    if(getVisibleHeight() > 0 && getVisibleHeight() <= 13)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon1);
                    }
                    else if(getVisibleHeight() > 13 && getVisibleHeight() <= 26)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon2);
                    }
                    else if(getVisibleHeight() > 26 && getVisibleHeight() <= 39)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon4);
                    }
                    else if(getVisibleHeight() > 39 && getVisibleHeight() <= 52)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon5);
                    }
                    else if(getVisibleHeight() > 52 && getVisibleHeight() <= 65)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon6);
                    }
                    else if(getVisibleHeight() > 65 && getVisibleHeight() <= 78)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon7);
                    }
                    else if(getVisibleHeight() > 78 && getVisibleHeight() <= 91)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon8);
                    }
                    else if(getVisibleHeight() > 91 && getVisibleHeight() <= 104)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon9);
                    }
                    else if(getVisibleHeight() > 104 && getVisibleHeight() <= 117)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon10);
                    }
                    else if(getVisibleHeight() > 117 && getVisibleHeight() <= 130)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon11);
                    }
                    else if(getVisibleHeight() > 130 && getVisibleHeight() <= 143)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon12);
                    }
                    else if(getVisibleHeight() > 143 && getVisibleHeight() <= 156)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon13);
                    }
                    else if(getVisibleHeight() > 156 && getVisibleHeight() <= 169)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon14);
                    }
                    else if(getVisibleHeight() > 169 && getVisibleHeight() <= 182)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon15);
                    }
                    else if(getVisibleHeight() > 182 && getVisibleHeight() <= 195)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon16);
                    }
                    else if(getVisibleHeight() > 195 && getVisibleHeight() <= 208)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon17);
                    }
                    else if(getVisibleHeight() > 208 && getVisibleHeight() <= 221)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon18);
                    }
                    else if(getVisibleHeight() > 221)
                    {
                        mRefreshImageView.setImageResource(R.drawable.refresh_header_icon19);
                    }
                    setState(STATE_NORMAL);
                }
            }
        }
    }
}
