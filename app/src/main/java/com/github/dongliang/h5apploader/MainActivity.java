package com.github.dongliang.h5apploader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private H5AppLoader h5AppLoader;

    private ProgressBar progressBar;
    private Button button;
    private TextView textView;
    private TextView logContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("H5 App Bootstrap Demo");
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);
        logContainer = (TextView) findViewById(R.id.logContainer);

        logContainer.setMovementMethod(ScrollingMovementMethod.getInstance());

        button.setOnClickListener(this);

        h5AppLoader = new H5AppLoader(setting);
    }

    private H5AppLoader.Setting setting = new H5AppLoader.Setting() {

        @Override
        public Context getContext() {
            return MainActivity.this;
        }

        @Override
        public String getAppServerUrl() {
            return "http://10.200.101.164";
        }
    };

    private H5AppLoader.LoadCallback callback = new H5AppLoader.LoadCallback() {
        @Override
        public void reportProgress(int progress, String currentFile, int modifiedType) {
            progressBar.setProgress(progress);
            textView.setText(currentFile);
            if (TextUtils.isEmpty(currentFile))
                return;
            String strModifiedType = getModifiedType(modifiedType);
            logContainer.append("文件 : " + currentFile + " -> " + strModifiedType + "\r\n");
        }

        private String getModifiedType(int modifiedType) {

            switch (modifiedType) {
                case AppManifest.FileItem.NEW:
                    return "新增";
                case AppManifest.FileItem.UPDATE:
                    return "修改";
                case AppManifest.FileItem.DELETE:
                    return "刪除";
                default:
                    return "不处理";
            }

        }

        @Override
        public void onError(Exception ex) {
            button.setText("启动");
            textView.setText(ex.getMessage());
        }

        @Override
        public void onSuccess(String startupPage) {
            button.setText("启动");
            textView.setText("启动完成!");

            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.START_PAGE_PARAM, startupPage);
            startActivity(intent);
        }

        @Override
        public void onCancelled() {
            textView.setText("已取消!");
        }
    };
    private H5AppLoader.Cancellable cancellable;

    @Override
    public void onClick(View v) {
        if (button.getText().equals("启动")) {
            cancellable = h5AppLoader.load("demoApp", callback);
            textView.setText("启动中...");
            button.setText("取消");
            logContainer.setText("");
        } else {
            cancellable.cancel();
            textView.setText("取消中...");
            button.setText("启动");
        }
    }
}
