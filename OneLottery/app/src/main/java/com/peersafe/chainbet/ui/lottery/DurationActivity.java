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
import android.widget.TextView;

import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.adapter.ListBaseAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * 活动时长,每注花费 选择页面
 * Created by czz on 2017/3/14.
 */
public class DurationActivity extends BasicActivity
{
    public static final String KEY_POSITION = "key_pos";
    public static final String KEY_ARRAY = "key_array";
    public static final String KEY_TITLE = "key_title";

    private LRecyclerView mRecyclerView;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;

    private RuleAdapter mAdapter = null;

    private List<String> list;
    private int position;
    private String[] strings;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rule_layout);

        if (getIntent() != null)
        {
            strings = getIntent().getStringArrayExtra(KEY_ARRAY);
            title = getIntent().getStringExtra(KEY_TITLE);
            position = getIntent().getIntExtra(KEY_POSITION, -1);
            if (position < 0 || (strings != null && position > strings.length))
            {
                position = -1;
            }
        }

        initToolBar();

        initView();
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(this.title);
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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DurationActivity.this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new RuleAdapter(DurationActivity.this);
        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mAdapter);

        mRecyclerView.setPullRefreshEnabled(false);
        mRecyclerView.setLoadMoreEnabled(false);
        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        setData();

    }

    private void setData()
    {
        list = Arrays.asList(strings);
        mAdapter.setDataList(list);
    }

    private class RuleAdapter extends ListBaseAdapter
    {
        LayoutInflater mLayoutInflater = null;
        Context context;

        public RuleAdapter(Context context)
        {
            this.context = context;
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return new ViewHolder(mLayoutInflater.
                    inflate(R.layout.duration_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int pos)
        {
            final String item = (String) mDataList.get(pos);
            final ViewHolder viewHolder = (ViewHolder) holder;

            if (item != null)
            {
                viewHolder.mName.setText(item);
            }
//            if (position == pos)
//            {
//                viewHolder.mName.setTextColor(getResources().getColor(R.color.app_primary_color));
//                viewHolder.mDivider.setBackgroundColor(getResources().getColor(R.color.app_primary_color));
//            }

            (viewHolder.mRoot).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    position = pos;
                    Intent intent = new Intent();
                    intent.putExtra(KEY_POSITION, pos);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });

        }

        private class ViewHolder extends RecyclerView.ViewHolder
        {
            View mRoot;
            TextView mName;
            View mDivider;

            public ViewHolder(View itemView)
            {
                super(itemView);
                mRoot = itemView.findViewById(R.id.root);
                mName = (TextView) itemView.findViewById(R.id.tv_duration_name);
                mDivider = itemView.findViewById(R.id.divider);
            }
        }
    }
}
