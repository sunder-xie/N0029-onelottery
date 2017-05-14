package com.peersafe.chainbet.ui.update;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.model.VersionModel;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.utils.config.OLPreferenceUtil;

public class UpdateDialogActivity extends BasicActivity implements View.OnClickListener
{
    TextView mUpdateContent, mUpdateApkSize;
    Button mUpdateOk, mUpdateCancel;
    String name, versionName, updateContent, apkUrl;
    String version;
    Long updateTime, apkSize;

    VersionModel model = null;

    boolean fromMain = false;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fir_update_dialog);

        initView();

        parseJson();

        mUpdateApkSize.setText(getString(R.string.fir_new_version) + versionName);

        mUpdateContent.setText(updateContent);

        mUpdateCancel.setOnClickListener(this);

        mUpdateOk.setOnClickListener(this);
    }

    /**
     * 将升级版本的json数据解析
     */
    private void parseJson()
    {
        if (getIntent() == null)
        {
            return;
        }

        fromMain = getIntent().getBooleanExtra("from",false);
        model = (VersionModel) getIntent().getSerializableExtra("version");
        name = model.getName();
        version = model.getVersion();
        updateContent = model.getChangelog();
        apkUrl = model.getInstallUrl();
        updateTime = model.getUpdated_at();
        apkSize = model.getBinary().getFsize();
        versionName = model.getVersionShort();
    }

    private void initView()
    {
        mUpdateContent = (TextView) findViewById(R.id.fir_update_content);
        mUpdateOk = (Button) findViewById(R.id.fir_update_id_ok);
        mUpdateCancel = (Button) findViewById(R.id.fir_update_id_cancel);
        mUpdateApkSize = (TextView) findViewById(R.id.fir_update_size);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.fir_update_id_cancel:
                //保存当时的时间
                if(fromMain)
                {
                    OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).setUpdateSystemTime(System.currentTimeMillis());
                }
                finish();
                break;
            case R.id.fir_update_id_ok:
                //开启服务下载新版的apk
                Intent intent = new Intent(UpdateDialogActivity.this, UpdateService.class);
                intent.putExtra("version", model);
                startService(intent);
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}
