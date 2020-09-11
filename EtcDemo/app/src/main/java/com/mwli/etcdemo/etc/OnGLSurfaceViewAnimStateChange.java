package com.mwli.etcdemo.etc;

public interface OnGLSurfaceViewAnimStateChange {

    public static int ANIM_START = 1;
    public static int ANIM_FINISH = 2;

    void onStateChange(int status);

}
