package com.github.dongliang.h5apploader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class WebViewActivity extends Activity {

    public static final String START_PAGE_PARAM = "com.github.dongliang.h5appbootstrap.WebViewActivity.START_PAGE";

    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        webView = (WebView)findViewById(R.id.webView);
        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        webView.setWebViewClient(webViewClient);
        String startupPage = getIntent().getStringExtra(START_PAGE_PARAM);


        try {
            webView.loadUrl(startupPage + "?accessToken=5aa30dee4bc048149cf07116d63e43f3&deviceUniqueId=a086d81951c6fe56&channel=2&version=1.0.10&apiRoot="+ URLEncoder.encode("http://hipartytest.830clock.cn/", "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private WebViewClient webViewClient = new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            view.loadUrl(url);
            return true;
        }
    };
}
