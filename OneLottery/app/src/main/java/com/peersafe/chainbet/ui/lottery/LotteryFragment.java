package com.peersafe.chainbet.ui.lottery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.github.why168.LoopViewPagerLayout;
import com.github.why168.listener.OnBannerItemClickListener;
import com.github.why168.listener.OnLoadImageViewListener;
import com.github.why168.modle.BannerInfo;
import com.github.why168.modle.IndicatorLocation;
import com.github.why168.modle.LoopStyle;
import com.google.gson.Gson;
import com.peersafe.chainbet.MainActivity;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.MessageNotify;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.WithdrawRecord;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.MessageNotifyDBHelper;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.manager.dbhelper.WithdrawRecordDBHelper;
import com.peersafe.chainbet.model.LotteryJsonBean.BannerBean;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicFragment;
import com.peersafe.chainbet.ui.adapter.LotteryAdapter;
import com.peersafe.chainbet.ui.setting.AboutUsActivity;
import com.peersafe.chainbet.ui.setting.MsgCenterActivity;
import com.peersafe.chainbet.ui.setting.TransactionDetailActivity;
import com.peersafe.chainbet.ui.setting.withdraw.BankcardActivity;
import com.peersafe.chainbet.ui.setting.withdraw.DetailActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.OkHttpUtils;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.log.OLLogger;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.NoticeView;
import com.peersafe.chainbet.widget.OLRefreshHeader;
import com.peersafe.chainbet.widget.WaitingDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2016 PeerSafe Technologies. All rights reserved.
 *
 * @author zhangyang
 * @name: OneLottery
 * @package: com.peersafe.onelottery.ui.lottery
 * @description:
 * @date 14/12/16 PM5:54
 */
public class LotteryFragment extends BasicFragment implements View.OnClickListener,
        OnItemClickListener
{
    private static final String TAG = LotteryFragment.class.getSimpleName();

    private LRecyclerView mRecyclerView = null;
    private LoopViewPagerLayout mLoopViewPagerLayout;

    private LotteryAdapter mDataAdapter = null;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;

    //活动分类的索引头
    private View mLotteryIndexHeaderView;

    //索引的横线
    private View mLotteryIndicatorView;

    //当前活动的索引
    private int mCurLotteryIndex = 0; //最新:0 最快:1 好友活动:2 我的活动:3

    //活动分类的浮动索引头
    private View mLotteryFloatIndexView;

    //活动分类浮动索引的横线
    private View mLotteryFloatIndicatorView;

    //活动查询按钮
    private ImageButton mLotterySearch;

    //活动消息通知按钮
    private ImageButton mLotteryMessage;

    //广告通知栏
    private NoticeView mNoticeView;
    private NoticeView mFloatNoticeView;

    private Button[] mFloatIndex;

    private Button[] mIndex;

    //空白页面
    private LinearLayout mLyNoData;
    private ImageView mIvNoData;
    private TextView mTvNoData;

    private List<OneLottery> mDataList;
    private int mToolBarBottom = 0;
    private int mToolBarHeight = 0;
    private int mToolBarTop = 0;
    private OneLottery betLottery;

    private View mAdviseView;
    private TextView mDivisionLine;
    private View mFloatAdvise;
    private TextView mFloatDivision;

    private NoticeView vNotice = null;
    private List<String> messages = new ArrayList<>();
    private boolean isRefresh = false;

    private ImageView mWhitePoint;

    private BannerBean[] bean;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_lottery, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);

        initViews();

        initToolBar();
    }

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            try
            {
                OLLogger.i(TAG, "handler msg:" + msg.obj + ", isLottery ? " + (msg.obj instanceof
                        OneLottery));
                betLottery = (OneLottery) msg.obj;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };


    private void initViews()
    {
        mWhitePoint = (ImageView) getView().findViewById(R.id.img_msg_point);
        mRecyclerView = (LRecyclerView) getView().findViewById(R.id.list);

        //setLayoutManager must before setAdapter
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);

        mRecyclerView.setLayoutManager(manager);

        mRecyclerView.setPullRefreshEnabled(true);
        mRecyclerView.setRefreshHeader(new OLRefreshHeader(getActivity()));

        //禁用自动加载更多功能
        mRecyclerView.setLoadMoreEnabled(false);

        mDataAdapter = new LotteryAdapter(getActivity(), mHandler);

        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);
        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        //添加广告界面
        addAdView();

        //添加悬浮按钮
        addLotteryIndexHeadView();

        //浮动头
        mLotteryFloatIndexView = getView().findViewById(R.id.lottery_float_indicator);
        mLotteryFloatIndicatorView = mLotteryFloatIndexView.findViewById(R.id.layout_indicator);
        mFloatAdvise = mLotteryFloatIndexView.findViewById(R.id.advise_layout);
        mFloatDivision = (TextView) mLotteryFloatIndexView.findViewById(R.id
                .tv_lottery_division_line);
        mFloatNoticeView = (NoticeView) mLotteryFloatIndexView.findViewById(R
                .id.tv_scroll_view);
        mLotteryFloatIndexView.findViewById(R.id.btn_lottery_newest).setOnClickListener(this);
        mLotteryFloatIndexView.findViewById(R.id.btn_lottery_fastest).setOnClickListener(this);
        mLotteryFloatIndexView.findViewById(R.id.btn_lottery_friend).setOnClickListener(this);
        mLotteryFloatIndexView.findViewById(R.id.btn_lottery_me).setOnClickListener(this);

        mLotteryFloatIndicatorView.setTranslationX(getResources().getDimension(R.dimen.x180) *
                mCurLotteryIndex + getResources().getDimension(R.dimen.x32));

        mFloatIndex = new Button[]{(Button) getView().findViewById(R.id.btn_lottery_newest),
                (Button) getView().findViewById(R.id.btn_lottery_fastest),
                (Button) getView().findViewById(R.id.btn_lottery_friend),
                (Button) getView().findViewById(R.id.btn_lottery_me)};
        mFloatIndex[mCurLotteryIndex].setSelected(true);

        mRecyclerView.setOnRefreshListener(new OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                if (!NetworkUtil.isNetworkConnected())
                {
                    mLyNoData.setVisibility(View.VISIBLE);
                    mIvNoData.setImageResource(R.drawable.no_network);
                    mTvNoData.setText(R.string.no_network);
                    mDataAdapter.setDataList(new ArrayList<OneLottery>());
                    mRecyclerView.refreshComplete();
                    return;
                }

                if (!OneLotteryManager.isServiceConnect)
                {
                    mLyNoData.setVisibility(View.VISIBLE);
                    mIvNoData.setImageResource(R.drawable.no_service);
                    mTvNoData.setText(R.string.no_service);
                    mDataAdapter.setDataList(new ArrayList<OneLottery>());
                    mRecyclerView.refreshComplete();
                    return;
                }

                isRefresh = true;

                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        OneLotteryManager.getInstance().getLotteries(false, true);
                    }
                }).start();
            }
        });

        mRecyclerView.setLScrollListener(new LRecyclerView.LScrollListener()
        {
            @Override
            public void onScrollUp()
            {
            }

            @Override
            public void onScrollDown()
            {
            }

            @Override
            public void onScrolled(int distanceX, int distanceY)
            {
                if (mToolBarBottom <= 0 || mToolBarTop <= 0 || mToolBarHeight <= 0)
                {
                    Toolbar toolbar = (Toolbar) getView().findViewById(R.id.lottery_toolbar);
                    int[] positionToolbar = new int[2];
                    toolbar.getLocationInWindow(positionToolbar);
                    mToolBarTop = positionToolbar[1];
                    mToolBarHeight = toolbar.getHeight();
                    if (mToolBarHeight > 0 && mToolBarTop > 0)
                    {
                        mToolBarBottom = mToolBarTop + mToolBarHeight;
                    } else
                    {
                        return;
                    }
                }

                int[] position = new int[2];
                mLotteryIndexHeaderView.getLocationInWindow(position);

                if (position[1] <= mToolBarBottom)
                {
                    if (mLotteryFloatIndexView.getVisibility() == View.GONE)
                    {
                        mLotteryFloatIndexView.setVisibility(View.VISIBLE);
                        setNoticeData();
                    }
                } else
                {
                    if (mLotteryFloatIndexView.getVisibility() == View.VISIBLE)
                    {
                        mLotteryFloatIndexView.setVisibility(View.GONE);
                        setNoticeData();
                    }
                }
            }

            @Override
            public void onScrollStateChanged(int state)
            {
            }
        });

        mLRecyclerViewAdapter.setOnItemClickListener(this);
        setCurIndexLottery(mCurLotteryIndex);

        setNoticeData();

        if (vNotice != null)
        {
            vNotice.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (!NetworkUtil.isNetworkConnected())
                    {
                        showToast(getString(R.string.check_network));
                        return;
                    }

                    if(!OneLotteryManager.getInstance().isServiceConnect)
                    {
                        showToast(getString(R.string.check_service));
                        return;
                    }

                    String s = messages.get(vNotice.getIndex());
                    String[] split = s.split(OneLotteryApi.RES_SEP);
                    String horn = split[0];
                    Integer type = Integer.parseInt(split[1]);
                    String lotterId = split[2];
                    String msgId = split[3];
                    String newTxId = split[4];

                    switch (type)
                    {
                        //跳转消息中心
                        case ConstantCode.MessageType.MESSAGE_TYPE_CREATE_FAIL:
                        case ConstantCode.MessageType.MESSAGE_TYPE_DELETE_SUCCESS:
                        case ConstantCode.MessageType.MESSAGE_TYPE_BET_FAIL:
                        case ConstantCode.MessageType.MESSAGE_TYPE_TRANSFOR_FAIL:

                            Intent msgCenter = new Intent(getActivity(), MsgCenterActivity.class);
                            startActivity(msgCenter);
                            break;

                        //跳转到明细详情
                        case ConstantCode.MessageType.MESSAGE_TYPE_TRANSFOR_FROM_OTHER:
                        case ConstantCode.MessageType.MESSAGE_TYPE_TRANSFOR_SUCCESS:

                            MessageNotify messageNotify = MessageNotifyDBHelper.getInstance()
                                    .getMsgByMsgId(msgId, lotterId, type, horn);
                            if (messageNotify != null)
                            {
                                messageNotify.setIsRead(true);
                                MessageNotifyDBHelper.getInstance().insertMessageNotify
                                        (messageNotify);
                            }

                            Intent tdDetail = new Intent(getActivity(), TransactionDetailActivity
                                    .class);
                            startActivity(tdDetail);
                            break;

                        //跳转详情页面
                        case ConstantCode.MessageType.MESSAGE_TYPE_CREATE_SUCCESS:
                        case ConstantCode.MessageType.MESSAGE_TYPE_BET_SUCCESS:
                        case ConstantCode.MessageType.MESSAGE_TYPE_DELETE_FAIL:
                        case ConstantCode.MessageType.MESSAGE_TYPE_REFUND:
                        case ConstantCode.MessageType.MESSAGE_TYPE_MODIFY_FAIL:
                        case ConstantCode.MessageType.MESSAGE_TYPE_MODIFY_SUCCESS:
                        case ConstantCode.MessageType.MESSAGE_TYPE_PRIZE:
                        case ConstantCode.MessageType.MESSAGE_TYPE_PERCENTAGE:

                            MessageNotify messageNotify1 = MessageNotifyDBHelper.getInstance()
                                    .getMsgByMsgId(msgId, lotterId, type, horn);
                            if (messageNotify1 != null)
                            {
                                messageNotify1.setIsRead(true);
                                MessageNotifyDBHelper.getInstance().insertMessageNotify
                                        (messageNotify1);
                            }

                            Intent lotteryDeatil = new Intent(getActivity(), LotteryDetailActivity.class);
                            lotteryDeatil.putExtra(ConstantCode.CommonConstant.LOTTERYID, lotterId);
                            startActivity(lotteryDeatil);
                            break;

                        //跳转到提现银行卡页面
                        case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_SEND_FAIL:
                            MessageNotify messageNotify2 = MessageNotifyDBHelper.getInstance().getMsgByMsgId(msgId);
                            messageNotify2.setIsRead(true);
                            MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify2);

                            Intent bankCard = new Intent(getActivity(), BankcardActivity.class);
                            startActivity(bankCard);
                            break;

                        //跳转详情页面
                        case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_SEND_SUCCESS:
                        case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_FAIL:
                        case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_SUCCESS:
                        case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_APPEAL_SEND_FAIL:
                        case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_APPEAL_SEND_SUCCESS:
                            MessageNotify messageNotify3 = MessageNotifyDBHelper.getInstance().getMsgByMsgId(msgId);
                            messageNotify3.setIsRead(true);
                            MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify3);

                            WithdrawRecord record = WithdrawRecordDBHelper.getInstance().getRecordByKey(newTxId);
                            Intent detail = new Intent(getActivity(), DetailActivity.class);
                            detail.putExtra(ConstantCode.CommonConstant.WITH_DRAW_RECORD,record);
                            startActivity(detail);
                            break;

                    }

                }
            });
        }
    }

    private void setNoticeData()
    {
        boolean isFloat = false;

        messages = MessageNotifyDBHelper.getInstance().getUnreadMessages();

        if (mLotteryFloatIndexView.getVisibility() != View.GONE)
        {
            isFloat = true;
        }

        if (isFloat)
        {
            vNotice = mFloatNoticeView;
        } else
        {
            vNotice = mNoticeView;
        }

        if (null != messages && messages.size() != 0)
        {
            if (isFloat)
            {
                mFloatAdvise.setVisibility(View.VISIBLE);
                mFloatDivision.setVisibility(View.VISIBLE);
                mAdviseView.setVisibility(View.GONE);
                mDivisionLine.setVisibility(View.GONE);
                mWhitePoint.setVisibility(View.VISIBLE);
            } else
            {
                mAdviseView.setVisibility(View.VISIBLE);
                mDivisionLine.setVisibility(View.VISIBLE);
                mFloatAdvise.setVisibility(View.GONE);
                mFloatDivision.setVisibility(View.GONE);
                mWhitePoint.setVisibility(View.VISIBLE);
            }

            vNotice.start(messages);
        } else
        {
            mAdviseView.setVisibility(View.GONE);
            mDivisionLine.setVisibility(View.GONE);
            mFloatAdvise.setVisibility(View.GONE);
            mFloatDivision.setVisibility(View.GONE);
            mWhitePoint.setVisibility(View.GONE);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);
        if (isRefresh)
        {
            isRefresh = false;
            mRecyclerView.refreshComplete();
        }

        if (!hidden)
        {
            onRefresh();
        }
    }

    @Override
    public void onRefresh()
    {
        super.onRefresh();
        setCurIndexLottery(mCurLotteryIndex);
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) getView().findViewById(R.id.lottery_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.setTitle("");

        TextView title = (TextView) getView().findViewById(R.id.tv_toolbar_title);
        title.setText(getString(R.string.lottery_compete_treasure));
        mLotteryMessage = (ImageButton) getView().findViewById(R.id.lottery_message);
        mLotterySearch = (ImageButton) getView().findViewById(R.id.lottery_search);
        mLotterySearch.setOnClickListener(this);
        mLotteryMessage.setOnClickListener(this);
    }

    /**
     * 添加广告栏
     */
    private void addAdView()
    {
        //LoopViewPagerLayout使用方法详见github：https://github.com/why168/LoopViewPagerLayout
        mLoopViewPagerLayout = (LoopViewPagerLayout) LayoutInflater.from(getActivity()).inflate(R
                .layout.layout_banner_header, (ViewGroup) getActivity().findViewById(android.R.id
                .content), false);
        mLoopViewPagerLayout.setBackgroundResource(R.drawable.guird_1);
        mLoopViewPagerLayout.setLoop_ms(3000);//轮播的速度(毫秒)
        mLoopViewPagerLayout.setLoop_duration(800);//滑动的速率(毫秒)
        mLoopViewPagerLayout.setLoop_style(LoopStyle.Empty);//轮播的样式-默认empty
        mLoopViewPagerLayout.setIndicatorLocation(IndicatorLocation.Center);
        mLoopViewPagerLayout.initializeData(getActivity());//初始化数据
        mLRecyclerViewAdapter.addHeaderView(mLoopViewPagerLayout);

        mLoopViewPagerLayout.setOnLoadImageViewListener(new OnLoadImageViewListener()
        {
            @Override
            public void onLoadImageView(ImageView imageView, Object parameter)
            {
                Glide.with(getActivity()).load(parameter).placeholder(R.drawable.guird_1).into
                        (imageView);
            }

            @Override
            public ImageView createImageView(Context context)
            {
                ImageView imageView = new ImageView(context);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                return imageView;
            }
        });

        getBannerImageview();

        mLoopViewPagerLayout.setOnBannerItemClickListener(new OnBannerItemClickListener()
        {
            @Override
            public void onBannerClick(int index, ArrayList<BannerInfo> banner)
            {
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    return;
                }

                if(!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    return;
                }

                if (bean != null && bean.length >= 1)
                {
                    Intent intent = new Intent(getActivity(), BannerViewActivity.class);
                    intent.putExtra("url", bean != null ? bean[index].getUrl() : "");
                    startActivity(intent);
                }
            }
        });
    }

    private void getBannerImageview()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {

                bean = new Gson().fromJson(OkHttpUtils.getJsonUrl(ConstantCode
                        .CommonConstant.BANNER_URL), BannerBean[].class);
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ArrayList<BannerInfo> data = new ArrayList<>();

                        if (bean == null)
                        {
                            return;
                        }

                        if (bean.length > 1)
                        {
                            for (int i = 0; i < bean.length; i++)
                            {
                                data.add(new BannerInfo<String>(ConstantCode.CommonConstant
                                        .BANNER_WEB + bean[i].getImgName(), ""));
                            }
                            mLoopViewPagerLayout.setLoopData(data);

                            mLoopViewPagerLayout.stopLoop();
                            mLoopViewPagerLayout.startLoop();

                        }
                        else if (bean.length == 1)
                        {
                            Glide.with(getActivity()).load(ConstantCode.CommonConstant.BANNER_WEB +
                                    bean[0].getImgName()).asBitmap().into(new SimpleTarget<Bitmap>()
                            {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<?
                                        super Bitmap> glideAnimation)
                                {
                                    Drawable drawable = new BitmapDrawable(resource);
                                    mLoopViewPagerLayout.setBackgroundDrawable(drawable);
                                    mLoopViewPagerLayout.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            Intent intent = new Intent(getActivity(), BannerViewActivity.class);
                                            intent.putExtra("url", bean != null ? bean[0].getUrl() : "");
                                            startActivity(intent);
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 添加
     */
    private void addLotteryIndexHeadView()
    {
        mLotteryIndexHeaderView = LayoutInflater.from(getActivity()).inflate(R.layout
                .lottery_index_indicator, (ViewGroup) getActivity().findViewById(android.R.id
                .content), false);
        mLRecyclerViewAdapter.addHeaderView(mLotteryIndexHeaderView);
        mLotteryIndicatorView = mLotteryIndexHeaderView.findViewById(R.id.layout_indicator);
        mLotteryIndexHeaderView.findViewById(R.id.btn_lottery_newest).setOnClickListener(this);
        mLotteryIndexHeaderView.findViewById(R.id.btn_lottery_fastest).setOnClickListener(this);
        mLotteryIndexHeaderView.findViewById(R.id.btn_lottery_friend).setOnClickListener(this);
        mLotteryIndexHeaderView.findViewById(R.id.btn_lottery_me).setOnClickListener(this);

        mAdviseView = mLotteryIndexHeaderView.findViewById(R.id.advise_layout);
        mDivisionLine = (TextView) mLotteryIndexHeaderView.findViewById(R.id
                .tv_lottery_division_line);
        mNoticeView = (NoticeView) mAdviseView.findViewById(R.id
                .tv_scroll_view);

        mIndex = new Button[]{(Button) mLotteryIndexHeaderView.findViewById(R.id
                .btn_lottery_newest), (Button) mLotteryIndexHeaderView.findViewById(R.id
                .btn_lottery_fastest), (Button) mLotteryIndexHeaderView.findViewById(R.id
                .btn_lottery_friend), (Button) mLotteryIndexHeaderView.findViewById(R.id
                .btn_lottery_me)};
        mCurLotteryIndex = 0;
        mIndex[0].setSelected(true);
        mLotteryIndicatorView.setTranslationX(getResources().getDimension(R.dimen.x180) *
                mCurLotteryIndex + getResources().getDimension(R.dimen.x32));

        mLyNoData = (LinearLayout) mLotteryIndexHeaderView.findViewById(R.id.ly_empty);
        mTvNoData = (TextView) mLotteryIndexHeaderView.findViewById(R.id.tv_empty);
        mIvNoData = (ImageView) mLotteryIndexHeaderView.findViewById(R.id.iv_empty);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_lottery_newest:
                setCurLotteryIndex(0);
                setCurIndexLottery(0);
                break;

            case R.id.btn_lottery_fastest:
                setCurLotteryIndex(1);
                setCurIndexLottery(1);
                break;

            case R.id.btn_lottery_friend:
                setCurLotteryIndex(2);
                String curUserId = OneLotteryApi.getCurUserId();
                if (StringUtils.isEmpty(curUserId) || curUserId.equals(ConstantCode
                        .CommonConstant.ONELOTTERY_DEFAULT_USERNAME))
                {
                    setCurIndexLottery(4);
                } else
                {
                    setCurIndexLottery(2);
                }
                break;

            case R.id.btn_lottery_me:
                setCurLotteryIndex(3);
                String curUserId1 = OneLotteryApi.getCurUserId();
                if (StringUtils.isEmpty(curUserId1) || curUserId1.equals(ConstantCode
                        .CommonConstant.ONELOTTERY_DEFAULT_USERNAME))
                {
                    setCurIndexLottery(4);
                } else
                {
                    setCurIndexLottery(3);
                }
                break;

            case R.id.lottery_message:
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

                if (!OneLotteryApi.isVisitor(getActivity()))
                {
                    Intent msgCenter = new Intent(getActivity(), MsgCenterActivity.class);
                    startActivity(msgCenter);
                }
                break;

            case R.id.lottery_search:
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

                Intent search = new Intent(getActivity(), SearchActivity.class);
                startActivity(search);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(final OLMessageModel model)
    {
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_ADD_NOTIFY:
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_MODIFY_NOTIFY:
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_DELETE_NOTIFY:
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_CAN_REFUND_NOTIFY:
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_NOTIFY:
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERYS_START_OR_END_NOTIFY:
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_OVER_NOTIFY:

                setCurIndexLottery(mCurLotteryIndex);
                break;

            case OLMessageModel.STMSG_MODEL_CLOSE_ADVISE:
                break;

            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_ADD_CALLBACK_SUCCESS:
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_MODIFY_CALLBACK_SUCCESS:
                setCurIndexLottery(mCurLotteryIndex);

                // logic内已经更新了活动详情，不需要再联网获取
                String txId = (String) model.getEventObject();
                OneLottery onelottery = OneLotteryDBHelper.getInstance()
                        .getLotteryByNewTxID(txId);
                if (onelottery != null)
                {
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            OneLotteryManager.getInstance().getLotteries(false, true);
                        }
                    }).start();
                }
                break;

            case OLMessageModel.STMSG_MODEL_OPEN_REWARD_CALLBACK:
            case OLMessageModel.STMSG_MODEL_OPEN_REWARD_NOTIFY:
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_DELETE_CALLBACK_SUCCESS:

                if (mCurLotteryIndex == 3)
                {
                    setCurIndexLottery(mCurLotteryIndex);
                }
                break;

            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_START_NOTIFY:
                String lotteryId = (String) model.getEventObject();
                OneLottery lottery = OneLotteryDBHelper.getInstance()
                        .getLotteryByLotterId(lotteryId);
                if (lottery != null)
                {
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            OneLotteryManager.getInstance().getLotteries(false, true);
                        }
                    }).start();
                }
                break;

            case OLMessageModel.STMSG_MODEL_ADVISE_MESSAGE:

                setNoticeData();
                break;

            case OLMessageModel.STMSG_MODEL_SETTING_CHANGE_ACCOUNT:
                //切换账号刷新喇叭消息
                setNoticeData();
                break;

            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_GET_LOTTERIES_OVER:
                boolean result = (boolean) model.getEventObject();
                setCurIndexLottery(mCurLotteryIndex);

                if (isRefresh)
                {
                    isRefresh = false;
                    mRecyclerView.refreshComplete();
                }
                break;

            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_CALLBACK:
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_CONSENSUS_OVER_NOTIFY:
            {
                OLLogger.i(TAG, "betLottery 2 " + betLottery + ", isMainAct ? " + (getActivity()
                        instanceof MainActivity));

                // 投注完成后
                if (betLottery != null)
                {
                    boolean isSuccess = false;
                    WaitingDialog waitingDialog = ((MainActivity) getActivity()).getWaitingDialog();

                    if (waitingDialog != null && waitingDialog.isShowing())
                    {
                        if (model.getEventType() == OLMessageModel
                                .STMSG_MODEL_ONE_LOTTERY_CONSENSUS_OVER_NOTIFY)
                        {
                            String betTxId = (String) model.getEventObject();
                            if (betTxId.equals(waitingDialog.getTxId()))
                            {
                                waitingDialog.setBtnText(getString(R.string.message_bet_success));
                            }
                        } else
                        {
                            isSuccess = (boolean) model.getEventObject();
                            if (isSuccess)
                            {
                                waitingDialog.setBtnText(getString(R.string.message_bet_success));
                            } else
                            {
                                waitingDialog.setBtnText(getString(R.string.message_bet_fail));
                            }
                        }
                    }

                    setCurIndexLottery(mCurLotteryIndex);
                    betLottery = null;
                } else
                {
                    // 获取活动列表以及相应详情（会在STMSG_MODEL_ONE_LOTTERY_GET_LOTTERIES_OVER里刷新）
                    // 不过更好的是只获取单个投注的活动详情，不过传递LotteryID会比较繁琐。获取全部的话，也比较好。
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            OneLotteryManager.getInstance().getLotteries(false, true);
                        }
                    }).start();
                }

                // 获取用户余额，刷新UI
                OneLotteryManager.getInstance().getUserBalance();
            }
            break;
            case OLMessageModel.STMSG_MODEL_NET_WORK_CONNECT:
                mRecyclerView.forceToRefresh();
                getBannerImageview();
                setCurIndexLottery(mCurLotteryIndex);
                break;

            case OLMessageModel.STMSG_MODEL_NET_WORK_DISCONNECT:
                if (isRefresh)
                {
                    // 如果断网的时候，应该强制将refresh标志关闭
                    isRefresh = false;
                    mRecyclerView.refreshComplete();
                }
                setCurIndexLottery(mCurLotteryIndex);
                break;
            default:
                break;
        }
    }

    /**
     * 设置当前活动索引
     *
     * @param curIndex
     */
    private void setCurLotteryIndex(int curIndex)
    {
        if (mCurLotteryIndex == curIndex)
        {
            return;
        }

        mFloatIndex[mCurLotteryIndex].setSelected(false);
        mFloatIndex[curIndex].setSelected(true);

        mIndex[mCurLotteryIndex].setSelected(false);
        mIndex[curIndex].setSelected(true);

        mCurLotteryIndex = curIndex;
        mLotteryIndicatorView.setTranslationX(getResources().getDimension(R.dimen.x180) *
                mCurLotteryIndex + getResources().getDimension(R.dimen.x32));
        mLotteryFloatIndicatorView.setTranslationX(getResources().getDimension(R.dimen.x180) *
                mCurLotteryIndex + getResources().getDimension(R.dimen.x32));
    }

    private void setCurIndexLottery(final int mCurLotteryIndex)
    {
        if (!NetworkUtil.isNetworkConnected())
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_network);
            mTvNoData.setText(R.string.no_network);
            mDataAdapter.setDataList(new ArrayList<OneLottery>());
            return;
        }

        if (!OneLotteryManager.isServiceConnect)
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_service);
            mTvNoData.setText(R.string.no_service);
            mDataAdapter.setDataList(new ArrayList<OneLottery>());
            return;
        }

        mDataAdapter.clear();
        switch (mCurLotteryIndex)
        {
            case 0:
                mDataList = OneLotteryDBHelper.getInstance().getNewestLotteres();
                break;
            case 1:
                mDataList = OneLotteryDBHelper.getInstance().getFastestLotteres();
                break;
            case 2:
                mDataList = OneLotteryDBHelper.getInstance().getFriendLotteres();
                break;
            case 3:
                mDataList = OneLotteryDBHelper.getInstance().getMyLotteres();
                break;
            case 4:
                mDataList = new ArrayList<>();
                break;
        }

        if (null != mDataList && !mDataList.isEmpty())
        {
            mLyNoData.setVisibility(View.GONE);
            mDataAdapter.setDataList(mDataList);
        } else
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_data);
            mDataAdapter.setDataList(new ArrayList<OneLottery>());
            mTvNoData.setText(R.string.no_lottery);
        }

    }

    // TODO 设置投注框的状态
    private void setWaitingDialogStatus(boolean result)
    {
        WaitingDialog waitingDialog = ((MainActivity) getActivity()).getWaitingDialog();

        if (waitingDialog != null && waitingDialog.isShowing() && result)
        {
            waitingDialog.setBtnText(getString(R.string.message_bet_success));
        } else if (waitingDialog != null && waitingDialog.isShowing())
        {
            waitingDialog.setBtnText(getString(R.string.message_bet_fail));
        }
    }

    @Override
    public void onItemClick(View view, int position)
    {
        if (!NetworkUtil.isNetworkConnected())
        {
            showToast(getString(R.string.check_network));
            return;
        }

        if(!OneLotteryManager.getInstance().isServiceConnect)
        {
            showToast(getString(R.string.check_service));
            return;
        }

        Intent intent = new Intent(getActivity(), LotteryDetailActivity.class);
        intent.putExtra(ConstantCode.CommonConstant.LOTTERYID, mDataList.get(position)
                .getLotteryId());
        startActivity(intent);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mLoopViewPagerLayout.stopLoop();
    }

}
