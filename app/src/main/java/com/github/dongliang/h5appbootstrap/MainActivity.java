package com.github.dongliang.h5appbootstrap;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AppBootstrap appBootstrap;

    private ProgressBar progressBar;
    private Button button;
    private TextView textView;
    private WebView webView;
    private EditText logContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("H5 App Bootstrap Demo");
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        button = (Button)findViewById(R.id.button);
        textView = (TextView)findViewById(R.id.textView);
        webView = (WebView)findViewById(R.id.webView);
        logContainer = (EditText)findViewById(R.id.logContainer);

        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        webView.setWebViewClient(webViewClient);
        button.setOnClickListener(this);

        appBootstrap = new AppBootstrap(new AppBootstrap.Setting() {
            @Override
            public Context getContext() {
                return MainActivity.this;
            }

            @Override
            public String getAppRootDirName() {
                return "h5apps";
            }

            @Override
            public String getAppServerUrl() {
                return "http://10.200.101.164";
            }
        });
    }

    private WebViewClient webViewClient = new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            view.loadUrl(url);
            return true;
        }
    };

    private AppBootstrap.BootCallback callback = new AppBootstrap.BootCallback() {
        @Override
        public void reportProgress(int progress, String currentFile) {
            progressBar.setProgress(progress);
            textView.setText(currentFile);
            if(TextUtils.isEmpty(currentFile))
                return;
            logContainer.append("Loading : " + currentFile+"\r\n");
        }

        @Override
        public void onError(Exception ex) {
            textView.setText(ex.getMessage());
        }

        @Override
        public void onSuccess(String startupPage) {
            button.setText("启动");
            textView.setText("启动完成!");
            webView.loadUrl(startupPage);
        }

        @Override
        public void onCancelled() {
            textView.setText("已取消!");
        }
    };
    private AppBootstrap.Cancellable cancellable;
    @Override
    public void onClick(View v) {
        if(button.getText().equals("启动")) {
            cancellable = appBootstrap.boot("demoApp", callback);
            textView.setText("启动中...");
            button.setText("取消");
        }else {
            cancellable.cancel();
            textView.setText("取消中...");
            button.setText("启动");
        }
//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                cancellable.cancel();
//            }
//        });
//        t.start();
    }
}
