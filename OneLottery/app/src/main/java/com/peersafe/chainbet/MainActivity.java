package com.peersafe.chainbet;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.logic.UserLogic;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.friend.FriendFragment;
import com.peersafe.chainbet.ui.lottery.CreateLotteryActivity;
import com.peersafe.chainbet.ui.lottery.LotteryFragment;
import com.peersafe.chainbet.ui.recent.RecentFragment;
import com.peersafe.chainbet.ui.setting.MenuRightFragment;
import com.peersafe.chainbet.ui.setting.PersonalFragment;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.config.OLPreferenceUtil;
import com.peersafe.chainbet.utils.log.OLLogger;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.utils.update.UpdateUtil;
import com.peersafe.chainbet.widget.AlertDialog;
import com.peersafe.chainbet.widget.InputPwdDialog;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.UMShareAPI;

import java.util.List;

public class MainActivity extends BasicActivity implements View.OnClickListener
{
    private DrawerLayout mDrawerLayout;

    private static final String TAG = "mainAct";
    public static final String KEY_TAB_INDEX = "KEY_TAB_INDEX";

    private LotteryFragment mLotteryFragment;
    private RecentFragment mRecentFragment;
    private FriendFragment mFriendFragment;
    private PersonalFragment mPersonalFragment;

    /**
     * 底部四个按钮
     */
    private LinearLayout mTabBtnActivity;
    private LinearLayout mTabBtnRecent;
    private LinearLayout mTabBtnFriend;
    private LinearLayout mTabBtnPersonal;

    //创建活动按钮
    private Button mBtnCreateLottery;

    /**
     * 底部实际的四个图片
     */
    private ImageView mImageViewActivity;
    private ImageView mImageViewRecent;
    private ImageView mImageViewFriend;
    private ImageView mImageViewPersonal;

    //底部按钮的文字
    private TextView mTextTabActivity;
    private TextView mTextTabRecent;
    private TextView mTextTabFriend;
    private TextView mTextTabPersonal;

    private FragmentManager fragmentManager;
    private static final int TAB_ACTIVITY = 0;
    private static final int TAB_RECENT = 1;
    private static final int TAB_FRIEND = 2;
    public static final int TAB_PERSONAL = 3;
    private int TAB_INDEX = TAB_ACTIVITY;

    private boolean isExit;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        initEvents();

        //fir的自动更新
//        UpdateUtil.updateManager(MainActivity.this,true);

        fragmentManager = getFragmentManager();

        setTabSelection(TAB_ACTIVITY);
    }

    private void initViews()
    {
        mTabBtnActivity = (LinearLayout) findViewById(R.id.ll_tab_bottom_activity);
        mTabBtnRecent = (LinearLayout) findViewById(R.id.ll_tab_bottom_recent);
        mTabBtnFriend = (LinearLayout) findViewById(R.id.ll_tab_bottom_friend);
        mTabBtnPersonal = (LinearLayout) findViewById(R.id.ll_tab_bottom_me);

        mBtnCreateLottery = (Button) findViewById(R.id.btn_create_lottery);

        mImageViewActivity = (ImageView) findViewById(R.id.btn_tab_bottom_activity);
        mImageViewRecent = (ImageView) findViewById(R.id.btn_tab_bottom_recent);
        mImageViewFriend = (ImageView) findViewById(R.id.btn_tab_bottom_friend);
        mImageViewPersonal = (ImageView) findViewById(R.id.btn_tab_bottom_me);

        mTextTabActivity = (TextView) findViewById(R.id.tv_tab_bottom_activity);
        mTextTabRecent = (TextView) findViewById(R.id.tv_tab_bottom_recent);
        mTextTabFriend = (TextView) findViewById(R.id.tv_tab_bottom_friend);
        mTextTabPersonal = (TextView) findViewById(R.id.tv_tab_bottom_me);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.id_drawerLayout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                Gravity.RIGHT);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        mTabBtnActivity.setOnClickListener(this);
        mTabBtnRecent.setOnClickListener(this);
        mTabBtnFriend.setOnClickListener(this);
        mTabBtnPersonal.setOnClickListener(this);
        mBtnCreateLottery.setOnClickListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        boolean hasNetwork = NetworkUtil.isNetworkConnected();
        boolean hasService = OneLotteryManager.isServiceConnect;
        if (hasNetwork && hasService)
        {
            // 获取活动规则
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    OneLotteryManager.getInstance().getPrizeRules();
                }
            }).start();
        }

        if (hasNetwork && hasService)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    // 获取未关闭的活动
                    OneLotteryManager.getInstance().getLotteries(false, true);
                    // 获取已关闭的活动
                    OneLotteryManager.getInstance().getLotteries(true, true);
                    // 获取余额
                    OneLotteryManager.getInstance().getUserBalance();
                    // 获取提现列表
                    OneLotteryManager.getInstance().getWithdrawList();

                }
            }).start();
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.ll_tab_bottom_activity:
                setTabSelection(TAB_ACTIVITY);
                break;
            case R.id.ll_tab_bottom_recent:
                setTabSelection(TAB_RECENT);
                break;
            case R.id.ll_tab_bottom_friend:
                setTabSelection(TAB_FRIEND);
                break;
            case R.id.ll_tab_bottom_me:
                setTabSelection(TAB_PERSONAL);
                break;
            case R.id.btn_create_lottery:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if(!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                //是否能创建活动的判断
                if (!OneLotteryApi.isVisitor(this))
                {
                    // 不能进入创建活动页面
                    // 判断是否有创建中或未结束的活动
                    if (!StringUtils.isEmpty(OLPreferenceUtil.getInstance(MainActivity.this).getAddLotteryName()))
                    {
                        showToast(getString(R.string.create_lottery_lottery_creating));
                        break;
                    }
                    List<OneLottery> myOnGoingLottery = OneLotteryDBHelper.getInstance().
                            getMyOnGoingLottery();
                    if (null != myOnGoingLottery && myOnGoingLottery.size() > 0)
                    {
                        showToast(getString(R.string.create_lottery_can_not_lottery));
                    } else
                    {
                        Intent intent = new Intent(this, CreateLotteryActivity.class);
                        intent.putExtra(ConstantCode.CommonConstant.TYPE,
                                CreateLotteryActivity.CREAT_LOTTERY_TYPE);
                        startActivity(intent);
                        overridePendingTransition(R.anim.push_bottom_in, 0);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 根据传入的index参数来设置选中的tab页。
     */
    private void setTabSelection(int index)
    {
        TAB_INDEX = index;
        // 重置按钮
        resetBtn();

        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);

        switch (index)
        {
            case TAB_ACTIVITY:
                mImageViewActivity.setImageResource(R.drawable.tab_lottery_pressed);
                mTextTabActivity.setTextColor(getResources().getColor(R.color.app_primary_color));
                if (mLotteryFragment == null)
                {
                    mLotteryFragment = new LotteryFragment();
                    transaction.add(R.id.id_content, mLotteryFragment);
                } else
                {
                    transaction.show(mLotteryFragment);
                }
                break;
            case TAB_RECENT:
                mImageViewRecent.setImageResource(R.drawable.tab_recent_pressed);
                mTextTabRecent.setTextColor(getResources().getColor(R.color.app_primary_color));
                if (mRecentFragment == null)
                {
                    mRecentFragment = new RecentFragment();
                    transaction.add(R.id.id_content, mRecentFragment);
                } else
                {
                    transaction.show(mRecentFragment);
                }
                break;
            case TAB_FRIEND:
                mImageViewFriend.setImageResource(R.drawable.tab_friend_pressed);
                mTextTabFriend.setTextColor(getResources().getColor(R.color.app_primary_color));
                if (mFriendFragment == null)
                {
                    mFriendFragment = new FriendFragment();
                    transaction.add(R.id.id_content, mFriendFragment);
                } else
                {
                    transaction.show(mFriendFragment);
                }
                break;
            case TAB_PERSONAL:
                mImageViewPersonal.setImageResource(R.drawable.tab_me_pressed);
                mTextTabPersonal.setTextColor(getResources().getColor(R.color.app_primary_color));
                if (mPersonalFragment == null)
                {
                    mPersonalFragment = new PersonalFragment();
                    transaction.add(R.id.id_content, mPersonalFragment);
                } else
                {
                    transaction.show(mPersonalFragment);
                }
                break;
        }
        transaction.commit();
    }

    /**
     * 清除掉所有的选中状态。
     */
    private void resetBtn()
    {
        mImageViewActivity.setImageResource(R.drawable.tab_lottery_normal);
        mImageViewRecent.setImageResource(R.drawable.tab_recent_normal);
        mImageViewFriend.setImageResource(R.drawable.tab_friend_normal);
        mImageViewPersonal.setImageResource(R.drawable.tab_me_normal);

        mTextTabActivity.setTextColor(getResources().getColor(R.color.tab_bar_text_color));
        mTextTabRecent.setTextColor(getResources().getColor(R.color.tab_bar_text_color));
        mTextTabFriend.setTextColor(getResources().getColor(R.color.tab_bar_text_color));
        mTextTabPersonal.setTextColor(getResources().getColor(R.color.tab_bar_text_color));
    }

    /**
     * 将所有的Fragment都置为隐藏状态。
     *
     * @param transaction 用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction)
    {
        if (mLotteryFragment != null)
        {
            transaction.hide(mLotteryFragment);
        }
        if (mRecentFragment != null)
        {
            transaction.hide(mRecentFragment);
        }
        if (mFriendFragment != null)
        {
            transaction.hide(mFriendFragment);
        }
        if (mPersonalFragment != null)
        {
            transaction.hide(mPersonalFragment);
        }
    }


    public void hideRightMenu()
    {
        if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
        {
            mDrawerLayout.closeDrawer(Gravity.RIGHT, true);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        }
    }

    public void showRightMenu()
    {
        if (!mDrawerLayout.isDrawerVisible(Gravity.RIGHT))
        {
            mDrawerLayout.openDrawer(Gravity.RIGHT, true);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
        }
    }

    private void initEvents()
    {
        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener()
        {
            @Override
            public void onDrawerStateChanged(int newState)
            {
                if (newState == DrawerLayout.STATE_IDLE && mDrawerLayout.isDrawerOpen(Gravity
                        .RIGHT))
                {
                    OLLogger.i(TAG, "onDrawerStateChanged enter state_idle");
                    OneLotteryManager.getInstance().SendEventBus(null, OLMessageModel
                            .STMSG_MODEL_REFRSH_SETTING_RIGHT_FRAGMENT);
                }
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset)
            {
                View mContent = mDrawerLayout.getChildAt(0);
                View mMenu = drawerView;
                float scale = 1 - slideOffset;
                float rightScale = 0.8f + scale * 0.2f;

                ViewHelper.setTranslationX(mContent,
                        -mMenu.getMeasuredWidth() * slideOffset);
                ViewHelper.setPivotX(mContent, mContent.getMeasuredWidth());
                ViewHelper.setPivotY(mContent,
                        mContent.getMeasuredHeight() / 2);
                mContent.invalidate();
                ViewHelper.setScaleX(mContent, rightScale);
                ViewHelper.setScaleY(mContent, rightScale);
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
            }

            @Override
            public void onDrawerClosed(View drawerView)
            {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
            {
                mDrawerLayout.closeDrawer(Gravity.RIGHT, true);
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity
                        .RIGHT);
            } else
            {
                exit();
            }
            return false;
        } else
        {
            return super.onKeyUp(keyCode, event);
        }
    }

    public void exit()
    {
        if (!isExit)
        {
            isExit = true;
            showToast(getString(R.string.exit_chain_bet_again));
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);

            //保存统计数据
            MobclickAgent.onKillProcess(this);
            System.exit(0);
        }
    }

    Handler mHandler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message message)
        {
            isExit = false;
            return true;
        }
    });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode)
            {
                case MenuRightFragment.DELETE_ACCOUNT_FLAG:
                    Bundle bundle = data.getBundleExtra(AlertDialog.BUNDLE_EXTRA);
                    if (bundle != null && !StringUtils.isEmpty(bundle.getString(ConstantCode.CommonConstant.USER_ID)))
                    {
//                        List<String> userList = UserInfoDBHelper.getInstance().getUserList(false);

                        InputPwdDialog dialog = new InputPwdDialog(MainActivity.this, InputPwdDialog
                                .DELETE_ACCOUNT, bundle.getString(ConstantCode.CommonConstant.USER_ID));
                        dialog.show();

                    }
                    break;
                case PersonalFragment.EXIT_FLAG:

                    List<String> userList = UserInfoDBHelper.getInstance().getUserList(true);

                    if (!UserLogic.isTourist())
                    {
                        String curUserId = OneLotteryApi.getCurUserId();
                        UserInfo user = UserInfoDBHelper.getInstance().getUserByUserId(curUserId);
                        user.setIsCurUser(false);
                        UserInfoDBHelper.getInstance().insertUserInfo(user);

                        if (userList.isEmpty())
                        {
                            Intent register = new Intent(MainActivity.this, RegisterActivity.class);
                            register.putExtra(ConstantCode.CommonConstant.TYPE, false);
                            startActivity(register);
                        } else
                        {
                            Intent login = new Intent(MainActivity.this, LoginActivity.class);
                            login.putExtra(ConstantCode.CommonConstant.TYPE, false);
                            startActivity(login);
                        }

                        finish();
                    }
                    else
                    {
                        if (userList.isEmpty())
                        {
                            Intent register = new Intent(MainActivity.this, RegisterActivity.class);
                            register.putExtra(ConstantCode.CommonConstant.TYPE, true);
                            startActivity(register);
                        } else
                        {
                            Intent login = new Intent(MainActivity.this, LoginActivity.class);
                            login.putExtra(ConstantCode.CommonConstant.TYPE, true);
                            startActivity(login);
                        }
                    }
                    break;
            }
        }
        //友盟QQ分享精简版
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    //返回当前的选项
    public int getCurrentFragment()
    {
        return TAB_INDEX;
    }
}
