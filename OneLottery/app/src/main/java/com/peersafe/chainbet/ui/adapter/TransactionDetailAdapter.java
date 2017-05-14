package com.peersafe.chainbet.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.TransactionDetail;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.ui.setting.TransactionDetailActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;

import java.text.DecimalFormat;

/**
 * @author caozhongzheng
 * @Description
 * @date 2017/1/17 17:42
 */
public class TransactionDetailAdapter extends ListBaseAdapter<TransactionDetail>
{
    public TransactionDetailAdapter(TransactionDetailActivity activity)
    {
        mContext =  activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new ViewHolder(LayoutInflater.from(mContext).
                inflate(R.layout.transaciton_detail_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        TransactionDetail tra = mDataList.get(position);
        ViewHolder vh = (ViewHolder) holder;

        String oppo = StringUtils.isEmpty(tra.getOppositeUserId()) ? StringUtils.getHeadTailString(tra.getOppositeHash())
                : StringUtils.getHeadTailString(tra.getOppositeUserId());

        String lotteryName = null;
        if (!StringUtils.isEmpty(tra.getTxId()))
        {
            OneLottery ol = OneLotteryDBHelper.getInstance().getLotteryByLotterId(tra.getTxId());
            if (ol != null)
            {
                lotteryName = StringUtils.getHeadTailString(ol.getLotteryName());
            } else {
                lotteryName = StringUtils.getHeadTailString(tra.getRemark());
            }
        }

        double amount = ((double) tra.getAmount() / ConstantCode.CommonConstant.ONELOTTERY_MONEY_MULTIPLE);
        DecimalFormat df = new DecimalFormat("0.00");

        vh.mMoney.setText("-" + df.format(amount));
        vh.mMoney.setTextColor(mContext.getResources().getColor(R.color.common_text_color));

        vh.mTime.setText(DateFormat.format(ConstantCode.CommonConstant.SIMPLE_DATE_FORMAT, tra.getTime().getTime()));

        /*
        final TransactionDetail td = tra;
        View.OnClickListener listener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!StringUtils.isEmpty(td.getTxId()))
                {
                    Intent intent_ld = new Intent(mContext, LotteryDetailActivity.class);
                    intent_ld.putExtra(ConstantCode.CommonConstant.LOTTERYID, td.getTxId());
                    mContext.startActivity(intent_ld);
                }
            }
        };
        vh.mView.setOnClickListener(listener);*/

        switch (tra.getType())
        {
            case ConstantCode.TransactionType.TRANSACTION_TYPE_TRANSFOR:
                vh.mTitle.setText( mContext.getString(R.string.transfer_type_transfer));
                vh.mContent.setText(String.format(mContext.getString(R.string.transfer_type_content_transfer), oppo));
                vh.mView.setOnClickListener(null);
                break;

            case ConstantCode.TransactionType.TRANSACTION_TYPE_TRANSFOR_FROM_OTHER:
                vh.mTitle.setText( mContext.getString(R.string.transfer_type_transfer));
                vh.mContent.setText(String.format(mContext.getString(R.string.transfer_type_content_transfer_from_other), oppo));
                vh.mMoney.setText("+" + df.format(amount));
                vh.mMoney.setTextColor( mContext.getResources().getColor(R.color.lottery_prize_text_color));
                vh.mView.setOnClickListener(null);
                break;

            case ConstantCode.TransactionType.TRANSACTION_TYPE_TRANSFOR_FROM_ADMIN:
                vh.mTitle.setText( mContext.getString(R.string.transfer_type_transfer_from_admin));
                vh.mContent.setText(mContext.getString(R.string.transfer_type_content_transfer_from_admin));
                vh.mMoney.setText("+" + df.format(amount));
                vh.mMoney.setTextColor( mContext.getResources().getColor(R.color.lottery_prize_text_color));
                vh.mView.setOnClickListener(null);
                break;

            case ConstantCode.TransactionType.TRANSACTION_TYPE_CREATE_LOTTERY:
                vh.mTitle.setText( mContext.getString(R.string.transfer_type_create_lottery));
                vh.mContent.setText(lotteryName);
                break;

            case ConstantCode.TransactionType.TRANSACTION_TYPE_MODIFY_LOTTERY:
                vh.mTitle.setText( mContext.getString(R.string.transfer_type_modify_lottery));
                vh.mContent.setText(lotteryName);
                break;

            case ConstantCode.TransactionType.TRANSACTION_TYPE_BET:
                vh.mTitle.setText( mContext.getString(R.string.transfer_type_bet));
                vh.mContent.setText(lotteryName);
                break;

            case ConstantCode.TransactionType.TRANSACTION_TYPE_REFUND:
                vh.mTitle.setText( mContext.getString(R.string.transfer_type_refund));
                vh.mContent.setText(String.format(mContext.getString(R.string.transfer_type_content_refund),
                        lotteryName));
                vh.mMoney.setText("+" + df.format(amount));
                vh.mMoney.setTextColor( mContext.getResources().getColor(R.color.lottery_prize_text_color));
                break;

            case ConstantCode.TransactionType.TRANSACTION_TYPE_PRIZE:
                vh.mTitle.setText( mContext.getString(R.string.transfer_type_prize));
                vh.mContent.setText(String.format(mContext.getString(R.string.transfer_type_content_prize),
                        lotteryName));
                vh.mMoney.setText("+" + df.format(amount));
                vh.mMoney.setTextColor( mContext.getResources().getColor(R.color.lottery_prize_text_color));
                break;

            case ConstantCode.TransactionType.TRANSACTION_TYPE_PERCENTAGE:
                vh.mTitle.setText( mContext.getString(R.string.transfer_type_percentage));
                vh.mContent.setText(String.format(mContext.getString(R.string.transfer_type_content_percentage),
                        lotteryName));
                vh.mMoney.setText("+" + df.format(amount));
                vh.mMoney.setTextColor( mContext.getResources().getColor(R.color.lottery_prize_text_color));
                break;

            case ConstantCode.TransactionType.TRANSACTION_TYPE_WITHDRAW_CONFIRM:
            case ConstantCode.TransactionType.TRANSACTION_TYPE_WITHDRAW_AUTO_CONFIRM:
            case ConstantCode.TransactionType.TRANSACTION_TYPE_WITHDRAW_APPEAL_DONE:
                vh.mTitle.setText( mContext.getString(R.string.transfer_type_withdraw));
                vh.mContent.setText(lotteryName);
                break;

            default:
                break;
        }
    }

    @Override
    public int getItemCount()
    {
        return mDataList.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        TextView mTitle;
        TextView mContent;
        TextView mTime;
        TextView mMoney;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView.findViewById(R.id.ly_tra_detail);
            mTitle = (TextView) itemView.findViewById(R.id.tv_tra_detail_title);
            mContent = (TextView) itemView.findViewById(R.id.tv_tra_detail_content);
            mTime = (TextView) itemView.findViewById(R.id.tv_tra_detail_time);
            mMoney = (TextView) itemView.findViewById(R.id.tv_tra_detail_money);
        }
    }

}
