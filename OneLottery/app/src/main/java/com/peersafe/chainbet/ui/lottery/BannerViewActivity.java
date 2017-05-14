package com.peersafe.chainbet.ui.lottery;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.peersafe.chainbet.R;
import com.peersafe.chainbet.ui.BasicActivity;

public class BannerViewActivity extends BasicActivity
{
    // webView
    private WebView mWebAgreement;

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_view);

        initToolBar();

        url = getIntent().getStringExtra("url");

        mWebAgreement = (WebView) findViewById(R.id.web_agreement);

        WebSettings webSettings = mWebAgreement.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mWebAgreement.loadUrl(url);

        mWebAgreement.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText("");

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
        mWebAgreement.destroy();
        mWebAgreement = null;
    }
}
