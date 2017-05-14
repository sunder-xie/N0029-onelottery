package com.peersafe.chainbet.utils.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.utils.common.ImageUtils;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/1/3
 * DESCRIPTION : 选择创建活动图片标签
 */

public class LotteryIconDialog extends Dialog implements AdapterView.OnItemClickListener {
    private GridView gridView;
    private Context mContext;

    int width;
    int height;

    //在构造方法里预加载我们的样式，这样就不用每次创建都指定样式了
    public LotteryIconDialog(Context context)

    {
        this(context, R.style.StyleLotteryBetDialog);
    }

    public LotteryIconDialog(Context context, int themeResId)
    {
        super(context, themeResId);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lottery_icon_layout);

        // 预先设置Dialog的一些属性
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        Display display = dialogWindow.getWindowManager().getDefaultDisplay();
        p.gravity = Gravity.BOTTOM;
        p.width = display.getWidth();
        dialogWindow.setAttributes(p);

        IconAdapter adapter = new IconAdapter();
        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        OneLotteryManager.getInstance().SendEventBus(1 + i, OLMessageModel.STMSG_MODEL_CREATE_LOTTERY_SELECT_ICON);
        dismiss();
    }

    class IconAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return ImageUtils.images.length;
        }

        @Override
        public Object getItem(int i)
        {
            return ImageUtils.images[i];
        }

        @Override
        public long getItemId(int i)
        {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.lottery_icon_gridview_item,null);

            ImageView imageView = (ImageView) view.findViewById(R.id.grid_view_item);

            Glide.with(mContext).load(ImageUtils.images[i]).asBitmap().override(187,128).into(imageView);

            return view;
        }
    }
}
