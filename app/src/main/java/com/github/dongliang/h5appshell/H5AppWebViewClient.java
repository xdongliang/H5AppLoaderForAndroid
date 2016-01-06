package com.github.dongliang.h5appshell;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by dongliang on 2016/1/5.
 */
public class H5AppWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
        view.loadUrl(url);
        return true;
    }

}
