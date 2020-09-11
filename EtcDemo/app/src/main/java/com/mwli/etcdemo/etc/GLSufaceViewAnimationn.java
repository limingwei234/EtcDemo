package com.mwli.etcdemo.etc;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.ETC1Util;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class GLSufaceViewAnimationn extends GLSurfaceView implements GLSurfaceView.Renderer {

    private static final String TAG = "GLSufaceViewAnimationn";
    private static final int MILLONS_UNIT = 1000;
    private GLRenderer renderer;
    private ZipPkmReader reader;
    private int frameRate = 16; // 默认帧率
    private String animationPath = "";
    private Handler handler = new Handler(Looper.getMainLooper());

    public GLSufaceViewAnimationn(Context context) {
        super(context);
        initView();
    }

    public GLSufaceViewAnimationn(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }


    private void initView() {
        setEGLContextClientVersion(2);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        renderer = new GLRenderer();
        reader = new ZipPkmReader(getContext());
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        renderer.initShader();
    }

    private int width = 0;
    private int heeight = 0;

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.heeight = height;
    }


    int index = 0;
    private int gap = 0;

    @Override
    public void onDrawFrame(GL10 gl) {
        long time = SystemClock.elapsedRealtime();
        InputStream inputStream = null;
        ETC1Util.ETC1Texture texture = null;
        // 避免多线程导致正在读取数据时关闭ZipStream，保证读取完一帧数据
        synchronized (this) {
            texture = reader.getNextTexture();
            frameIndex++;
        }

        if (texture != null) {
            renderer.drawFrame(texture, width, heeight);
        }

        try {
            long sleepTime = MILLONS_UNIT / frameRate - (SystemClock.elapsedRealtime() - time);
            if (sleepTime < 0) {
                sleepTime = 0;
            }
            if (texture == null) {
                // total frame count get fail execute this method
                performFinishListener();
                return;
            }
            if (frameIndex == totalFrameCount) {
                performFinishListener();
                return;
            }
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
        requestRender();
    }

    private void performFinishListener() {
        isRunning = false;
        if (surfaceViewAnimStateChange != null) {
            handler.removeCallbacks(finishRunnable);
            surfaceViewAnimStateChange.onStateChange(OnGLSurfaceViewAnimStateChange.ANIM_FINISH);
        }
    }


    /**
     * 设置动画资源路径
     *
     * @param path
     */
    public void setAnimationPath(String path) {
        this.animationPath = path;
        if (reader != null) {
            reader.setZipPath(path);
        }
    }

    private int frameIndex = 0;
    private int totalFrameCount = -1;

    public synchronized void startAnim() {
        if (reader != null) {
            Log.i(TAG, "startAnim");
            if (reader == null) {
                reader = new ZipPkmReader(getContext());
                reader.setZipPath(animationPath);
            }
            boolean isSuccess = reader.open();
            frameIndex = 0;
            try {
                totalFrameCount = new ZipFile(animationPath).size();
                if (totalFrameCount != 0) {
                    handler.postDelayed(finishRunnable, (totalFrameCount + 1) * MILLONS_UNIT / frameRate);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "open assets isSuccess = " + isSuccess + ", frameCount = " + totalFrameCount);
            isRunning = true;
            requestRender();
            if (isSuccess) {
                if (surfaceViewAnimStateChange != null) {
                    surfaceViewAnimStateChange.onStateChange(OnGLSurfaceViewAnimStateChange.ANIM_START);
                }
            }
        }
    }

    public synchronized void stopAnim() {
        if (reader != null) {
            reader.close();
        }
        isRunning = false;
        if (surfaceViewAnimStateChange != null) {
            surfaceViewAnimStateChange.onStateChange(OnGLSurfaceViewAnimStateChange.ANIM_FINISH);
        }
    }

    private Runnable finishRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "finishRunnable");
            isRunning = false;
            if (surfaceViewAnimStateChange != null) {
                surfaceViewAnimStateChange.onStateChange(OnGLSurfaceViewAnimStateChange.ANIM_FINISH);
            }
        }
    };

    public synchronized void release() {

    }

    private OnGLSurfaceViewAnimStateChange surfaceViewAnimStateChange;

    public void setAnimStateChangeListener(OnGLSurfaceViewAnimStateChange stateChangeListener) {
        this.surfaceViewAnimStateChange = stateChangeListener;
    }


    public void setAnimationFrameRate(int fps) {
        this.frameRate = fps;
    }

    private boolean isRunning = false;

    public synchronized boolean isRunning() {
        return isRunning;
    }

}
