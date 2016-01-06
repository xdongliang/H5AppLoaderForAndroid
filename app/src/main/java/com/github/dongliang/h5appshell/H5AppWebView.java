package com.github.dongliang.h5appshell;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Created by dongliang on 2016/1/5.
 */
public class H5AppWebView extends WebView {
    public H5AppWebView(Context context) {
        super(context);
        setWebViewClient();
    }

    public H5AppWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWebViewClient();
    }

    public H5AppWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWebViewClient();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public H5AppWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWebViewClient();
    }

    private void setWebViewClient(){
        this.setWebViewClient(new H5AppWebViewClient());
    }



}
