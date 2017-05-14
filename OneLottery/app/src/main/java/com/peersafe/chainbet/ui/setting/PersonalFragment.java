package com.peersafe.chainbet.ui.setting;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.peersafe.chainbet.MainActivity;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.ali.ALIFeedBack;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicFragment;
import com.peersafe.chainbet.ui.GuideActivity;
import com.peersafe.chainbet.ui.setting.withdraw.BankcardActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.Defaultcontent;
import com.peersafe.chainbet.utils.common.PermissionUtils;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.utils.update.UpdateUtil;
import com.peersafe.chainbet.widget.AlertDialog;
import com.peersafe.chainbet.widget.ImportDialog;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.ShareBoardConfig;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.ui.setting
 * @description:
 * @date 14/12/16 PM5:55
 */
public class PersonalFragment extends BasicFragment implements View.OnClickListener
{
    public static final int EXIT_FLAG = 2;
    //切换账户
    private TextView mTvChangeUserAccount;

    //用户账户
    private TextView mTvUserAccount;

    //账户余额
    private TextView mTvBalance;

    //复制按钮
    private Button mBtnCopy;

    //钱包地址
    private TextView mTvWalletAddress;

    //转账按钮
    private Button mBtnTransferAccount;

    //充值按钮
    private Button mBtnRecharge;

    //分享按钮
    private Button mBtnShare;

    //意见反馈按钮
    private Button mBtnFeedback;

    //当前版本
    private TextView mTvCurVersion;

    //退出
    private Button mExit;

    ShareAction mShareAction;
    private UMWeb web;
    private UMImage umImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_personal, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        initViews();
        initToolBar();
        onRefresh();
    }

    @Override
    public void onRefresh()
    {
        super.onRefresh();
        initData();
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);
        if (!hidden)
        {
            onRefresh();
        }
    }

    private void initViews()
    {
        mTvUserAccount = (TextView) getView().findViewById(R.id.tv_user_account);
        mTvBalance = (TextView) getView().findViewById(R.id.tv_my_balance);
        mBtnCopy = (Button) getView().findViewById(R.id.btn_copy_wallet_address);
        mTvWalletAddress = (TextView) getView().findViewById(R.id.tv_wallet_address);
        mBtnTransferAccount = (Button) getView().findViewById(R.id.btn_transfer_account);
        mBtnRecharge = (Button) getView().findViewById(R.id.btn_recharge);
        mBtnShare = (Button) getView().findViewById(R.id.btn_share);
        mBtnFeedback = (Button) getView().findViewById(R.id.btn_feedback);
        mTvCurVersion = (TextView) getView().findViewById(R.id.tv_cur_version);
        mExit = (Button) getView().findViewById(R.id.exit);

        mBtnCopy.setOnClickListener(this);
        mBtnTransferAccount.setOnClickListener(this);
        mBtnRecharge.setOnClickListener(this);
        mBtnShare.setOnClickListener(this);
        mBtnFeedback.setOnClickListener(this);
        mExit.setOnClickListener(this);

        getView().findViewById(R.id.rl_bet_query).setOnClickListener(this);
        getView().findViewById(R.id.rl_detail_query).setOnClickListener(this);
        getView().findViewById(R.id.rl_msg_center).setOnClickListener(this);
        getView().findViewById(R.id.rl_with_draw).setOnClickListener(this);
        getView().findViewById(R.id.rl_version_update).setOnClickListener(this);
        getView().findViewById(R.id.rl_wallet_export).setOnClickListener(this);
        getView().findViewById(R.id.rl_guide_page).setOnClickListener(this);
//        getView().findViewById(R.id.rl_about_us).setOnClickListener(this);
//        getView().findViewById(R.id.rl_commont_question).setOnClickListener(this);

        umImage = new UMImage(getActivity(), R.drawable.ic_launcher);
        web = new UMWeb(Defaultcontent.getUrl(false, null));
        web.setTitle(getString(R.string.share_content_title));
        web.setThumb(umImage);
        web.setDescription(getString(R.string.share_contecnt_text));

        if (!UpdateUtil.isHasUpdateFunc())
        {
            getView().findViewById(R.id.rl_version_update).setVisibility(View.GONE);
        }
    }

    private void initData()
    {
        // TODO android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original
        // thread that created a view hierarchy can touch its views.
//        at android.view.ViewRootImpl.checkThread(ViewRootImpl.java:6072)
        mTvCurVersion.setText(getVersion());
        UserInfo me = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (me == null || StringUtils.isEmpty(me.getUserId()))
        {
            mTvUserAccount.setText("");
            mTvBalance.setText("");
            mTvWalletAddress.setText("");

            setExitText();
        } else if (ConstantCode.CommonConstant.ONELOTTERY_DEFAULT_USERNAME.equals(me.getUserId()))
        {
            mTvUserAccount.setText(getString(R.string.setting_deault_user_account));
            mTvBalance.setText("");
            mTvWalletAddress.setText("");

            setExitText();
        } else
        {
            OneLotteryApi.setCurUserId(me.getUserId());
            mTvUserAccount.setText(me.getUserId());
            double s = ((double) me.getBalance() / ConstantCode.CommonConstant
                    .ONELOTTERY_MONEY_MULTIPLE);
            DecimalFormat df = new DecimalFormat("0.00");
            mTvBalance.setText(df.format(s));
            mTvWalletAddress.setText(me.getWalletAddr());
        }
    }

    private void setExitText()
    {
        List<String> userList = UserInfoDBHelper.getInstance().getUserList(true);
        if (userList.isEmpty())
        {
            mExit.setText(getString(R.string.register));
        } else
        {
            mExit.setText(getString(R.string.login));
        }
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) getView().findViewById(R.id.personal_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.setTitle("");

        mTvChangeUserAccount = (TextView) getView().findViewById(R.id.tv_change_account);
        mTvChangeUserAccount.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.tv_change_account:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if (!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                if (!OneLotteryApi.isVisitor(getActivity()))
                {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.showRightMenu();
                }
                break;
            case R.id.btn_copy_wallet_address:
                ClipboardManager copy = (ClipboardManager)
                        getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                copy.setText(mTvWalletAddress.getText());
                showToast(getString(R.string.common_copy_success));
                break;
            case R.id.btn_transfer_account:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if (!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                if (!OneLotteryApi.isVisitor(getActivity()))
                {
                    Intent i_tran = new Intent(getActivity(), TransferActivity.class);
                    startActivity(i_tran);
                }
                break;
            case R.id.btn_recharge:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if (!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                if (!OneLotteryApi.isVisitor(getActivity()))
                {
                    Intent reChange = new Intent(getActivity(), RechangeActivity.class);
                    startActivity(reChange);
                }
                break;
            case R.id.btn_share:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if (!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                sharePlatform();
                break;
            case R.id.btn_feedback:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if (!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                if (!OneLotteryApi.isVisitor(getActivity()))
                {
                    ALIFeedBack.startFeedBackView(getActivity());
                }
                break;
            case R.id.rl_bet_query:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if (!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                if (!OneLotteryApi.isVisitor(getActivity()))
                {
                    Intent bet = new Intent(getActivity(), MyBetAcitivity.class);
                    startActivity(bet);
                }
                break;
            case R.id.rl_detail_query:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if (!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                if (!OneLotteryApi.isVisitor(getActivity()))
                {
                    Intent i_deiq = new Intent(getActivity(), TransactionDetailActivity.class);
                    startActivity(i_deiq);
                }
                break;
            case R.id.rl_msg_center:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if (!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                if (!OneLotteryApi.isVisitor(getActivity()))
                {
                    Intent i_msg = new Intent(getActivity(), MsgCenterActivity.class);
                    startActivity(i_msg);
                }
                break;
            case R.id.rl_with_draw:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if (!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                if (!OneLotteryApi.isVisitor(getActivity()))
                {
                    Intent i_bank = new Intent(getActivity(), BankcardActivity.class);
                    startActivity(i_bank);
                }
                break;
            case R.id.rl_version_update:
//                if (!NetworkUtil.isNetworkConnected())
//                {
//                    showToast(getString(R.string.check_network));
//                    break;
//                }
//
//                if (!OneLotteryManager.getInstance().isServiceConnect)
//                {
//                    showToast(getString(R.string.check_service));
//                    break;
//                }
//
//                UpdateUtil.updateManager(getActivity(), false);
                break;
            case R.id.rl_wallet_export:

                if (!OneLotteryApi.isVisitor(getActivity()))
                {
                    boolean result = checkPermissions(new String[]{PermissionUtils
                            .PERMISSION_WRITE_EXTERNAL_STORAGE, PermissionUtils
                            .PERMISSION_READ_EXTERNAL_STORAGE});
                    if (result)
                    {
                        ImportDialog dialog = new ImportDialog(getActivity(), ImportDialog
                                .EXPORT_FILE, "");
                        dialog.show();
                    } else
                    {
                        requestPermisson(getActivity());
                    }
                }
                break;
//            case R.id.rl_commont_question:
//                break;
            case R.id.rl_guide_page:
                Intent i_guide = new Intent(getActivity(), GuideActivity.class);
                i_guide.putExtra(GuideActivity.KEY_FROM, PersonalFragment.class.getSimpleName());
                startActivity(i_guide);

                break;
//            case R.id.rl_about_us:
//                Intent aboutUs = new Intent(getActivity(), AboutUsActivity.class);
//                aboutUs.putExtra("aboutus", true);
//                startActivity(aboutUs);
//                break;
            case R.id.exit:

                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if (!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                if (!OneLotteryApi.isVisitor(getActivity()))
                {
                    Intent intent = new Intent(getActivity(), AlertDialog.class);
                    intent.putExtra(AlertDialog.TIP_MESSAGE, getString(R.string
                            .setting_exit_tip_message));
                    intent.putExtra(AlertDialog.SHOW_CANCEL_BTN, true);
                    intent.putExtra(AlertDialog.SHOW_CONFIRM_BTN, true);
                    getActivity().startActivityForResult(intent, EXIT_FLAG);
                }
                break;
        }
    }

    /**
     * 分享平台
     */
    private void sharePlatform()
    {
        mShareAction = new ShareAction(getActivity());
        mShareAction.setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,
                SHARE_MEDIA.LINE, SHARE_MEDIA.WHATSAPP, SHARE_MEDIA.EMAIL, SHARE_MEDIA.SMS)
                .setShareboardclickCallback(new ShareBoardlistener()
                {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media)
                    {
                        if (share_media == SHARE_MEDIA.WEIXIN || share_media == SHARE_MEDIA
                                .WEIXIN_CIRCLE)
                        {
                            new ShareAction(getActivity())
                                    .setPlatform(share_media)
                                    .withMedia(web)
                                    .setCallback(umShareListener).share();
                        } else if (share_media == SHARE_MEDIA.WHATSAPP || share_media ==
                                SHARE_MEDIA.SMS || share_media == SHARE_MEDIA.EMAIL)
                        {
                            new ShareAction(getActivity())
                                    .withText(getString(R.string.share_content_titile_and_link))
                                    .setPlatform(share_media)
                                    .setCallback(umShareListener).share();
                        } else if (share_media == SHARE_MEDIA.LINE)
                        {
                            new ShareAction(getActivity())
                                    .setPlatform(share_media)
                                    .withText(getString(R.string.share_content_titile_and_link))
                                    .setCallback(umShareListener).share();
                        }
                    }
                });

        ShareBoardConfig shareBoardConfig = new ShareBoardConfig();
        shareBoardConfig.setTitleText(getActivity().getString(R.string.choice_share_platform));
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
                showToast(getString(R.string.share_success));
            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t)
        {
            TextView title = new TextView(getActivity());

            title.setGravity(Gravity.CENTER_HORIZONTAL);
            title.setPadding(0, (int) getResources().getDimension(R.dimen.y15), 0, 0);
            title.setText(getString(R.string.share_fail));
            title.setTextColor(getResources().getColor(R.color.common_text_color));
            title.setTextSize(getResources().getDimension(R.dimen.y20));

            new android.support.v7.app.AlertDialog.Builder(getActivity())
                    .setCustomTitle(title)
                    .setMessage(t.getMessage().contains("2008") ? getString(R.string
                            .app_not_install) : getString(R.string.unkown))
                    .setPositiveButton(getString(R.string.confirm), null)
                    .show();
        }

        @Override
        public void onCancel(SHARE_MEDIA platform)
        {
        }
    };

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public String getVersion()
    {
        try
        {
            PackageManager manager = getActivity().getPackageManager();
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            String version = info.versionName;
            version = String.format(getString(R.string.setting_cur_version), version);
            return version;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            String version = String.format(getString(R.string.setting_cur_version), "V1.0");
            return version;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(OLMessageModel messageModel)
    {
        if (messageModel != null)
        {
            switch (messageModel.getEventType())
            {
                case OLMessageModel.STMSG_MODEL_TRANSFER_ACCOUNT_NOTIFY:
                    if (messageModel.getEventObject() != null && messageModel.getEventObject()
                            instanceof UserInfo)
                    {
                        initData();
                    }
                    break;

                case OLMessageModel.STMSG_MODEL_REFRSH_SETTING_BALANCE:
                case OLMessageModel.STMSG_MODEL_TRANSFER_CALLBACK:
                case OLMessageModel.STMSG_MODEL_SETTING_CHANGE_ACCOUNT:
                    initData();
                    break;

                case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_CALLBACK:
                    boolean hasNetwork = NetworkUtil.isNetworkConnected();
                    if (hasNetwork)
                    {
                        // 获取余额
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (OneLotteryManager.getInstance().getUserBalance() != null)
                                {
                                    getActivity().runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            initData();
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                    break;
                case OLMessageModel.STMSG_MODEL_IMPORT_WALLET_OK:
                    onRefresh();
                    break;
                case OLMessageModel.STMSG_MODEL_EXPORT_SD_FILE:
                    Boolean result = (Boolean) messageModel.getEventObject();
                    if (result)
                    {
                        showToast(getString(R.string.setting_emport_success));
                    } else
                    {
                        Intent intent = new Intent(getActivity(), AlertDialog.class);
                        intent.putExtra(AlertDialog.TIP_TITLE, getString(R.string.setting_file_emport_fail));
                        intent.putExtra(AlertDialog.TIP_MESSAGE, getString(R.string
                                .setting_emport_fail));
                        intent.putExtra(AlertDialog.SHOW_CONFIRM_BTN, true);
                        startActivity(intent);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
