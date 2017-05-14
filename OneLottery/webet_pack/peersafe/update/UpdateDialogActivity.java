package com.peersafe.chainbet.ui.update;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.config.OLPreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateDialogActivity extends BasicActivity implements View.OnClickListener
{

    TextView mUpdateContent, mUpdateApkSize;
    Button mUpdateOk, mUpdateCancel;
    String name, versionName, updateContent, apkUrl;
    int version;
    Long updateTime, apkSize;

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
        Intent intent = getIntent();
        String versionJson = intent.getStringExtra("version");
        try
        {
            if (!StringUtils.isEmpty(versionJson))
            {
                JSONObject jsonObject = new JSONObject(versionJson);
                name = jsonObject.getString("name");
                version = jsonObject.getInt("version");
                updateContent = jsonObject.getString("changelog");
                apkUrl = jsonObject.getString("install_url");
                updateTime = jsonObject.getLong("updated_at");
                apkSize = jsonObject.getJSONObject("binary").getLong("fsize");
                versionName = jsonObject.getString("versionShort");

            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void initView()
    {
        mUpdateContent = (TextView) findViewById(R.id.fir_update_content);
        mUpdateOk = (Button) findViewById(R.id.fir_update_id_ok);
        mUpdateCancel = (Button) findViewById(R.id.fir_update_id_cancel);
        mUpdateApkSize = (TextView) findViewById(R.id.fir_update_size);
    }

    /**
     * 获取当前的版本号
     */
    public int getlocalVersion()
    {
        int localversion = 0;
        try
        {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            localversion = info.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return localversion;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.fir_update_id_cancel:
                //保存一个标识来判断是否再开启
                OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext()).setUpdateNext(1);
                //保存当时的时间
                Long systemTime = System.currentTimeMillis();
                OLPreferenceUtil.getInstance(OneLotteryApplication.getAppContext())
                        .setUpdateSystemTime(systemTime);
                finish();
                break;
            case R.id.fir_update_id_ok:
                //开启服务下载新版的apk
                Intent intent = new Intent(UpdateDialogActivity.this, UpdateService.class);
                intent.putExtra("apkUrl", apkUrl);
                intent.putExtra("apkName", name);
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
