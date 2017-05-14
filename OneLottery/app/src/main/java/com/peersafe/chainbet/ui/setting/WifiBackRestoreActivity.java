package com.peersafe.chainbet.ui.setting;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peersafe.chainbet.MainActivity;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.logic.wifibackrestore.WifiBackRestoreServerRunner;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.utils.common.CommonUtils;
import com.peersafe.chainbet.utils.common.PermissionUtils;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.netstate.NetChangeObserver;
import com.peersafe.chainbet.utils.netstate.NetworkStateReceiver;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Random;

public class WifiBackRestoreActivity extends BasicActivity implements
        View.OnClickListener, NetChangeObserver
{
    private Handler handler;
    private boolean wifiIsAvailable = false;
    private String wifiIpAddr;
    private View mWifiConnectedView;
    private LinearLayout mLyWifiLogo;

    private Button mBtnWifiBackupAddress;
    private TextView mBackRestoreTip1;
    private TextView mBackRestoreTip2;
    private TextView mWifiConnectName;
    // 无网络情况
    private LinearLayout mLyNoData;
    private ImageView mIvNoData;
    private TextView mTvNoData;

    private int mWifiPort = 0;

    public final static String KEY_ENTER_MAIN = "key_enter_main";
    public final static String KEY_CALL_IMPORT = "key_call_import";
    public final static String KEY_IMPORT_RSULT = "key_import_result";
    public final static String KEY_IS_IMPORT = "key_is_import";

    private boolean bEnterMain;
    private boolean bCallImport;
    private boolean bIsImport;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_back_restore);

        EventBus.getDefault().register(this);

        if (getIntent() != null)
        {
            bEnterMain = getIntent().getBooleanExtra(KEY_ENTER_MAIN, false);
            bCallImport = getIntent().getBooleanExtra(KEY_CALL_IMPORT, false);
            bIsImport = getIntent().getBooleanExtra(KEY_IS_IMPORT, false);
        }

        initView();
        initData();
    }

    private void initView()
    {
        mLyWifiLogo = (LinearLayout) findViewById(R.id.ly_wifi_logo);
        mBtnWifiBackupAddress = (Button) findViewById(R.id.btn_wifibackup_address);
        mWifiConnectedView = findViewById(R.id.rl_wifi_connected);
        mBackRestoreTip1 = (TextView) findViewById(R.id.tv_wifi_backrestore_tip1);
        mBackRestoreTip2 = (TextView) findViewById(R.id.tv_wifi_backrestore_tip2);
        mWifiConnectName = (TextView) findViewById(R.id.tv_wifi_name);
        mLyNoData = (LinearLayout) findViewById(R.id.ly_empty);
        mTvNoData = (TextView) findViewById(R.id.tv_empty);
        mIvNoData = (ImageView) findViewById(R.id.iv_empty);
        findViewById(R.id.btn_back).setOnClickListener(this);
        mBtnWifiBackupAddress.setOnClickListener(this);
    }

    private void initData()
    {
        handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                // get the ip address successfully, start the server
                Bundle bundle = msg.getData();
                wifiIpAddr = bundle.getString("ip");

                mLyWifiLogo.setVisibility(View.VISIBLE);
                mBackRestoreTip1.setText(getString(R.string
                        .setting_wifi_backrestore_enter_address));
                mBackRestoreTip2.setText(getString(R.string.setting_wifi_backrestore_not_leave));

                WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                mBtnWifiBackupAddress.setText(getBrowserAddr());
                mWifiConnectName.setText(String.format(getString(R.string
                        .setting_wifi_connect_name), wifiInfo.getSSID()));

                // start the server
                WifiBackRestoreServerRunner.startServer(mWifiPort, bIsImport, WifiBackRestoreActivity.this);
            }
        };

        checkWifiState();

        NetworkStateReceiver.registerObserver(this);

    }


    @Override
    protected void onStop()
    {
        if (bEnterMain)
        {
            // 进入主界面
            Intent intent = new Intent(WifiBackRestoreActivity.this, MainActivity.class);
            startActivity(intent);
            bEnterMain = false;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        NetworkStateReceiver.removeRegisterObserver(this);
        WifiBackRestoreServerRunner.stopServer();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(final OLMessageModel model)
    {
        if (model == null)
        {
            return;
        }
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_IMPORT_WALLET_OK:
                String name = (String) model.getEventObject();
                if (!StringUtils.isEmpty(name))
                {
                    UserInfo userInfo = new UserInfo();
                    userInfo.setUserId(name);
                    userInfo.setWalletAddr(OneLotteryApi.getPubkeyHash(name));
                    userInfo.setBalance(0l);  //设置假数据

                    UserInfoDBHelper.getInstance().setCurPrimaryAccount(userInfo);

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
                            }
                        }).start();
                    }

                    OneLotteryApi.setCurUserId(name);
                    if (bCallImport)
                    {
                        Intent intent = new Intent();
                        intent.putExtra(KEY_IMPORT_RSULT, true);
                        setResult(RESULT_OK, intent);
                    }
                    finish();
                }
                break;
            case OLMessageModel.STMSG_MODEL_IMPORT_WALLET_ERR:
                String errMsg = (String) model.getEventObject();
                showToast(errMsg);
                break;
            default:
                break;
        }
    }

    private void checkWifiState()
    {
        if (!NetworkUtil.isNetworkConnected())
        {
            mWifiConnectedView.setVisibility(View.GONE);
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_network);
            mTvNoData.setText(R.string.no_network);
            return;
        }

        if (!OneLotteryManager.isServiceConnect)
        {
            mWifiConnectedView.setVisibility(View.GONE);
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_service);
            mTvNoData.setText(R.string.no_service);
            return;
        }

        mWifiConnectedView.setVisibility(View.VISIBLE);
        mLyNoData.setVisibility(View.GONE);

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        wifiIsAvailable = CommonUtils.isWifiConnected(wifiManager, wifiInfo);
        if (wifiIsAvailable)
        {
            // wifi is available, get the ip address in a new thread
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Bundle bundle = new Bundle();
                    bundle.putString("ip",
                            CommonUtils.int2Ip(wifiInfo.getIpAddress()));
                    Message msg = new Message();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }).start();
        } else
        {
            mLyWifiLogo.setVisibility(View.INVISIBLE);
            mBackRestoreTip1.setText(getString(R.string.setting_wifi_mode_close));
            mBackRestoreTip2.setText(getString(R.string.setting_wifi_please_reconnect));
            mBtnWifiBackupAddress.setText("");
        }
    }

    public String getBrowserAddr()
    {
        if (wifiIsAvailable)
        {
            mWifiPort = generatePort();
            return "http://" + wifiIpAddr + ":" + mWifiPort;
        } else
        {
            return "";
        }
    }

    /**
     * Description
     *
     * @param v
     * @see View.OnClickListener#onClick(View)
     */

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_back:
                if(bEnterMain)
                {
                    Intent intent = new Intent(WifiBackRestoreActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                else
                {
                    finish();
                }
                break;

            case R.id.btn_wifibackup_address:
                if (!StringUtils.isEmpty(mBtnWifiBackupAddress.getText().toString()))
                {
                    ClipboardManager copy = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    copy.setText(mBtnWifiBackupAddress.getText());
                    showToast(getString(R.string.contact_copy_invite_code_success));
//                startActivity(new Intent(this, AlertDialog.class)
//                       .putExtra(AlertDialog.TIP_MESSAGE, getString(R.string.contact_copy_invite_code_success)));

                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onConnect(int type)
    {
        checkWifiState();
    }

    @Override
    public void onDisConnect()
    {
        checkWifiState();

        WifiBackRestoreServerRunner.stopServer();
    }

    public int generatePort()
    {
        return 1024 + new Random().nextInt(47976);
    }

}
