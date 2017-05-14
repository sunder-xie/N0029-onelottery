package com.peersafe.chainbet.utils.common;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import com.peersafe.chainbet.R;

/**
 * AUTHOR : sunhaitao.
 * DATA : 16/12/15
 * DESCRIPTION :
 */

public class ImageUtils
{
    public static Integer[] images = new Integer[]{
            R.drawable.lottery_icon_1,
            R.drawable.lottery_icon_2,
            R.drawable.lottery_icon_3,
            R.drawable.lottery_icon_4,
            R.drawable.lottery_icon_5,
            R.drawable.lottery_icon_6,
            R.drawable.lottery_icon_7,
            R.drawable.lottery_icon_8,
            R.drawable.lottery_icon_9};

    public static Integer[] head_images = new Integer[]{
            R.drawable.lottery_header_icon_1,
            R.drawable.lottery_header_icon_2,
            R.drawable.lottery_header_icon_3,
            R.drawable.lottery_header_icon_4,
            R.drawable.lottery_header_icon_5,
            R.drawable.lottery_header_icon_6,
            R.drawable.lottery_header_icon_7,
            R.drawable.lottery_header_icon_8,
            R.drawable.lottery_header_icon_9};

    public static Integer[] square_images = new Integer[]{
            R.drawable.lottery_square_icon1,
            R.drawable.lottery_square_icon2,
            R.drawable.lottery_square_icon3,
            R.drawable.lottery_square_icon4,
            R.drawable.lottery_square_icon5,
            R.drawable.lottery_square_icon6,
            R.drawable.lottery_square_icon7,
            R.drawable.lottery_square_icon8,
            R.drawable.lottery_square_icon9};

    public static Integer getLotterySquare(int pos)
    {
        if (pos <= 0 || pos > square_images.length)
        {
            return square_images[0];
        }
        return square_images[pos - 1];
    }

    public static Integer getLotteryImageview(int pos)
    {
        if (pos <= 0 || pos > images.length)
        {
            return images[0];
        }
        return images[pos - 1];
    }

    public static Integer getLotterHeaderImg(int pos)
    {
        if (pos <= 0 || pos > head_images.length)
        {
            return head_images[0];
        }
        return head_images[pos - 1];
    }


    /**
     * @param res
     * @param resId
     * @param inSampleSize
     * @return
     * @Description 根据缩放比例获取资源对应的Bitmap
     * @author zhangyang
     */
    public static Bitmap decodeBitmapFromResourseInSampleSize(Resources res,
                                                              int resId, int inSampleSize)
    {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        return BitmapFactory.decodeResource(res, resId, options);
    }


    /**
     * 根据viewID设置图片bitmap
     * @param activity
     * @param bitmap
     * @param viewId
     * @param drawableId
     */
    public static void setBitmap(Activity activity, Bitmap bitmap, int viewId, int drawableId)
    {
        if (null == bitmap || bitmap.isRecycled())
        {
            bitmap = ImageUtils.decodeBitmapFromResourseInSampleSize(activity.getResources(), drawableId, 1);
        }

        if (null != bitmap)
        {
            activity.findViewById(viewId).setBackgroundDrawable(new BitmapDrawable(activity.getResources(), bitmap));
        }
    }
}
