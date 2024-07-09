package com.mwli.etcdemo

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mwli.etcdemo.etc.GLSufaceViewAnimationn
import com.mwli.etcdemo.etc.OnGLSurfaceViewAnimStateChange
import com.mwli.etcdemo.etc.ZipUtils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        copyWakeupAnimResources()
        Log.i("limingwei", "test = " + resources.getDimension(R.dimen.test))

        val list:ArrayList<String> = prepareAnimList(this)
        var index:Int = 0;

        var glSufaceViewAnimationn = findViewById<GLSufaceViewAnimationn>(R.id.surfaceView)
        glSufaceViewAnimationn.setAnimationFrameRate(16)
        glSufaceViewAnimationn.setAnimationPath(list?.get(index))
        glSufaceViewAnimationn.startAnim()
        glSufaceViewAnimationn.setAnimStateChangeListener {
            if (it == OnGLSurfaceViewAnimStateChange.ANIM_FINISH) {
                index++
                if (index == list.size) {
                    index %= list.size
                }
                glSufaceViewAnimationn.setAnimationPath(list?.get(index))
                glSufaceViewAnimationn.startAnim()
            }
        }
    }

    fun prepareAnimList(context: Context) : ArrayList<String> {

        val list: ArrayList<String> = arrayListOf()

        list.add(context.filesDir.absolutePath.toString() + "/anim/ap.zip")
//        list.add(context.filesDir.absolutePath.toString() + "/anim/lt.zip")
//        list.add(context.filesDir.absolutePath.toString() + "/anim/as.zip")
//        list.add(context.filesDir.absolutePath.toString() + "/anim/xs.zip")
        return list
    }

    fun copyWakeupAnimResources() {
        val path = "assets/anim.zip"
        try {
            ZipUtils.unZipFolder(this, path, filesDir.absolutePath.toString() + "/anim")
        } catch (e: Exception) {
            Log.i("etcdemo", "copyWakeupAnimResources, " + e.message)
        }
    }

}
