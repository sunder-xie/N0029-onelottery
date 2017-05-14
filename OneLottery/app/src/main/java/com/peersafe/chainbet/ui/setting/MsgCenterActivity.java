package com.peersafe.chainbet.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.interfaces.OnItemLongClickListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.MessageNotify;
import com.peersafe.chainbet.db.OneLottery;
import com.peersafe.chainbet.db.WithdrawRecord;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.MessageNotifyDBHelper;
import com.peersafe.chainbet.manager.dbhelper.OneLotteryDBHelper;
import com.peersafe.chainbet.manager.dbhelper.WithdrawRecordDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.adapter.MessageAdapter;
import com.peersafe.chainbet.ui.lottery.LotteryDetailActivity;
import com.peersafe.chainbet.ui.setting.withdraw.BankcardActivity;
import com.peersafe.chainbet.ui.setting.withdraw.DetailActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.log.OLLogger;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.AlertDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author caozhongzheng
 * @Description
 * @date 2017/1/17 16:53
 */
public class MsgCenterActivity extends BasicActivity implements View.OnClickListener,
        OnItemClickListener, OnItemLongClickListener
{
    private static final String TAG = "MSCA";
    private static final int DELETE_MESSAGE_FLAG = 1;

    private LRecyclerView mRecyclerView = null;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;
//    private RelativeLayout mLyDelMsg;

    private MessageAdapter mDataAdapter = null;

    private TextView tvSelectAll;

    private LinearLayout mLyNoData;
    private TextView mTvNoData;
    private ImageView mIvNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        setContentView(R.layout.activity_message_center);

        initToolBar();

        initView();
    }

    private void initView()
    {
        mRecyclerView = (LRecyclerView) findViewById(R.id.list);

//        mLyDelMsg = (RelativeLayout) findViewById(R.id.ly_delete_msg);
//        mLyDelMsg.setVisibility(View.GONE);
//        mLyDelMsg.setOnClickListener(this);

        //setLayoutManager must before setAdapter
        LinearLayoutManager manager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(manager);

        mRecyclerView.setPullRefreshEnabled(false);

        //禁用自动加载更多功能
        mRecyclerView.setLoadMoreEnabled(false);

        mDataAdapter = new MessageAdapter(this);

        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);

        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        mLyNoData = (LinearLayout) findViewById(R.id.ly_empty);
        mTvNoData = (TextView) findViewById(R.id.tv_empty);
        mIvNoData = (ImageView) findViewById(R.id.iv_empty);

        setData();

        mLRecyclerViewAdapter.setOnItemClickListener(this);
        mLRecyclerViewAdapter.setOnItemLongClickListener(this);
    }

    private void setData()
    {
        if (!NetworkUtil.isNetworkConnected())
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_network);
            mDataAdapter.setDataList(new ArrayList<MessageNotify>());
            mTvNoData.setText(R.string.no_network);
            return;
        }

        if (!OneLotteryManager.isServiceConnect)
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_service);
            mDataAdapter.setDataList(new ArrayList<MessageNotify>());
            mTvNoData.setText(R.string.no_service);
            return;
        }

        List<MessageNotify> list = MessageNotifyDBHelper.getInstance().getAllMessages();
        OLLogger.i(TAG, list != null ? "msgNum: " + list.size() : " empty ");
        if (list != null && !list.isEmpty())
        {
            mLyNoData.setVisibility(View.GONE);
            mDataAdapter.setDataList(list);
        } else
        {
            mLyNoData.setVisibility(View.VISIBLE);
            mIvNoData.setImageResource(R.drawable.no_data);
            mTvNoData.setText(R.string.no_data);
            mDataAdapter.setDataList(new ArrayList<MessageNotify>());
        }
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(getString(R.string.setting_msg_center));
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
        tvSelectAll = (TextView) findViewById(R.id.tv_right);
        tvSelectAll.setVisibility(View.GONE);
        tvSelectAll.setText(R.string.message_select_all);
        tvSelectAll.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.tv_right:
                mDataAdapter.selectMsg(mDataAdapter.ALL);
                if (mDataAdapter.isSelectAll())
                {
                    tvSelectAll.setText(getString(R.string.message_select_none));
                } else
                {
                    tvSelectAll.setText(getString(R.string.message_select_all));
                }
                break;
            case R.id.ly_delete_msg:
                if (mDataAdapter.getMap() != null && mDataAdapter.getMap().size() > 0)
                {
                    for (MessageNotify msg : mDataAdapter.getMap().values())
                    {
                        MessageNotifyDBHelper.getInstance().deleteMessageNotify(msg);
                    }
                }
                mDataAdapter.setMap(null);
                mDataAdapter.setIsSelectMode(false);
                setData();

                tvSelectAll.setText(getString(R.string.message_select_all));
                tvSelectAll.setVisibility(View.GONE);
//                mLyDelMsg.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position)
    {
        if (mDataAdapter.isSelectMode())
        {
            // select or unselect this one
            mDataAdapter.selectMsg(position);
        } else
        {
            // TODO 跳转详情 mDataList.get(position)
            MessageNotify msg = mDataAdapter.getDataList().get(position);
            if (msg != null)
            {
                switch (msg.getType())
                {
                    case ConstantCode.MessageType.MESSAGE_TYPE_CREATE_FAIL:
                    case ConstantCode.MessageType.MESSAGE_TYPE_BET_FAIL:
                    case ConstantCode.MessageType.MESSAGE_TYPE_TRANSFOR_FAIL:
                    case ConstantCode.MessageType.MESSAGE_TYPE_DELETE_SUCCESS:
                        break;
                    case ConstantCode.MessageType.MESSAGE_TYPE_TRANSFOR_SUCCESS:
                    case ConstantCode.MessageType.MESSAGE_TYPE_TRANSFOR_FROM_OTHER:
                        // 跳转明细
                        Intent i_td = new Intent(MsgCenterActivity.this,
                                TransactionDetailActivity.class);
                        startActivity(i_td);
                        break;
                    case ConstantCode.MessageType.MESSAGE_TYPE_CREATE_SUCCESS:
                    case ConstantCode.MessageType.MESSAGE_TYPE_MODIFY_SUCCESS:
                    case ConstantCode.MessageType.MESSAGE_TYPE_MODIFY_FAIL:
                    case ConstantCode.MessageType.MESSAGE_TYPE_DELETE_FAIL:
                    case ConstantCode.MessageType.MESSAGE_TYPE_BET_SUCCESS:
                    case ConstantCode.MessageType.MESSAGE_TYPE_REFUND:
                    case ConstantCode.MessageType.MESSAGE_TYPE_PRIZE:
                    case ConstantCode.MessageType.MESSAGE_TYPE_PERCENTAGE:
                        // 跳转活动详情
                        OneLottery ol = OneLotteryDBHelper.getInstance().getLotteryByLotterId(msg
                                .getLotteryId());
                        if (ol != null)
                        {
                            Intent intent = new Intent(MsgCenterActivity.this,
                                    LotteryDetailActivity.class);
                            intent.putExtra(ConstantCode.CommonConstant.LOTTERYID, msg
                                    .getLotteryId());
                            startActivity(intent);
                        }
                        break;
                    //跳转到提现银行卡页面
                    case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_SEND_FAIL:
                        Intent bankCard = new Intent(MsgCenterActivity.this, BankcardActivity.class);
                        startActivity(bankCard);
                        break;

                    //跳转详情页面
                    case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_SEND_SUCCESS:
                    case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_FAIL:
                    case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_SUCCESS:
                    case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_APPEAL_SEND_FAIL:
                    case ConstantCode.MessageType.MESSAGE_TYPE_WITH_DRAW_APPEAL_SEND_SUCCESS:
                        WithdrawRecord record = WithdrawRecordDBHelper.getInstance().getRecordByKey(msg.getNewTxId());
                        Intent detail = new Intent(MsgCenterActivity.this, DetailActivity.class);
                        detail.putExtra(ConstantCode.CommonConstant.WITH_DRAW_RECORD,record);
                        startActivity(detail);
                        break;
                    default:
                        break;
                }
                if (!msg.getIsRead())
                {
                    msg.setIsRead(true);
                    mDataAdapter.getDataList().get(position).setIsRead(true);
                    mDataAdapter.notifyDataSetChanged();
                    MessageNotifyDBHelper.getInstance().insertMessageNotify(msg);
                }
            }
        }
    }

    @Override
    public void onItemLongClick(View view, final int position)
    {
        /*
        final AlertDialog.Builder builder = new AlertDialog.Builder(MsgCenterActivity.this);
        final View v = LayoutInflater.from(MsgCenterActivity.this).inflate(R.layout
        .message_delete_dialog, null);
        Button confirm = (Button) v.findViewById(R.id.btn_confirm_del);
        builder.setView(v);
        final AlertDialog dialog = builder.show();

        confirm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MessageNotifyDBHelper.getInstance().deleteMessageNotify(mDataAdapter.getDataList
                ().get(position));
                setData();
                dialog.dismiss();
            }
        });
        */

        Intent intent = new Intent(MsgCenterActivity.this, AlertDialog.class);
        intent.putExtra(AlertDialog.TIP_TITLE, getString(R.string.message_delete_msg));
        intent.putExtra(AlertDialog.TIP_MESSAGE, getString(R.string.message_delete_msg));
        intent.putExtra(AlertDialog.SHOW_CONFIRM_BTN, true);
        intent.putExtra(AlertDialog.SHOW_CANCEL_BTN, true);
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        intent.putExtra(AlertDialog.BUNDLE_EXTRA, bundle);
        startActivityForResult(intent, DELETE_MESSAGE_FLAG);
        /*

        if (mDataAdapter.isSelectMode())
        {
            return;
        }

        boolean ok = mDataAdapter.onItemLongClick(position);
        if (ok)
        {
            tvSelectAll.setVisibility(View.VISIBLE);
            mLyDelMsg.setVisibility(View.VISIBLE);
            if (mDataAdapter.isSelectAll())
            {
                tvSelectAll.setText(getString(R.string.message_select_none));
            } else
            {
                tvSelectAll.setText(getString(R.string.message_select_all));
            }
        }
        */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DELETE_MESSAGE_FLAG && resultCode == RESULT_OK)
        {
            Bundle bundle = data.getBundleExtra(AlertDialog.BUNDLE_EXTRA);
            if (bundle != null && bundle.getInt("position", -1) >= 0)
            {
                MessageNotifyDBHelper.getInstance().deleteMessageNotify(mDataAdapter.getDataList
                        ().get(bundle.getInt("position")));
                setData();
            }
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
            case OLMessageModel.STMSG_MODEL_NET_WORK_CONNECT:
                setData();
                break;

            case OLMessageModel.STMSG_MODEL_NET_WORK_DISCONNECT:
                setData();
                break;
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        OLLogger.d(TAG, "onStop ");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        EventBus.getDefault().unregister(this);

        OLLogger.d(TAG, "onDestroy ");
        // 设置全部消息已读
        if (mDataAdapter != null && mDataAdapter.getDataList() != null)
        {
            for (MessageNotify msg : mDataAdapter.getDataList())
            {
                if (msg != null && !msg.getIsRead())
                {
                    msg.setIsRead(true);
                    MessageNotifyDBHelper.getInstance().insertMessageNotify(msg);
                }
            }

            OneLotteryManager.getInstance().SendEventBus(null, OLMessageModel
                    .STMSG_MODEL_ADVISE_MESSAGE);
        }
    }

}
