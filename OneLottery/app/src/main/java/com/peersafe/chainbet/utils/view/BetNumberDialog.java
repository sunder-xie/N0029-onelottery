package com.peersafe.chainbet.utils.view;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;

import java.util.ArrayList;
import java.util.List;

/**
 * AUTHOR : sunhaitao.
 * DATA : 16/12/14
 * DESCRIPTION :
 */

public class BetNumberDialog extends Dialog
{
    // 显示列表的数据
    private List<String> list;

    private Context mContext;
    private static BetNumberDialog instance;

    public BetNumberDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    //在构造方法里预加载我们的样式，这样就不用每次创建都指定样式了
    public BetNumberDialog(Context context, List<String> list)
    {
        this(context, R.style.StyleLotteryBetDialog);
        this.list = list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_pop_menu);

        // 预先设置Dialog的一些属性
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        Display display = dialogWindow.getWindowManager().getDefaultDisplay();
        p.gravity = Gravity.BOTTOM;
        p.width = display.getWidth();
        dialogWindow.setAttributes(p);

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        ListView listView = (ListView) findViewById(R.id.lottery_detail_check_list);

        //设置数据
        final ArrayList<String> alist = new ArrayList<>();
        for (int i = 0; i < list.size(); i++)
        {
            alist.add(list.get(i));
        }

        ArrayAdapter adapter = new ArrayAdapter(OneLotteryApplication.getAppContext(),
                R.layout.lottery_detail_check_all_item,alist);

        listView.setAdapter(adapter);
    }
}
