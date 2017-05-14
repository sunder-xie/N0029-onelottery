package com.peersafe.chainbet.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.utils.common.Defaultcontent;
import com.peersafe.chainbet.utils.common.ImageUtils;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.ShareBoardConfig;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/2/16
 * DESCRIPTION :
 */

public class SharePlatform
{
    private static SharePlatform instance;
    private Context context;

    private SharePlatform()
    {

    }

    public static synchronized SharePlatform getInstance()
    {
        if (instance == null)
        {
            instance = new SharePlatform();
        }
        return instance;
    }

    public void share(final Activity activity, final OneLottery lottery)
    {
        this.context = activity;
        final UMImage imageurl = new UMImage(activity, ImageUtils.getLotterySquare(lottery
                .getPictureIndex()));
        String url = Defaultcontent.getUrl(true, lottery);
        final UMWeb web = new UMWeb(url);
        web.setTitle(activity.getString(R.string.share_content_title));
        web.setDescription(activity.getString(R.string.share_contecnt_text));
        web.setThumb(imageurl);

        ShareAction mShareAction = new ShareAction(activity);

        mShareAction.setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,
                SHARE_MEDIA.LINE, SHARE_MEDIA.WHATSAPP,SHARE_MEDIA.EMAIL,SHARE_MEDIA.SMS)
                .setShareboardclickCallback(new ShareBoardlistener()
                {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media)
                    {
                        if (share_media == SHARE_MEDIA.WEIXIN || share_media == SHARE_MEDIA
                                .WEIXIN_CIRCLE)
                        {
                            new ShareAction(activity)
                                    .withMedia(web)
                                    .setPlatform(share_media)
                                    .setCallback(umShareListener).share();
                        } else if (share_media == SHARE_MEDIA.WHATSAPP || share_media == SHARE_MEDIA.SMS || share_media == SHARE_MEDIA.EMAIL)
                        {
                            new ShareAction(activity)
                                    .withText(activity.getString(R.string
                                            .share_content_titile_and_link))
                                    .setPlatform(share_media)
                                    .setCallback(umShareListener).share();
                        } else if (share_media == SHARE_MEDIA.LINE)
                        {
                            new ShareAction(activity)
                                    .withText(activity.getString(R.string
                                            .share_content_titile_and_link))
                                    .setPlatform(share_media)
                                    .setCallback(umShareListener).share();
                        }
                    }
                });

        ShareBoardConfig shareBoardConfig = new ShareBoardConfig();

        shareBoardConfig.setTitleText(OneLotteryApplication.getAppContext().getString(R.string.choice_share_platform));
        shareBoardConfig.setCancelButtonText("");
        shareBoardConfig.setIndicatorColor(Color.parseColor("#E9EFF2"), Color.parseColor
                ("#E9EFF2" + ""));
        mShareAction.open(shareBoardConfig);
    }

    private UMShareListener umShareListener = new UMShareListener()
    {
        @Override
        public void onStart(SHARE_MEDIA share_media)
        {

        }

        @Override
        public void onResult(SHARE_MEDIA platform)
        {
            if (platform == SHARE_MEDIA.WEIXIN_CIRCLE || platform == SHARE_MEDIA.QZONE)
            {
                Toast.makeText(context, context.getString(R.string.share_success), Toast
                        .LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t)
        {
            TextView title = new TextView(context);
            title.setGravity(Gravity.CENTER_HORIZONTAL);
            title.setPadding(0, (int) context.getResources().getDimension(R.dimen.y15),0,0);
            title.setText(context.getString(R.string.share_fail));
            title.setTextColor(context.getResources().getColor(R
                    .color.common_text_color));
            title.setTextSize(context.getResources().getDimension(R
                    .dimen.y20));

            new android.support.v7.app.AlertDialog.Builder(context)
                    .setCustomTitle(title)
                    .setMessage(t.getMessage().contains("2008") ? OneLotteryApplication
                            .getAppContext().getString(R.string.app_not_install) :
                            context.getString(R.string.unkown))
                    .setPositiveButton(context.getString(R.string
                            .confirm), null)
                    .show();
        }

        @Override
        public void onCancel(SHARE_MEDIA platform)
        {
        }
    };
}
