package com.peersafe.chainbet.ui.lottery;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.interfaces.OnLoadMoreListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.github.jdsjlzx.util.RecyclerViewStateUtils;
import com.github.jdsjlzx.view.LoadingFooter;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.Friend;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.OneLotteryBet;
import com.peersafe.chainbet.db.PrizeRule;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.FriendDBHelper;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryBetDBHelper;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.manager.dbhelper.PrizeRuleDBHelper;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.AttendBean;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.adapter.LotteryAttendAdapter;
import com.peersafe.chainbet.utils.common.CommonUtils;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.DateUtils;
import com.peersafe.chainbet.utils.common.ImageUtils;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.log.OLLogger;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.utils.view.BetNumberDialog;
import com.peersafe.chainbet.widget.OLRefreshHeader;
import com.peersafe.chainbet.widget.SharePlatform;
import com.peersafe.chainbet.widget.WaitingDialog;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.media.UMImage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LotteryDetailActivity extends BasicActivity implements View.OnClickListener,
        OnItemClickListener
{

    private static final String TAG = LotteryDetailActivity.class.getSimpleName();

    //适配器
    private LotteryAttendAdapter mDataAdapter = null;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;

    //数据库一共多少条数据
    private int TOTAL_COUNTER = 0;

    //每一页展示多少条数据
    private int REQUEST_COUNT = 20;

    //已经获取到多少条数据了
    private static int mCurrentCounter = 0;

    //活动倒计时显示的数字文本
    private TextView mTvCountDown;

    //活动倒计时显示图标文本
    private Button mBtnCountDown;

    //活动倒计时布局
    private RelativeLayout mRelCountDown;

    //活动结束时间
    private TextView mTvTime;

    //添加关注
    private Button mBtnAddConcern;

    //投注进度条显示
    private ProgressBar mProPercent;

    //投注进度百分比显示
    private TextView mTvPercent;

    //我的信息显示
    private TextView mTvMyText;

    //显示我的投注的
    private RelativeLayout mRelMyBet;

    //显示我的投注的文本
    private TextView mTvMyBet;

    //查看我的投注的button
    private Button mBtnCheck;

    //参与者列表
    private LRecyclerView mRecyclerView = null;

    //定时器
    private CountDownTimer timer;

    private Button mBtnAddRewardCn;

    private Button mBtnAddCreaterCn;
    //活动
    private OneLottery lottery;

    //活动的Id
    private String lotterId;

    //投注
    private Button mBet, mShare;

    private ShareAction mShareAction;
    private UMImage imageurl;
    //参与者
    private List<AttendBean> attendList;

    private LinearLayout mBtnLinerlayout;
    private LinearLayout mLyMainContent;
    private LinearLayout mLyEnd;
    private int state;
    private UserInfo curUser;
    private TextView mBtnCalculate;
    private ImageView mLotteryImage;
    private TextView mTvDescribtion;
    private TextView mTvRule;
    private TextView mTvTitle;
    private TextView mTvBetTotal;
    private TextView mTvBetRest;
    private TextView mTvCreater;
    private TextView mTvCreaterPrize;
    private TextView mTvRewardNum;
    private TextView mTvRewardName;
    private TextView mTvRewardBet;
    private TextView mTvTotalCost;

    private boolean isRefresh = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottery_detail);
        EventBus.getDefault().register(this);

        lotterId = getIntent().getStringExtra(ConstantCode.CommonConstant.LOTTERYID);
        curUser = UserInfoDBHelper.getInstance().getCurPrimaryAccount();

        initToolBar();

        initViews();

        updateView();

        if (getIntent().getBooleanExtra("startBet", false))
        {
            LotteryBetDialog lotteryBetDialog = new LotteryBetDialog(LotteryDetailActivity.this,
                    lottery);
            lotteryBetDialog.show();
        }

    }

    private void initViews()
    {
        mBet = (Button) findViewById(R.id.btn_lottery_bet);
        mShare = (Button) findViewById(R.id.btn_lottery_share);
        mBtnLinerlayout = (LinearLayout) findViewById(R.id.ll_lottery_btn);
        mRecyclerView = (LRecyclerView) findViewById(R.id.partake_list);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.setLoadMoreEnabled(true);

        mRecyclerView.setPullRefreshEnabled(true);
        mRecyclerView.setRefreshHeader(new OLRefreshHeader(this));

        mDataAdapter = new LotteryAttendAdapter(this,lotterId);
        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);

        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        View firstHeader = LayoutInflater.from(this).inflate(R.layout.lottery_detail_header,
                (ViewGroup) findViewById(android.R.id.content), false);
        mLRecyclerViewAdapter.addHeaderView(firstHeader);

        //活动描述显示
        mTvCountDown = (TextView) firstHeader.findViewById(R.id.lottery_count_down);
        mRelCountDown = (RelativeLayout) firstHeader.findViewById(R.id.rl_count_down);
        mBtnCountDown = (Button) firstHeader.findViewById(R.id.btn_count_down_icon);
        mTvTime = (TextView) firstHeader.findViewById(R.id.tv_lottery_time);

        mBtnCalculate = (TextView) firstHeader.findViewById(R.id.btn_lottery_calculate);
        mLotteryImage = (ImageView) firstHeader.findViewById(R.id.img_lottery_icon);
        mTvDescribtion = (TextView) firstHeader.findViewById(R.id.tv_lottery_descrition);
        mTvRule = (TextView) firstHeader.findViewById(R.id.tv_lottery_rule);
        mTvTitle = (TextView) firstHeader.findViewById(R.id.tv_lottery_title);

        mBtnCalculate.setOnClickListener(this);
        mShare.setOnClickListener(this);

        //活动投注情况显示
        mLyMainContent = (LinearLayout) firstHeader.findViewById(R.id.ll_main_content);
        mBtnAddConcern = (Button) firstHeader.findViewById(R.id.btn_add_concern);
        mProPercent = (ProgressBar) firstHeader.findViewById(R.id.pro_lottery_percent);
        mTvPercent = (TextView) firstHeader.findViewById(R.id.tv_lottery_percent);

        mTvBetTotal = (TextView) firstHeader.findViewById(R.id.tv_bet_total_count);
        mTvBetRest = (TextView) firstHeader.findViewById(R.id.tv_rest_bet_count);
        mTvCreater = (TextView) firstHeader.findViewById(R.id.tv_lottery_creater);

        mBtnAddConcern.setOnClickListener(this);

        //活动中奖界面显示
        mLyEnd = (LinearLayout) firstHeader.findViewById(R.id.ll_end);
        mTvCreaterPrize = (TextView) firstHeader.findViewById(R.id.tv_lottery_prize_creater);
        mTvRewardNum = (TextView) firstHeader.findViewById(R.id.tv_prize_number);
        mTvRewardName = (TextView) firstHeader.findViewById(R.id.tv_lottery_reward_name);
        mTvRewardBet = (TextView) firstHeader.findViewById(R.id.tv_reward_bet_count);
        mTvTotalCost = (TextView) firstHeader.findViewById(R.id.tv_reward_bet_total_cost);
        mBtnAddRewardCn = (Button) firstHeader.findViewById(R.id.btn_add_reward_concern);
        mBtnAddCreaterCn = (Button) firstHeader.findViewById(R.id.btn_add_creater_concern);

        if(!CommonUtils.isLanguageChinese())
        {
            mBtnCountDown.getLayoutParams().width = (int) getResources().getDimension(R.dimen.x140);
            mBtnAddRewardCn.getLayoutParams().width = (int) getResources().getDimension(R.dimen.x140);
            mBtnAddCreaterCn.getLayoutParams().width = (int) getResources().getDimension(R.dimen.x140);
        }

        //我投注消息展示
        mTvMyText = (TextView) firstHeader.findViewById(R.id.tv_my_bet_text);

        mRelMyBet = (RelativeLayout) firstHeader.findViewById(R.id.rl_bet_count_and_check);
        mBtnCheck = (Button) firstHeader.findViewById(R.id.btn_lottery_detail_check);
        mTvMyBet = (TextView) firstHeader.findViewById(R.id.tv_lottery_my_bet);

        //参与列表数据
        requestData();

        //下拉刷新
        setRefreshListen();

        //加载更多
        setOnLoadMoreListen();

        mLRecyclerViewAdapter.setOnItemClickListener(this);
    }

    private void updateView()
    {
        lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId(lotterId);
        if (lottery == null)
        {
            return;
        }
//      lottery.setState(ConstantCode.OneLotteryState.ONELOTTERY_STATE_CAN_REWARD);
        state = lottery.getState();
        OLLogger.i(TAG, "update lottery=" + lottery);
        OLLogger.i(TAG, "state=" + state);

        //活动描述显示
        mLotteryImage.setImageResource(ImageUtils.getLotterHeaderImg(lottery.getPictureIndex()));

        Glide.with(LotteryDetailActivity.this).load(ImageUtils.getLotterHeaderImg(lottery.getPictureIndex())).into(mLotteryImage);

        mTvDescribtion.setText(lottery.getDescription());
        mTvTitle.setText(lottery.getLotteryName());

        PrizeRule prizeRule = PrizeRuleDBHelper.getInstance().getPrizeRuleById(lottery.getRuleId());
        if (null != prizeRule)
        {
            mTvRule.setText(String.format(getString(R.string.lottery_detail_rule_percent),
                    prizeRule.getPercentage() + "%"));
        }


        mRelCountDown.setVisibility(View.GONE);

        switch (state)
        {
            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_NOT_STARTED:
                mRelCountDown.setVisibility(View.VISIBLE);

                mTvTime.setText(getSpannableString(R.string.lottery_detail_deadline, false));

                // test start
                lottery.setStartTime(new Date(lottery.getStartTime().getTime()));
                OLLogger.i(TAG, "setCountDown = " + lottery.getStartTime());
                // test end
                if (lottery.getStartTime().getTime() > System.currentTimeMillis())
                {
                    setCountDown(lottery.getStartTime().getTime() - System.currentTimeMillis());
                } else
                {
                    mRelCountDown.setVisibility(View.GONE);
                }

                String publisherHash = lottery.getPublisherHash();
                if (curUser != null && publisherHash.equals(curUser.getWalletAddr()))
                {
                    mBet.setText(getString(R.string.lottery_detail_revise));
                    mBet.setBackgroundResource(R.drawable.selector_my_lottery_bet_btn_bg);
                    mBet.setEnabled(true);
                    mBet.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            Intent intent = new Intent(LotteryDetailActivity.this,
                                    CreateLotteryActivity.class);
                            intent.putExtra(ConstantCode.CommonConstant.TYPE,
                                    CreateLotteryActivity.MODIFY_LOTTERY_TYPE);
                            intent.putExtra(ConstantCode.CommonConstant.LOTTERYID, lottery
                                    .getLotteryId());
                            startActivity(intent);
                        }
                    });
                } else
                {
                    mBet.setBackgroundResource(R.drawable.selector_lottery_bet_btn_bg);
                    mBet.setEnabled(false);
                }
                break;

            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_ON_GOING:
                mTvTime.setText(getSpannableString(R.string.lottery_detail_deadline, false));

                mBet.setText(getString(R.string.bet));
                mBet.setBackgroundResource(R.drawable.selector_my_lottery_bet_btn_bg);
                mBet.setEnabled(true);
                mBet.setOnClickListener(this);
                break;

            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_CAN_REWARD:
                mTvTime.setText(getSpannableString(R.string.lottery_detail_deadline, false));

                mBet.setBackgroundResource(R.color.lottery_detaile_bet_gray_color);
                mBet.setEnabled(false);
                break;

            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_REWARD_ING:
                mTvTime.setText(getString(R.string.lottery_detail_reward_on_going));
                mTvTime.setTextColor(getResources().getColor(R.color.app_primary_color));

                mBet.setBackgroundResource(R.color.lottery_detaile_bet_gray_color);
                mBet.setEnabled(false);
                break;

            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_FAIL:
                mTvTime.setText(getSpannableString(R.string.lottery_detail_lottery_fail, false));
                mBtnLinerlayout.setVisibility(View.GONE);
                break;

            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_CAN_REFUND:
                mTvTime.setText(getSpannableString(R.string.lottery_detail_lottery_can_refund,
                        false));
                mBtnLinerlayout.setVisibility(View.GONE);
                break;

            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_REFUND_ING:
                mTvTime.setText(getSpannableString(R.string.lottery_detail_lottery_can_refund,
                        false));
                mBtnLinerlayout.setVisibility(View.GONE);
                break;

            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_REFUND_ALREADY:
                mTvTime.setText(getSpannableString(R.string
                        .lottery_detail_lottery_refund_already, false));
                mBtnLinerlayout.setVisibility(View.GONE);
                break;

            case ConstantCode.OneLotteryState.ONELOTTERY_STATE_REWARD_ALREADY:
                mTvTime.setText(getSpannableString(R.string.lottery_detail_end_time, true));
                mBtnLinerlayout.setVisibility(View.GONE);
                break;

            default:
                break;
        }

        if (state != ConstantCode.OneLotteryState.ONELOTTERY_STATE_REWARD_ALREADY)
        {
            //活动投注情况显示
            mLyMainContent.setVisibility(View.VISIBLE);
            mLyEnd.setVisibility(View.GONE);

            //活动创建者
            mTvCreater.setText(lottery.getPublisherName2());

            mTvPercent.setText(StringUtils.getStringPercent(lottery.getProgress() / 100f));
            mProPercent.setProgress(lottery.getProgress());

            mTvBetTotal.setText(String.valueOf(lottery.getMaxBetCount()));

            //设置剩余注数
            mTvBetRest.setText(String.valueOf(lottery.getMaxBetCount() - (lottery.getCurBetCount
                    () == null ? 0 : lottery.getCurBetCount())));

            // 官方和我自己的
            String publisherHash = lottery.getPublisherHash();
            setBtnSatatus(publisherHash, mBtnAddConcern);
        } else
        {
            //活动中奖界面显示
            mLyMainContent.setVisibility(View.GONE);
            mLyEnd.setVisibility(View.VISIBLE);

            mTvCreaterPrize.setText(lottery.getPublisherName2());
            mTvRewardNum.setText(getString(R.string.lottery_detail_luckey_number) + lottery
                    .getRewardNumbers());

            OneLotteryBet lotteryBet = OneLotteryBetDBHelper.getInstance().getLotteryBetByTxID
                    (lottery.getPrizeTxID());
            if (null != lotteryBet)
            {
                mTvRewardName.setText(lotteryBet.getAttendeeName());

                List<String> betNumbers = OneLotteryBetDBHelper.getInstance().getBetNumbers
                        (lotteryBet.getAttendeeName(), lotterId);
                String countString = String.format(getString(R.string.lottery_detail_bet_count), betNumbers.size());
                int index = countString.indexOf("" + betNumbers.size());
                SpannableString spnConuntString = new SpannableString(countString);
                spnConuntString.setSpan(new ForegroundColorSpan(getResources().
                        getColor(R.color.app_primary_color)), index, index + String.valueOf(betNumbers.size()).length(), Spanned
                        .SPAN_EXCLUSIVE_EXCLUSIVE);
                mTvRewardBet.setText(spnConuntString);

                long l = (lottery.getOneBetCost() * lottery.getMaxBetCount()) / 10000;
                String totalString = String.format(getString(R.string.lottery_detail_bet_total_cost), l);
                int index1 = totalString.indexOf(l + "");
                SpannableString spnTotalString = new SpannableString(totalString);
                spnTotalString.setSpan(new ForegroundColorSpan(getResources().
                        getColor(R.color.app_primary_color)), index1, index1 + String.valueOf(l).length(), Spanned
                        .SPAN_EXCLUSIVE_EXCLUSIVE);
                mTvTotalCost.setText(spnTotalString);

                // 公钥hash判断是否是我的好友
                String attendeeHash = lotteryBet.getAttendeeHash();
                setBtnSatatus(attendeeHash, mBtnAddRewardCn);
            }

            // 官方和我自己的
            String publisherHash = lottery.getPublisherHash();
            setBtnSatatus(publisherHash, mBtnAddCreaterCn);
        }

        //我投注消息展示
        List<OneLotteryBet> lotteryBetList = OneLotteryBetDBHelper.getInstance()
                .getMyBetByLotteryId(lotterId);
        if (null != lotteryBetList)
        {
            mTvMyText.setVisibility(View.GONE);
            mRelMyBet.setVisibility(View.VISIBLE);

            int betCount = 0;
            for (int i = 0; i < lotteryBetList.size(); i++)
            {
                betCount += lotteryBetList.get(i).getBetCount() == null ? 0 : lotteryBetList.get
                        (i).getBetCount();
            }

            String format = String.format(getString(R.string.lottery_detail_my_add_bet), betCount);
            int index = format.indexOf(betCount + "");
            SpannableString string = new SpannableString(format);
            string.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                    .app_primary_color)), index, index + String.valueOf(betCount).length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            mTvMyBet.setText(string);

            mBtnCheck.setOnClickListener(this);
        } else
        {
            mTvMyBet.setVisibility(View.VISIBLE);
            mRelMyBet.setVisibility(View.GONE);

            if (state == ConstantCode.OneLotteryState.ONELOTTERY_STATE_NOT_STARTED)
            {
                mTvMyText.setText(getString(R.string.lottery_detail_lottery_start));
            } else
            {
                mTvMyText.setText(getString(R.string.lottery_detail_not_add_lottery));
            }
        }

        attendList = OneLotteryBetDBHelper.getInstance().getAttendLotteryByLotteryId(lotterId);
        TOTAL_COUNTER = attendList.size();
    }

    private void initToolBar()
    {
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(getString(R.string.lottery_details));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_lottery_calculate:

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

                Intent intent = new Intent(LotteryDetailActivity.this,
                        CalculateDetailActivity.class);
                intent.putExtra(ConstantCode.CommonConstant.LOTTERYID, lotterId);
                startActivity(intent);
                break;

            case R.id.btn_lottery_detail_check:
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

                UserInfo curPrimaryAccount = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
                List numberIdList = OneLotteryBetDBHelper.getInstance().getBetNumbers
                        (curPrimaryAccount.getUserId(), lotterId);
                BetNumberDialog frgment = new BetNumberDialog(this, numberIdList);
                frgment.show();
                break;

            case R.id.btn_add_reward_concern:
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

                if (!OneLotteryApi.isVisitor(this))
                {
                    mBtnAddRewardCn.setBackgroundResource(R.drawable
                            .lottery_detail_add_concern_already);
                    mBtnAddRewardCn.setTextColor(getResources().getColor(R.color
                            .add_concern_btn_color));
                    mBtnAddRewardCn.setCompoundDrawables(null, null, null, null);
                    mBtnAddRewardCn.setText(getString(R.string.lottery_detail_add_concern_already));

                    String prizeTxID = lottery.getPrizeTxID();
                    OneLotteryBet oneLotteryBet = OneLotteryBetDBHelper.getInstance().getBetByTxId(prizeTxID);
                    if (null != oneLotteryBet)
                    {
                        addFriendConcern(oneLotteryBet.getAttendeeName(), oneLotteryBet.getAttendeeHash());
                    }

                    if(oneLotteryBet.getAttendeeHash().equals(lottery.getPublisherHash()))
                    {
                        mBtnAddCreaterCn.setBackgroundResource(R.drawable
                                .lottery_detail_add_concern_already);
                        mBtnAddCreaterCn.setTextColor(getResources().getColor(R.color
                                .add_concern_btn_color));
                        mBtnAddCreaterCn.setCompoundDrawables(null, null, null, null);
                        mBtnAddCreaterCn.setText(getString(R.string
                                .lottery_detail_add_concern_already));
                    }
                }
                break;

            case R.id.btn_add_creater_concern:
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

                if (!OneLotteryApi.isVisitor(this))
                {
                    mBtnAddCreaterCn.setBackgroundResource(R.drawable
                            .lottery_detail_add_concern_already);
                    mBtnAddCreaterCn.setTextColor(getResources().getColor(R.color
                            .add_concern_btn_color));
                    mBtnAddCreaterCn.setCompoundDrawables(null, null, null, null);
                    mBtnAddCreaterCn.setText(getString(R.string
                            .lottery_detail_add_concern_already));

                    addFriendConcern(lottery.getPublisherName(), lottery.getPublisherHash());

                    OneLotteryBet lotteryBet = OneLotteryBetDBHelper.getInstance().getLotteryBetByTxID(lottery.getPrizeTxID());
                    String attendeeHash = lotteryBet.getAttendeeHash();
                    if(lottery.getPublisherHash().equals(attendeeHash))
                    {
                        mBtnAddRewardCn.setBackgroundResource(R.drawable.lottery_detail_add_concern_already);
                        mBtnAddRewardCn.setTextColor(getResources().getColor(R.color
                                .add_concern_btn_color));
                        mBtnAddRewardCn.setCompoundDrawables(null, null, null, null);
                        mBtnAddRewardCn.setText(getString(R.string
                                .lottery_detail_add_concern_already));
                    }
                }
                break;

            case R.id.btn_add_concern:
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

                if (!OneLotteryApi.isVisitor(this))
                {
                    mBtnAddConcern.setBackgroundResource(R.drawable
                            .lottery_detail_add_concern_already);
                    mBtnAddConcern.setTextColor(getResources().getColor(R.color
                            .add_concern_btn_color));
                    mBtnAddConcern.setCompoundDrawables(null, null, null, null);
                    mBtnAddConcern.setText(getString(R.string.lottery_detail_add_concern_already));

                    addFriendConcern(lottery.getPublisherName(), lottery.getPublisherHash());
                }
                break;

            case R.id.btn_lottery_bet:

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

                if (!OneLotteryApi.isVisitor(this))
                {
                    showBetDialog(this, lottery);
                }
                break;

            case R.id.btn_lottery_share:
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

                SharePlatform.getInstance().share(LotteryDetailActivity.this, lottery);
                break;
        }
    }

    /**
     * 加好友关注逻辑
     */
    private void addFriendConcern(String name, String hash)
    {
        UserInfo curPrimaryAccount = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        String userId = curPrimaryAccount.getUserId();

        Friend friend = new Friend();
        friend.setFriendId(name);
        friend.setFriendHash(hash);
        friend.setUserId(userId);
        FriendDBHelper.getInstance().insertFriend(friend);

        attendList = OneLotteryBetDBHelper.getInstance().getAttendLotteryByLotteryId(lotterId);
        TOTAL_COUNTER = attendList.size();

        mDataAdapter.setDataList(attendList);


        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                OneLotteryManager.getInstance().getLotteries(false, false);
            }
        }).start();
    }

    private void setOnLoadMoreListen()
    {
        mRecyclerView.setOnLoadMoreListener(new OnLoadMoreListener()
        {
            @Override
            public void onLoadMore()
            {
                LoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState
                        (mRecyclerView);
                if (state == LoadingFooter.State.Loading)
                {
                    return;
                }

                if (mCurrentCounter < TOTAL_COUNTER)
                {
                    // loading more
                    RecyclerViewStateUtils.setFooterViewState(LotteryDetailActivity.this,
                            mRecyclerView, REQUEST_COUNT, LoadingFooter.State.Loading, null);
                    requestData();
                } else
                {
                    //the end
                    RecyclerViewStateUtils.setFooterViewState(LotteryDetailActivity.this,
                            mRecyclerView, REQUEST_COUNT, LoadingFooter.State.TheEnd, null);

                }
            }
        });
    }

    private void setRefreshListen()
    {
        mRecyclerView.setOnRefreshListener(new OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        isRefresh = true;
                        OneLotteryManager.getInstance().getLotteryDetail(lotterId);
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (isRefresh)
                                {
                                    isRefresh = false;
                                    mRecyclerView.refreshComplete();
                                }
                            }
                        });
                    }
                }).start();
            }
        });
    }

    /**
     * 请求底部参与列表数据
     */
    private void requestData()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                super.run();
                try
                {
                    Thread.sleep(80);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                // 更新界面
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (NetworkUtil.isNetworkConnected() && OneLotteryManager.isServiceConnect)
                        {
                            if (LotteryDetailActivity.this.isRefresh)
                            {
                                mDataAdapter.clear();
                                mCurrentCounter = 0;
                            }

                            int currentSize = mDataAdapter.getItemCount();

                            //模拟组装10个数据
                            ArrayList<AttendBean> newList = new ArrayList<>();
                            for (int i = 0; i < 20; i++)
                            {
                                if (newList.size() + currentSize >= TOTAL_COUNTER)
                                {
                                    break;
                                }
                                newList.add(attendList.get(currentSize + i));
                            }

                            if (LotteryDetailActivity.this.isRefresh)
                            {
                                LotteryDetailActivity.this.isRefresh = false;
                                LotteryDetailActivity.this.mRecyclerView.refreshComplete();
                            }

                            addItems(newList);

                            RecyclerViewStateUtils.setFooterViewState(mRecyclerView, LoadingFooter
                                    .State.Normal);
                            mDataAdapter.notifyDataSetChanged();
                        } else
                        {
                            if (LotteryDetailActivity.this.isRefresh)
                            {
                                LotteryDetailActivity.this.isRefresh = false;
                                LotteryDetailActivity.this.mRecyclerView.refreshComplete();
                            }
                            mDataAdapter.notifyDataSetChanged();
                            RecyclerViewStateUtils.setFooterViewState(LotteryDetailActivity.this,
                                    mRecyclerView,
                                    REQUEST_COUNT, LoadingFooter.State.NetWorkError, mFooterClick);
                        }
                    }
                });
            }
        }.start();
    }

    private void addItems(ArrayList<AttendBean> list)
    {
        mDataAdapter.addAll(list);
        mCurrentCounter += list.size();
    }

    /**
     * 点击底部加载更多
     */
    private View.OnClickListener mFooterClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            RecyclerViewStateUtils.setFooterViewState(LotteryDetailActivity.this, mRecyclerView,
                    REQUEST_COUNT, LoadingFooter.State.Loading, null);
            requestData();
        }
    };

    /**
     * 倒计时显示
     *
     * @param mill
     */
    private void setCountDown(final long mill)
    {
        final long time = System.currentTimeMillis();
        resetTimer();
        timer = new CountDownTimer(mill, 1000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                if (millisUntilFinished > 24 * 3600 * 1000)
                {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy - MM - dd");
                    mTvCountDown.setText(simpleDateFormat.format(DateUtils.getDateFromLong(mill +
                            time)));
                    mBtnCountDown.setText(getString(R.string.lottery_detail_start_date));
                    return;
                }

                int mDisplayDays = (int) ((millisUntilFinished / 1000) / 86400);
                int mDisplayHours = (int) (((millisUntilFinished / 1000) - (mDisplayDays * 86400)
                ) / 3600);
                int mDisplayMinutes = (int) (((millisUntilFinished / 1000) - ((mDisplayDays *
                        86400) + (mDisplayHours * 3600))) / 60);
                int mDisplaySeconds = (int) ((millisUntilFinished / 1000) % 60);

                String displayHours = mDisplayHours < 10 ? ("0" + mDisplayHours) : String.valueOf
                        (mDisplayHours);
                String displayMinutes = mDisplayMinutes < 10 ? ("0" + mDisplayMinutes) : String
                        .valueOf(mDisplayMinutes);
                String displaySeconds = mDisplaySeconds < 10 ? ("0" + mDisplaySeconds) : String
                        .valueOf(mDisplaySeconds);

                mTvCountDown.setText(displayHours + " : " + displayMinutes + " : " +
                        displaySeconds);
                mBtnCountDown.setText(getString(R.string.lottery_detail_start_countdown));
            }

            @Override
            public void onFinish()
            {
                // 时间结束刷新界面
                mRelCountDown.setVisibility(View.GONE);
                mBet.setBackgroundResource(R.drawable.selector_my_lottery_bet_btn_bg);
                mBet.setEnabled(true);
                mBet.setText(R.string.bet);
                mBet.setOnClickListener(LotteryDetailActivity.this);

                resetTimer();
            }
        }.start();
    }

    @NonNull
    private SpannableString getSpannableString(int stringID, boolean isOpen)
    {
        SimpleDateFormat format = new SimpleDateFormat(ConstantCode.CommonConstant
                .SIMPLE_DATE_FORMAT);
        Date time = null;
        if (isOpen)
        {
            time = lottery.getLastCloseTime();
        } else
        {
            time = lottery.getCloseTime();
        }
        String formatTime = format.format(time);
        String string = String.format(getString(stringID), formatTime);
        int index = string.indexOf(formatTime);
        SpannableString spannableString = new SpannableString(string);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                .app_primary_color)), index, string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    private void resetTimer()
    {
        if (null != timer)
        {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        resetTimer();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (this.isRefresh)
        {
            this.isRefresh = false;
            this.mRecyclerView.refreshComplete();
        }
        mCurrentCounter = 0;
    }

    @Override
    public void onItemClick(View view, int position)
    {
        if (!OneLotteryApi.isVisitor(this))
        {
            AttendBean bean = attendList.get(position);
            List ticketIdList = OneLotteryBetDBHelper.getInstance().getPerBetNumbers(
                    (bean.getBetNumbers()));
            BetNumberDialog frgment = new BetNumberDialog(this, ticketIdList);
            frgment.show();
        }
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
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_NOTIFY:
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_OVER_NOTIFY:
            case OLMessageModel.STMSG_MODEL_OPEN_REWARD_CALLBACK:
            case OLMessageModel.STMSG_MODEL_OPEN_REWARD_NOTIFY:
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_REFUND_NOTIFY:
            {
                String lotteryId = (String) model.getEventObject();
                if (!lotteryId.equals(lotterId))
                {
                    OLLogger.d(TAG, "!!!Is not bet of this lottery");
                    return;
                }

                updateView();

                refreshAttendList();
            }
            break;

            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_BET_CALLBACK:
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_CONSENSUS_OVER_NOTIFY:
            {
                boolean result = false;
                if (model.getEventType() == OLMessageModel
                        .STMSG_MODEL_ONE_LOTTERY_CONSENSUS_OVER_NOTIFY)
                {
                    String lotteryId = (String) model.getEventId();
                    if (!lotteryId.equals(lotterId))
                    {
                        OLLogger.d(TAG, "!!!Is not bet of this lottery");
                        return;
                    }
                    result = true;
                } else
                {
                    result = (boolean) model.getEventObject();
                }

                if (result)
                {
                    updateView();

                    refreshAttendList();
                }

                WaitingDialog waitingDialog = getWaitingDialog();
                if (waitingDialog != null && waitingDialog.isShowing())
                {
                    if (result)
                    {
                        getWaitingDialog().setBtnText(getString(R.string.message_bet_success));
                    } else
                    {
                        getWaitingDialog().setBtnText(getString(R.string.message_bet_fail));
                    }
                }
                // 获取用户余额，刷新UI
                OneLotteryManager.getInstance().getUserBalance();
            }
            break;
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_MODIFY_CALLBACK_SUCCESS:
            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_CAN_REFUND_NOTIFY:
            {
                updateView();

                refreshAttendList();
            }
            break;

            case OLMessageModel.STMSG_MODEL_REFRSH_LOTTERY_DETAIL:

                updateView();
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

            default:
                break;

            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_DELETE_CALLBACK_SUCCESS:
                finish();
                break;
        }
    }

    private void refreshAttendList()
    {
        attendList = OneLotteryBetDBHelper.getInstance()
                .getAttendLotteryByLotteryId(lotterId);
        TOTAL_COUNTER = attendList.size();
        mDataAdapter.setDataList(attendList);
        mCurrentCounter = mDataAdapter.getItemCount();
    }

    public void setBtnSatatus(String hash, Button button)
    {
        UserInfo curPrimaryAccount = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (null != curPrimaryAccount && curPrimaryAccount.getWalletAddr().toString().equals(hash))
        {
            button.setVisibility(View.GONE);
            return;
        }

        if (ConstantCode.CommonConstant.ONELOTTERY_DEFAULT_OFFICAL_HASH.equals(hash))
        {
            button.setVisibility(View.GONE);
            return;
        }

        boolean myFriend = FriendDBHelper.getInstance().isMyFriend(hash);
        if (!myFriend)
        {
            button.setVisibility(View.VISIBLE);
            button.setEnabled(true);
            button.setOnClickListener(this);
        } else
        {
            button.setBackgroundResource(R.drawable
                    .lottery_detail_add_concern_already);
            button.setTextColor(getResources().getColor(R.color
                    .add_concern_btn_color));
            button.setCompoundDrawables(null, null, null, null);
            button.setText(getString(R.string
                    .lottery_detail_add_concern_already));
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(mDataAdapter.isAddFriend())
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    OneLotteryManager.getInstance().getLotteries(false,true);
                }
            }).start();
        }
    }

}
