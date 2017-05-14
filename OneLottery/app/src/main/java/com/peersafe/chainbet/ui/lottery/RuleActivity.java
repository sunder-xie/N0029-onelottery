package com.peersafe.chainbet.ui.lottery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.PrizeRule;
import com.peersafe.chainbet.manager.dbhelper.PrizeRuleDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.adapter.ListBaseAdapter;
import com.peersafe.chainbet.utils.common.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 规则activity
 * Created by czz on 2017/3/14.
 */
public class RuleActivity extends BasicActivity
{
    public static final String KEY_RULE_ID = "key_rule_id";
    private LRecyclerView mRecyclerView;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;

    private RuleAdapter mAdapter = null;

    private List<PrizeRule> prizeRules;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rule_layout);

        initToolBar();

        initView();

        EventBus.getDefault().register(this);
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(getString(R.string.lottery_detail_rule_type));
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }

        });
    }

    private void initView()
    {
        mRecyclerView = (LRecyclerView) findViewById(R.id.list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(RuleActivity.this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new RuleAdapter(RuleActivity.this);
        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mAdapter);

        mRecyclerView.setPullRefreshEnabled(false);
        mRecyclerView.setLoadMoreEnabled(false);
        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        setRules();
    }

    private void setRules()
    {
        prizeRules = PrizeRuleDBHelper.getInstance().getPrizeRules(false);
        if (prizeRules == null)
        {
            prizeRules = new ArrayList<>();
        }
        mAdapter.setDataList(prizeRules);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(OLMessageModel model)
    {
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_PRIZE_RULE_ADD_NOTIFY:
                if (model != null && model.getEventObject() != null && model.getEventObject()
                        instanceof PrizeRule)
                {
                    PrizeRule addedRule = (PrizeRule) model.getEventObject();
                    if (StringUtils.isEmpty(addedRule.getRuleId()) || addedRule.getHidden())
                    {
                        return;
                    }
                    setRules();
                }
                break;

            case OLMessageModel.STMSG_MODEL_PRIZE_RULE_MODIFY_NOTIFY:
            case OLMessageModel.STMSG_MODEL_PRIZE_RULE_DELETE_NOTIFY:
                if (model != null && model.getEventObject() != null && model.getEventObject()
                        instanceof PrizeRule)
                {
                    PrizeRule rule = (PrizeRule) model.getEventObject();
                    if (StringUtils.isEmpty(rule.getRuleId()))
                    {
                        return;
                    }

                    setRules();
                }
                break;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private class RuleAdapter extends ListBaseAdapter
    {
        LayoutInflater mLayoutInflater = null;
        Context context;
        int[] iconID = {R.drawable.y, R.drawable.b, R.drawable.j, R.drawable.c};

        public RuleAdapter(Context context)
        {
            this.context = context;
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return new ViewHolder(mLayoutInflater.
                    inflate(R.layout.rule_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int pos)
        {
            final PrizeRule item = (PrizeRule) mDataList.get(pos);
            final ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.mIcon.setImageResource(iconID[pos % 4]);

            if (item != null)
            {
                viewHolder.mName.setText(getString(R.string.create_lottery_rule) + item.getRuleName());
                viewHolder.mWinnerPer.setText(String.format(getString(R.string.create_lottery_winner_percentage),
                        item.getPercentage().intValue()));
                viewHolder.mCreatorPer.setText(String.format(getString(R.string.create_lottery_creator_percentage),
                        100 - item.getPercentage().intValue()));
            }

            viewHolder.mDivider.setVisibility(pos == mDataList.size() - 1 ? View.GONE : View.VISIBLE);

            (viewHolder.contentView).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // 选择了规则后，返回
                    Intent intent = new Intent();
                    intent.putExtra(KEY_RULE_ID, item.getRuleId());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });

        }

        private class ViewHolder extends RecyclerView.ViewHolder
        {
            View contentView;
            ImageView mIcon;
            TextView mName;
            TextView mWinnerPer;
            TextView mCreatorPer;
            View mDivider;

            public ViewHolder(View itemView)
            {
                super(itemView);
                contentView = itemView.findViewById(R.id.root);
                mIcon = (ImageView) itemView.findViewById(R.id.iv_item_icon);
                mName = (TextView) itemView.findViewById(R.id.tv_rule_name);
                mWinnerPer = (TextView) itemView.findViewById(R.id.tv_winner_percentage);
                mCreatorPer = (TextView) itemView.findViewById(R.id.tv_creator_percentage);
                mDivider = itemView.findViewById(R.id.divider);
            }
        }
    }
}
