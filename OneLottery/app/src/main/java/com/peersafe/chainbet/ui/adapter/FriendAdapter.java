package com.peersafe.chainbet.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.Friend;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.ui.friend.FriendFragment;
import com.peersafe.chainbet.ui.friend.FriendLotteryActivity;
import com.peersafe.chainbet.ui.lottery.LotteryDetailActivity;
import com.peersafe.chainbet.utils.common.ImageUtils;
import com.peersafe.chainbet.utils.log.OLLogger;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.SwipeMenuView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/1/9
 * DESCRIPTION :
 */

public class FriendAdapter extends ListBaseAdapter<Friend>
{
    private LayoutInflater mLayoutInflater;
    private Context context;
    private FriendFragment fragment;

    public FriendAdapter(Context context, FriendFragment friendFragment)
    {
        this.context = context;
        this.fragment = friendFragment;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new FriendAdapter.ViewHolder(mLayoutInflater.
                inflate(R.layout.friend_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {
        final Friend friend = mDataList.get(position);
        final FriendAdapter.ViewHolder viewHolder = (FriendAdapter.ViewHolder) holder;

        ((SwipeMenuView) viewHolder.itemView).setLeftSwipe(true);

        //设置圆圈颜色
        switch (position % 4)
        {
            case 0:
                Glide.with(context).load(R.drawable.c).into(viewHolder.mImgIcon);
                break;
            case 1:
                Glide.with(context).load(R.drawable.y).into(viewHolder.mImgIcon);
                break;
            case 2:
                Glide.with(context).load(R.drawable.b).into(viewHolder.mImgIcon);
                break;
            case 3:
                Glide.with(context).load(R.drawable.j).into(viewHolder.mImgIcon);
                break;
        }
        viewHolder.mTvName.setText(friend.getFriendId());

        //好友创建的正在进行中的活动
        List<OneLottery> lotterys = OneLotteryDBHelper.getInstance().getLotteryByPublishHash
                (friend.getFriendHash());
        if (null != lotterys)
        {
            viewHolder.mTvCount.setText(String.format(context.getString(R.string
                    .friend_duration_lottery), lotterys.size()));
        }

        viewHolder.mBtnCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!NetworkUtil.isNetworkConnected())
                {
                    Toast.makeText(context, context.getString(R.string.check_network), Toast
                            .LENGTH_SHORT).show();
                    return;
                }

                if(!OneLotteryManager.getInstance().isServiceConnect)
                {
                    Toast.makeText(OneLotteryApplication.getAppContext(),context.getString(R.string.check_service),Toast
                            .LENGTH_SHORT).show();
                    return;
                }

                if (null != mOnSwipeListener)
                {
                    //如果删除时，不使用mAdapter.notifyItemRemoved(pos)，则删除没有动画效果，
                    //且如果想让侧滑菜单同时关闭，需要同时调用 ((CstSwipeDelMenu) holder.itemView).quickClose();
                    //((CstSwipeDelMenu) holder.itemView).quickClose();
                    mOnSwipeListener.onDel(position);
                }
            }
        });

        (viewHolder.contentView).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if (!NetworkUtil.isNetworkConnected())
                {
                    Toast.makeText(context, context.getString(R.string.check_network), Toast
                            .LENGTH_SHORT).show();
                    return;
                }

                if(!OneLotteryManager.getInstance().isServiceConnect)
                {
                    Toast.makeText(OneLotteryApplication.getAppContext(),mContext.getString(R.string.check_service),Toast
                            .LENGTH_SHORT).show();
                    return;
                }

                if (!((SwipeMenuView) viewHolder.itemView).isExpand)
                {
                    Intent intent = new Intent(context, FriendLotteryActivity.class);
                    intent.putExtra("FriendHash", friend.getFriendHash());
                    intent.putExtra("FriendId", friend.getFriendId());
                    context.startActivity(intent);
                }
            }
        });
    }


    public interface onSwipeListener
    {
        void onDel(int pos);
    }

    private onSwipeListener mOnSwipeListener;

    public void setOnDelListener(onSwipeListener mOnDelListener)
    {
        this.mOnSwipeListener = mOnDelListener;
    }

    @Override
    public int getItemCount()
    {
        return mDataList.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder
    {
        View contentView;
        ImageView mImgIcon;
        TextView mTvName;
        TextView mTvCount;
        Button mBtnCancel;

        public ViewHolder(View itemView)
        {
            super(itemView);
            contentView = itemView.findViewById(R.id.swipe_content);
            mImgIcon = (ImageView) itemView.findViewById(R.id.img_friend_item_icon);
            mTvName = (TextView) itemView.findViewById(R.id.tv_friend_item_name);
            mTvCount = (TextView) itemView.findViewById(R.id.tv_friend_item_count);
            mBtnCancel = (Button) itemView.findViewById(R.id.btn_friend_item_cancel);
        }
    }
}
