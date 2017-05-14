package com.peersafe.chainbet.ui.setting;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.peersafe.chainbet.R;
import com.peersafe.chainbet.ui.BasicActivity;

public class AboutUsActivity extends BasicActivity
{
    // webView
    private WebView mWebAgreement;

    private boolean isArgment;
    private boolean isAboutUs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        isAboutUs = getIntent().getBooleanExtra("aboutus",false);
        isArgment = getIntent().getBooleanExtra("argment",false);

        mWebAgreement = (WebView) findViewById(R.id.web_agreement);

        if(isAboutUs)
        {
            //中文版
            mWebAgreement.loadUrl("file:///android_asset/aboutUs/index.html");
        }

        if(isArgment)
        {
            mWebAgreement.loadUrl("file:///android_asset/agreement.html");
        }

        //英语版
//      mWebAgreement.loadUrl("file:///android_asset/aboutUs/index_en.html");
        initToolBar();
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) findViewById(R.id.tv_title);
        if(isAboutUs)
        {
            title.setText(getString(R.string.setting_about_us));
        }

        if(isArgment)
        {
            title.setText(getString(R.string.setting_argment));
        }
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

    /**
     * Description
     *
     * @see android.app.Activity#onDestroy()
     */

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mWebAgreement.destroy();
        mWebAgreement = null;
    }
}
