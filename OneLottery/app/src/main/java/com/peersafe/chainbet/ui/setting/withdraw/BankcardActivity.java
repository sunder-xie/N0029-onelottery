package com.peersafe.chainbet.ui.setting.withdraw;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.WithdrawBankCard;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.WithdrawBankCardDBHelper;
import com.peersafe.chainbet.manager.dbhelper.WithdrawRecordDBHelper;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.adapter.ListBaseAdapter;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.AlertDialog;
import com.peersafe.chainbet.widget.SwipeMenuView;

import java.util.ArrayList;
import java.util.List;

public class BankcardActivity extends BasicActivity implements View.OnClickListener
{
    private static final int DELETE_BANK_CARD = 1;

    private LRecyclerView mRecyclerView;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;
    private BankcardAdapter mAdapter = null;
    private List mDataList = new ArrayList();

    public static Activity isntance;

    public int position = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_bankcard);

        isntance = this;

        mRecyclerView = (LRecyclerView) findViewById(R.id.list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(BankcardActivity.this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new BankcardActivity.BankcardAdapter(BankcardActivity.this);
        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mAdapter);

        mRecyclerView.setPullRefreshEnabled(false);
        mRecyclerView.setLoadMoreEnabled(false);
        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        getDataList();

        mAdapter.setDataList(mDataList);

        findViewById(R.id.tv_new_card).setOnClickListener(this);
        findViewById(R.id.btn_withdraw_list).setOnClickListener(this);

        initToolBar();
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.tv_new_card:
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

                Intent add = new Intent(this, AddBankCardActivity.class);
                startActivity(add);
                break;
            case R.id.btn_withdraw_list:
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

                startActivity(new Intent(this,WithdrawListActivity.class));
                break;
        }
    }

    public void getDataList()
    {
        mDataList = WithdrawBankCardDBHelper.getInstance().getAllBankCard();

        if(mDataList == null)
        {
            mDataList = new ArrayList();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        getDataList();
        mAdapter.setDataList(mDataList);
    }

   public class BankcardAdapter extends ListBaseAdapter
   {
        LayoutInflater mLayoutInflater = null;
        Context context;

        public BankcardAdapter(Context context)
        {
            this.context = context;
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return new BankcardAdapter.ViewHolder(mLayoutInflater.
                    inflate(R.layout.bank_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int pos)
        {
            final WithdrawBankCard item = (WithdrawBankCard) mDataList.get(pos);
            final BankcardAdapter.ViewHolder viewHolder = (BankcardAdapter.ViewHolder) holder;

            ((SwipeMenuView) viewHolder.itemView).setLeftSwipe(true);

            viewHolder.mBankName.setText(item.getOpeningBank());

            String bankCardId = item.getBankCardId();
            String newCardId = bankCardId.replace(" ","");
            String substring = newCardId.substring(newCardId.length() - 4, newCardId.length());
            viewHolder.mBankNumber.setText("(" + substring + ")");

            if (item.getIsDefaultCard())
            {
                viewHolder.mIocn.setImageResource(R.drawable.bank_card_seclect);
            } else
            {
                viewHolder.mIocn.setImageResource(R.drawable.bank_card_noseclect);
            }

            viewHolder.contentView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!((SwipeMenuView) viewHolder.itemView).isExpand)
                    {
                        if (!NetworkUtil.isNetworkConnected())
                        {
                            showToast(getString(R.string.check_network));
                            return;
                        }

                        if (!OneLotteryManager.getInstance().isServiceConnect)
                        {
                            showToast(getString(R.string.check_service));
                            return;
                        }

                        WithdrawBankCardDBHelper.getInstance().setPrimaryBankCard(item);
                        Intent intent = new Intent(BankcardActivity.this,WithdrawActivity.class);
                        intent.putExtra(ConstantCode.CommonConstant.BANK_CARD,item);
                        startActivity(intent);
                    }
                }
            });

            viewHolder.mDelete.setOnClickListener(new View.OnClickListener()
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

                    Intent intent = new Intent(context, AlertDialog.class);
                    intent.putExtra(AlertDialog.TIP_MESSAGE, getString(R.string.withdraw_delete_warn));
                    intent.putExtra(AlertDialog.SHOW_CANCEL_BTN, true);
                    intent.putExtra(AlertDialog.SHOW_CONFIRM_BTN, true);
                    startActivityForResult(intent, DELETE_BANK_CARD);
                    position = pos;
                }
            });

            viewHolder.mModeify.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
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

                    Intent intent = new Intent(BankcardActivity.this,AddBankCardActivity.class);
                    intent.putExtra(ConstantCode.CommonConstant.BANK_CARD,item);
                    startActivity(intent);
                }
            });
        }

        private class ViewHolder extends RecyclerView.ViewHolder
        {
            View contentView;
            TextView mBankName;
            TextView mBankNumber;
            ImageView mIocn;
            Button mDelete;
            Button mModeify;

            public ViewHolder(View itemView)
            {
                super(itemView);
                contentView = itemView.findViewById(R.id.swipe_content);
                mBankName = (TextView) itemView.findViewById(R.id.tv_bank_name);
                mBankNumber = (TextView) itemView.findViewById(R.id.tv_bank_number);
                mIocn = (ImageView) itemView.findViewById(R.id.img_icon);
                mDelete = (Button) itemView.findViewById(R.id.btn_bank_card_delete);
                mModeify = (Button) itemView.findViewById(R.id.btn_bank_card_modify);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            if(requestCode == DELETE_BANK_CARD)
            {
                WithdrawRecordDBHelper.getInstance().delete((WithdrawBankCard) mDataList.get(position));

                if (position != (mAdapter.getDataList().size()))
                { // 如果移除的是最后一个，忽略
                    mAdapter.notifyItemRangeChanged(position, mAdapter.getDataList().size() - position);
                }
            }
        }
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        TextView title = (TextView) findViewById(R.id.tv_toolbar_title);
        title.setText(getString(R.string.withdraw_select_bank_card));

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
}
