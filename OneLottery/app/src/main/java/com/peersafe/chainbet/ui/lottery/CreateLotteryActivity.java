package com.peersafe.chainbet.ui.lottery;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.datetime.DateUtil;
import com.datetime.MyPickerDialog;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.MessageNotify;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.PrizeRule;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.MessageNotifyDBHelper;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.manager.dbhelper.PrizeRuleDBHelper;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.ImageUtils;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.config.OLPreferenceUtil;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.utils.view.LotteryIconDialog;
import com.peersafe.chainbet.widget.InputPwdDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CreateLotteryActivity extends BasicActivity implements View.OnClickListener
{
    private static final int SELECT_RULE_FLAG = 0x101;
    private static final int SELECT_DURATION_FLAG = 0x102;
    private static final int SELECT_ONEBETCOST_FLAG = 0x103;

    public static CreateLotteryActivity instance = null;
    //创建活动类型
    public static final int CREAT_LOTTERY_TYPE = 0;

    //修改活动
    public static final int MODIFY_LOTTERY_TYPE = 1;

    private static final int MAX_TOTAL_BET = 10000;

    private int mCurOperType;

    //活动开始时间
    private TextView mStartTime;
    private TextView mDuration;

    //总共投注
    private EditText mTotalBet;

    //募集金额
    private TextView mRaiseAmount;

    //活动标题
    private EditText mTitle;
    //活动描述
    private EditText mDescribe;

    private TextView mTitleLen;
    private TextView mRuleName;
    private TextView mSelLabel;
    private TextView mOneBetCost;

    //活动图标
    private ImageButton mLotteryIcon;

    //按钮
    private Button mBtnCreate;

    //删除按钮
    private Button mBtnDelete;

    private String lotterId;
    public static OneLottery lottery;

    private PrizeRule prizeRule;

    private int totalBet = 0;
    private int oneBetCost = 0;

    private int pictureIndex = -1;
    private MyPickerDialog myPicker;
    private MyPickerDialog.Builder.Bean dateBean;

    //密码输入框
    private InputPwdDialog inputPwdDialog;
    private int durationPos = 0;
    private int oneBetCostPos = 0;
    private int[] durationArr, onebetCostArr;
    private ArrayList dateTimeList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_lottery);
        EventBus.getDefault().register(this);
        instance = this;

        mCurOperType = getIntent().getIntExtra(ConstantCode.CommonConstant.TYPE, 0);
        lotterId = getIntent().getStringExtra(ConstantCode.CommonConstant.LOTTERYID);
        lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId(lotterId);

        initToolBar();

        initPicker();

        durationArr = getResources().getIntArray(R.array.lottery_duration_i);
        onebetCostArr = getResources().getIntArray(R.array.one_bet_cost_i);

        initViews();
    }

    private void initPicker()
    {
        dateTimeList = new ArrayList<>(731);
        for (int i = 0; i < 731; i++)
        {
            Calendar now = Calendar.getInstance();
            now.add(Calendar.DAY_OF_MONTH, i - 365);
            String date = DateUtil.formatDate(now.getTimeInMillis(), getString(R.string.date_fromat));
            dateTimeList.add(date);
        }
        dateTimeList.remove(365);
        dateTimeList.add(365, getResources().getString(R.string.timepicker_today));

        Calendar now = Calendar.getInstance();
        dateBean = new MyPickerDialog.Builder.Bean(365, now.get(now.HOUR_OF_DAY), now.get(now.MINUTE));
    }

    private void initViews()
    {
        mTitle = (EditText) findViewById(R.id.et_lottery_title);
        mTitleLen = (TextView) findViewById(R.id.tv_title_len);

        mRuleName = (TextView) findViewById(R.id.tv_rule_name);
        mDescribe = (EditText) findViewById(R.id.et_lottery_describe);
        mLotteryIcon = (ImageButton) findViewById(R.id.ibt_lottery_icon);
        mSelLabel = (TextView) findViewById(R.id.tv_sel_label);
        mStartTime = (TextView) findViewById(R.id.et_lottery_start_time);
        mDuration = (TextView) findViewById(R.id.tv_lottery_duration);
        mTotalBet = (EditText) findViewById(R.id.et_total_bet);
        mOneBetCost = (TextView) findViewById(R.id.tv_one_bet_cost);
        mRaiseAmount = (TextView) findViewById(R.id.tv_raise_amount);

        mBtnCreate = (Button) findViewById(R.id.btn_create_lottery);
        mBtnDelete = (Button) findViewById(R.id.btn_delete_lottery);


        mTitle.setFilters(new InputFilter[]{StringUtils.getFilter("\n")/*, new InputFilter.LengthFilter(20)*/});
        mDescribe.setFilters(new InputFilter[]{StringUtils.getFilter("\n")/*,new InputFilter.LengthFilter(30)*/});
        mTitle.addTextChangedListener(new MyTextWatcher(mTitle, 20, mTitleLen));
        mDescribe.addTextChangedListener(new MyTextWatcher(mDescribe, 30, null));
        mLotteryIcon.setOnClickListener(this);

        mStartTime.setText(DateFormat.format(ConstantCode.CommonConstant.SIMPLE_DATE_FORMAT3, System.currentTimeMillis()));
        mStartTime.setOnClickListener(this);

        mBtnCreate.setOnClickListener(this);

        mTotalBet.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                String val = editable.toString();
                if (StringUtils.isEmpty(val))
                {
                    setTotalAmount(0);
                } else
                {
                    try
                    {
                        totalBet = Integer.parseInt(val);
                        if (totalBet > 0)
                        {
                            if (totalBet > MAX_TOTAL_BET)
                            {
                                mTotalBet.setText("" + MAX_TOTAL_BET);
                                mTotalBet.setSelection(("" + MAX_TOTAL_BET).length());
                            }
                            setTotalAmount(totalBet > 0 ? ((long) totalBet * oneBetCost) : 0);
                        } else
                        {
                            mTotalBet.setText("");
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

        //修改活动显示
        if (mCurOperType == MODIFY_LOTTERY_TYPE)
        {
            mBtnDelete.setVisibility(View.VISIBLE);
            mBtnDelete.setOnClickListener(this);

            mBtnCreate.setText(getString(R.string.lottery_detail_revise));

            mTitle.setText(lottery.getLotteryName());
            mTitle.setSelection(lottery.getLotteryName().length());
            mDescribe.setText(lottery.getDescription());

            Glide.with(CreateLotteryActivity.this).load(ImageUtils.getLotterySquare(lottery.getPictureIndex())).into(mLotteryIcon);
            mSelLabel.setVisibility(View.GONE);

            pictureIndex = lottery.getPictureIndex();

            //规则
            String ruleId = lottery.getRuleId();
            prizeRule = PrizeRuleDBHelper.getInstance().getPrizeRuleById(ruleId);
            setRule();

            // 开始时间
            if (lottery.getStartTime().getTime() > System.currentTimeMillis())
            {
                mStartTime.setText(DateFormat.format(ConstantCode.CommonConstant
                        .SIMPLE_DATE_FORMAT3, lottery.getStartTime()));
                Calendar cal = Calendar.getInstance();
                cal.setTime(lottery.getStartTime());

                Calendar now = Calendar.getInstance();
                dateBean.setPosition(365 + DateUtil.compareDate(now, cal, 0));

                dateBean.setHour(cal.get(cal.HOUR_OF_DAY));
                dateBean.setMinu(cal.get(cal.MINUTE));
            }

            //活动时长
            long days = (lottery.getCloseTime().getTime() - lottery.getStartTime().getTime()) / 86400000;
            for (int i = 0; i < durationArr.length; i++)
            {
                if (durationArr[i] == days)
                {
                    durationPos = i;
                    break;
                }
            }

            // 每注花费
            long oneBetCost = lottery.getOneBetCost() / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE;
            for (int i = 0; i < onebetCostArr.length; i++)
            {
                if (onebetCostArr[i] == oneBetCost)
                {
                    oneBetCostPos = i;
                    break;
                }
            }

            // 总注数，总金额
            totalBet = lottery.getMaxBetCount();
            mTotalBet.setText(String.valueOf(lottery.getMaxBetCount()));
            mTotalBet.setSelection(String.valueOf(lottery.getMaxBetCount()).length());
            setTotalAmount(lottery.getBetTotalAmount() / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE);
        }

        setDuration();
        setOneBetCost();
    }

    private void setTotalAmount(long amount)
    {
        String total = String.format(getString(R.string.create_lottery_peersafe_coin), amount);
        int len = 1;
        while ((amount /= 10) > 0)
        {
            len++;
        }
        SpannableString totalSpan = new SpannableString(total);
        totalSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.add_concern_btn_color)),
                len, total.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        mRaiseAmount.setText(totalSpan);
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        TextView title = (TextView) findViewById(R.id.tv_title);
        if (mCurOperType == CREAT_LOTTERY_TYPE)
        {
            title.setText(getString(R.string.lottery_create));
        } else
        {
            title.setText(getString(R.string.create_lottery_change_lottery));
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    private void setRule()
    {
        if (prizeRule != null)
        {
            mRuleName.setText(getString(R.string.create_lottery_rule) + prizeRule.getRuleName());
            mRuleName.setTextColor(getResources().getColor(R.color.common_text_color));
        } else {
            mRuleName.setText("");
            mRuleName.setTextColor(getResources().getColor(R.color.create_lottery_title_text_hint_color));
        }
    }

    public void selectRule(View view)
    {
        Intent intent = new Intent(CreateLotteryActivity.this, RuleActivity.class);
        if (prizeRule != null)
        {
            intent.putExtra(RuleActivity.KEY_RULE_ID, prizeRule.getRuleId());
        }
        startActivityForResult(intent, SELECT_RULE_FLAG);
        hideSoftKeyboard();
    }

    public void selectDuration(View view)
    {
        Intent intent = new Intent(CreateLotteryActivity.this, DurationActivity.class);
        intent.putExtra(DurationActivity.KEY_POSITION, durationPos);
        intent.putExtra(DurationActivity.KEY_TITLE, getString(R.string.create_lottery_duration_time_text));
        intent.putExtra(DurationActivity.KEY_ARRAY, getResources().getStringArray(R.array.lottery_duration));
        startActivityForResult(intent, SELECT_DURATION_FLAG);
        hideSoftKeyboard();
    }

    private void setDuration()
    {
        if (durationPos >= 0 && durationPos < durationArr.length)
        {
            int idur = getResources().getIntArray(R.array.lottery_duration_i)[durationPos];
            int len = 1;
            while ((idur /= 10) > 0)
            {
                len++;
            }
            String dur = getResources().getStringArray(R.array.lottery_duration)[durationPos];
            SpannableString durSpan = new SpannableString(dur);
            durSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.add_concern_btn_color)),
                    idur, dur.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            mDuration.setText(durSpan);
        } else
        {
            mDuration.setText("");
        }
    }

    public void selectOneBetCost(View view)
    {
        Intent intent = new Intent(CreateLotteryActivity.this, DurationActivity.class);
        intent.putExtra(DurationActivity.KEY_POSITION, oneBetCostPos);
        intent.putExtra(DurationActivity.KEY_TITLE, getString(R.string.create_lottery_bet_cost_text));
        intent.putExtra(DurationActivity.KEY_ARRAY, getResources().getStringArray(R.array.one_bet_cost));
        startActivityForResult(intent, SELECT_ONEBETCOST_FLAG);
        hideSoftKeyboard();
    }

    private void setOneBetCost()
    {
        if (oneBetCostPos >= 0 && oneBetCostPos < onebetCostArr.length)
        {
            int iobc = getResources().getIntArray(R.array.one_bet_cost_i)[oneBetCostPos];
            int len = 1;
            while ((iobc /= 10) > 0)
            {
                len++;
            }
            String obc = getResources().getStringArray(R.array.one_bet_cost)[oneBetCostPos];
            SpannableString obcSpan = new SpannableString(obc);
            obcSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.add_concern_btn_color)),
                    len, obc.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            mOneBetCost.setText(obcSpan);
            mOneBetCost.setTextColor(getResources().getColor(R.color.common_text_color));
            oneBetCost = onebetCostArr[oneBetCostPos];
            setTotalAmount(totalBet > 0 ? ((long) totalBet * oneBetCost) : 0);
        } else
        {
            mOneBetCost.setText("");
            mOneBetCost.setTextColor(getResources().getColor(R.color.create_lottery_title_text_hint_color));
            oneBetCost = 0;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case SELECT_RULE_FLAG:
                    String ruleId = data != null ? data.getStringExtra(RuleActivity.KEY_RULE_ID) : "";
                    if (!StringUtils.isEmpty(ruleId))
                    {
                        prizeRule = PrizeRuleDBHelper.getInstance().getPrizeRuleById(ruleId);
                        setRule();
                    }
                    break;
                case SELECT_DURATION_FLAG:
                    durationPos = data != null ? data.getIntExtra(DurationActivity.KEY_POSITION, 0) : 0;
                    setDuration();
                    break;
                case SELECT_ONEBETCOST_FLAG:
                    oneBetCostPos = data != null ? data.getIntExtra(DurationActivity.KEY_POSITION, 0) : 0;
                    setOneBetCost();
                    break;
            }
        }
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, R.anim.push_bottom_out);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.et_lottery_start_time:

                MyPickerDialog.Builder builder = new MyPickerDialog.Builder(this);

                myPicker = builder.setData(dateTimeList)
                        .setSelection(dateBean.getPosition())
                        .setHour(dateBean.getHour())
                        .setMin(dateBean.getMinu())
                        .setOnDataSelectedListener(new MyPickerDialog.OnDataSelectedListener()
                        {

                            @Override
                            public void onDataSelected(MyPickerDialog.Builder.Bean bean)
                            {
                                dateBean = bean;
                                checkTimeBefore();
                                checkTimeAfter();

                                Calendar cal = Calendar.getInstance();
                                cal.add(cal.DAY_OF_MONTH, dateBean.getPosition() - 365);
                                cal.set(cal.HOUR_OF_DAY, dateBean.getHour());
                                cal.set(cal.MINUTE, dateBean.getMinu());
                                mStartTime.setText(DateFormat.format(ConstantCode.CommonConstant.SIMPLE_DATE_FORMAT3, cal.getTime()));
                            }

                            // 判断是否小于当前时间
                            private void checkTimeBefore()
                            {
                                if (dateBean.getPosition() <= 365)
                                {
                                    dateBean.setPosition(365);

                                    Calendar now = Calendar.getInstance();
                                    int h = now.get(Calendar.HOUR_OF_DAY);
                                    int m = now.get(Calendar.MINUTE);
                                    if ((h == dateBean.getHour() && dateBean.getMinu() < m) ||
                                            dateBean.getHour() < h)
                                    {
                                        dateBean.setHour(now.get(Calendar.HOUR_OF_DAY));
                                        dateBean.setMinu(now.get(Calendar.MINUTE));
                                    }
                                }
                            }

                            // 判断是否超过第30天的当前时间
                            private void checkTimeAfter()
                            {
                                if (dateBean.getPosition() >= 394)
                                {
                                    dateBean.setPosition(394);

                                    Calendar now = Calendar.getInstance();
                                    int h = now.get(Calendar.HOUR_OF_DAY);
                                    int m = now.get(Calendar.MINUTE);
                                    if ((h == dateBean.getHour() && dateBean.getMinu() > m) ||
                                            dateBean.getHour() > h)
                                    {
                                        dateBean.setHour(now.get(Calendar.HOUR_OF_DAY));
                                        dateBean.setMinu(now.get(Calendar.MINUTE));
                                    }
                                }
                            }

                            @Override
                            public void onCancel()
                            {
                            }
                        }).create();

                myPicker.setOnDismissListener(new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        myPicker = null;
                    }
                });

                myPicker.show();
                break;

            case R.id.ibt_lottery_icon:

                LotteryIconDialog iconDialog = new LotteryIconDialog(this);
                iconDialog.show();
                break;

            case R.id.btn_delete_lottery:

                inputPwdDialog = new InputPwdDialog(this, InputPwdDialog
                        .DELETE_LOTTERY_TYPE, lottery);
                inputPwdDialog.show();
                break;

            case R.id.btn_create_lottery:

                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if(!OneLotteryManager.isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                // 判断是否有创建中或未结束的活动
                if (mCurOperType == CREAT_LOTTERY_TYPE)
                {
                    if (!StringUtils.isEmpty(OLPreferenceUtil.getInstance(CreateLotteryActivity.this).getAddLotteryName()))
                    {
                        showToast(getString(R.string.create_lottery_lottery_creating));
                        break;
                    }
                    List<OneLottery> myOnGoingLottery = OneLotteryDBHelper.getInstance().
                            getMyOnGoingLottery();
                    if (null != myOnGoingLottery && myOnGoingLottery.size() > 0)
                    {
                        showToast(getString(R.string.create_lottery_can_not_lottery));
                        break;
                    }
                }

                if (StringUtils.isEmpty(String.valueOf(mTitle.getText()).trim()))
                {
                    mTitle.requestFocus();
                    showToast(getString(R.string.create_lottery_title_empty));
                    break;
                }

                if (prizeRule == null)
                {
                    mRuleName.requestFocus();
                    showToast(getString(R.string.create_lottery_rule_empty));
                    break;
                }

                if (pictureIndex < 0)
                {
                    mLotteryIcon.requestFocus();
                    showToast(getString(R.string.create_lottery_chose_lottery_label));
                    mLotteryIcon.setBackgroundResource(R.drawable.create_lottery_label_red);
                    break;
                }

                if (durationPos < 0 || durationPos >= durationArr.length)
                {
                    mDuration.requestFocus();
                    showToast(getString(R.string.create_lottery_chose_duration));
                    break;
                }

                if (StringUtils.isEmpty(String.valueOf(mTotalBet.getText())))
                {
                    mTotalBet.requestFocus();
//                  mTotalBet.setBackgroundResource(R.drawable.create_lottery_orange_recentage);
                    showToast(getString(R.string.create_lottery_bet_count_empty));
                    break;
                }

                if (oneBetCost <= 0)
                {
                    mOneBetCost.requestFocus();
                    showToast(getString(R.string.create_lottery_chose_onebetcost));
                    break;
                }

                UserInfo curPrimaryAccount = UserInfoDBHelper.getInstance()
                        .getCurPrimaryAccount();
                if (curPrimaryAccount.getBalance() / ConstantCode.CommonConstant
                        .ONELOTTERY_MONEY_MULTIPLE < 1)
                {
                    showToast(getString(R.string.create_lottery_balance_insufficient));
                    break;
                }

                OneLottery oneLottery = createNewLottery();

                //TODO 活动状态暂时不设置，默认是0：未开始吧，还是客户端自己判断。不过客户端判断不准确
                if (mCurOperType == CREAT_LOTTERY_TYPE)
                {
                    inputPwdDialog = new InputPwdDialog(this, InputPwdDialog
                            .CREATE_LOTTERY_TYPE, oneLottery);

                    //处理网络速度慢导致context消掉
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    {
                        if (!instance.isDestroyed() || !instance.isFinishing())
                        {
                            inputPwdDialog.show();
                        }
                    }
                } else if (mCurOperType == MODIFY_LOTTERY_TYPE)
                {
                    inputPwdDialog = new InputPwdDialog(this, InputPwdDialog
                            .MODIFY_LOTTERY_TYPE, oneLottery);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    {
                        if (!instance.isDestroyed() || !instance.isFinishing())
                        {
                            inputPwdDialog.show();
                        }
                    }
                }

                break;
        }
    }

    @NonNull
    private OneLottery createNewLottery()
    {
        OneLottery oneLottery = null;
        if (mCurOperType == CREAT_LOTTERY_TYPE)
        {
            oneLottery = new OneLottery();

            UserInfo curUser = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
            if (curUser != null)
            {
                oneLottery.setPublisherName(curUser.getUserId());
                oneLottery.setPublisherHash(curUser.getWalletAddr());
            }
            oneLottery.setCreateTime(new Date());
            oneLottery.setUpdateTime(new Date());

        } else if (mCurOperType == MODIFY_LOTTERY_TYPE)
        {
            oneLottery = this.lottery;
            oneLottery.setUpdateTime(new Date());
        }
        oneLottery.setLotteryName(String.valueOf(mTitle.getText()).trim());
        oneLottery.setDescription(String.valueOf(mDescribe.getText()).trim());
        oneLottery.setPictureIndex(pictureIndex);
        oneLottery.setRuleId(prizeRule.getRuleId());
        oneLottery.setRuleType(OneLotteryApi.RuleType_Prize);
        oneLottery.setOneBetCost((long) oneBetCost * ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE);
        oneLottery.setMaxBetCount(totalBet);
        oneLottery.setMinBetCount(1);
        oneLottery.setProgress(0);

        String amountS = mRaiseAmount.getText().toString();
        long totalAmount = Long.parseLong(amountS.substring(0, amountS.indexOf(" ")).trim());
        oneLottery.setBetTotalAmount(totalAmount * ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE);

        // 选择的时间
        Calendar cal = Calendar.getInstance();
        cal.add(cal.DAY_OF_MONTH, dateBean.getPosition() - 365);
        cal.set(cal.HOUR_OF_DAY, dateBean.getHour());
        cal.set(cal.MINUTE, dateBean.getMinu());
        // 如果选择的时间比系统时间小，那就用系统时间+1s
        if (cal.getTimeInMillis() <= oneLottery.getUpdateTime().getTime())
        {
            cal.setTimeInMillis(oneLottery.getUpdateTime().getTime() + 1000);
            oneLottery.setStartTime(cal.getTime());
        } else
        {
            oneLottery.setStartTime(cal.getTime());
        }

        cal.add(Calendar.DAY_OF_MONTH, durationArr[durationPos]);
        oneLottery.setCloseTime(cal.getTime());

        return oneLottery;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(OLMessageModel model)
    {
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_CREATE_LOTTERY_SELECT_ICON:

                int pos = (int) model.getEventObject();
                pictureIndex = pos;
                mLotteryIcon.setImageResource(ImageUtils.getLotteryImageview(pictureIndex));
                mSelLabel.setVisibility(View.GONE);
                break;

            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_ADD_CALLBACK_SUCCESS:

                String csTxId = (String) model.getEventObject();

                if (null != getWaitingDialog() && getWaitingDialog().isShowing())
                {
                    dismissWaitingDialog();
                } else
                {
                    setMessageNotify(csTxId, getString(R.string.message_create_success), StringUtils.refactorLotteryName(lottery
                                    .getLotteryName())
                                    + getString(R.string.message_create_lottery_success),
                            ConstantCode.MessageType.MESSAGE_TYPE_CREATE_SUCCESS);
                }
                Intent intent = new Intent(this, CreateSuccessActivity.class);
                intent.putExtra(ConstantCode.CommonConstant.LOTTERYID, csTxId);
                intent.putExtra(ConstantCode.CommonConstant.TYPE, CreateSuccessActivity
                        .CREATE_LOTTERY_SUCCESS);
                startActivity(intent);
                finish();
                break;

            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_ADD_CALLBACK_FAIL:
                String cfTxId = (String) model.getEventObject();

                if (null != getWaitingDialog() && getWaitingDialog().isShowing())
                {
                    getWaitingDialog().setBtnText(getString(R.string.message_create_fail));
                } else
                {
                    setMessageNotify(cfTxId, getString(R.string.message_create_fail),
                            StringUtils.refactorLotteryName(lottery.getLotteryName()) + getString(R.string
                                    .message_create_lottery_fail),
                            ConstantCode.MessageType.MESSAGE_TYPE_CREATE_FAIL);
                }

                break;

            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_DELETE_CALLBACK_SUCCESS:
                String dfTxId = (String) model.getEventObject();

                // 修改为弹出修改成功的提示，不再直接跳转主界面
                if (null != getWaitingDialog() && getWaitingDialog().isShowing())
                {
//                    dismissWaitingDialog();
                    getWaitingDialog().setBtnText(getString(R.string.message_delete_success));
                } else
                {
                    setMessageNotify(dfTxId, getString(R.string.message_delete_success),
                            StringUtils.refactorLotteryName(lottery.getLotteryName()) + getString(R.string
                                    .message_delete_lottery_success),
                            ConstantCode.MessageType.MESSAGE_TYPE_DELETE_SUCCESS);

                }

                // 进入主界面。不管是从详情还是列表，或者创建成功页面过来，都应该删除后跳到主页
//                Intent mainIntent = new Intent(CreateLotteryActivity.this, MainActivity.class);
//                startActivity(mainIntent);
//
//                finish();
                break;

            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_DELETE_CALLBACK_FAIL:
                String dsTxId = (String) model.getEventObject();
                if (null != getWaitingDialog() && getWaitingDialog().isShowing())
                {
                    getWaitingDialog().setBtnText(getString(R.string.message_delete_fail));
                } else
                {
                    setMessageNotify(dsTxId, getString(R.string.message_delete_fail),
                            StringUtils.refactorLotteryName(lottery.getLotteryName()) + getString(R.string
                            .message_delete_lottery_fail),
                            ConstantCode.MessageType.MESSAGE_TYPE_DELETE_FAIL);
                }
                break;

            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_MODIFY_CALLBACK_SUCCESS:
                String msTxId = (String) model.getEventObject();

                if (null != getWaitingDialog() && getWaitingDialog().isShowing())
                {
                    dismissWaitingDialog();
                } else
                {
                    setMessageNotify(msTxId, getString(R.string.message_modify_success),
                            StringUtils.refactorLotteryName(lottery.getLotteryName()) + getString(R.string
                            .message_modify_lottery_success),
                            ConstantCode.MessageType.MESSAGE_TYPE_MODIFY_SUCCESS);
                }
                Intent intent1 = new Intent(this, CreateSuccessActivity.class);
                intent1.putExtra(ConstantCode.CommonConstant.LOTTERYID, lottery.getLotteryId());
                intent1.putExtra(ConstantCode.CommonConstant.TYPE, CreateSuccessActivity
                        .MODIFY_LOTTERY_SUCCESS);
                startActivity(intent1);
                finish();
                break;

            case OLMessageModel.STMSG_MODEL_ONE_LOTTERY_MODIFY_CALLBACK_FAIL:
                String mfTxId = (String) model.getEventObject();

                if (null != getWaitingDialog() && getWaitingDialog().isShowing())
                {
                    getWaitingDialog().setBtnText(getString(R.string.message_modify_fail));
                } else
                {
                    setMessageNotify(mfTxId, getString(R.string.message_modify_fail),
                            StringUtils.refactorLotteryName(lottery.getLotteryName()) + getString(R.string
                                    .message_modify_lottery_fail),
                            ConstantCode.MessageType.MESSAGE_TYPE_MODIFY_FAIL);
                }
                break;

            case OLMessageModel.STMSG_MODEL_CREATE_LOTTERY_FINISH:
                dismissWaitingDialog();
                finish();
                break;

            default:
                break;
        }
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

        if (inputPwdDialog != null)
        {
            inputPwdDialog.dismiss();
        }
    }

    //设置消息列表消息,因为这个时候点击了逛逛其他，dialog已经消失了，在这儿入库
    public void setMessageNotify(String txId, String content, String hornContent, int type)
    {
        MessageNotify messageNotify = new MessageNotify();
        messageNotify.setIsRead(false);
        messageNotify.setMsgId(txId);
        messageNotify.setHornContent(hornContent);
        messageNotify.setLotteryId(type == ConstantCode.MessageType.MESSAGE_TYPE_CREATE_SUCCESS
                ? txId : lottery.getLotteryId());
        messageNotify.setTitle(content);
        if (type == ConstantCode.MessageType.MESSAGE_TYPE_DELETE_SUCCESS)
        {
            messageNotify.setContent(lottery != null ? StringUtils.getHeadTailString(lottery
                    .getLotteryName()) : "");
        } else if (type != ConstantCode.MessageType.MESSAGE_TYPE_CREATE_FAIL)
        {
            messageNotify.setContent(getString(R.string.message_see_content));
        }
        messageNotify.setTime(new Date());
        messageNotify.setType(type);
        messageNotify.setUserId(OneLotteryApi.getCurUserId());
        MessageNotifyDBHelper.getInstance().insertMessageNotify(messageNotify);
    }

    private class MyTextWatcher implements TextWatcher
    {
        private EditText et;
        private TextView tl;
        private int len;
        public MyTextWatcher(EditText text, int length, TextView tvLen)
        {
            et = text;
            tl = tvLen;
            len = length;
        }


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {

        }

        @Override
        public void afterTextChanged(Editable s)
        {
            int l = s.toString().length();
            if (l > len) {
                int editEnd = et.getSelectionEnd();
                s.delete(l - 1, l);
                l = len;
                et.setText(s);
                et.setSelection(editEnd > len ? len : editEnd);
            }
            if (tl != null)
            {
                tl.setText(String.valueOf(len - l));
            }
        }
    }
}
