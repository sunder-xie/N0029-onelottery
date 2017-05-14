package com.peersafe.chainbet.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.HandlerThread;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.Friend;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.OneLotteryBet;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.FriendDBHelper;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryBetDBHelper;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.AttendBean;
import com.peersafe.chainbet.model.LotteryJsonBean.OneLotteryDeleteRet;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.utils.common.CommonUtils;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * AUTHOR : sunhaitao.
 * DATA : 16/12/23
 * DESCRIPTION : 活动详情内参与列表adapter
 */

public class LotteryAttendAdapter extends ListBaseAdapter<AttendBean>
{
    private LayoutInflater mLayoutInflater;
    private Context context;
    private String lotteryId = null;
    private boolean isFriend = false;

    public LotteryAttendAdapter(Context context, String lotteryId)
    {
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);

        this.lotteryId = lotteryId;
        HandlerThread thread = new HandlerThread("");
        thread.start();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new LotteryAttendAdapter.ViewHolder(mLayoutInflater.
                inflate(R.layout.lottery_partake_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        AttendBean itemModel = mDataList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.mTvAttender.setText(itemModel.getAttendName());
        viewHolder.mTvBetCount.setText(String.format(context.getString(R.string
                .lottery_detail_per_bet), itemModel.getAttendCount()));

        //TODO null attendTime
        Date attendTime = itemModel.getAttendTime();
        SimpleDateFormat format = new SimpleDateFormat(ConstantCode.CommonConstant
                .SIMPLE_DATE_FORMAT);
        viewHolder.mTvAttendTime.setText(attendTime != null ? format.format(attendTime) : "");

        boolean myFriend = FriendDBHelper.getInstance().isMyFriend(itemModel.getAttendHash());
        if (!myFriend)
        {
            viewHolder.mBtnAddAttention.setBackgroundResource(R.drawable
                    .shape_lottery_common_bg_btn);
            viewHolder.mBtnAddAttention.setTextColor(context.getResources().getColor(R.color
                    .app_primary_color));
            Drawable drawable = context.getResources().getDrawable(R.drawable
                    .lottery_detail_add_concern);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            viewHolder.mBtnAddAttention.setCompoundDrawables(drawable, null, null, null);
            viewHolder.mBtnAddAttention.setText(context.getString(R.string
                    .lottery_detail_add_concern));

            AddConcernClickLister clickLister = new AddConcernClickLister();
            clickLister.mBtn = viewHolder.mBtnAddAttention;
            clickLister.itemModel = itemModel;
            viewHolder.mBtnAddAttention.setOnClickListener(clickLister);
        } else
        {
            viewHolder.mBtnAddAttention.setBackgroundResource(R.drawable
                    .lottery_detail_add_concern_already);
            viewHolder.mBtnAddAttention.setTextColor(context.getResources().getColor(R.color
                    .add_concern_btn_color));
            viewHolder.mBtnAddAttention.setCompoundDrawables(null, null, null, null);
            viewHolder.mBtnAddAttention.setText(context.getString(R.string
                    .lottery_detail_add_concern_already));
        }

        UserInfo curPrimaryAccount = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (null != curPrimaryAccount)
        {
            if (itemModel.getAttendHash().equals(curPrimaryAccount.getWalletAddr()) || itemModel
                    .getAttendName().equals(ConstantCode.CommonConstant
                            .ONELOTTERY_DEFAULT_OFFICAL_NAME))
            {
                viewHolder.mBtnAddAttention.setVisibility(View.GONE);
            } else
            {
                viewHolder.mBtnAddAttention.setVisibility(View.VISIBLE);
            }
        } else
        {
            viewHolder.mBtnAddAttention.setVisibility(View.VISIBLE);
        }

        boolean languageChinese = CommonUtils.isLanguageChinese();
        if(!languageChinese)
        {
            viewHolder.mBtnAddAttention.getLayoutParams().width = (int) context.getResources().getDimension(R.dimen.x140);
        }
    }

    public boolean isAddFriend()
    {
        return isFriend;
    }

    private class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView mTvAttender;
        TextView mTvBetCount;
        TextView mTvAttendTime;
        Button mBtnAddAttention;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mTvAttender = (TextView) itemView.findViewById(R.id.tv_item_partaker);
            mTvBetCount = (TextView) itemView.findViewById(R.id.tv_item_bet_count);
            mTvAttendTime = (TextView) itemView.findViewById(R.id.tv_item_attend_time);
            mBtnAddAttention = (Button) itemView.findViewById(R.id.btn_item_add_attention);
        }
    }

    class AddConcernClickLister implements View.OnClickListener
    {
        Button mBtn;
        AttendBean itemModel;

        @Override
        public void onClick(View view)
        {

            if (!NetworkUtil.isNetworkConnected())
            {
                Toast.makeText(context, context.getString(R.string.check_network), Toast
                        .LENGTH_SHORT).show();
                return;
            }

            if(!OneLotteryManager.isServiceConnect)
            {
                Toast.makeText(context,context.getString(R.string.check_service),Toast.LENGTH_SHORT).show();
                return;
            }

            if (!OneLotteryApi.isVisitor(context))
            {
                UserInfo curPrimaryAccount = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
                String userId = curPrimaryAccount.getUserId();

                Friend friend = new Friend();
                friend.setFriendId(itemModel.getAttendName());
                friend.setFriendHash(itemModel.getAttendHash());
                friend.setUserId(userId);
                FriendDBHelper.getInstance().insertFriend(friend);

                mBtn.setBackgroundResource(R.drawable
                        .lottery_detail_add_concern_already);
                mBtn.setTextColor(context.getResources().getColor(R.color
                        .add_concern_btn_color));
                mBtn.setCompoundDrawables(null, null, null, null);
                mBtn.setText(context.getString(R.string
                        .lottery_detail_add_concern_already));

                isFriend = true;

                // 刷新创建者和中奖者是否是好友的状态
                OneLottery lottery = OneLotteryDBHelper.getInstance().getLotteryByLotterId
                        (lotteryId);
                boolean a = lottery.getPublisherName() != null && lottery.getPublisherName().equals(itemModel.getAttendName());
                boolean b = lottery.getState() == ConstantCode.OneLotteryState.ONELOTTERY_STATE_REWARD_ALREADY;

                if (a || b)
                {
                    OneLotteryManager.getInstance().SendEventBus(null, OLMessageModel
                            .STMSG_MODEL_REFRSH_LOTTERY_DETAIL);
                }

                notifyDataSetChanged();
            }
        }
    }
}
