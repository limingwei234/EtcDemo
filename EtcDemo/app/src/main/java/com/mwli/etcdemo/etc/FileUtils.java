package com.mwli.etcdemo.etc;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static int Version = 18;

    public static String AppPath = null;
    private static FileUtils instance;
    private static final int SUCCESS = 1;
    private static final int FAILED = 0;
    private Context context;
    private FileOperateCallback callback;
    private volatile boolean isSuccess;
    private String errorStr;


    public static FileUtils getInstance(Context context) {
        if (instance == null) {
            instance = new FileUtils(context);
        }
        return instance;
    }

    private FileUtils(Context context) {
        this.context = context;

    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (callback != null) {
                if (msg.what == SUCCESS) {
                    callback.onSuccess((List<String>) msg.obj);
                }
                if (msg.what == FAILED) {
                    callback.onFailed(msg.obj.toString());
                }
            }
        }
    };

    public FileUtils copyAssetsToSD(final String srcPath, final String sdPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> files = new ArrayList<>();
                copyAssetsToDst(context, files, srcPath, sdPath);
                if (isSuccess) {
                    handler.obtainMessage(SUCCESS, files).sendToTarget();
                } else {
                    handler.obtainMessage(FAILED, errorStr).sendToTarget();
                }
            }
        }).start();
        return this;
    }

    public void setFileOperateCallback(FileOperateCallback callback) {
        this.callback = callback;
    }

    private void copyAssetsToDst(Context context, List<String> files, String srcPath, String dstPath) {
        try {
            String[] fileNames = context.getAssets().list(srcPath);
            if (fileNames.length > 0) {
                File file = new File(FileUtils.AppPath, dstPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                for (String fileName : fileNames) {
                    Log.d("=============", "fileName = " + fileName);
                    if (!srcPath.equals("")) { // assets 文件夹下的目录
                        copyAssetsToDst(context, files, srcPath + File.separator + fileName,
                                dstPath + File.separator + fileName);
                    } else { // assets 文件夹
                        copyAssetsToDst(context, files, fileName, dstPath + File.separator + fileName);
                    }
                }
            } else {
                File outFile = new File(FileUtils.AppPath, dstPath);
                if (!outFile.exists()) {
                    outFile.delete();
                }
                InputStream is = context.getAssets().open(srcPath);
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
                files.add(outFile.getAbsolutePath());
            }
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            errorStr = e.getMessage();
            isSuccess = false;
        }
    }

    public interface FileOperateCallback {
        void onSuccess(List<String> files);

        void onFailed(String error);
    }


}
