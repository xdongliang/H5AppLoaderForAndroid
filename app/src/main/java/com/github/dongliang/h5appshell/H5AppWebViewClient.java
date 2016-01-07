package com.github.dongliang.h5appshell;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.dongliang.h5appshell.plugins.Plugin;

/**
 * Created by dongliang on 2016/1/5.
 */
public class H5AppWebViewClient extends WebViewClient {

    private static final String CUSTOM_PROTOCOL_SCHEME  = "invoke://";
    private PluginLocator mPluginLocator;
    private Parser mParser;
    private WebView mWebView;

    public H5AppWebViewClient(PluginLocator pluginLocator, Parser parser, WebView webView){
        mPluginLocator = pluginLocator;
        mParser = parser;
        mWebView = webView;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(url.startsWith(CUSTOM_PROTOCOL_SCHEME)){
            PluginParameter  pluginParameter = mParser.parse(url);
            Plugin plugin =  mPluginLocator.locate(pluginParameter.fullName);
            plugin.execute(mWebView, pluginParameter);
            return true;
        }

        return super.shouldOverrideUrlLoading(view, url);
    }

}
