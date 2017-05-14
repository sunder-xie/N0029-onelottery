package com.peersafe.chainbet.ui.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.peersafe.chainbet.MainActivity;
import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.RegisterActivity;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicFragment;
import com.peersafe.chainbet.ui.adapter.ListBaseAdapter;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.FileUtils;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.config.OLPreferenceUtil;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.AlertDialog;
import com.peersafe.chainbet.widget.InputPwdDialog;
import com.peersafe.chainbet.widget.SwipeMenuView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

public class MenuRightFragment extends BasicFragment implements View.OnClickListener
{
    public static final int DELETE_ACCOUNT_FLAG = 1;
    private LRecyclerView mRecyclerView = null;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;

    private AccountAdapter mDataAdapter = null;

    private TextView mCurAccount;
    private TextView mBanlance;
    private List<String> userList;

    public static int position;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        EventBus.getDefault().register(this);
        return inflater.inflate(R.layout.menu_layout_right, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mCurAccount = (TextView) getView().findViewById(R.id.setting_right_cur_account);
        mBanlance = (TextView) getView().findViewById(R.id.setting_right_balance);
        mRecyclerView = (LRecyclerView) getView().findViewById(R.id.list);

        getView().findViewById(R.id.btn_add_new_concern).setOnClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(linearLayoutManager);
        mDataAdapter = new AccountAdapter(getActivity());
        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);

        mRecyclerView.setPullRefreshEnabled(false);
        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        setAccount();

        userList = UserInfoDBHelper.getInstance().getUserList(false);
        //主账号不能显示出来
        mDataAdapter.setDataList(userList);

        mDataAdapter.setOnEachItemListener(new AccountAdapter.onSwipeListener()
        {
            @Override
            public void onDelete(int pos)
            {
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    return;
                }

                if(!OneLotteryManager.isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    return;
                }

                position = pos;

                Intent intent = new Intent(getActivity(), AlertDialog.class);
                intent.putExtra(AlertDialog.TIP_TITLE, getString(R.string
                        .setting_delete_account_tip_title));
                intent.putExtra(AlertDialog.TIP_MESSAGE, getString(R.string
                        .setting_delete_account_tip_message));
                intent.putExtra(AlertDialog.SHOW_CANCEL_BTN, true);
                intent.putExtra(AlertDialog.SHOW_CONFIRM_BTN, true);

                Bundle bundle = new Bundle();
                bundle.putString(ConstantCode.CommonConstant.USER_ID, userList.get(position));
                intent.putExtra(AlertDialog.BUNDLE_EXTRA, bundle);

                getActivity().startActivityForResult(intent, DELETE_ACCOUNT_FLAG);
            }

            @Override
            public void onEnter(int pos)
            {
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    return;
                }

                if(!OneLotteryManager.isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    return;
                }

                position = pos;
                InputPwdDialog dialog = new InputPwdDialog(getActivity(), InputPwdDialog
                        .CHANGE_ACCOUNT, userList.get(pos));
                dialog.show();
            }
        });
    }

    private void setAccount()
    {
        if (!StringUtils.isEmpty(OneLotteryApi.getCurUserId()) && !OneLotteryApi.getCurUserId()
                .equals(ConstantCode.CommonConstant.ONELOTTERY_DEFAULT_USERNAME))
        {
            UserInfo userInfo = UserInfoDBHelper.getInstance().getUserByUserId(OneLotteryApi
                    .getCurUserId());
            double s = ((double) userInfo.getBalance() / ConstantCode.CommonConstant
                    .ONELOTTERY_MONEY_MULTIPLE);
            mCurAccount.setText(userInfo.getUserId());
            DecimalFormat df = new DecimalFormat("0.00");
            mBanlance.setText(String.format(getString(R.string.lottery_bet_balance),
                    df.format(s)));
        }

        userList = UserInfoDBHelper.getInstance().getUserList(false);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_add_new_concern:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    return;
                }

                if(!OneLotteryManager.isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    return;
                }

                Intent intent = new Intent(getActivity(), RegisterActivity.class);
                intent.putExtra(ConstantCode.CommonConstant.TYPE, true);
                startActivity(intent);
                break;
        }
    }

    public static class AccountAdapter extends ListBaseAdapter
    {
        LayoutInflater mLayoutInflater = null;
        Context context;

        public AccountAdapter(Context context)
        {
            this.context = context;
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return new AccountAdapter.ViewHolder(mLayoutInflater.
                    inflate(R.layout.setting_right_user_list, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int pos)
        {
            final String item = (String) mDataList.get(pos);
            final AccountAdapter.ViewHolder viewHolder = (AccountAdapter.ViewHolder) holder;
            ((SwipeMenuView) viewHolder.itemView).setLeftSwipe(true);

            switch (pos % 4)
            {
                case 0:
                    viewHolder.mIcon.setImageResource(R.drawable.y);
                    break;
                case 1:
                    viewHolder.mIcon.setImageResource(R.drawable.b);
                    break;
                case 2:
                    viewHolder.mIcon.setImageResource(R.drawable.j);
                    break;
                case 3:
                    viewHolder.mIcon.setImageResource(R.drawable.c);
                    break;
            }

            viewHolder.mName.setText(item);

            viewHolder.mDelete.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (null != mOnSwipeListener)
                    {
                        mOnSwipeListener.onDelete(pos);
                    }
                }
            });

            viewHolder.mEnter.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (null != mOnSwipeListener)
                    {
                        mOnSwipeListener.onEnter(pos);
                    }
                }
            });

            (viewHolder.contentView).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (!((SwipeMenuView) viewHolder.itemView).isExpand)
                    {
                        if (!NetworkUtil.isNetworkConnected())
                        {
                            Toast.makeText(context, context.getString(R.string.check_network),
                                    Toast
                                    .LENGTH_SHORT).show();
                            return;
                        }

                        if(!OneLotteryManager.isServiceConnect)
                        {
                            Toast.makeText(context,context.getString(R.string.check_service),Toast.LENGTH_SHORT).show();
                            return;
                        }

                        position = pos;
                        InputPwdDialog dialog = new InputPwdDialog(context, InputPwdDialog.CHANGE_ACCOUNT, item);
                        dialog.show();
                    }
                }
            });
        }

        public interface onSwipeListener
        {
            void onDelete(int pos);

            void onEnter(int pos);
        }

        private AccountAdapter.onSwipeListener mOnSwipeListener;

        public void setOnEachItemListener(AccountAdapter.onSwipeListener mOnDelListener)
        {
            this.mOnSwipeListener = mOnDelListener;
        }

        private class ViewHolder extends RecyclerView.ViewHolder
        {
            View contentView;
            ImageView mIcon;
            TextView mName;
            ImageButton mDelete;
            ImageButton mEnter;

            public ViewHolder(View itemView)
            {
                super(itemView);
                contentView = itemView.findViewById(R.id.swipe_content);
                mIcon = (ImageView) itemView.findViewById(R.id.setting_right_item_icon);
                mName = (TextView) itemView.findViewById(R.id.setting_right_item_name);
                mDelete = (ImageButton) itemView.findViewById(R.id.setting_right_delete);
                mEnter = (ImageButton) itemView.findViewById(R.id.setting_right_enter);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(OLMessageModel model)
    {
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_SETTING_DELETE_ACCOUNT:
                try
                {
                    userList = UserInfoDBHelper.getInstance().getUserList(false);
                    final String user = OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).getDeleteUid();
                    if (!StringUtils.isEmpty(user))
                    {
                        OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).setDeleteUid("");
                        UserInfoDBHelper.getInstance().deleteUser(user);

                        userList.remove(user);
                        //删除账号文件
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                File file = new File(OneLotteryApplication.getAppContext().getFilesDir()
                                        .getAbsolutePath() + File.separator + "onelottery" + File
                                        .separator + "crypto" +
                                        File.separator + "client" + File.separator + user);
                                FileUtils.delete(file);
                            }
                        }).start();
                    }
                }
                catch (Exception e)// ArrayList.throwIndexOutOfBoundsException
                {
                    e.printStackTrace();
                }

                mDataAdapter.setDataList(userList);

                break;

            case OLMessageModel.STMSG_MODEL_SETTING_CHANGE_ACCOUNT:

                if (userList == null || userList.size() <= position)
                {
                    return;
                }

                userList = UserInfoDBHelper.getInstance().getUserList(false);
                mDataAdapter.setDataList(userList);

                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // 获取用户余额，刷新UI
                        OneLotteryManager.getInstance().getUserBalance();

                        // 获取未关闭的活动
                        OneLotteryManager.getInstance().getLotteries(false, true);

                        // 获取已关闭的活动
                        OneLotteryManager.getInstance().getLotteries(true, true);

                    }
                }).start();

                setAccount();

                MainActivity activity = (MainActivity) getActivity();
                activity.hideRightMenu();
                break;

            case OLMessageModel.STMSG_MODEL_TRANSFER_ACCOUNT_NOTIFY:
                if (model.getEventObject() != null && model.getEventObject()
                        instanceof UserInfo)
                {
                    setAccount();
                }
                break;
            case OLMessageModel.STMSG_MODEL_REFRSH_SETTING_RIGHT_FRAGMENT:
                if (null != userList)
                {
                    setAccount();
                    mDataAdapter.setDataList(userList);
                }
                break;
        }
    }

    @Override
    public void onDestroyView()
    {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }
}
