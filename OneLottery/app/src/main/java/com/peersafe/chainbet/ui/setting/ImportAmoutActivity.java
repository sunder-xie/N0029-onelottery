package com.peersafe.chainbet.ui.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.peersafe.chainbet.LoginActivity;
import com.peersafe.chainbet.MainActivity;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.RegisterActivity;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.adapter.ListBaseAdapter;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.AlertDialog;
import com.peersafe.chainbet.widget.InputPwdDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImportAmoutActivity extends BasicActivity
{
    private static final int EMPORT_SD_FILE = 5;
    private LRecyclerView mRecyclerView;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;
    private ImportAmountAdapter mDataAdapter = null;

    private LinearLayout mLyNoData;
    private TextView mTvNoData;
    private ImageView mIvNoData;

    private List<File> mDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_amout);

        EventBus.getDefault().register(this);

        mRecyclerView = (LRecyclerView) findViewById(R.id.list);
        mLyNoData = (LinearLayout) findViewById(R.id.ly_empty);
        mTvNoData = (TextView) findViewById(R.id.tv_empty);
        mIvNoData = (ImageView) findViewById(R.id.iv_empty);

        LinearLayoutManager manager = new LinearLayoutManager(ImportAmoutActivity.this);
        mRecyclerView.setLayoutManager(manager);

        mRecyclerView.setPullRefreshEnabled(false);
        mRecyclerView.setLoadMoreEnabled(false);

        mDataAdapter = new ImportAmountAdapter(ImportAmoutActivity.this);

        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);
        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        File exfile = new File(ConstantCode.CommonConstant.EXTER_PATH);
        if (exfile.exists())
        {
            File[] files = exfile.listFiles();
            mDataList = Arrays.asList(files);
        }

        if (!exfile.exists())
        {
            File inFile = new File(ConstantCode.CommonConstant.INNER_PATH);
            if (inFile.exists())
            {
                File[] files = inFile.listFiles();
                mDataList = Arrays.asList(files);
            }
        }

        mDataAdapter.setDataList(mDataList);
        initToolBar();
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
            case OLMessageModel.STMSG_MODEL_IMPORT_SD_WALLENT_OK:
                String name = (String) model.getEventObject();
                if (!StringUtils.isEmpty(name))
                {
                    UserInfo userInfo = new UserInfo();
                    userInfo.setUserId(name);
                    userInfo.setWalletAddr(OneLotteryApi.getPubkeyHash(name));
                    userInfo.setBalance(0l);  //设置假数据

                    UserInfoDBHelper.getInstance().setCurPrimaryAccount(userInfo);

                    boolean hasNetwork = NetworkUtil.isNetworkConnected();
                    boolean hasService = OneLotteryManager.isServiceConnect;
                    if (hasNetwork && hasService)
                    {
                        // 获取活动规则
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                OneLotteryManager.getInstance().getPrizeRules();
                            }
                        }).start();
                    }

                    if (hasNetwork && hasService)
                    {
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // 获取未关闭的活动
                                OneLotteryManager.getInstance().getLotteries(false, true);
                                // 获取已关闭的活动
                                OneLotteryManager.getInstance().getLotteries(true, true);
                                // 获取余额
                                OneLotteryManager.getInstance().getUserBalance();
                            }
                        }).start();
                    }

                    OneLotteryApi.setCurUserId(name);
                    finish();
                }
                break;
            case OLMessageModel.STMSG_MODEL_IMPORT_SD_FILE:
                boolean result = (boolean) model.getEventObject();
                if (result)
                {
                    Intent main = new Intent(ImportAmoutActivity.this, MainActivity.class);
                    startActivity(main);
                    RegisterActivity.instance.finish();
                    LoginActivity.instance.finish();
                    finish();
                } else
                {
                    Intent intent = new Intent(ImportAmoutActivity.this, AlertDialog.class);
                    intent.putExtra(AlertDialog.TIP_MESSAGE, getString(R.string.setting_import_waller_fail));
                    intent.putExtra(AlertDialog.SHOW_CONFIRM_BTN, true);
                    startActivityForResult(intent, EMPORT_SD_FILE);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            if(requestCode == EMPORT_SD_FILE)
            {
            }
        }
    }

    class ImportAmountAdapter extends ListBaseAdapter
    {
        LayoutInflater mLayoutInflater = null;
        Context context;

        public ImportAmountAdapter(Context context)
        {
            this.context = context;
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return new ImportAmountAdapter.ViewHolder(mLayoutInflater.
                    inflate(R.layout.friend_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
        {
            final File file = (File) mDataList.get(position);
            final ImportAmountAdapter.ViewHolder viewHolder = (ImportAmountAdapter.ViewHolder)
                    holder;

            viewHolder.mName.setText(file.getName());

            switch (position % 4)
            {
                case 0:
                    Glide.with(context).load(R.drawable.c).into(viewHolder.mIcon);
                    break;
                case 1:
                    Glide.with(context).load(R.drawable.y).into(viewHolder.mIcon);
                    break;
                case 2:
                    Glide.with(context).load(R.drawable.b).into(viewHolder.mIcon);
                    break;
                case 3:
                    Glide.with(context).load(R.drawable.j).into(viewHolder.mIcon);
                    break;
            }

            viewHolder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    InputPwdDialog dialog = new InputPwdDialog(ImportAmoutActivity.this,
                            InputPwdDialog.IMPORT_SD, file);
                    dialog.show();
                }
            });
        }

        private class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView mName;
            ImageView mIcon;

            public ViewHolder(View itemView)
            {
                super(itemView);
                mIcon = (ImageView) itemView.findViewById(R.id.img_friend_item_icon);
                mName = (TextView) itemView.findViewById(R.id.tv_friend_item_name);
            }
        }
    }

    public void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(getString(R.string.setting_import_account));

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
    protected void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
