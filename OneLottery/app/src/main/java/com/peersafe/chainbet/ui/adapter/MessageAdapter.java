package com.peersafe.chainbet.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.MessageNotify;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.log.OLLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author caozhongzheng
 * @Description
 * @date 2017/1/17 17:42
 */
public class MessageAdapter extends ListBaseAdapter<MessageNotify>
{
    private boolean is_select_mode;
    private Map<String, MessageNotify> map;
    public int ALL = -1;

    public MessageAdapter(Context msgCenterActivity)
    {
        mContext = msgCenterActivity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new MessageAdapter.ViewHolder(LayoutInflater.from(mContext).
                inflate(R.layout.message_center_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        MessageNotify msg = mDataList.get(position);
        ViewHolder vh = (ViewHolder) holder;

        vh.mTvTitle.setText(msg.getTitle());
        vh.mTvContent.setText(msg.getContent());
        vh.mTvTime.setText(DateFormat.format(ConstantCode.CommonConstant.SIMPLE_DATE_FORMAT, msg.getTime().getTime()));
        vh.mImgSelect.setVisibility(is_select_mode ? View.VISIBLE : View.GONE);
        vh.mImgIsRead.setVisibility(msg.getIsRead() ? View.GONE : View.VISIBLE);
        if (map != null && map.containsKey(msg.getMsgId()))
        {
            Glide.with(mContext).load(R.drawable.msg_select).into(vh.mImgSelect);
        } else
        {
            Glide.with(mContext).load(R.drawable.msg_select).into(vh.mImgSelect);

        }

        OLLogger.e("MessageAdapter","msg name" + msg.getTitle() + " msg type " + msg.getType());
        if (msg.getType() != null)
        {
            switch (msg.getType())
            {
                case ConstantCode.MessageType.MESSAGE_TYPE_CREATE_FAIL:
                case ConstantCode.MessageType.MESSAGE_TYPE_MODIFY_FAIL:
                case ConstantCode.MessageType.MESSAGE_TYPE_DELETE_FAIL:
                case ConstantCode.MessageType.MESSAGE_TYPE_BET_FAIL:
                case ConstantCode.MessageType.MESSAGE_TYPE_TRANSFOR_FAIL:
                    Glide.with(mContext).load(R.drawable.msg_fail).into(vh.mImgMsgType);

                    break;
                case ConstantCode.MessageType.MESSAGE_TYPE_CREATE_SUCCESS:
                case ConstantCode.MessageType.MESSAGE_TYPE_MODIFY_SUCCESS:
                case ConstantCode.MessageType.MESSAGE_TYPE_DELETE_SUCCESS:
                case ConstantCode.MessageType.MESSAGE_TYPE_BET_SUCCESS:
                case ConstantCode.MessageType.MESSAGE_TYPE_TRANSFOR_SUCCESS:
                case ConstantCode.MessageType.MESSAGE_TYPE_REFUND:
                    Glide.with(mContext).load(R.drawable.msg_success).into(vh.mImgMsgType);
                    break;
                case ConstantCode.MessageType.MESSAGE_TYPE_PRIZE:
                    Glide.with(mContext).load(R.drawable.msg_prize).into(vh.mImgMsgType);
                    break;
                case ConstantCode.MessageType.MESSAGE_TYPE_PERCENTAGE:
                    Glide.with(mContext).load(R.drawable.msg_percentage).into(vh.mImgMsgType);
                    break;
                default:
                    Glide.with(mContext).load(R.drawable.msg_success).into(vh.mImgMsgType);
                    break;
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return mDataList.size();
    }

    public Map<String, MessageNotify> getMap()
    {
        return map;
    }

    public void setMap(Map<String, MessageNotify> map)
    {
        this.map = map;
    }

    public boolean isSelectMode()
    {
        return is_select_mode;
    }

    public void setIsSelectMode(boolean is_select_mode)
    {
        this.is_select_mode = is_select_mode;
    }

    public boolean onItemLongClick(int position)
    {
        if (is_select_mode)
        {
            return false;
        }
        if (position < 0 || position > getItemCount())
        {
            return false;
        }

        is_select_mode = !is_select_mode;

        selectMsg(position);
        return true;
    }

    public void selectMsg(int position)
    {
        if (map == null)
        {
            map = new HashMap<>();
        }

        if (position == ALL)
        {
            if (map.size() != getItemCount())
            {
                map.clear();
                for (MessageNotify msg : mDataList)
                {
                    map.put(msg.getMsgId(), msg);
                }
            } else
            {
                map.clear();
            }
        } else
        {
            if (map.containsKey(mDataList.get(position).getMsgId()))
            {
                map.remove(mDataList.get(position).getMsgId());
            } else
            {
                map.put(mDataList.get(position).getMsgId(), mDataList.get(position));
            }
        }
        notifyDataSetChanged();
    }

    public boolean isSelectAll()
    {
        return map != null && map.size() == getItemCount();
    }

    private class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView mImgMsgType;
        ImageView mImgIsRead;
        TextView mTvTitle;
        TextView mTvTime;
        TextView mTvContent;
        ImageView mImgSelect;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mImgMsgType = (ImageView) itemView.findViewById(R.id.iv_msg_type);
            mImgIsRead = (ImageView) itemView.findViewById(R.id.iv_is_read);
            mTvTitle = (TextView) itemView.findViewById(R.id.tv_msg_title);
            mTvTime = (TextView) itemView.findViewById(R.id.tv_msg_time);
            mTvContent = (TextView) itemView.findViewById(R.id.tv_msg_content);
            mImgSelect = (ImageView) itemView.findViewById(R.id.iv_msg_selected);
        }
    }

}
