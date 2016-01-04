package com.github.dongliang.h5appbootstrap;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dongliang on 2015/12/30.
 */
public class AppBootstrap {

    private Setting mSetting;
    private String h5appRootDirPath;
    private volatile boolean isCancelled;
    private Gson gson = new Gson();

    public AppBootstrap(@NonNull Setting setting) {
        mSetting = setting;
        //获取保存app的根目录
        File appRootDir = setting.getContext().getExternalFilesDir(null);
        h5appRootDirPath = appRootDir.getAbsolutePath() + "/" + setting.getAppRootDirName();
    }

    @NonNull
    public Cancellable boot(@NonNull final String appName, @NonNull final BootCallback progress) {
        isCancelled = false;
        final Handler mainHandler = new Handler(mSetting.getContext().getMainLooper(), new HandlerCallback(progress));
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isCancelled) {
                        onCancelled();
                        return;
                    }
                    //创建保存指定app的目录
                    String appDirPath = makeAppDir(appName);
                    //获取服务器端manifest文件url
                    String manifestFileUrl = getManifestFileServerUrl(appName);
                    //旧的manifest文件路径
                    String oldManifestFilePath = getManifestFileLocalPath(appName);
                    //新的manifest文件路径
                    String newManifestFilePath = appDirPath + "/manifest.tmp";
                    //尝试获取本地manifest文件的最近修改日期,如果存在的话
                    long lastModified = getAppManifestLastModified(oldManifestFilePath, newManifestFilePath);
                    // 尝试从服务器端获取manifest文件并保存, 返回true表示有更新, 返回false表示无更新
                    if (getHttpFile(manifestFileUrl, lastModified, newManifestFilePath)) {
                        if (isCancelled) {
                            onCancelled();
                            return;
                        }
                        //从本地加载旧的manifest文件,并解析成AppManifest对象
                        AppManifest oldManifest = parseAppManifestFromFile(oldManifestFilePath);
                        //从本地加载新的manifest文件,并解析成AppManifest对象
                        AppManifest newManifest = parseAppManifestFromFile(newManifestFilePath);
                        //比较新旧manifest的不同并返还不同处
                        List<AppManifest.FileItem> diffItems = compare(oldManifest, newManifest);
                        //处理不同的项目
                        processDiffItems(appDirPath, oldManifestFilePath, newManifestFilePath, oldManifest, newManifest, diffItems);
                    } else {
                        //从本地加载旧的manifest文件,并解析成AppManifest对象
                        AppManifest oldManifest = parseAppManifestFromFile(getManifestFileLocalPath(appName));
                        // 报告启动完毕, 并返回指定的启动页
                        onSuccess(appDirPath, oldManifest);
                    }
                } catch (Exception e) {
                    //报告启动异常
                    onError(e);
                }
            }

            private void processDiffItems(
                    String appDirPath, String oldManifestFilePath, String newManifestFilePath,
                    AppManifest oldManifest, AppManifest newManifest,
                    List<AppManifest.FileItem> diffItems) throws IOException {

                oldManifest.version = newManifest.version;
                oldManifest.startup = newManifest.startup;
                oldManifest.bundle = newManifest.bundle;

                for (int currentIdx = 0; currentIdx < diffItems.size(); currentIdx++) {
                    if (isCancelled) {
                        onCancelled();
                        return;
                    }
                    AppManifest.FileItem fileItem = diffItems.get(currentIdx);
                    String localPath = appDirPath + "/" + fileItem.path;
                    //更新进度
                    reportProgress(getProgress(diffItems.size(), currentIdx), fileItem.path);

                    switch (fileItem.modifiedType) {
                        case AppManifest.FileItem.NEW://update or new
                        case AppManifest.FileItem.UPDATE:
                            String serverPath = mSetting.getAppServerUrl() + "/" + appName + "/" + fileItem.path;
                            getHttpFile(serverPath, 0, localPath);
                            if (fileItem.modifiedType == AppManifest.FileItem.NEW) {
                                oldManifest.files.add(fileItem);
                            } else {
                                int idx = oldManifest.files.indexOf(fileItem);
                                oldManifest.files.get(idx).hash = fileItem.hash;
                            }
                            break;
                        case AppManifest.FileItem.DELETE: //delete
                            File file = new File(localPath);
                            file.delete();
                            //从旧的manifest文件移除这个fileItem
                            oldManifest.files.remove(fileItem);
                            break;
                        default:
                            break;
                    }
                    //保存旧manifest文件
                    Utils.writeTextFile(oldManifestFilePath, gson.toJson(oldManifest));
                }
                if (isCancelled) {
                    onCancelled();
                    return;
                }
                //删除临时文件
                File file = new File(newManifestFilePath);
                file.delete();
                //成功
                onSuccess(appDirPath, oldManifest);
            }

            private int getProgress(float diffItemCount, float currentIdx) {
                if (currentIdx > diffItemCount)
                    return 100;
                return (int) (currentIdx / diffItemCount * 100);
            }

            private void reportProgress(int progress, String currentFile) {

                Bundle data = new Bundle();
                data.putInt(HandlerCallback.PROGRESS, progress);
                data.putString(HandlerCallback.CURRENT_FILE, currentFile);

                Message msg = new Message();
                msg.what = HandlerCallback.REPORT_PROGRESS;
                msg.setData(data);

                mainHandler.sendMessage(msg);
            }

            private void onSuccess(String appDirPath, AppManifest oldManifest) {
                //报告100%进度
                reportProgress(100, "");

                String startupPage;
                // 报告启动完毕, 并返回指定的启动页
                if (TextUtils.isEmpty(oldManifest.startup)) {
                    startupPage = ("file://" + appDirPath + "/index.html");
                } else {
                    startupPage = ("file://" + appDirPath + "/" + oldManifest.startup);
                }

                Bundle data = new Bundle();
                data.putString(HandlerCallback.STARTUP_PAGE, startupPage);
                Message msg = new Message();
                msg.what = HandlerCallback.SUCCESS;
                msg.setData(data);
                mainHandler.sendMessage(msg);
            }

            private void onError(Exception ex) {
                Bundle data = new Bundle();
                data.putSerializable(HandlerCallback.ERROR_EXCEPTION, ex);
                Message msg = new Message();
                msg.what = HandlerCallback.ERROR;
                msg.setData(data);
                mainHandler.sendMessage(msg);
            }

            private void onCancelled() {
                mainHandler.sendEmptyMessage(HandlerCallback.CANCEL);
            }
        });
        thread.start();

        return new Cancellable() {
            @Override
            public void cancel() {
                isCancelled = true;
            }

        };
    }

    /**
     * 比较新旧两个manifest,最后,返回不同的fileitem
     *
     * @param oldManifest
     * @param newManifest
     * @return
     */
    @NonNull
    private List<AppManifest.FileItem> compare(AppManifest oldManifest, AppManifest newManifest) {

        Map<String, AppManifest.FileItem> fileItemMap = new HashMap<>();

        for (AppManifest.FileItem newItem : newManifest.files) {
            newItem.modifiedType = AppManifest.FileItem.NEW;
            fileItemMap.put(newItem.path, newItem);
        }

        for (AppManifest.FileItem oldItem : oldManifest.files) {
            AppManifest.FileItem item = fileItemMap.get(oldItem.path);
            if (item == null) {
                oldItem.modifiedType = AppManifest.FileItem.DELETE;
                fileItemMap.put(oldItem.path, oldItem);
            } else {
                if (item.hash.equals(oldItem.hash)) {
                    item.modifiedType = AppManifest.FileItem.NONE;
                }else {
                    item.modifiedType = AppManifest.FileItem.UPDATE;
                }
            }
        }

        return new ArrayList<>(fileItemMap.values());
    }

    private class HandlerCallback implements Handler.Callback {

        public static final int REPORT_PROGRESS = 100;
        public static final int ERROR = 200;
        public static final int SUCCESS = 300;
        public static final int CANCEL = 400;

        public static final String STARTUP_PAGE = "STARTUP_PAGE";
        public static final String ERROR_EXCEPTION = "ERROR_EXCEPTION";
        public static final String PROGRESS = "PROGRESS";
        public static final String CURRENT_FILE = "CURRENT_FILE";


        private BootCallback mProgress;

        public HandlerCallback(BootCallback progress) {
            mProgress = progress;
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case REPORT_PROGRESS:
                    int progress = msg.getData().getInt(PROGRESS);
                    String currentFile = msg.getData().getString(CURRENT_FILE);
                    mProgress.reportProgress(progress, currentFile);
                    break;
                case SUCCESS:
                    String startupPage = msg.getData().getString(STARTUP_PAGE);
                    mProgress.onSuccess(startupPage);
                    break;
                case ERROR:
                    Exception ex = (Exception) msg.getData().getSerializable(ERROR_EXCEPTION);
                    mProgress.onError(ex);
                    break;
                case CANCEL:
                    mProgress.onCancelled();
                    break;
                default:
            }
            return true;
        }
    }

    private AppManifest parseAppManifestFromFile(String manifestFilePath) {
        String json = Utils.readTextFile(manifestFilePath);
        if (TextUtils.isEmpty(json)) {
            AppManifest appManifest = new AppManifest();
            appManifest.files = new ArrayList<>();
            return appManifest;
        }
        return gson.fromJson(json, AppManifest.class);
    }

    @NonNull
    private String makeAppDir(@NonNull String appName) {
        String appDirPath = h5appRootDirPath + "/" + appName;
        File appDirFile = new File(appDirPath);
        if (!appDirFile.exists()) {
            appDirFile.mkdirs();
        }
        return appDirPath;
    }

    private long getAppManifestLastModified(@NonNull String oldManifestFilePath, @NonNull String newManifestFilePath) {
        File newManifestFile = new File(newManifestFilePath);
        if (newManifestFile.exists()) {
            return 0L;
        }

        File manifestFile = new File(oldManifestFilePath);
        if (manifestFile.exists()) {
            return manifestFile.lastModified();
        } else {
            return 0L;
        }
    }

    @NonNull
    private String getManifestFileLocalPath(@NonNull String appName) {
        return h5appRootDirPath + "/" + appName + "/manifest.json";
    }

    @NonNull
    private String getManifestFileServerUrl(@NonNull String appName) {
        return mSetting.getAppServerUrl() + "/" + appName + "/manifest.json";
    }

    private boolean getHttpFile(String httpUrl, long lastModified, String saveFilePath) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(httpUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);
        if (lastModified > 0) {
            conn.setIfModifiedSince(lastModified);
        }
        if (conn.getResponseCode() == 200) {
            Utils.writeFile(conn.getInputStream(), saveFilePath);
            return true;
        }
        return false;
    }


    public interface Setting {

        Context getContext();

        String getAppRootDirName();

        String getAppServerUrl();
    }

    public interface BootCallback {
        void reportProgress(int progress, String currentFile);

        void onError(Exception ex);

        void onSuccess(String startupPage);

        void onCancelled();
    }

    public interface Cancellable {
        void cancel();
    }
}
