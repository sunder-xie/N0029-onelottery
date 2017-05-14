package com.peersafe.chainbet.ui.lottery;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.SharePlatform;

public class CreateSuccessActivity extends BasicActivity implements View.OnClickListener
{
    public static final int CREATE_LOTTERY_SUCCESS = 1;

    public static final int MODIFY_LOTTERY_SUCCESS = 2;

    private ImageView mImgHeader;
    private AnimationDrawable mAnimationDrawable;

    private OneLottery oneLottery;

    private int mCurType;
    private TextView mTvSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_success);

        String lotteryId = getIntent().getStringExtra(ConstantCode.CommonConstant.LOTTERYID);
        mCurType = getIntent().getIntExtra(ConstantCode.CommonConstant.TYPE, 0);
        oneLottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId(lotteryId);

        mImgHeader = (ImageView) findViewById(R.id.img_create_success);
        mTvSuccess = (TextView) findViewById(R.id.tv_success);
        mImgHeader.setImageResource(R.drawable.create_lottery_success_header_anim);
        mAnimationDrawable = (AnimationDrawable) mImgHeader.getDrawable();
        mAnimationDrawable.setOneShot(true);
        mAnimationDrawable.start();

        findViewById(R.id.btn_enter_lotterty).setOnClickListener(this);
        findViewById(R.id.btn_change_lottery).setOnClickListener(this);
        findViewById(R.id.btn_bet).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);

        initToolBar();
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) findViewById(R.id.tv_title);
        if (mCurType == CREATE_LOTTERY_SUCCESS)
        {
            title.setText(getString(R.string.message_create_success));
            mTvSuccess.setText(getString(R.string.message_create_success));
        } else if (mCurType == MODIFY_LOTTERY_SUCCESS)
        {
            title.setText(getString(R.string.message_modify_success));
            mTvSuccess.setText(getString(R.string.message_modify_success));
        }

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
            case R.id.btn_enter_lotterty:

                if(!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if(!OneLotteryManager.isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                Intent intent = new Intent(this, LotteryDetailActivity.class);
                intent.putExtra(ConstantCode.CommonConstant.LOTTERYID, oneLottery.getLotteryId());
                startActivity(intent);

                finish();
                break;

            case R.id.btn_change_lottery:
                if(!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if(!OneLotteryManager.isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                OneLottery lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId
                        (oneLottery.getLotteryId());
                if (lottery.getState() != null && lottery.getState() > ConstantCode
                        .OneLotteryState.ONELOTTERY_STATE_NOT_STARTED)
                {
                    showToast(getString(R.string.lottery_has_start_notify));
                    return;
                }
                Intent intent1 = new Intent(this, CreateLotteryActivity.class);
                intent1.putExtra(ConstantCode.CommonConstant.LOTTERYID, oneLottery.getLotteryId());
                intent1.putExtra(ConstantCode.CommonConstant.TYPE, CreateLotteryActivity
                        .MODIFY_LOTTERY_TYPE);
                startActivity(intent1);

                finish();
                break;

            case R.id.btn_bet:
                if(!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if(!OneLotteryManager.isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                if (oneLottery.getStartTime().getTime() > System.currentTimeMillis())
                {
                    showToast(getString(R.string.lottery_bet_not_start));
                    return;
                }
                Intent betIntent = new Intent(this, LotteryDetailActivity.class);
                betIntent.putExtra(ConstantCode.CommonConstant.LOTTERYID, oneLottery.getLotteryId
                        ());
                betIntent.putExtra("startBet", true);
                startActivity(betIntent);

                finish();
                break;

            case R.id.btn_share:
                if(!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if(!OneLotteryManager.isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                SharePlatform.getInstance().share(CreateSuccessActivity.this, oneLottery);
                break;
        }
    }
}
