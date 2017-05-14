package com.peersafe.chainbet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peersafe.chainbet.LoginActivity;
import com.peersafe.chainbet.MainActivity;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.RegisterActivity;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.ui.setting.AboutUsActivity;
import com.peersafe.chainbet.ui.setting.PersonalFragment;
import com.peersafe.chainbet.utils.config.OLPreferenceUtil;

import java.util.List;

/**
 * 引导页面
 * Created by moying on 15/8/4.
 */
public class GuideActivity extends BasicActivity implements ViewPager.OnPageChangeListener
{
    public static final String KEY_FROM = "from";

    private LayoutInflater mInflater;

    private ViewPager viewPager;

    private PicAdapter vpAdapter;

    private ImageView[] points;

    public int[] bgs = {R.drawable.guide_bg_1, R.drawable.guide_bg_2, R.drawable.guide_bg_3};

    public int[] pics = {R.drawable.guide_top_1, R.drawable.guide_top_2, R.drawable.guide_top_3};

    public int[] titles = {R.string.guide_title1, R.string.guide_title2, R.string.guide_title3};

    public int[] contents = {R.string.guide_content1, R.string.guide_content2, R.string.guide_content3};

    private int currentIndex = 1;

    private String from;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_guide_layout);

        mInflater = LayoutInflater.from(GuideActivity.this);

        if (getIntent() != null)
        {
            from = getIntent().getStringExtra(KEY_FROM);
        }

        initView();

        initData();
    }

    private void initView()
    {
        viewPager = (ViewPager) findViewById(R.id.vp_guide);

        vpAdapter = new PicAdapter();
    }

    private void initData()
    {
        viewPager.setAdapter(vpAdapter);

        viewPager.addOnPageChangeListener(this);

        initPoint();

        setCurDot(0);
    }

    private void initPoint()
    {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ly_point);

        points = new ImageView[pics.length];

        for (int i = 0; i < pics.length; i++)
        {
            points[i] = (ImageView) linearLayout.getChildAt(i);
            points[i].setEnabled(false);
//          points[i].setOnClickListener(this);
            points[i].setTag(i);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            setResult(RESULT_OK);

            if (!PersonalFragment.class.getSimpleName().equals(from))
            {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setCurView(int position)
    {
        if (position < 0 || position >= pics.length)
        {
            return;
        }
        viewPager.setCurrentItem(position);
    }

    private void setCurDot(int positon)
    {
        if (positon < 0 || positon > pics.length - 1 || currentIndex == positon)
        {
            return;
        }
        points[positon].setEnabled(true);
        points[currentIndex].setEnabled(false);

        currentIndex = positon;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {
    }

    @Override
    public void onPageSelected(int position)
    {
        setCurDot(position);
    }

    @Override
    public void onPageScrollStateChanged(int state)
    {
    }

    class PicAdapter extends PagerAdapter
    {
        @Override
        public int getCount()
        {
            return pics.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object)
        {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            if (pics.length < position && position < 100)
            {
                return null;
            }

            View v = mInflater.inflate(R.layout.activity_guide_item, null);

            ImageView iv_bg = (ImageView) v.findViewById(R.id.iv_guide_bg);
            iv_bg.setImageResource(bgs[position]);

            ImageView iv = (ImageView) v.findViewById(R.id.iv_guide_top);
            iv.setImageResource(pics[position]);

            TextView title = (TextView) v.findViewById(R.id.tv_guide_title);
            title.setText(titles[position]);
            TextView content = (TextView) v.findViewById(R.id.tv_guide_content);
            content.setText(contents[position]);

            final Button goMain = (Button) v.findViewById(R.id.btn_go);
            goMain.setVisibility(position == pics.length - 1 ? View.VISIBLE : View.GONE);
            goMain.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    gotoMoin();
                }
            });

            LinearLayout ly_protocal = (LinearLayout) v.findViewById(R.id.ly_protocal);
            ly_protocal.setVisibility(position == pics.length - 1 ? View.VISIBLE : View.GONE);
            TextView protocal = (TextView) v.findViewById(R.id.tv_protocl_content);
//            protocal.setOnClickListener(new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    Intent argment = new Intent(GuideActivity.this, AboutUsActivity.class);
//                    argment.putExtra("argment",true);
//                    startActivity(argment);
//                }
//            });

            ((ViewPager) container).addView(v, 0);

            return v;
        }

        @Override
        public void destroyItem(View container, int position, Object object)
        {
            ((ViewPager) container).removeView((View) object);
        }

    }

    private void gotoMoin()
    {
        OLPreferenceUtil.getInstance(getApplicationContext()).setWelcomeShowFlag(false);

        if (PersonalFragment.class.getSimpleName().equals(from))
        {
            finish();
            return;
        }

        // 判断是否有主账户
        UserInfo curPrimaryAccount = UserInfoDBHelper.getInstance().
                getCurPrimaryAccount();

        if (null != curPrimaryAccount)
        {
            // 没必要   （和Launcherscreen一样？）
            OneLotteryApi.setCurUserId(curPrimaryAccount.getUserId());

            // 进入主界面
            Intent intent = new Intent(GuideActivity.this, MainActivity.class);
            startActivity(intent);
        } else
        {
            List<String> userList = UserInfoDBHelper.getInstance().getUserList(true);
            if (userList.isEmpty())
            {
                // 进入注册界面
                Intent register = new Intent(GuideActivity.this, RegisterActivity.class);
                startActivity(register);
            } else
            {
                // 进入登录界面
                Intent login = new Intent(GuideActivity.this, LoginActivity.class);
                startActivity(login);
            }
        }
        finish();
    }

}
