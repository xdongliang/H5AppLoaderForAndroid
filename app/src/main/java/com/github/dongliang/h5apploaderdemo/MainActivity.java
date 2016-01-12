package com.github.dongliang.h5apploaderdemo;

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

import com.github.dongliang.h5apploader.AppManifest;
import com.github.dongliang.h5apploader.H5AppLoader;

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
            logContainer.append("file : " + currentFile + " -> " + strModifiedType + "\r\n");
        }

        private String getModifiedType(int modifiedType) {

            switch (modifiedType) {
                case AppManifest.FileItem.ADD:
                    return "Add";
                case AppManifest.FileItem.UPDATE:
                    return "Update";
                case AppManifest.FileItem.DELETE:
                    return "Delete";
                default:
                    return "DoNothing";
            }

        }

        @Override
        public void onError(Exception ex) {
            button.setText("Load");
            textView.setText(ex.getMessage());
        }

        @Override
        public void onSuccess(String appPath, String startupPage) {
            button.setText("Load");
            textView.setText("Load Completed!");

            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.START_PAGE_PARAM, appPath + "/" + startupPage);
            startActivity(intent);
        }

        @Override
        public void onCancelled() {
            textView.setText("Cancelled!");
        }
    };
    private H5AppLoader.Cancellable cancellable;

    @Override
    public void onClick(View v) {
        if (button.getText().equals("Load")) {
            cancellable = h5AppLoader.loadApp("demoApp", callback);
            textView.setText("Loading...");
            button.setText("Cancel");
            logContainer.setText("");
        } else {
            cancellable.cancel();
            textView.setText("Cancelling...");
            button.setText("Load");
        }
    }
}
